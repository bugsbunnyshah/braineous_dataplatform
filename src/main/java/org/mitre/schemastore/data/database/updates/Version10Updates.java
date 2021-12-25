// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database.updates;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Run updates for version 10
 * @author CWOLF
 */
class Version10Updates extends AbstractVersionUpdate
{
	/** Retrieves the version number */
	protected int getVersionUpdateNumber() { return 10; }
	
	/** Runs the version updates */
	protected void runUpdates() throws SQLException
	{
		// Fix the "upper" and "lower" default functions to only have one input value
		Statement stmt = connection.createStatement();
		stmt.executeUpdate("DELETE FROM function_input WHERE function_id=481 AND input_loc=2");
		stmt.executeUpdate("DELETE FROM function_input WHERE function_id=482 AND input_loc=2");
		stmt.close();
	}
}