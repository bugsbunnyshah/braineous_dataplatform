// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database.updates;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Run updates for version 2
 * @author CWOLF
 */
class Version2Updates extends AbstractVersionUpdate
{
	/** Retrieves the version number */
	protected int getVersionUpdateNumber() { return 2; }
	
	/** Runs the version updates */
	protected void runUpdates() throws SQLException
	{
		// Add model and side fields to mapping schema table
		Statement stmt = connection.createStatement();
		stmt.executeUpdate("ALTER TABLE mapping_schema ADD COLUMN model CHARACTER VARYING(256)");
		stmt.executeUpdate("ALTER TABLE mapping_schema ADD COLUMN side CHARACTER");
		stmt.executeUpdate("DELETE FROM annotation WHERE attribute='SchemaModelsForMapping'");
		stmt.executeUpdate("DELETE FROM annotation WHERE attribute='SchemaSidesForMapping'");

		// Close the statement
		stmt.close();
	}
}