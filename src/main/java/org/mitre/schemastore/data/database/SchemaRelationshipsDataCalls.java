// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Handles schema relationship data calls in the database
 * @author CWOLF
 */
public class SchemaRelationshipsDataCalls extends AbstractDataCalls
{	
	/** Class for returning extensions */
	public class Extension
	{
		/** Stores the schema ID */
		private Integer schemaID;

		/** Stores the extension ID */
		private Integer extensionID;

		/** Constructs the extension */
		private Extension(Integer schemaID, Integer extensionID)
			{ this.schemaID = schemaID;  this.extensionID = extensionID; }

		/** Returns the schema ID */
		public Integer getSchemaID() { return schemaID; }

		/** Returns the extension ID */
		public Integer getExtensionID() { return extensionID; }
	}
	
	/** Constructs the data call class */
	SchemaRelationshipsDataCalls(DatabaseConnection connection) { super(connection); }

	/** Retrieves the schema extensions validation number */
	public Integer getSchemaExtensionsValidationNumber()
	{
		Integer validationNumber = 0;
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT sum((mod(schema_id,100)+1)*(mod(base_id,100)+1)) AS validation_number FROM extensions");
			if(rs.next())
				validationNumber = rs.getInt("validation_number");
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) SchemaRelationshipsDataCalls:getSchemaExtensionsValidationNumber: "+e.getMessage()); }
		return validationNumber;
	}

	/** Retrieves the list of schema extensions */
	public ArrayList<Extension> getSchemaExtensions()
	{
		ArrayList<Extension> extensions = new ArrayList<Extension>();
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT schema_id,base_id FROM extensions");
			while(rs.next())
				extensions.add(new Extension(rs.getInt("base_id"),rs.getInt("schema_id")));
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) SchemaRelationshipsDataCalls:getSchemaExtensions: "+e.getMessage()); }
		return extensions;
	}

	/** Updates the specified schema parents */
	public boolean setSchemaParents(Integer schemaID, ArrayList<Integer> parentIDs)
	{
		boolean success = false;
		try {
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("DELETE FROM extensions WHERE schema_id="+schemaID);
			for(Integer parentSchemaID : parentIDs)
				stmt.executeUpdate("INSERT INTO extensions(schema_id,base_id) VALUES("+schemaID+","+parentSchemaID+")");
			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) SchemaRelationshipsDataCalls:updateSchema: "+e.getMessage());
		}
		return success;
	}
}