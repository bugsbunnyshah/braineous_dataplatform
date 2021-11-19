// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Handles access to the database
 * @author CWOLF
 */
public class Database
{
	/** Sets up a database connection */
	private DatabaseConnection connection = null;

	// Stores the various data call classes
	private SchemaDataCalls schemaDataCalls = null;
	private SchemaRelationshipsDataCalls schemaRelationshipsDataCalls = null;
	private SchemaElementDataCalls schemaElementDataCalls = null;
	private TagDataCalls tagDataCalls = null;
	private DataSourceDataCalls dataSourceDataCalls = null;
	private ProjectDataCalls projectDataCalls = null;
	private FunctionDataCalls functionDataCalls = null;
	private AnnotationDataCalls annotationDataCalls = null;
	
	/** Constructs the database class */
	public Database(DatabaseConnection connection)
	{
		this.connection = connection;
		schemaDataCalls = new SchemaDataCalls(connection);
		schemaRelationshipsDataCalls = new SchemaRelationshipsDataCalls(connection);
		schemaElementDataCalls = new SchemaElementDataCalls(connection);
		tagDataCalls = new TagDataCalls(connection);
		dataSourceDataCalls = new DataSourceDataCalls(connection);
		projectDataCalls = new ProjectDataCalls(connection);
		functionDataCalls = new FunctionDataCalls(connection);
		annotationDataCalls = new AnnotationDataCalls(connection);
	}

	// GraphData call getters
	public SchemaDataCalls getSchemaDataCalls() { return schemaDataCalls; }
	public SchemaRelationshipsDataCalls getSchemaRelationshipsDataCalls() { return schemaRelationshipsDataCalls; }
	public SchemaElementDataCalls getSchemaElementDataCalls() { return schemaElementDataCalls; }
	public TagDataCalls getTagDataCalls() { return tagDataCalls; }
	public DataSourceDataCalls getDataSourceDataCalls() { return dataSourceDataCalls; }
	public ProjectDataCalls getProjectDataCalls() { return projectDataCalls; }
	public FunctionDataCalls getFunctionDataCalls() { return functionDataCalls; }
	public AnnotationDataCalls getAnnotationDataCalls() { return annotationDataCalls; }
	
    /** Indicates if the database is properly connected */
 	public boolean isConnected()
 		{ try { connection.getStatement(); return true; } catch(SQLException e) { return false; } }
 
	/** Retrieves a universal id */
	public Integer getUniversalIDs(int count) throws SQLException
	{
		Statement stmt = connection.getStatement();
		stmt.executeUpdate("LOCK TABLE universal_id IN exclusive MODE");
		stmt.executeUpdate("UPDATE universal_id SET id = id+"+count);
		ResultSet rs = stmt.executeQuery("SELECT id FROM universal_id");
		rs.next();
		Integer universalID = rs.getInt("id")-count;
		stmt.close();
		connection.commit();
		return universalID;
	}
 	
 	/** Compresses the database */
 	public boolean compress()
 	{
		try {
    		Statement stmt = connection.getStatement();

    		// Compresses a derby database
    		if(connection.getDatabaseType().equals(DatabaseConnection.DERBY))
        	{
    			// Retrieve the tables to compress
    			ArrayList<String> tableNames = new ArrayList<String>();
    			ResultSet rs = stmt.executeQuery("SELECT tablename FROM SYS.SYSTABLES WHERE TABLETYPE='T'");
    			while(rs.next())
    				tableNames.add(rs.getString("tablename"));
    			
    			// Compress the tables
				String schema = connection.getDatabaseUser().toUpperCase();
				for(String tableName : tableNames)
    				stmt.execute("CALL SYSCS_UTIL.SYSCS_COMPRESS_TABLE('"+schema+"','"+tableName+"',0)");
        	}
			
			// Compresses a postgres database
			else
			{
				connection.setAutoCommit(true);
				stmt.execute("VACUUM FULL");
				connection.setAutoCommit(false);
			}
			
			stmt.close();
			connection.commit();
			return true;
		}
  		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) Database:compress: "+e.getMessage());
	  		return false;
		}
 	}
}