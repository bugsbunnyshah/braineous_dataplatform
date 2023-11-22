package prototype.stores;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class JDBCPostgresStoreTests {
    private Connection connection;

    @Test
    public void executeJdbcFlow() throws Exception{

        this.configure(null);
        this.storeData(null);

    }

    public void configure(JsonObject configJson) throws Exception{
        Statement statement = null;
        //try {
            String url = "jdbc:postgresql://localhost:55001/braineous_staging_db";
            String username = "postgres";
            String password = "postgrespw";
            String sha256hex = DigestUtils.sha256Hex(password);

            this.connection = DriverManager.getConnection(
                    url, username, sha256hex);
            System.out.println(
                    "Connection Established successfully");

            //create schema and tables
            String createTableSql = "CREATE TABLE REGISTRATION " +
                    "(id INTEGER not NULL, " +
                    " first VARCHAR(255), " +
                    " PRIMARY KEY ( id ))";
            statement = this.connection.createStatement();
            statement.executeUpdate(createTableSql);
            System.out.println("Created table in given database...");
        //}//finally {
          //  statement.close();
        //}
    }

    public void storeData(JsonArray dataSet) throws Exception{
        try {
            String query = "select * from REGISTRATION";

            //populate table
            for (int i = 0; i < 10; i++) {
                String insertSql = "insert into REGISTRATION (id, first) values (" + i + ", 'hello_" + i + "_world')";
                Statement insertStatement = this.connection.createStatement();
                insertStatement.executeUpdate(insertSql);
                insertStatement.close();
            }

            Statement st = this.connection.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                String name = rs.getString("first");
                System.out.println(name);
            }
            st.close();

            System.out.println("Connection Closed....");
        }finally {
            this.connection.close();
        }
    }
}
