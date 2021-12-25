// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database.updates;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Run updates for version 9
 * @author CWOLF
 */
class Version9Updates extends AbstractVersionUpdate
{
	/** Retrieves the version number */
	protected int getVersionUpdateNumber() { return 9; }
	
	/** Runs the version updates */
	protected void runUpdates() throws SQLException
	{
		// Create the new tables
		Statement stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE data_type (id integer NOT NULL,type character varying(30) NOT NULL,description character varying(500))");
        stmt.executeUpdate("CREATE TABLE functions (id integer NOT NULL,name character varying(50) NOT NULL,description character varying(4096),expression character varying(200),category character varying(100),output_type integer NOT NULL)");
        stmt.executeUpdate("CREATE TABLE function_input (function_id integer NOT NULL,input_type integer NOT NULL,input_loc integer NOT NULL)");
        stmt.executeUpdate("CREATE TABLE function_implementation (function_id integer NOT NULL,language character varying(50) NOT NULL,dialect character varying(50),implementation character varying(500) NOT NULL)");
        stmt.executeUpdate("CREATE TABLE mapping_cell (id integer NOT NULL,mapping_id integer NOT NULL,input_ids character varying(200) NOT NULL,output_id integer NOT NULL,score numeric(6,3) NOT NULL,function_id integer,author character varying(100),modification_date date,notes character varying(4096))");

        // Add constraints to tables
    	stmt.executeUpdate("ALTER TABLE mapping_cell ADD CONSTRAINT mappingcell_pkey PRIMARY KEY (id)");
    	stmt.executeUpdate("ALTER TABLE data_type ADD CONSTRAINT datatype_pkey PRIMARY KEY (id)");
    	stmt.executeUpdate("ALTER TABLE functions ADD CONSTRAINT function_pkey PRIMARY KEY (id)");
    	stmt.executeUpdate("ALTER TABLE mapping_cell ADD CONSTRAINT mappingcell_mapping_id_fkey FOREIGN KEY (mapping_id) REFERENCES mapping(id)");
    	stmt.executeUpdate("ALTER TABLE mapping_cell ADD CONSTRAINT mappingcell_function_id_fkey FOREIGN KEY (function_id) REFERENCES functions(id)");
    	stmt.executeUpdate("ALTER TABLE functions ADD CONSTRAINT function_output_fkey FOREIGN KEY (output_type) REFERENCES data_type(id)");
        stmt.executeUpdate("ALTER TABLE function_input ADD CONSTRAINT functioninput_function_id_fkey FOREIGN KEY (function_id) REFERENCES functions(id)");
        stmt.executeUpdate("ALTER TABLE function_input ADD CONSTRAINT functioninput_input_fkey FOREIGN KEY (input_type) REFERENCES data_type(id)");
        stmt.executeUpdate("ALTER TABLE function_implementation ADD CONSTRAINT functionimp_function_id_fkey FOREIGN KEY (function_id) REFERENCES functions(id)");

        // Define indexes on table
        stmt.executeUpdate("CREATE INDEX mappingcell_function_idx ON mapping_cell (function_id)");
        stmt.executeUpdate("CREATE UNIQUE INDEX function_name_idx ON functions (name)");
        stmt.executeUpdate("CREATE UNIQUE INDEX function_implementation_idx ON function_implementation (function_id, language, dialect)");

        // Populate the function table
        DatabaseUpdates.initializeData(stmt,"SchemaStoreFunctionData.txt");
        
        // Transfer all proposed mapping cells to mapping cell table
        stmt.executeUpdate("INSERT INTO mapping_cell(id,mapping_id,input_ids,output_id,score,author,modification_date,notes) " +
        				   "SELECT id,mapping_id,CAST(input_id AS CHAR(200)),output_id,score,author,modification_date,notes FROM proposed_mapping_cell");
        
        // Transfer all validated mapping cells to mapping cell table
        stmt.executeUpdate("INSERT INTO mapping_cell(id,mapping_id,input_ids,output_id,score,function_id,author,modification_date,notes) " +
        				   "SELECT id,mapping_id,CAST(input_id AS CHAR(200)),output_id,1.0,450,author,modification_date,notes FROM validated_mapping_cell,mapping_input WHERE id=cell_id AND input_order=1");
        
        // Rename fields and drop old tables
        stmt.executeUpdate("DROP TABLE mapping_input");
        stmt.executeUpdate("DROP TABLE proposed_mapping_cell");
        stmt.executeUpdate("DROP TABLE validated_mapping_cell");
        
        // Close the statement
		stmt.close();
	}
}