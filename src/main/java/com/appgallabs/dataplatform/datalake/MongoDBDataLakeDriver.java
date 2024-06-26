package com.appgallabs.dataplatform.datalake;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class MongoDBDataLakeDriver implements DataLakeDriver, Serializable {
    private static Logger logger = LoggerFactory.getLogger(MongoDBDataLakeDriver.class);

    private String connectionString;
    private String collection;

    private boolean isMetaDataCreated = false;

    @Override
    public void configure(String configJson) {
        JsonObject datalakeConfig = JsonUtil.validateJson(configJson).getAsJsonObject()
                .get("datalake").getAsJsonObject()
                .get("configuration").getAsJsonObject();

        this.connectionString = datalakeConfig.get("connectionString").getAsString();
        this.collection = datalakeConfig.get("collection").getAsString();
    }

    @Override
    public String name() {
        return "MongoDBDataLakeDriver";
    }

    @Override
    public void storeIngestion(Tenant tenant, String jsonObjectString) {
        /*try {
            System.out.println("************DELTA_LAKE***********************");
            String fileName = UUID.randomUUID().toString()+".parquet";

            Schema schema = SchemaBuilder
                    .record("MyRecord")
                    .namespace("mynamespace")
                    .fields().requiredString("myfield")
                    .endRecord();

            ParquetWriter<GenericRecord> writer = AvroParquetWriter.
                    <GenericRecord>builder(new Path("delta/"+fileName))
                    .withSchema(schema)
                    .build();

            GenericRecord record = new GenericData.Record(schema);
            record.put("myfield", "myvalue");
            writer.write(record);
            long size = writer.getDataSize();
            System.out.println("SIZE: " + size);
            writer.close();

            System.out.println("****START_TXN****");
            DeltaLog log = DeltaLog.forTable(new Configuration(), "delta");
            List<Action> actions = List.of(new AddFile(fileName, new HashMap<String, String>(), size, System.currentTimeMillis(), true, null, null));
            OptimisticTransaction txn = log.startTransaction();

            System.out.println("****META_DATA****");

            this.createMetaData(txn);
            System.out.println("****COMMIT_TXN_WITH_META_DATA****");
            txn.commit(actions, new Operation(Operation.Name.CREATE_TABLE), fileName);

            System.out.println("*******DATALAKE_STORAGE_SUCCESS******");
        }catch(Exception e){
        }*/
    }

    /*private void createMetaData(OptimisticTransaction txn){
        try {
            Metadata metaData = txn.metadata()
                    .copyBuilder()
                    .partitionColumns(new ArrayList<String>())
                    .schema(new StructType()
                            .add(new StructField("myfield", new StringType(), true))).build();
            txn.updateMetadata(metaData);
            this.isMetaDataCreated = true;
        }catch(Exception e){
        }
    }*/
}
