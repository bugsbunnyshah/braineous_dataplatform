// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database.updates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.mitre.schemastore.data.database.DatabaseConnection;

/**
 * Handles initialization and updates to the database
 * @author CWOLF
 */
public class DatabaseUpdates
{
	/** Stores the current version */
	static private Integer currVersion = 16;

	/** Retrieve the contents of a file as a buffered string */
	static private StringBuffer getFileContents(String filename) throws IOException
	{
		// Initialize the string buffer
		StringBuffer buffer = new StringBuffer("");

		// Transfer the file to the string buffer
		InputStream fileStream = DatabaseUpdates.class.getResourceAsStream(filename);
		BufferedReader in = new BufferedReader(new InputStreamReader(fileStream));
		String line;
		while((line=in.readLine())!=null)
			if(!line.startsWith("//")) buffer.append(line+"\n");
		in.close();

		// Return the string buffer
		return buffer;
	}

	/** Sets the specified property in DERBY */
	static private void setProperty(Statement statement, String parameter, String value) throws SQLException
		{ statement.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('" + parameter + "','" + value + "')"); }

	/** Initializes the specified data file */
	static public void initializeData(Statement stmt, String filename) throws SQLException
	{
		// Retrieve the data from file
		StringBuffer buffer = null;
		try { buffer = getFileContents(filename); }
		catch(IOException e) { throw new SQLException("Failed to access data file " + filename); }
		
		// Parse out data and store to database
		String commands[] = buffer.toString().split("\\n\\s*\\n");
		for(String command : commands)
		{
			// Run through series of insert statements
			String rows[] = command.split("\\n");
			String header = rows[0];
			for(int i=1; i<rows.length; i++)
				stmt.addBatch(header + " VALUES (" + rows[i] + ")");
		}
		stmt.executeBatch();		
	}
	
	/** Initializes the database if needed */
	static public void initialize(Connection connection, Integer type, String user, String password) throws SQLException
	{
		// Checks to see if database structure already exists
		boolean exists = true;
		Statement stmt = connection.createStatement();
		try { stmt.executeQuery("SELECT * FROM extensions"); }
		catch(Exception e) { connection.rollback(); exists=false; }

		// Initializes the database if it doesn't exist
		if(!exists)
		{
			try {
		   		// Set authentication for DERBY databases
	    		if(type.equals(DatabaseConnection.DERBY))
	    		{
	     	        setProperty(stmt,"derby.connection.requireAuthentication","true");
	    	        setProperty(stmt,"derby.authentication.provider","BUILTIN");
	    	        setProperty(stmt,"derby.user."+user,password);
	    	        setProperty(stmt,"derby.database.fullAccessUsers",user);
	    	        setProperty(stmt,"derby.database.defaultConnectionMode","noAccess");
	    		}
				
				// Generate the database structure
				StringBuffer buffer = getFileContents("SchemaStoreStructure.txt");
				String commands[] = buffer.toString().split(";");
				for(String command : commands)
				{
					String text = command.trim().replaceAll("\n","");
					if(text.length()>0) stmt.addBatch(text);
				}
				stmt.executeBatch();

				// Generate the database data
				initializeData(stmt,"SchemaStoreSchemaData.txt");
				initializeData(stmt,"SchemaStoreFunctionData.txt");
				stmt.executeUpdate("INSERT INTO version(id) VALUES("+DatabaseUpdates.currVersion+")");

				// Commit all changes
				connection.commit();
			}
			catch (Exception e)
				{ connection.rollback(); throw new SQLException("Failed to initialize database\n" + e.getMessage()); }
		}

		stmt.close();
	}
	
	/** Retrieves the current version */
	static private Integer getVersion(Connection connection) throws SQLException
	{
		// Retrieves the version number
		Integer version = 0;
		Statement stmt = connection.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT id FROM version");
			if(rs.next()) version = rs.getInt("id");
		}

		// Construct table for storing version number if it doesn't exist
		catch(Exception e)
		{
			stmt.close(); connection.rollback();
			stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE version (id integer NOT NULL)");
			stmt.executeUpdate("INSERT INTO version(id) VALUES(0)");
			connection.commit();
		}

		// Return the current version
		stmt.close();
		return version;
	}
	
	/** Updates the database as needed */
	static public void update(Connection connection) throws SQLException
	{
		try {
			Integer version = getVersion(connection);
			if(version<1) new Version1Updates().runUpdates(connection);
			if(version<2) new Version2Updates().runUpdates(connection);
			if(version<3) new Version3Updates().runUpdates(connection);
			if(version<4) new Version4Updates().runUpdates(connection);
			if(version<5) new Version5Updates().runUpdates(connection);
			if(version<6) new Version6Updates().runUpdates(connection);
			if(version<7) new Version7Updates().runUpdates(connection);
			if(version<8) new Version8Updates().runUpdates(connection);
			if(version<9) new Version9Updates().runUpdates(connection);
			if(version<10) new Version10Updates().runUpdates(connection);
			if(version<11) new Version11Updates().runUpdates(connection);
			if(version<12) new Version12Updates().runUpdates(connection);
			if(version<13) new Version13Updates().runUpdates(connection);
			if(version<14) new Version14Updates().runUpdates(connection);
			if(version<15) new Version15Updates().runUpdates(connection); 
			if(version<16) new Version16Updates().runUpdates(connection);
			if(version>currVersion) throw new Exception("(E) Software must be updated to handle database version " + version);
		}
		catch (Exception e)
			{ connection.rollback(); throw new SQLException("Failed to fully update database\n" + e.getMessage()); }
	}

}