// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.Thesaurus;
import org.mitre.schemastore.model.Vocabulary;

/**
 * Handles schema data calls in the database
 * @author CWOLF
 */
public class SchemaDataCalls extends AbstractDataCalls
{	
	/** Retrieves the list of schemas in the repository */
	private ArrayList<Schema> retrieveSchemas(Integer schemaID, Class type)
	{
		// Generate the SQL command
		String command = "SELECT id,name,author,source,\"type\",description,locked FROM \"schema\" WHERE 1=1";
		if(schemaID!=null) command += " AND id="+schemaID;
		if(type!=null)
		{
			if(type!=Schema.class)
				command += " AND \"type\"='"+type.toString()+"'";
			else command += " AND \"type\"!='"+Thesaurus.class+"' AND \"type\"!='"+Vocabulary.class+"'";
		}
		
		// Retrieve the list of schemas
		ArrayList<Schema> schemas = new ArrayList<Schema>();
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery(command);
			while(rs.next())
				schemas.add(new Schema(rs.getInt("id"),rs.getString("name"),rs.getString("author"),rs.getString("source"),rs.getString("type"),rs.getString("description"),rs.getString("locked").equals("t")));
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) SchemaDataCalls:retrieveSchemas: "+e.getMessage()); }
		return schemas;
	}
	
	/** Constructs the data call class */
	SchemaDataCalls(DatabaseConnection connection) { super(connection); }

	/** Retrieves the list of schemas in the repository */
	public ArrayList<Schema> getSchemas(Class type)
		{ return retrieveSchemas(null,type); }
	
	/** Retrieves the specified schema in the repository */
	public Schema getSchema(Integer schemaID)
	{
		ArrayList<Schema> schemas = retrieveSchemas(schemaID,null);
		return schemas.size()==0 ? null : schemas.get(0);
	}

	/** Extends the specified schema */
	public Schema extendSchema(Schema schema)
	{
		Schema extendedSchema = null;
		try {
			int schemaID = getUniversalIDs(1);
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("INSERT INTO \"schema\"(id,name,author,source,\"type\",description,locked) VALUES("+schemaID+",'"+scrub(schema.getName(),100)+" Extension','"+scrub(schema.getAuthor(),100)+"','"+scrub(schema.getSource(),200)+"','"+scrub(schema.getType(),100)+"','Extension of "+scrub(schema.getName(),483)+"','f')");
			stmt.executeUpdate("INSERT INTO extensions(schema_id,base_id) VALUES("+schemaID+","+schema.getId()+")");
			stmt.close();
			connection.commit();
			extendedSchema = new Schema(schemaID,schema.getName()+" Extension",schema.getAuthor(),schema.getSource(),schema.getType(),"Extension of "+schema.getName(),false);
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) SchemaDataCalls:extendSchema: "+e.getMessage());
		}
		return extendedSchema;
	}

	/** Adds the specified schema */
	public Integer addSchema(Schema schema)
	{
		Integer schemaID = 0;
		try {
			schemaID = getUniversalIDs(1);
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("INSERT INTO \"schema\"(id,name,author,source,\"type\",description,locked) VALUES("+schemaID+",'"+scrub(schema.getName(),100)+"','"+scrub(schema.getAuthor(),100)+"','"+scrub(schema.getSource(),200)+"','"+scrub(schema.getType(),100)+"','"+scrub(schema.getDescription(),500)+"','"+(schema.getLocked()?"t":"f")+"')");
			stmt.close();
			connection.commit();
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			schemaID = 0;
			System.out.println("(E) SchemaDataCalls:addSchema: "+e.getMessage());
		}
		return schemaID;
	}

	/** Updates the specified schema */
	public boolean updateSchema(Schema schema)
	{
		boolean success = false;
		try {
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("UPDATE \"schema\" SET name='"+scrub(schema.getName(),100)+"', author='"+scrub(schema.getAuthor(),100)+"', source='"+scrub(schema.getSource(),200)+"', \"type\"='"+scrub(schema.getType(),100)+"', description='"+scrub(schema.getDescription(),4096)+"' WHERE id="+schema.getId());
			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) SchemaDataCalls:updateSchema: "+e.getMessage());
		}
		return success;
	}

	/** Returns the list of deletable schemas */
	public ArrayList<Integer> getDeletableSchemas()
	{
		ArrayList<Integer> schemas = new ArrayList<Integer>();
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM \"schema\" " +
											 "EXCEPT SELECT schema_id AS id FROM data_source " +
											 "EXCEPT SELECT schema_id AS id FROM project_schema " +
											 "EXCEPT SELECT vocabulary_id AS id FROM project " +
											 "EXCEPT SELECT base_id AS id FROM extensions");
			while(rs.next())
				schemas.add(rs.getInt("id"));
			stmt.close();
		}
		catch(SQLException e) { System.out.println("(E) SchemaDataCalls:getDeletableSchemas: "+e.getMessage()); }
		return schemas;
	}

	/** Deletes the specified schema */
	public boolean deleteSchema(int schemaID)
	{
		boolean success = false;
		try {
			// Identify which schema elements should be deleted
			Statement stmt = connection.getStatement();

			// Delete the schema elements
			stmt.executeUpdate("DELETE FROM alias WHERE schema_id="+schemaID);
			stmt.executeUpdate("DELETE FROM synonym WHERE schema_id="+schemaID);
			stmt.executeUpdate("DELETE FROM subtype WHERE schema_id="+schemaID);
			stmt.executeUpdate("DELETE FROM containment WHERE schema_id="+schemaID);
			stmt.executeUpdate("DELETE FROM relationship WHERE schema_id="+schemaID);
			stmt.executeUpdate("DELETE FROM attribute WHERE schema_id="+schemaID);
			stmt.executeUpdate("DELETE FROM domainvalue WHERE schema_id="+schemaID);
			stmt.executeUpdate("DELETE FROM \"domain\" WHERE schema_id="+schemaID);
			stmt.executeUpdate("DELETE FROM entity WHERE schema_id="+schemaID);

			// Delete the schema from the database
			stmt.executeUpdate("DELETE FROM schema_tag WHERE schema_id="+schemaID);
			stmt.executeUpdate("DELETE FROM extensions WHERE schema_id="+schemaID);
			stmt.executeUpdate("DELETE FROM \"schema\" WHERE id="+schemaID);
			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) SchemaDataCalls:deleteSchema: "+e.getMessage());
		}
		return success;
	}

	/** Locks the specified schema */
	public boolean lockSchema(int schemaID, boolean locked)
	{
		boolean success = false;
		try {
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("UPDATE \"schema\" SET locked='"+(locked?"t":"f")+"' WHERE id="+schemaID);
			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) SchemaDataCalls:lockSchema: "+e.getMessage());
		}
		return success;
	}
}