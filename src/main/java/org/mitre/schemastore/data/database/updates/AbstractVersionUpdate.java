// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database.updates;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Abstract version update
 * @author CWOLF
 */
abstract class AbstractVersionUpdate
{
	/** Stores the connection on which all actions occur */
	protected Connection connection = null;
	
	/** Retrieves the version number */
	abstract protected int getVersionUpdateNumber();
	
	/** Runs the version updates */
	abstract protected void runUpdates() throws SQLException;

	/** Renames the specified table (different versions for Derby and Postgres) */
	protected void renameTable(Statement stmt, String table, String newTableName) throws SQLException
	{
		try { stmt.executeUpdate("ALTER TABLE " + table + " RENAME TO " + newTableName); }
		catch(Exception e) { stmt.executeUpdate("RENAME TABLE " + table + " TO " + newTableName); }
	}

	/** Renames the specified column (different versions for Derby and Postgres) */
	protected void renameColumn(Statement stmt, String table, String oldColumnName, String newColumnName) throws SQLException
	{
		try { stmt.executeUpdate("ALTER TABLE " + table + " RENAME COLUMN " + oldColumnName + " TO " + newColumnName); }
		catch(Exception e) { stmt.executeUpdate("RENAME COLUMN " + table + "." + oldColumnName + " TO " + newColumnName); }
	}

	/** Retrieves a universal id */
	protected Integer getUniversalIDs(Statement stmt, int count) throws SQLException
	{
		stmt.executeUpdate("LOCK TABLE universal_id IN exclusive MODE");
		stmt.executeUpdate("UPDATE universal_id SET id = id+"+count);
		ResultSet rs = stmt.executeQuery("SELECT id FROM universal_id");
		rs.next();
		Integer universalID = rs.getInt("id")-count;
		return universalID;
	}
	
	/** Runs the version updates */
	protected void runUpdates(Connection connection) throws SQLException
	{
		this.connection = connection;
		
		// Runs the version updates
		runUpdates();
		
		// Update the  version ID
		Statement stmt = connection.createStatement();
		stmt.executeUpdate("UPDATE version SET id="+getVersionUpdateNumber());
		stmt.close();

		// Commit all changes performed during the update
		connection.commit();
	}	
}