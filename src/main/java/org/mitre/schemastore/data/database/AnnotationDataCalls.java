// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.mitre.schemastore.model.Annotation;

/**
 * Handles annotation data calls in the database
 * @author CWOLF
 */
public class AnnotationDataCalls extends AbstractDataCalls
{		
	/** Constructs the data call class */
	AnnotationDataCalls(DatabaseConnection connection) { super(connection); }

	/** Sets the specified annotation in the database */
	public boolean setAnnotation(int elementID, int groupID, String attribute, String value)
	{
		// Set the annotation
		boolean success = false;
		try {
			Statement stmt = connection.getStatement();
			
			// Delete the old annotation
			stmt.executeUpdate("DELETE FROM annotation " +
					   		   "WHERE element_id="+elementID + " AND attribute='"+attribute+"' AND group_id ="+groupID);

			// Insert the new annotation
			stmt.executeUpdate("INSERT INTO annotation(element_id,group_id,attribute,value) " +
							   "VALUES("+elementID+","+groupID+",'"+attribute+"','"+value+"')");

			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) Database:setAnnotation: "+e.getMessage());
		}
		return success;
	}
	
	/** Sets the specified list of annotations in the database */
	public boolean setAnnotations(List<Annotation> annotations)
	{
		boolean success = false;
		try {
			Statement stmt = connection.getStatement();
			for(Annotation annotation : annotations)
			{	
				// Get the annotation pieces
				Integer elementID = annotation.getElementID();
				Integer groupID = annotation.getGroupID(); if(groupID==null) groupID=0;
				String attribute = annotation.getAttribute();
				String value = annotation.getValue();				
				
				// Delete old annotation
				stmt.addBatch("DELETE FROM annotation " +
					   		  "WHERE element_id="+elementID + " AND attribute='"+attribute+"' AND group_id ="+groupID);

				// Insert the new annotation
				stmt.addBatch("INSERT INTO annotation(element_id,group_id,attribute,value) " +
							  "VALUES("+elementID+","+groupID+",'"+attribute+"','"+value+"')");
			}
			stmt.executeBatch();
			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) Database:setAnnotations: "+e.getMessage());
		}
		return success;
		
	}	
	
	/** Gets the specified annotation in the database */
	public String getAnnotation(int elementID, int groupID, String attribute)
	{
		String value = null;
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT value FROM annotation " +
											 "WHERE element_id=" + elementID + " AND " +
											 "      attribute='" + attribute + "' AND " +
											 "      group_id=" + groupID);
			if(rs.next()) value = rs.getString("value");
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) Database:getAnnotation: "+e.getMessage()); }
		return value;
	}

	/** Gets the annotations for the specified group */
	public ArrayList<Annotation> getAnnotations(int elementID, String attribute)
	{
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT group_id, value FROM annotation WHERE element_id="+elementID+" AND attribute LIKE '"+attribute+"%'");
			while(rs.next())
				annotations.add(new Annotation(elementID,rs.getInt("group_id"),attribute,rs.getString("value")));
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) Database:getAnnotations: "+e.getMessage()); }
		return annotations;
	}
	
	/** Gets the annotations for the specified group */
	public ArrayList<Annotation> getAnnotationsByGroup(int groupID, String attribute)
	{
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT element_id, value FROM annotation WHERE group_id="+groupID+" AND attribute LIKE '"+attribute+"%'");
			while(rs.next())
				annotations.add(new Annotation(rs.getInt("element_id"),groupID,attribute,rs.getString("value")));
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) Database:getAnnotationsByGroup: "+e.getMessage()); }
		return annotations;
	}
	
 	/** Clears the specified annotation in the database */
 	public boolean clearAnnotation(int elementID, int groupID, String attribute)
 	{
		boolean success = false;
		try {
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("DELETE FROM annotation " +
							   "WHERE element_id="+elementID + " AND attribute='"+attribute+"' AND group_id="+groupID);
			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) Database:clearAnnotation: "+e.getMessage());
		}
		return success;
 	}
	
 	/** Clears the specified group annotations in the database */
 	public boolean clearAnnotations(int groupID, String attribute)
 	{
		boolean success = false;
		try {
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("DELETE FROM annotation WHERE attribute='"+attribute+"' AND group_id="+groupID);
			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) Database:clearAnnotations: "+e.getMessage());
		}
		return success;
 	}
}