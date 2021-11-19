// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database.updates;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Run updates for version 13
 * @author CWOLF
 */
class Version13Updates extends AbstractVersionUpdate
{
	/** Retrieves the version number */
	protected int getVersionUpdateNumber() { return 13; }
	
	/** Runs the version updates */
	protected void runUpdates() throws SQLException
	{
		// Adds a description field to the relationship table
		Statement stmt = connection.createStatement();
		stmt.executeUpdate("ALTER TABLE project DROP CONSTRAINT project_project_fkey");
		stmt.executeUpdate("ALTER TABLE project ADD CONSTRAINT project_schema_fkey FOREIGN KEY (vocabulary_id) REFERENCES \"schema\"(id)");
		stmt.close();
	}
}