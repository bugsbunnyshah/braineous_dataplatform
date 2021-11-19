package org.mitre.schemastore.data.database.updates;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Adds a group field to the annotations table for faster lookup of various objects which belong
 * to a group.
 * 
 * @author CWOLF
 * 
 */
public class Version16Updates extends AbstractVersionUpdate
{
	/** Retrieves the version number */
	protected int getVersionUpdateNumber() { return 16; }

	/** Runs the version updates */
	protected void runUpdates() throws SQLException
	{
		Statement stmt = connection.createStatement();
		stmt.executeUpdate("ALTER TABLE annotation ADD COLUMN group_id INTEGER");
		stmt.executeUpdate("CREATE INDEX annotation_element_idx ON annotation(element_id)");
		stmt.executeUpdate("CREATE INDEX annotation_group_idx ON annotation(group_id)");
		stmt.close();
	}
}