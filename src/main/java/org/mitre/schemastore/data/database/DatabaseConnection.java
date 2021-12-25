// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mitre.schemastore.data.database.updates.DatabaseUpdates;


/**
 * Handles the connection to the database
 * @author CWOLF
 */
public class DatabaseConnection
{
	// Constants for indicating if the database connection is to Postgres or Derby
	static public final Integer POSTGRES = 0;
	static public final Integer DERBY = 1;

	// Stores the database connection info
	private Integer databaseType = null;
	private String databaseURI = null;
	private String databaseName = null;
	private String databaseUser = null;
	private String databasePassword = null;

	/** Stores the database connection */
	private Connection connection = null;

	/** Retrieve the value for the the specified tag */
	private String getValue(StringBuffer buffer, String tag)
	{
		String value = "";
		Pattern serverPattern = Pattern.compile("<"+tag+">(.*?)</"+tag+">");
		Matcher serverMatcher = serverPattern.matcher(buffer);
		if(serverMatcher.find()) value = serverMatcher.group(1);
		return value;
	}

	/** Constructs the connection from the configuration file */
	public DatabaseConnection()
	{
		// Load database properties from file
		try {
			// Pull the entire file into a string
			InputStream configStream = getClass().getResourceAsStream("/schemastore.xml");
			BufferedReader in = new BufferedReader(new InputStreamReader(configStream));
			StringBuffer buffer = new StringBuffer("");
			String line; while((line=in.readLine())!=null) buffer.append(line);
			in.close();

			// Retrieve the database values from the configuration file
			databaseType = getValue(buffer,"databaseType").equals("postgres") ? POSTGRES : DERBY;
			databaseURI = getValue(buffer,"databaseURI");
			databaseName = getValue(buffer,"databaseName");
			databaseUser = getValue(buffer,"databaseUser");
			databasePassword = getValue(buffer,"databasePassword");

			// Assign generic location if none given
			if(databaseURI.equals(""))
			{
				String databasePath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
				databasePath = databasePath.replaceAll(".*:","").replaceAll("\\%20"," ");
				if(databasePath.endsWith("SchemaStoreClient.jar"))
					databaseURI = databasePath.replace("/SchemaStoreClient.jar","");
				else if(databasePath.endsWith("/build/classes/"))
					databaseURI = databasePath.replaceAll("/build/classes/","");
				else databaseURI = databasePath.replaceAll("/WEB-INF/.*","");
			}
		}
		catch(IOException e)
			{ System.out.println("(E)DatabaseConnection - schemastore.xml has failed to load!\n"+e.getMessage()); }
	}

	/** Constructs the connection from the specified settings */
	public DatabaseConnection(Integer type, String uri, String database, String user, String password)
	{
		// Set the connection information
		databaseType = type;
		databaseURI = uri;
		databaseName = database;
		databaseUser = user;
		databasePassword = password;

		// Adjust the database location for derby files
		if(databaseType.equals(DERBY))
			try { databaseURI = new URI(databaseURI).getPath().replaceAll(".*:",""); } catch(Exception e) {}
	}

	/** Checks to see if a connection can be make */
    private boolean checkConnection() throws SQLException
    {
		// Check to make sure database connection still works
		if(connection!=null)
			try {
				Statement stmt = connection.createStatement();
				stmt.executeQuery("SELECT id FROM universal_id");
                stmt.close();
				return true;
			} catch(Exception e) { connection=null; }

		// Attempt to connect to database
		try {
			if(connection==null)
			{
				// Connect to the database
	    		boolean useDerby = databaseType.equals(DERBY);
	    		if(databaseURI.equals("")) databaseURI = ".";
	    		Class.forName(useDerby ? "org.apache.derby.jdbc.EmbeddedDriver" : "org.postgresql.Driver");
	    		String dbURL = useDerby ? "jdbc:derby:"+databaseURI+"/"+databaseName+";create=true" : "jdbc:postgresql://"+databaseURI+":5432/"+databaseName;
    			connection = DriverManager.getConnection(dbURL,databaseUser,databasePassword);
	    		connection.setAutoCommit(false);

	    		// Initialize and update the database as needed
	    		DatabaseUpdates.initialize(connection,databaseType,databaseUser,databasePassword);
	    		DatabaseUpdates.update(connection);
                return true;
			}
		}
		catch (Exception e)
			{ connection=null; System.out.println("(E) Failed to connect to database - " + e.getMessage()); }

		// Indicates that a statement failed to be created
		throw new SQLException();
    }

    /** Returns the database type */
    Integer getDatabaseType()
    	{ return databaseType; }
    
    /** Returns the database user */
    String getDatabaseUser()
    	{ return databaseUser; }
    
	/** Creates a sql statement */
	Statement getStatement() throws SQLException
	{
        checkConnection();
        return connection.createStatement();
	}

	/** Creates a prepared statement */
	PreparedStatement getPreparedStatement(String statement) throws SQLException
	{
        checkConnection();
        return connection.prepareStatement(statement);
	}

	/** Sets the auto commit */
	void setAutoCommit(boolean autoCommit) throws SQLException
		{ connection.setAutoCommit(autoCommit); }
	
	/** Commits changes to the database */
	void commit() throws SQLException
		{ connection.commit(); }

	/** Rolls back changes to the database */
	void rollback() throws SQLException
		{ connection.rollback(); }
}