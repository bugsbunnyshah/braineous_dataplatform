package com.appgallabs.dataplatform.targetSystem.core.driver;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.reporting.IngestionReportingService;
import com.appgallabs.dataplatform.targetSystem.framework.staging.Record;
import com.appgallabs.dataplatform.targetSystem.framework.staging.StagingStore;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.snowflake.ingest.SimpleIngestManager;
import net.snowflake.ingest.example.IngestExampleHelper;
import net.snowflake.ingest.utils.StagedFileWrapper;

import org.apache.commons.codec.binary.Base64;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class SnowflakeStagingStore implements StagingStore {
    private static Logger logger = LoggerFactory.getLogger(SnowflakeStagingStore.class);

    private static String ALGORITHM = "RSA";

    private Connection connection;
    private JsonObject configJson;

    //TODO: (NOW) - thread it in
    private IngestionReportingService ingestionReportingService;

    @Override
    public void configure(JsonObject configJson) {
        try {
            this.configJson = configJson;

            this.connection = this.getConnection();
        }catch(Exception e){
            logger.error(e.getMessage());

            //report to the pipeline monitoring service
            //JsonObject jsonObject = new JsonObject();
            //this.ingestionReportingService.reportDataError(jsonObject);
        }
    }

    @Override
    public String getName() {
        return this.configJson.get("stage").getAsString();
    }

    @Override
    public JsonObject getConfiguration() {
        return this.configJson;
    }

    @Override
    public void storeData(Tenant tenant, String pipeId, String entity, List<Record> records) {
        try {
            //Create a Staging Area
            final String fileLocationUrl = this.configJson.get("source_location").getAsString();

            //generate a json file on the fly in the Local Staging Area
            final String fileName = UUID.randomUUID() +".json";

            JsonArray jsonArray = new JsonArray();
            for(Record record: records){
                JsonObject jsonObject = record.toJson();
                jsonArray.add(jsonObject);
            }
            String jsonData = jsonArray.toString();

            String localFsFileDirectory = fileLocationUrl.replaceAll("file://", "");
            File file = new File(localFsFileDirectory + fileName);
            file.createNewFile();
            FileUtils.write(file, jsonData, StandardCharsets.UTF_8);

            KeyPair keypair = this.generateKeyPair();
            this.createStagingArea(connection, keypair, fileLocationUrl, fileName);
            System.out.println("*****CREATE_STAGING_AREA*****");
            System.out.println("STATUS: " + "SUCCESS");

            //Ingest a file
            this.ingestFile(fileName, keypair);
            logger.info("*****INGEST_FILE*****");
            logger.info("STATUS: " + "SUCCESS");
        }catch(Exception e){
            logger.error(e.getMessage());

            //report to the pipeline monitoring service
            //JsonObject jsonObject = new JsonObject();
            //this.ingestionReportingService.reportDataError(jsonObject);
        }
    }

    @Override
    public List<Record> getData(Tenant tenant, String pipeId, String entity) {
        return null;
    }
    //-------------------------------------------------------------------------------------------------
    private void createStagingArea(Connection conn, KeyPair keypair, String filesLocation, String file)
            throws Exception{
        String user = this.configJson.get("user").getAsString();
        String database = this.configJson.get("database").getAsString();
        String schema = this.configJson.get("schema").getAsString();
        String stage = this.configJson.get("stage").getAsString();
        String table = this.configJson.get("table").getAsString();
        String pipe = this.configJson.get("pipe").getAsString();

        // use the right database
        this.doQuery(conn, "use database " + database);

        // use the right schema
        this.doQuery(conn, "use schema " + schema);

        boolean doesStageExists = false;
        try{
            this.doQuery(
                    conn, "create stage " + stage + " FILE_FORMAT=(type='json' COMPRESSION=NONE)");
        }catch (Exception e){
            System.out.println("******STAGING_AREA_EXISTS**********");
            doesStageExists = true;
        }

        if(!doesStageExists) {
            System.out.println("******CREATING_THE_STAGE_AREA**********");
            // create the target stage
            //this.doQuery(
            //        conn, "create stage " + stage + " FILE_FORMAT=(type='json' COMPRESSION=NONE)");

            // create the target
            this.doQuery(
                    conn,
                    "create or replace table " + table + " (src variant)"
            );
            // Create the pipe for subsequently ingesting files to.
            this.doQuery(
                    conn,
                    "create or replace pipe "
                            + pipe
                            + " as copy into "
                            + table
                            + " from @"
                            + stage
                            + " file_format=(type='json')");
        }

        String pk = IngestExampleHelper.getPublicKeyString(keypair);

        // assume the necessary privileges
        this.doQuery(conn, "use role accountadmin");

        // set the public key
        this.doQuery(conn, "alter user " + user + " set RSA_PUBLIC_KEY='" + pk + "'");
        this.doQuery(
                conn, "PUT " + filesLocation + file + " @" + stage + " AUTO_COMPRESS=FALSE");

        this.doQuery(conn, "use role sysadmin");
    }

    private void ingestFile(String filename, KeyPair keypair) throws Exception{
        String account = this.configJson.get("account_identifier").getAsString();
        String host = this.configJson.get("host").getAsString();
        String user = this.configJson.get("user").getAsString();
        int port = this.configJson.get("port").getAsInt();

        String database = this.configJson.get("database").getAsString();
        String schema = this.configJson.get("schema").getAsString();
        String pipe = this.configJson.get("pipe").getAsString();
        String fqPipe = database + "." + schema + "." + pipe;

        //TODO: decide scheme based on port
        String scheme = "https";

        SimpleIngestManager manager = new SimpleIngestManager(account, user, fqPipe, keypair, scheme, host, port);


        StagedFileWrapper myFile = new StagedFileWrapper(filename, null);
        manager.ingestFile(myFile, null);
    }

    private Connection getConnection() throws Exception{
        String account = this.configJson.get("account_identifier").getAsString();
        String host = this.configJson.get("host").getAsString();
        String user = this.configJson.get("user").getAsString();
        String password = this.configJson.get("password").getAsString();
        int port = this.configJson.get("port").getAsInt();

        //TODO: decide scheme based on port
        String scheme = "https";

        // check first to see if we have the Snowflake JDBC
        Class.forName("net.snowflake.client.jdbc.SnowflakeDriver");

        // build our properties
        Properties props = new Properties();
        props.put("user", user);
        props.put("password", password);
        props.put("account", account);
        //props.put("ssl", "on");

        // the actual connection string
        String connectString = "jdbc:snowflake://" + host + ":" + port;

        Connection connection = DriverManager.getConnection(connectString, props);

        return connection;
    }

    private void doQuery(Connection connection, String query){
        try (Statement statement = connection.createStatement()) {
            statement.executeQuery(query);
        }
        // if ANY exceptions occur, an illegal state has been reached
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Generates an RSA keypair for use in this test
     *
     * @return a valid RSA keypair
     * @throws NoSuchAlgorithmException if we don't have an RSA algo
     * @throws NoSuchProviderException if we can't use SHA1PRNG for randomization
     */
    private KeyPair generateKeyPair() throws NoSuchProviderException, NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(2048, random);
        return keyGen.generateKeyPair();
    }

    /**
     * Generate the public key as a string
     *
     * @return the public key as a string
     */
    private String getPublicKeyString(KeyPair keypair)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        final PublicKey pk = keypair.getPublic();
        X509EncodedKeySpec spec = keyFactory.getKeySpec(pk, X509EncodedKeySpec.class);
        return Base64.encodeBase64String(spec.getEncoded());
    }
}
