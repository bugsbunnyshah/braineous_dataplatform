// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database.updates;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Run updates for version 7
 * @author CWOLF
 */
class Version7Updates extends AbstractVersionUpdate
{
	/** Private class for storing project schemas */
	private class ProjectSchemas
	{
		// Stores the project schemas by side
		private ArrayList<Integer> leftIDs = new ArrayList<Integer>();
		private ArrayList<Integer> rightIDs = new ArrayList<Integer>();
		
		/** Adds a schema to the project schemas */
		private void add(Integer schemaID, String side)
		{
			if(side==null || side.equals("l")) leftIDs.add(schemaID);
			if(side==null || side.equals("r")) rightIDs.add(schemaID);
		}

		/** Indicates if any valid mappings exist */
		private boolean validMappingsExist()
			{ return leftIDs.size()>0 && rightIDs.size()>0; }
		
		/** Returns the list of schemas associated with the project */
		private HashSet<Integer> getSchemaIDs()
		{
			HashSet<Integer> schemaIDs = new HashSet<Integer>(leftIDs); 
			schemaIDs.addAll(rightIDs);
			return schemaIDs;
		}
	}

	/** Class for storing mapping info */
	private class Mapping
	{
		Integer mappingID, leftID, rightID;
		Mapping(Integer mappingID, Integer leftID, Integer rightID)
			{ this.mappingID=mappingID; this.leftID=leftID; this.rightID=rightID; }
	}	
	
	/** Class for storing a left/right element pair */
	private class ElementPair
	{
		// Stores the left and right elements
		private Integer leftID, rightID;
		
		/** Constructs the element pair */
		private ElementPair(Integer leftID, Integer rightID)
			{ this.leftID = leftID; this.rightID = rightID; }
		
		/** Generates a hash code for the element pair */
		public int hashCode()
			{ return leftID*rightID; }
		
		/** Indicates that the two element pairs are equal */
		public boolean equals(Object object)
		{
			if(object instanceof ElementPair)
				return leftID.equals(((ElementPair)object).leftID) && rightID.equals(((ElementPair)object).rightID);
			return false;
		}
	}
	
	/** Class for storing mapping cells */
	private class MappingCells<T> extends HashMap<ElementPair,T>
	{
		/** Adds an object */
		private void add(ElementPair pair, T cell, Mapping mapping, HashMap<Integer,HashSet<Integer>> elementIDs)
		{
    		// Retrieve left and right elements
    		HashSet<Integer> leftIDs = elementIDs.get(mapping.leftID);
    		HashSet<Integer> rightIDs = elementIDs.get(mapping.rightID);

    		// Determine if proposed cell associated with mapping
    		boolean switched = false;
    		if(!leftIDs.contains(pair.leftID) || !rightIDs.contains(pair.rightID))
    		{
    			if(!leftIDs.contains(pair.rightID) || !rightIDs.contains(pair.leftID)) return;
    			else { pair = new ElementPair(pair.rightID,pair.leftID); switched = true; }
    		}
    		
    		// Store proposed cell in mapping
    		if(switched && get(pair)!=null) return;
    		put(pair, cell);			
		}
	}
	
	/** Retrieves the version number */
	protected int getVersionUpdateNumber() { return 7; }
	
	/** Retrieve the project schemas */
	private HashMap<Integer,ProjectSchemas> getProjectSchemas() throws SQLException
	{
		HashMap<Integer,ProjectSchemas> projectSchemaMap = new HashMap<Integer,ProjectSchemas>();
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT project_id,schema_id,side FROM project_schema");
		while(rs.next())
		{
			Integer projectID = rs.getInt("project_id");
			Integer schemaID = rs.getInt("schema_id");
			String side = rs.getString("side");
			ProjectSchemas projectSchemas = projectSchemaMap.get(projectID);
			if(projectSchemas==null) projectSchemaMap.put(projectID, projectSchemas = new ProjectSchemas());
			projectSchemas.add(schemaID,side);
		}
		stmt.close();
		return projectSchemaMap;
	}
	
	/** Retrieves the element IDs for the specified schema IDs */
	private HashMap<Integer,HashSet<Integer>> getElementIDs(HashSet<Integer> schemaIDs) throws SQLException
	{
		HashMap<Integer,HashSet<Integer>> elementIDs = new HashMap<Integer,HashSet<Integer>>();
		Statement stmt = connection.createStatement();
		for(Integer schemaID : schemaIDs)
    	{
    		HashSet<Integer> schemaElementIDs = new HashSet<Integer>();
    		ResultSet rs = stmt.executeQuery("SELECT id FROM schema_elements WHERE schema_id="+schemaID);
    		while(rs.next()) schemaElementIDs.add(rs.getInt("id"));
    		elementIDs.put(schemaID,schemaElementIDs);
    	}
		stmt.close();
		return elementIDs;
	}

