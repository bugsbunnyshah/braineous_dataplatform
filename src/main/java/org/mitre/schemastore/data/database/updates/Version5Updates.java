// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database.updates;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Run updates for version 5
 * @author CWOLF
 */
class Version5Updates extends AbstractVersionUpdate
{
	/** Retrieves the version number */
	protected int getVersionUpdateNumber() { return 5; }
	
	/** Runs the version updates */
	protected void runUpdates() throws SQLException
	{
		// Increase the size of the notes field in the proposed_mapping_cell table
		Statement stmt = connection.createStatement();
		stmt.executeUpdate("ALTER TABLE proposed_mapping_cell ADD COLUMN temp_notes CHARACTER VARYING(4096)");
		stmt.executeUpdate("UPDATE proposed_mapping_cell SET temp_notes=notes");
		stmt.executeUpdate("ALTER TABLE proposed_mapping_cell DROP COLUMN notes");
		renameColumn(stmt,"proposed_mapping_cell","temp_notes","notes");

		// Increase the size of the notes field in the validated_mapping_cell table
		stmt.executeUpdate("ALTER TABLE validated_mapping_cell ADD COLUMN temp_notes CHARACTER VARYING(4096)");
		stmt.executeUpdate("UPDATE validated_mapping_cell SET temp_notes=notes");
		stmt.executeUpdate("ALTER TABLE validated_mapping_cell DROP COLUMN notes");
		renameColumn(stmt,"validated_mapping_cell","temp_notes","notes");

		// Close the statement
		stmt.close();
	}
}