package prototype.stores;

import net.snowflake.ingest.example.IngestExampleHelper;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

import net.snowflake.ingest.SimpleIngestManager;
import net.snowflake.ingest.utils.StagedFileWrapper;

public class SnowPipeStoreTests {
    private static String ALGORITHM = "RSA";

    @Test
    public void testBulkWrite() throws Exception{
        // Get Connection
        Connection connection = this.getConnection();
        System.out.println(connection);

        //Create a Staging Area
        final String fileLocationUrl = "file:///tmp/braineous/";
        final String fileName = "valid.json";
        KeyPair keypair = this.generateKeyPair();
        this.createStagingArea(connection, keypair, fileLocationUrl, fileName);
        System.out.println("*****CREATE_STAGING_AREA*****");
        System.out.println("STATUS: "+"SUCCESS");

        //Ingest a file
        this.ingestFile(fileName, keypair);
        System.out.println("*****INGEST_FILE*****");
        System.out.println("STATUS: "+"SUCCESS");
    }

    @Test
    public void testRead() throws Exception{

    }

    //-----------------------------------------------------------------
    private Connection getConnection() throws Exception{
        String account = "xxlpraf-ubb29207";
        String user = "bugsbunnyshah";
        String host = "xxlpraf-ubb29207.snowflakecomputing.com";
        String scheme = "https";
        String password = "gagumaani@A61$";
        int port = 443;

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

    private void createStagingArea(Connection conn, KeyPair keypair, String filesLocation, String file)
    throws Exception{
        String user = "bugsbunnyshah";
        String database = "testdb";
        String schema = "public";
        String stage = "braineous_json_stage";
        String table = "braineous_json_table";
        String pipe = "braineous_json_pipe";

        // use the right database
        this.doQuery(conn, "use database " + database);

        // use the right schema
        this.doQuery(conn, "use schema " + schema);

        // create the target stage
        this.doQuery(
                conn, "create or replace stage " + stage + " FILE_FORMAT=(type='json' COMPRESSION=NONE)");

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

        String pk = IngestExampleHelper.getPublicKeyString(keypair);

        // assume the necessary privileges
        this.doQuery(conn, "use role accountadmin");

        // set the public key
        this.doQuery(conn, "alter user " + user + " set RSA_PUBLIC_KEY='" + pk + "'");
        this.doQuery(
                conn, "PUT " + filesLocation + file + " @" + stage + " AUTO_COMPRESS=FALSE");

        this.doQuery(conn, "use role sysadmin");
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

    private void ingestFile(String filename, KeyPair keypair) throws Exception{
        String account = "xxlpraf-ubb29207";
        String user = "bugsbunnyshah";
        String scheme = "https";
        String host = "xxlpraf-ubb29207.snowflakecomputing.com";
        int port = 443;

        String database = "testdb";
        String schema = "public";
        String pipe = "braineous_json_pipe";
        String fqPipe = database + "." + schema + "." + pipe;

        SimpleIngestManager manager = new SimpleIngestManager(account, user, fqPipe, keypair, scheme, host, port);


        StagedFileWrapper myFile = new StagedFileWrapper(filename, null);
        manager.ingestFile(myFile, null);
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