	/** Transfers the proposed mapping cells */
	private void transferProposedCells(Integer projectID, ArrayList<Mapping> mappings, HashMap<Integer,HashSet<Integer>> elementIDs) throws SQLException
	{
		/** Class for storing proposed cell info */
		class ProposedCell
		{
			Double score; String author; Date modificationDate; String notes;
			ProposedCell(Double score, String author, Date modificationDate, String notes)
				{ this.score=score; this.author=author; this.modificationDate=modificationDate; this.notes=notes; }
		}
		
		// Retrieves the proposed cells to transfer
		HashMap<Integer,MappingCells<ProposedCell>> proposedCells = new HashMap<Integer,MappingCells<ProposedCell>>();
		
		// Transfer over proposed mapping cells associated with mapping
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT input_id, output_id, score, author, modification_date, notes FROM proposed_mapping_cell WHERE mapping_id=" + projectID);
        while(rs.next())
        {
        	// Retrieve the proposed mapping cell
        	ElementPair pair = new ElementPair(rs.getInt("input_id"),rs.getInt("output_id"));
        	ProposedCell cell = new ProposedCell(rs.getDouble("score"),rs.getString("author"),rs.getDate("modification_date"),rs.getString("notes"));
        	
        	// Stores the proposed mapping cell
        	for(Mapping mapping : mappings)
        	{
            	MappingCells<ProposedCell> cells = proposedCells.get(mapping.mappingID);
            	if(cells==null) proposedCells.put(mapping.mappingID, cells = new MappingCells<ProposedCell>());
            	cells.add(pair, cell, mapping, elementIDs);
        	}
        }
        
        // Stores the proposed mapping cells
        for(Integer mappingID : proposedCells.keySet())
        {
        	MappingCells<ProposedCell> cells = proposedCells.get(mappingID);
        	Integer mappingCellID = getUniversalIDs(stmt, cells.size());
        	for(ElementPair pair : cells.keySet())
        	{
        		ProposedCell cell = cells.get(pair);
        		String author = cell.author==null?"null":"'"+cell.author+"'";
        		String modificationDate = cell.modificationDate==null?"null":"'"+cell.modificationDate+"'";
        		String notes = cell.notes==null?"null":"'"+cell.notes+"'";
        		stmt.addBatch("INSERT INTO proposed_mapping_cell (id, mapping_id, input_id, output_id, score, author, modification_date, notes) " +
                		      "VALUES("+mappingCellID+","+mappingID+","+pair.leftID+","+pair.rightID+","+cell.score+","+author+","+modificationDate+","+notes+")");
        		mappingCellID++;
        	}
        	stmt.executeBatch();
        }
        stmt.close();
	}
	
	/** Transfers the validated mapping cells */
	private void transferValidatedCells(Integer projectID, ArrayList<Mapping> mappings, HashMap<Integer,HashSet<Integer>> elementIDs) throws SQLException
	{
		/** Class for storing validated cell info */
		class ValidatedCell
		{
			String function; String author; Date modificationDate; String notes;
			ValidatedCell(String function, String author, Date modificationDate, String notes)
				{ this.function=function; this.author=author; this.modificationDate=modificationDate; this.notes=notes; }
		}
		
		// Retrieves the validated cells to transfer
		HashMap<Integer,MappingCells<ValidatedCell>> validatedCells = new HashMap<Integer,MappingCells<ValidatedCell>>();
		
		// Transfer over validated mapping cells associated with mapping
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT input_id, output_id, function_class, author, modification_date, notes FROM validated_mapping_cell,mapping_input WHERE mapping_id=" + projectID + " AND id=cell_id AND input_order=1");
        while(rs.next())
        {
        	// Retrieve the validated mapping cell
        	ElementPair pair = new ElementPair(rs.getInt("input_id"),rs.getInt("output_id"));
        	ValidatedCell cell = new ValidatedCell(rs.getString("function_class"),rs.getString("author"),rs.getDate("modification_date"),rs.getString("notes"));
        	
        	// Stores the validated mapping cell
        	for(Mapping mapping : mappings)
        	{
             	MappingCells<ValidatedCell> cells = validatedCells.get(mapping.mappingID);
            	if(cells==null) validatedCells.put(mapping.mappingID, cells = new MappingCells<ValidatedCell>());
            	cells.add(pair, cell, mapping, elementIDs);
        	}
        }   
        
        // Stores the validated mapping cells
        for(Integer mappingID : validatedCells.keySet())
        {
        	MappingCells<ValidatedCell> cells = validatedCells.get(mappingID);
        	Integer mappingCellID = getUniversalIDs(stmt, cells.size());
        	for(ElementPair pair : cells.keySet())
        	{
        		ValidatedCell cell = cells.get(pair);
        		String author = cell.author==null?"null":"'"+cell.author+"'";
        		String modificationDate = cell.modificationDate==null?"null":"'"+cell.modificationDate+"'";
        		String notes = cell.notes==null?"null":"'"+cell.notes+"'";
                stmt.addBatch("INSERT INTO validated_mapping_cell (id, mapping_id, output_id, function_class, author, modification_date, notes) " +
                		      "VALUES("+mappingCellID+","+mappingID+","+pair.rightID+",'"+cell.function+"',"+author+","+modificationDate+","+notes+")");        		
                stmt.addBatch("INSERT INTO mapping_input(cell_id,input_id,input_order) VALUES("+mappingCellID+","+pair.leftID+",1)");
                mappingCellID++;
        	}
        	stmt.executeBatch();
        }
        stmt.close();
	}
	
