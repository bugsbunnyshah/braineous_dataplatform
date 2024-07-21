package prototype.stores;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.clickhouse.client.*;
import com.clickhouse.data.ClickHouseFormat;
import com.clickhouse.jdbc.ClickHouseDataSource;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Properties;

public class ClickHouseStoreTests {
    private static Logger logger = LoggerFactory.getLogger(ClickHouseStoreTests.class);

    @Test
    public void testDrive() throws Exception{
        /*ClickHouseNodes servers = ClickHouseNodes.of(
                "http://localhost:8123/default");

        try (ClickHouseClient client = ClickHouseClient.newInstance(ClickHouseProtocol.HTTP);
             ClickHouseResponse response = client.read(servers)
                     .format(ClickHouseFormat.RowBinaryWithNamesAndTypes)
                     .query("select * from numbers(:limit)")
                     .params(1000)
                     .executeAndWait()) {
            ClickHouseResponseSummary summary = response.getSummary();
            long totalRows = summary.getTotalRowsToRead();

            System.out.println("********************");
            System.out.println("TOTAL_ROWS: "+totalRows);
            System.out.println(summary.toString());
        }*/

        String url = "jdbc:ch://localhost:8123/default"; // use http protocol and port 8123 by default

        Properties properties = new Properties();

        ClickHouseDataSource dataSource = new ClickHouseDataSource(url, properties);
        try (Connection conn = dataSource.getConnection("default", "");
             /*Statement stmt = conn.createStatement()) {
             ResultSet rs = stmt.executeQuery("select * from numbers(50000)");
             while(rs.next()) {
                 System.out.println(rs.getString(1));
             }*/

             Statement stmt = conn.createStatement()) {
                    String insertSql = "INSERT INTO my_first_table (user_id, message, timestamp, metric) VALUES\n" +
                            "    (101, 'Hello, ClickHouse!',                                 now(),       -1.0    ),\n" +
                            "    (102, 'Insert a lot of rows per batch',                     yesterday(), 1.41421 ),\n" +
                            "    (102, 'Sort your data based on your commonly-used queries', today(),     2.718   ),\n" +
                            "    (101, 'Granules are the smallest chunks of data read',      now() + 5,   3.14159 )";
                    stmt.executeUpdate(insertSql);

                    String readSql = " SELECT *\n" +
                            " FROM my_first_table\n" +
                            " ORDER BY timestamp";
                    ResultSet rs = stmt.executeQuery(readSql);
                    while(rs.next()) {
                        System.out.println(rs.getString(2));
                    }
             }
    }


    @Test
    public void testBulkWrite() throws Exception{
        //Get Connection
        String url = "jdbc:ch://localhost:8123/default"; // use http protocol and port 8123 by default
        Properties properties = new Properties();
        ClickHouseDataSource dataSource = new ClickHouseDataSource(url, properties);
        Connection connection = dataSource.getConnection("default", "");

        String createTableSql = "CREATE TABLE IF NOT EXISTS staging_store\n" +
                "        (\n" +
                "                id String,\n" +
                "                data String\n" +
                "        )\n" +
                "        ENGINE = MergeTree\n" +
                "        PRIMARY KEY (id)";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(createTableSql);

        //Add a batch of records
        int size = 10;
        for(int i=0; i<size; i++){
            JsonObject dataJson = new JsonObject();
            dataJson.addProperty("value", i);
            String id = JsonUtil.getJsonHash(dataJson);

            String insertSql = "insert into staging_store (id, data) values ('"+id+"','" + dataJson.toString() + "')";
            stmt.addBatch(insertSql);
        }
        stmt.executeBatch();
    }


    @Test
    public void testRead() throws Exception{
        //Get Connection
        String url = "jdbc:ch://localhost:8123/default"; // use http protocol and port 8123 by default
        Properties properties = new Properties();
        ClickHouseDataSource dataSource = new ClickHouseDataSource(url, properties);
        Connection connection = dataSource.getConnection("default", "");

        Statement stmt = connection.createStatement();

        String selectSql = "SELECT *\n" +
                " FROM staging_store";

        ResultSet rs = stmt.executeQuery(selectSql);
        while(rs.next()) {
            System.out.println(rs.getString(1));
            System.out.println(rs.getString(2));
            System.out.println("******************************");
        }
    }
}
