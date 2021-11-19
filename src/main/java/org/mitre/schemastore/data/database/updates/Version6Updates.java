// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database.updates;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Run updates for version 6
 * @author CWOLF
 */
class Version6Updates extends AbstractVersionUpdate
{
	/** Retrieves the version number */
	protected int getVersionUpdateNumber() { return 6; }
	
	/** Runs the version updates */
	protected void runUpdates() throws SQLException
	{
		// Remove contraints referencing tables and columns referencing "group"
		Statement stmt = connection.createStatement();
		stmt.executeUpdate("ALTER TABLE schema_group DROP CONSTRAINT schemagroup_schema_fkey");
		stmt.executeUpdate("ALTER TABLE schema_group DROP CONSTRAINT schemagroup_groups_fkey");
		stmt.executeUpdate("ALTER TABLE groups DROP CONSTRAINT groups_groups_fkey");
		stmt.executeUpdate("ALTER TABLE groups DROP CONSTRAINT groups_pkey");
		stmt.executeUpdate("DROP INDEX schema_group_idx");
		
		// Rename group to tag on database tables and columns
		renameTable(stmt,"groups","tags");
		renameTable(stmt,"schema_group","schema_tag");
		renameColumn(stmt,"schema_tag","group_id","tag_id");

		// Add constraints back to reference renamed tables and columns
		stmt.executeUpdate("ALTER TABLE tags ADD CONSTRAINT tags_pkey PRIMARY KEY (id)");
		stmt.executeUpdate("ALTER TABLE tags ADD CONSTRAINT tags_tags_fkey FOREIGN KEY (parent_id) REFERENCES tags(id)");		
		stmt.executeUpdate("ALTER TABLE schema_tag ADD CONSTRAINT schematag_tags_fkey FOREIGN KEY (tag_id) REFERENCES tags(id)");
		stmt.executeUpdate("ALTER TABLE schema_tag ADD CONSTRAINT schematag_schema_fkey FOREIGN KEY (schema_id) REFERENCES \"schema\"(id)");
		stmt.executeUpdate("CREATE UNIQUE INDEX schema_tag_idx ON schema_tag (schema_id, tag_id)");

		// Close the statement
		stmt.close();
	}
}