	/** Migrate the mapping information */
	private void migrateMappingInfo() throws SQLException
	{
		// Get a list of all schemas found in each project
		HashMap<Integer,ProjectSchemas> projectSchemaMap = getProjectSchemas();

		// Cycle through all projects
		for(Integer projectID : projectSchemaMap.keySet())
		{
			// Check to make sure that project contains valid mappings
			ProjectSchemas projectSchemas = projectSchemaMap.get(projectID);			
			if(!projectSchemas.validMappingsExist()) continue;
			
			// Retrieve the schema elements associated with the schemas in this project
			HashMap<Integer,HashSet<Integer>> elementIDs = getElementIDs(projectSchemas.getSchemaIDs());
			
			// Generate mappings for the project
			ArrayList<Mapping> mappings = new ArrayList<Mapping>();
			Statement stmt = connection.createStatement();
			Integer mappingID = getUniversalIDs(stmt, projectSchemas.leftIDs.size()*projectSchemas.rightIDs.size());
        	for(Integer leftID : projectSchemas.leftIDs)
        		for(Integer rightID : projectSchemas.rightIDs)
        			if(!leftID.equals(rightID))
	        		{
	        			stmt.executeUpdate("INSERT INTO mapping(id, project_id, source_id, target_id) VALUES (" + mappingID + "," + projectID + "," + leftID + "," + rightID + ")");			
	        			mappings.add(new Mapping(mappingID,leftID,rightID));
	        			mappingID++;
	        		}
        	stmt.close();
        	
			// Transfer the mapping cells to the new mappings
			transferProposedCells(projectID,mappings,elementIDs);
			transferValidatedCells(projectID,mappings,elementIDs);
		}        	
	}
	
	/** Runs the version updates */
	protected void runUpdates() throws SQLException
	{
		// Remove constraints referencing tables and columns referencing mappings
		Statement stmt = connection.createStatement();
		stmt.executeUpdate("ALTER TABLE mapping_schema DROP CONSTRAINT mappingschema_schema_fkey");
		stmt.executeUpdate("ALTER TABLE mapping_schema DROP CONSTRAINT mappingschema_mapping_fkey");
		stmt.executeUpdate("ALTER TABLE proposed_mapping_cell DROP CONSTRAINT pmappingcell_mapping_id_fkey");
		stmt.executeUpdate("ALTER TABLE validated_mapping_cell DROP CONSTRAINT vmappingcell_mapping_id_fkey");
		stmt.executeUpdate("ALTER TABLE mapping DROP CONSTRAINT mapping_pkey");
		
		// Move to project context from mapping context
		renameTable(stmt,"mapping","project");
		renameTable(stmt,"mapping_schema","project_schema");
		renameColumn(stmt,"project_schema","mapping_id","project_id");
        stmt.executeUpdate("CREATE TABLE mapping (id integer NOT NULL, project_id integer NOT NULL, source_id integer NOT NULL, target_id integer NOT NULL)");

        // Migrate the mapping information to the new tables
        migrateMappingInfo();
        
        // Delete the old mapping cells
        stmt.executeUpdate("DELETE FROM proposed_mapping_cell WHERE mapping_id IN (SELECT id FROM project)");
        stmt.executeUpdate("DELETE FROM mapping_input WHERE cell_id IN (SELECT id FROM validated_mapping_cell WHERE mapping_id IN (SELECT id FROM project))");
        stmt.executeUpdate("DELETE FROM validated_mapping_cell WHERE mapping_id IN (SELECT id FROM project)");
        
        // Drop the unneeded side field now that transformation is complete
        stmt.executeUpdate("ALTER TABLE project_schema DROP COLUMN side");

		// Add constraints back to reference renamed tables and columns
		stmt.executeUpdate("ALTER TABLE project ADD CONSTRAINT project_pkey PRIMARY KEY (id)");
		stmt.executeUpdate("ALTER TABLE mapping ADD CONSTRAINT mapping_pkey PRIMARY KEY (id)");
		stmt.executeUpdate("ALTER TABLE proposed_mapping_cell ADD CONSTRAINT pmappingcell_mapping_id_fkey FOREIGN KEY (mapping_id) REFERENCES mapping(id)");
		stmt.executeUpdate("ALTER TABLE validated_mapping_cell ADD CONSTRAINT vmappingcell_mapping_id_fkey FOREIGN KEY (mapping_id) REFERENCES mapping(id)");
		stmt.executeUpdate("ALTER TABLE project_schema ADD CONSTRAINT projectschema_project_fkey FOREIGN KEY (project_id) REFERENCES project(id)");
		stmt.executeUpdate("ALTER TABLE project_schema ADD CONSTRAINT projectschema_schema_fkey FOREIGN KEY (schema_id) REFERENCES \"schema\"(id)");

		// Close the statement
		stmt.close();
	}
}