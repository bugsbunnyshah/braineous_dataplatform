// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database.updates;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Run updates for version 11
 * @author CWOLF
 */
class Version11Updates extends AbstractVersionUpdate
{
	/** Retrieves the version number */
	protected int getVersionUpdateNumber() { return 11; }
	
	/** Runs the version updates */
	protected void runUpdates() throws SQLException
	{
		// Adds a description field to the relationship table
		Statement stmt = connection.createStatement();
		stmt.executeUpdate("ALTER TABLE relationship ADD COLUMN description CHARACTER VARYING(4096)");
		stmt.close();
	}
}