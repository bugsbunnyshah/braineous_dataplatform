// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database.updates;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Run updates for version 3
 * @author CWOLF
 */
class Version3Updates extends AbstractVersionUpdate
{
	/** Retrieves the version number */
	protected int getVersionUpdateNumber() { return 3; }
	
	/** Runs the version updates */
	protected void runUpdates() throws SQLException
	{
		// Increase the size of the notes field in the mapping_cell table
		Statement stmt = connection.createStatement();
		stmt.executeUpdate("ALTER TABLE mapping_cell ADD COLUMN temp_notes CHARACTER VARYING(4096)");
		stmt.executeUpdate("UPDATE mapping_cell SET temp_notes=notes");
		stmt.executeUpdate("ALTER TABLE mapping_cell DROP COLUMN notes");
		renameColumn(stmt,"mapping_cell","temp_notes","notes");

		// Close the statement
		stmt.close();
	}
}