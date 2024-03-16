package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.infrastructure.TenantService;
import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.ingestion.util.IngestionUtil;
import com.appgallabs.dataplatform.pipeline.manager.service.PipelineMonitoringService;
import com.appgallabs.dataplatform.pipeline.manager.service.PipelineServiceType;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.*;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.catalog.ObjectPath;
import org.apache.flink.table.catalog.ResolvedSchema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class JobManager {
    private static Logger logger = LoggerFactory.getLogger(JobManager.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private PipelineMonitoringService pipelineMonitoringService;

    @Inject
    private SchemalessMapper schemalessMapper;

    @Inject
    private DataLakeTableGenerator dataLakeTableGenerator;

    @Inject
    private DataLakeSessionManager dataLakeSessionManager;

    @Inject
    private DataLakeSqlGenerator dataLakeSqlGenerator;

    @Inject
    private TenantService tenantService;

    private Map<String,Long> pipeToOffset = new HashMap<>();

    private ExecutorService submitJobPool = Executors.newFixedThreadPool(25);
    private ExecutorService retryJobPool = Executors.newFixedThreadPool(25);


    @PostConstruct
    public void start(){

    }

    public synchronized void submit(StreamExecutionEnvironment env, SecurityToken securityToken,
                       String driverConfiguration, String entity,
                       String pipeId, long offset, String jsonString){
        try {
            //pre-process for pipeline monitor
            this.preProcess(jsonString, driverConfiguration, securityToken, pipeId, entity);

            //tenant
            String apiKey = securityToken.getPrincipal();
            Tenant tenant = this.tenantService.getTenant(apiKey);

            String catalog = tenant.getDataLakeId();
            String database = pipeId.replaceAll("-", "").toLowerCase();
            String tableName = entity.replaceAll("-", "").toLowerCase();



            JsonElement jsonElement = JsonParser.parseString(jsonString);

            JsonArray ingestion = IngestionUtil.generationIngestionArray(jsonElement);

            //ingestion array
            List<Map<String, Object>> flatArray = new ArrayList();
            for (int i = 0; i < ingestion.size(); i++) {
                JsonObject jsonObject = ingestion.get(i).getAsJsonObject();
                Map<String, Object> flatJson = this.schemalessMapper.mapAll(jsonObject.toString());
                flatArray.add(flatJson);
            }

            Map<String, Object> row = flatArray.get(0);
            String table = this.createTable(env, catalog, database, tableName, row);

            //asynchronous
            this.submitJob(env, catalog, table, flatArray);
        }catch(Exception e){
            logger.error(e.getMessage(), e);

            //TODO: handle system level errors (NOW)
        }
    }

    private synchronized void submitJob(StreamExecutionEnvironment env,
                                        String catalogName,
                                        String table,
                                        List<Map<String, Object>> flatArray){
        submitJobPool.execute(() -> {
            try {
                this.addData(env,catalogName, table, flatArray);
                logger.debug("****JOB_SUCCESS*****");
            }catch(Exception e){
                logger.error(e.getMessage(), e);
                this.retryJob(env, catalogName, table, flatArray);
            }
        });
    }

    private void retryJob(StreamExecutionEnvironment env,
                          String catalogName,
                          String table,
                          List<Map<String, Object>> flatArray){
        retryJobPool.execute(() -> {
            while(true){
                try {
                    this.addData(env, catalogName, table, flatArray);
                    logger.info("****JOB_RETRY_SUCCESS*****");
                    break;
                }catch(Exception e){
                    logger.error(e.getMessage(), e);
                    //TODO: handle system level errors (NOW)
                }
            }
        });
    }

    private void addData(StreamExecutionEnvironment env,
                         String catalogName,
                         String table,
                         List<Map<String, Object>> rows) throws Exception
    {
        final StreamTableEnvironment tableEnv = this.dataLakeSessionManager.newDataLakeCatalogSession(
                env,
                catalogName
        );

        //check if table should be altered to accomodate more columns
        List<String> newColumns = this.shouldAlterTable(
              tableEnv,
              table,
              rows
        );
        if(newColumns != null && !newColumns.isEmpty()){
            this.updateTable(
                    tableEnv,
                    table,
                    newColumns
            );
        }

        Table data = tableEnv.from(table);
        System.out.println(data.getResolvedSchema().toString());
        ResolvedSchema resolvedSchema = data.getResolvedSchema();
        List<String> currentColumns = resolvedSchema.getColumnNames();

        String insertSql = this.dataLakeSqlGenerator.generateInsertSql(table, currentColumns, rows);
        logger.debug("*********INSERT_SQL************");
        logger.debug(insertSql);
        logger.debug("*******************************");
        // insert some example data into the table
        final TableResult insertionResult =
                tableEnv.executeSql(insertSql);

        // since all cluster operations of the Table API are executed asynchronously,
        // we need to wait until the insertion has been completed,
        // an exception is thrown in case of an error
        insertionResult.await();

        this.printData(tableEnv, table);
    }

    private String createTable(StreamExecutionEnvironment env, String catalogName,
                               String database, String tableName, Map<String,Object> row) throws Exception{
        final StreamTableEnvironment tableEnv = this.dataLakeSessionManager.newDataLakeSessionWithNewDatabase(
                env,
                catalogName,
                database
        );
        String table = catalogName + "." + database + "." + tableName;
        String objectPath = database + "." + tableName;

        //TODO: (NOW)
        String filePath = "file:///Users/babyboy/datalake/"+tableName;

        String format = "csv";

        String currentCatalog = tableEnv.getCurrentCatalog();
        Optional<Catalog> catalog = tableEnv.getCatalog(currentCatalog);
        boolean tableExists = catalog.get().tableExists(ObjectPath.fromString(objectPath));

        if(!tableExists) {
            // Create a catalog table
            TableDescriptor tableDescriptor = this.dataLakeTableGenerator.createFileSystemTable(row,
                    filePath,
                    format);

            tableEnv.createTable(table, tableDescriptor);
        }

        logger.debug("TABLE_CREATED: " + !tableExists);

        return table;
    }

    private List<String> shouldAlterTable(
            StreamTableEnvironment tableEnv,
            String table,
            List<Map<String, Object>> rows

    ){
        List<String> newColumns = new ArrayList<>();

        Table data = tableEnv.from(table);
        ResolvedSchema resolvedSchema = data.getResolvedSchema();

        //should contain all columns in the table
        List<String> currentColumns = resolvedSchema.getColumnNames();

        for(Map<String, Object> row: rows) {
            Set<String> payloadColumns = row.keySet();

            //add new columns, update the schema
            for(String payloadColumn: payloadColumns){
                if(!currentColumns.contains(payloadColumn)){
                    newColumns.add(payloadColumn);
                    currentColumns.add(payloadColumn);
                }
            }

            for(String currentColumn: currentColumns){
                if(!row.containsKey(currentColumn)){
                    row.put(currentColumn, "");
                }
            }

            //reorient the row
            for(String currentColumn: currentColumns){
                Object value = row.get(currentColumn);
                row.put(currentColumn, value);
            }
        }

        return newColumns;
    }

    private String updateTable(
            StreamTableEnvironment tableEnv,
            String table,
            List<String> newColumns) throws Exception{
        //String objectPath = database + "." + this.tableName;

        String currentCatalog = tableEnv.getCurrentCatalog();
        Optional<Catalog> catalog = tableEnv.getCatalog(currentCatalog);

        for(String newColumn: newColumns) {
            final TableResult updateTableResult = tableEnv.
                    executeSql("ALTER TABLE " + table + " ADD `" + newColumn + "` String NULL");
            // since all cluster operations of the Table API are executed asynchronously,
            // we need to wait until the insertion has been completed,
            // an exception is thrown in case of an error
            updateTableResult.await();
        }

        return table;
    }
    //-----------------------------------------------------------------------------------------------------------
    private void preProcess(String data,
                            String driverConfiguration,
                            SecurityToken securityToken,
                            String pipeId,
                            String entity
                            ){
        PipelineServiceType pipelineServiceType = PipelineServiceType.DATALAKE;
        JsonObject metaData = JsonUtil.validateJson(driverConfiguration).getAsJsonObject();
        boolean incoming = true;

        this.pipelineMonitoringService.record(
                pipelineServiceType,
                metaData,
                securityToken,
                pipeId,
                entity,
                data,
                incoming
        );
    }

    private void printData(StreamTableEnvironment tableEnv, String table) throws Exception{
        String selectSql = "select name,expensive from "+table;
        System.out.println(selectSql);

        // insert some example data into the table
        final TableResult result =
                tableEnv.executeSql(selectSql);

        // since all cluster operations of the Table API are executed asynchronously,
        // we need to wait until the insertion has been completed,
        // an exception is thrown in case of an error
        result.await();
        result.print();
    }
}
