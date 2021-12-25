// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database.updates;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Run updates for version 14
 * @author CWOLF
 */
class Version14Updates extends AbstractVersionUpdate
{
	/** Retrieves the version number */
	protected int getVersionUpdateNumber() { return 14; }
	
	/** Runs the version updates */
	protected void runUpdates() throws SQLException
	{
		// Increase the size of the author field in the mapping_cell table
		Statement stmt = connection.createStatement();
		stmt.executeUpdate("ALTER TABLE mapping_cell ADD COLUMN temp_author CHARACTER VARYING(400)");
		stmt.executeUpdate("UPDATE mapping_cell SET temp_author=author");
		stmt.executeUpdate("ALTER TABLE mapping_cell DROP COLUMN author");
		renameColumn(stmt,"mapping_cell","temp_author","author");

		// Close the statement
		stmt.close();
	}
}