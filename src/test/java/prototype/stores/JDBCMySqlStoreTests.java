package prototype.stores;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class JDBCMySqlStoreTests {
    private Connection connection;

    @Test
    public void executeJdbcFlow() throws Exception{
        this.configure(null);
        this.storeData(null);
    }

    public void configure(JsonObject configJson) throws Exception{
        Statement createTableStatement = null;
        try {
            String url = "jdbc:mysql://localhost:3306/braineous_staging_database";
            String username = "root";
            String password = "";

            this.connection = DriverManager.getConnection(
                    url, username, password);
            System.out.println(
                    "Connection Established successfully");

            //create schema and tables
            String createTableSql = "CREATE TABLE IF NOT EXISTS staged_data (\n" +
                    "    id int NOT NULL AUTO_INCREMENT,\n" +
                    "    data varchar(255) NOT NULL,\n" +
                    "    PRIMARY KEY (id)\n" +
                    ")";
            createTableStatement = this.connection.createStatement();
            createTableStatement.executeUpdate(createTableSql);
            System.out.println("Created table in given database...");

        }finally {
            createTableStatement.close();
        }
    }

    public void storeData(JsonArray dataSet) throws Exception{
        try {
            String query = "select * from staged_data";

            //populate table
            for (int i = 0; i < 10; i++) {
                String insertSql = "insert into staged_data (data) values ('hello_" + i + "_world')";
                Statement insertStatement = this.connection.createStatement();
                insertStatement.executeUpdate(insertSql);
                insertStatement.close();
            }

            Statement queryStatement = this.connection.createStatement();
            ResultSet rs = queryStatement.executeQuery(query);
            while (rs.next()) {
                String id = rs.getString("id");
                String data = rs.getString("data");
                System.out.println(id);
                System.out.println(data);
                System.out.println("***************");
            }
            queryStatement.close();

            System.out.println("Connection Closed....");
        }finally {
            this.connection.close();
        }
    }
}
