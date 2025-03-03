package com.appgallabs.dataplatform.infrastructure;

import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCHelper {
    private static JDBCHelper singleton = new JDBCHelper();

    private JDBCHelper(){

    }

    public static JDBCHelper getInstance(){
        return JDBCHelper.singleton;
    }

    public Connection getConnection(JsonObject configJson)
            throws SQLException {
        Connection connection = null;

        String url = configJson.get("connectionString").getAsString();
        String username = configJson.get("username").getAsString();
        String password = configJson.get("password").getAsString();

        connection = DriverManager.getConnection(
                url, username, password);

        return connection;
    }
}
