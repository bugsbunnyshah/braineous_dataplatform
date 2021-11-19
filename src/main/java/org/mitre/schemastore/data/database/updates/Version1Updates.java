// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database.updates;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Run updates for version 1
 * @author CWOLF
 */
class Version1Updates extends AbstractVersionUpdate
{
	/** Retrieves the version number */
	protected int getVersionUpdateNumber() { return 1; }
	
	/** Runs the version updates */
	protected void runUpdates() throws SQLException
	{
		// Rename object_id to element_id in the annotations table
		Statement stmt = connection.createStatement();
		renameColumn(stmt,"annotation","object_id","element_id");

		// Increase the size of the value field in the annotations table
		stmt.executeUpdate("ALTER TABLE annotation ADD COLUMN temp_value CHARACTER VARYING(4096)");
		stmt.executeUpdate("UPDATE annotation SET temp_value=value");
		stmt.executeUpdate("ALTER TABLE annotation DROP COLUMN value");
		renameColumn(stmt,"annotation","temp_value","value");

		// Close the statement
		stmt.close();
	}
}