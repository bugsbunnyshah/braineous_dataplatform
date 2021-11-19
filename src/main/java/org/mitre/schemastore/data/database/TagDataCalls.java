// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.mitre.schemastore.model.Tag;

/**
 * Handles tag data calls in the database
 * @author CWOLF
 */
public class TagDataCalls extends AbstractDataCalls
{		
	/** Class for returning a schema tag */
	public class SchemaTag
	{
		/** Stores the schema ID */
		private Integer schemaID;

		/** Stores the tag ID */
		private Integer tagID;

		/** Constructs the schema tag */
		private SchemaTag(Integer schemaID, Integer tagID)
			{ this.schemaID = schemaID;  this.tagID = tagID; }

		/** Returns the schema ID */
		public Integer getSchemaID() { return schemaID; }

		/** Returns the tag ID */
		public Integer getTagID() { return tagID; }
	}
	
	/** Constructs the data call class */
	TagDataCalls(DatabaseConnection connection) { super(connection); }

	/** Retrieves the schema tags validation number */
	public Integer getSchemaTagValidationNumber()
	{
		Integer validationNumber = 0;
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT sum((mod(schema_id,100)+1)*(mod(tag_id,100)+1)) AS validation_number FROM schema_tag");
			if(rs.next())
				validationNumber = rs.getInt("validation_number");
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) TagDataCalls:getSchemaTagValidationNumber: "+e.getMessage()); }
		return validationNumber;
	}

	/** Retrieves the list of tags */
	public ArrayList<Tag> getTags()
	{
		ArrayList<Tag> tags = new ArrayList<Tag>();
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT id,name,parent_id FROM tags");
			while(rs.next())
				tags.add(new Tag(rs.getInt("id"),rs.getString("name"),rs.getInt("parent_id")));
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) TagDataCalls:getTags: "+e.getMessage()); }
		return tags;
	}

	/** Retrieves the specified tag from the repository */
	public Tag getTag(Integer tagID)
	{
		Tag tag = null;
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT id,name,parent_id FROM tags WHERE id = " + tagID);
			if(rs.next())
				tag = new Tag(rs.getInt("id"),rs.getString("name"),rs.getInt("parent_id"));
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) TagDataCalls:getTag: "+e.getMessage()); }
		return tag;
	}

	/** Retrieves the list of sub-categories for the specified tag */
	public ArrayList<Tag> getSubcategories(Integer tagID)
	{
		ArrayList<Tag> tags = new ArrayList<Tag>();
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT id,name,parent_id FROM tags WHERE parent_id " + (tagID==null ? " IS NULL" : "= "+tagID));
			while(rs.next())
				tags.add(new Tag(rs.getInt("id"),rs.getString("name"),rs.getInt("parent_id")));
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) TagDataCalls:getSubcatagories: "+e.getMessage()); }
		return tags;
	}

	/** Adds the specified tag */
	public Integer addTag(Tag tag)
	{
		Integer tagID = 0;
		try {
			tagID = getUniversalIDs(1);
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("INSERT INTO tags(id,name,parent_id) VALUES("+tagID+",'"+scrub(tag.getName(),100)+"',"+tag.getParentId()+")");
			stmt.close();
			connection.commit();
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			tagID = 0;
			System.out.println("(E) TapDataCalls:addTag: "+e.getMessage());
		}
		return tagID;
	}

	/** Updates the specified tag */
	public Boolean updateTag(Tag tag)
	{
		boolean success = false;
		try {
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("UPDATE tags SET name='"+scrub(tag.getName(),100)+"' WHERE id="+tag.getId());
			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) TapDataCalls:updateTags: "+e.getMessage());
		}
		return success;
	}

	/** Deletes the specified tag */
	public boolean deleteTag(int tagID)
	{
		boolean success = false;
		try {
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("DELETE FROM schema_tag WHERE tag_id="+tagID);
			stmt.executeUpdate("DELETE FROM tags WHERE id="+tagID);
			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) TapDataCalls:deleteTag: "+e.getMessage());
		}
		return success;
	}

	/** Retrieve the list of schema tags */
	public ArrayList<SchemaTag> getSchemaTags()
	{
		ArrayList<SchemaTag> schemaTags = new ArrayList<SchemaTag>();
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT schema_id, tag_id FROM schema_tag");
			while(rs.next())
				schemaTags.add(new SchemaTag(rs.getInt("schema_id"),rs.getInt("tag_id")));
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) TagDataCalls:getSchemaTags: "+e.getMessage()); }
		return schemaTags;
	}

	/** Add the tag to the specified schema */
	public Boolean addTagToSchema(Integer schemaID, Integer tagID)
	{
		Boolean success = false;
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT count(*) AS count FROM schema_tag WHERE schema_id="+schemaID+" AND tag_id="+tagID);
			rs.next();
			if(rs.getInt("count")==0)
				stmt.executeUpdate("INSERT INTO schema_tag(schema_id,tag_id) VALUES ("+schemaID+","+tagID+")");
			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) TagDataCalls:addTagToSchema: "+e.getMessage());
		}
		return success;
	}

	/** Remove the tag to the specified schema */
	public Boolean removeTagFromSchema(Integer schemaID, Integer tagID)
	{
		Boolean success = false;
		try {
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("DELETE FROM schema_tag WHERE schema_id="+schemaID+" AND tag_id="+tagID);
			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) TagDataCalls:removeTagFromSchema: "+e.getMessage());
		}
		return success;
	}
}