// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database.updates;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Run updates for version 4
 * @author CWOLF
 */
class Version4Updates extends AbstractVersionUpdate
{
	/** Retrieves the version number */
	protected int getVersionUpdateNumber() { return 4; }
	
	/** Runs the version updates */
	protected void runUpdates() throws SQLException
	{
		// Create the new mapping cell tables
		Statement stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE proposed_mapping_cell (id integer NOT NULL, mapping_id integer, input_id integer, output_id integer, score numeric(6,3), author character varying(100), modification_date date, notes character varying(4096))");
        stmt.executeUpdate("CREATE TABLE validated_mapping_cell (id integer NOT NULL, mapping_id integer, function_class character varying(100), output_id integer, author character varying(100), modification_date date, notes character varying(4096) ) ");
        stmt.executeUpdate("CREATE TABLE mapping_input (cell_id integer, input_id integer, input_order integer ) ");

        // Set up the new primary keys
        stmt.executeUpdate("ALTER TABLE proposed_mapping_cell ADD CONSTRAINT pmappingcell_pkey PRIMARY KEY (id)");
        stmt.executeUpdate("ALTER TABLE validated_mapping_cell ADD CONSTRAINT vmappingcell_pkey PRIMARY KEY (id)");
        stmt.executeUpdate("ALTER TABLE proposed_mapping_cell ADD CONSTRAINT pmappingcell_mapping_id_fkey FOREIGN KEY (mapping_id) REFERENCES mapping(id)");
        stmt.executeUpdate("ALTER TABLE validated_mapping_cell ADD CONSTRAINT vmappingcell_mapping_id_fkey FOREIGN KEY (mapping_id) REFERENCES mapping(id)");

        // Update the notes field to include the transform notes
        stmt.executeUpdate("UPDATE mapping_cell SET notes = notes || '<transform>' || transform || '</transform>' WHERE transform IS NOT NULL AND transform!=''");
        
        // Move all data to the new mapping cell table
        stmt.executeUpdate("INSERT INTO proposed_mapping_cell (id, mapping_id, input_id, output_id, score, author, modification_date, notes) SELECT id, mapping_id, element1_id, element2_id, score, author, modification_date, notes FROM mapping_cell WHERE validated = 'f'");
        stmt.executeUpdate("INSERT INTO validated_mapping_cell (id, mapping_id, function_class, output_id, author, modification_date, notes) SELECT id, mapping_id, 'org.org.mitre.schemastore.mapfunctions.NullFunction', element2_id, author, modification_date, notes FROM mapping_cell WHERE validated = 't'");
        stmt.executeUpdate("INSERT INTO mapping_input (cell_id, input_id, input_order) SELECT id, element1_id, 1 FROM mapping_cell WHERE validated = 't'");

        // Drop the old mapping cell table
        stmt.executeUpdate("DROP TABLE mapping_cell");

        // Close the statement
        stmt.close();
	}
}