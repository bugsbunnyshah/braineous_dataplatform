package prototype.stores;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class JDBCHSQLDBStoreTests {
    private Connection connection;

    @Test
    public void executeJdbcFlow() throws Exception{

        this.configure(null);
        this.storeData(null);

    }

    public void configure(JsonObject configJson){
        Statement statement = null;
        try {
            String url = "jdbc:h2:mem:myDb;DB_CLOSE_DELAY=-1";
            String username = "sa";
            String password = "sa";

            this.connection = DriverManager.getConnection(
                    url, username, password);
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
        }catch(Exception e){
            throw new RuntimeException(e);
        }finally {
            try {
                statement.close();
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    public void storeData(JsonArray dataSet){
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
        }catch(Exception e){
            throw new RuntimeException(e);
        }finally {
            try {
                this.connection.close();
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }
}
