// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database.updates;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Run updates for version 12
 * @author CWOLF
 */
class Version12Updates extends AbstractVersionUpdate
{
	/** Retrieves the version number */
	protected int getVersionUpdateNumber() { return 12; }
	
	/** Runs the version updates */
	protected void runUpdates() throws SQLException
	{
		// Adds a description field to the relationship table
		Statement stmt = connection.createStatement();
		stmt.executeUpdate("ALTER TABLE project ADD COLUMN vocabulary_id INTEGER");
		stmt.executeUpdate("ALTER TABLE project ADD CONSTRAINT project_project_fkey FOREIGN KEY (vocabulary_id) REFERENCES project(id)");
		stmt.close();
	}
}