// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Provides functions used by the various data call classes
 * @author CWOLF
 */
abstract class AbstractDataCalls
{
	/** Stores the reference to the database connection */
	protected DatabaseConnection connection;
	
	/** Constructs the abstract data call class */
	AbstractDataCalls(DatabaseConnection connection)
		{ this.connection = connection; }
	
	/** Scrub strings to avoid database errors */
	String scrub(String word, int length)
	{
		if(word!=null)
		{
			word = word.replace("'","''");
			if(connection.getDatabaseType()==DatabaseConnection.POSTGRES)
				word = word.replace("\\","\\\\");
			if(word.length()>length) word = word.substring(0,length);
		}
		return word;
	}
	
	/** Generates the substring of the specified word*/
	String substring(String word, int length)
	{
		if(word!=null && word.length()>length)
			word = word.substring(0,length);
		return word;
	}
	
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
}