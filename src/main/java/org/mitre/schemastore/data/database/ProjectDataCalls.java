// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.MappingCellInput;
import org.mitre.schemastore.model.Project;
import org.mitre.schemastore.model.ProjectSchema;
import org.mitre.schemastore.model.terms.AssociatedElement;

/**
 * Handles project data calls in the database
 * @author CWOLF
 */
public class ProjectDataCalls extends AbstractDataCalls
{	
	/** Constructs the data call class */
	ProjectDataCalls(DatabaseConnection connection) { super(connection); }

	//----------------------------------
	// Handles Projects in the Database
	//----------------------------------

	/** Retrieves the list of projects in the repository */
	public ArrayList<Project> getProjects()
	{
		ArrayList<Project> projects = new ArrayList<Project>();
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM project");
			while(rs.next())
				projects.add(getProject(rs.getInt("id")));
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) ProjectDataCalls:getProjects: "+e.getMessage()); }
		return projects;
	}

	/** Retrieves the specified project from the repository */
	public Project getProject(Integer projectID)
	{
		Project project = null;
		try {
			Statement stmt = connection.getStatement();

			// Get the schemas associated with project
			ArrayList<ProjectSchema> schemas = new ArrayList<ProjectSchema>();
			ResultSet rs = stmt.executeQuery("SELECT schema_id,name,model FROM project_schema,\"schema\" WHERE project_id="+projectID+" AND schema_id=id");
			while(rs.next())
				schemas.add(new ProjectSchema(rs.getInt("schema_id"),rs.getString("name"),rs.getString("model")));
			
			// Get the project information
			ResultSet rs2 = stmt.executeQuery("SELECT name,description,author FROM project WHERE id="+projectID);
			if(rs2.next())
				project = new Project(projectID,rs2.getString("name"),rs2.getString("description"),rs2.getString("author"),schemas.toArray(new ProjectSchema[0]));

			stmt.close();
		} catch(SQLException e) { System.out.println("(E) ProjectDataCalls:getProject: "+e.getMessage()); }
		return project;
	}

	/** Retrieves the list of projects associated with the specified schema in the repository */
	public ArrayList<Integer> getSchemaProjectIDs(Integer schemaID)
	{
		ArrayList<Integer> projectIDs = new ArrayList<Integer>();
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT DISTINCT project_id FROM project_schema WHERE schema_id="+schemaID);
			while(rs.next())
				projectIDs.add(rs.getInt("project_id"));
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) ProjectDataCalls:getSchemaProjects: "+e.getMessage()); }
		return projectIDs;
	}

	/** Adds the specified project */
	public Integer addProject(Project project)
	{
		Integer projectID = 0;
		try {
			// Generate prepared statements for creating the project
			PreparedStatement projectStmt = connection.getPreparedStatement("INSERT INTO project(id,name,description,author) VALUES(?,?,?,?)");
			PreparedStatement schemaStmt = connection.getPreparedStatement("INSERT INTO project_schema(project_id,schema_id,model) VALUES(?,?,?)");

			// Insert the project
			projectID = getUniversalIDs(1);
			projectStmt.setInt(1, projectID);
			projectStmt.setString(2, substring(project.getName(),100));
			projectStmt.setString(3, substring(project.getDescription(),4096));
			projectStmt.setString(4, substring(project.getAuthor(),100));
			projectStmt.execute();
			
			// Insert the project schemas
			for(ProjectSchema schema : project.getSchemas())
			{
				schemaStmt.setInt(1,projectID);
				schemaStmt.setInt(2, schema.getId());
				schemaStmt.setString(3, schema.getModel());
				schemaStmt.execute();
			}
				
			// Close and commit
			projectStmt.close();
			schemaStmt.close();
			connection.commit();
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			projectID = 0;
			System.out.println("(E) ProjectDataCalls:addProject: "+e.getMessage());
		}
		return projectID;
	}

	/** Updates the specified project */
	public boolean updateProject(Project project)
	{
		boolean success = false;
		try {
			// Generate prepared statements for creating the project
			PreparedStatement projectStmt = connection.getPreparedStatement("UPDATE project SET name=?, description=?, author=? WHERE id=?");
			PreparedStatement deleteSchemaStmt = connection.getPreparedStatement("DELETE FROM project_schema WHERE project_id=?");
			PreparedStatement insertSchemaStmt = connection.getPreparedStatement("INSERT INTO project_schema(project_id,schema_id,model) VALUES(?,?,?)");
			
			// Update the project
			projectStmt.setString(1, substring(project.getName(),100));
			projectStmt.setString(2, substring(project.getDescription(),4096));
			projectStmt.setString(3, substring(project.getAuthor(),100));
			projectStmt.setInt(4, project.getId());
			projectStmt.execute();
			
			// Delete the old project schemas
			deleteSchemaStmt.setInt(1, project.getId());
			deleteSchemaStmt.execute();
			
			// Update the project schemas
			for(ProjectSchema schema : project.getSchemas())
			{
				insertSchemaStmt.setInt(1, project.getId());
				insertSchemaStmt.setInt(2, schema.getId());
				insertSchemaStmt.setString(3, schema.getModel());
				insertSchemaStmt.execute();
			}

			// Close and commit
			projectStmt.close();
			deleteSchemaStmt.close();
			insertSchemaStmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) ProjectDataCalls:updateProject: "+e.getMessage());
		}
		return success;
	}

	/** Deletes the specified project */
	public boolean deleteProject(int projectID)
	{
		boolean success = false;
		try {
			// Delete all mappings associated with the project
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM mapping WHERE project_id="+projectID);
			while(rs.next())
				deleteMapping(rs.getInt("id"));
			
			// Delete the project
			stmt.executeUpdate("DELETE FROM project_schema WHERE project_id="+projectID);
			stmt.executeUpdate("DELETE FROM project WHERE id="+projectID);
			stmt.close();

			// Commit the deletion of the project
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) ProjectDataCalls:deleteProject: "+e.getMessage());
		}
		return success;
	}

	//----------------------------------
	// Handles Mappings in the Database
	//----------------------------------

	/** Retrieves the list of mappings for the specified project ID */
	public ArrayList<Mapping> getMappings(Integer projectID, boolean vocabularyMappings)
	{
		ArrayList<Mapping> mappings = new ArrayList<Mapping>();
		try {
			Statement stmt = connection.getStatement();

			// Get the vocabulary ID
			Integer vocabularyID = null;
			ResultSet rs = stmt.executeQuery("SELECT vocabulary_id FROM project WHERE id="+projectID);
			if(rs.next()) vocabularyID = rs.getInt("vocabulary_id");
			if((vocabularyID==null || vocabularyID.equals(0)) && vocabularyMappings) return mappings;
			
			// Construct the mapping query
			String query = "SELECT id,source_id,target_id FROM mapping WHERE project_id="+projectID;
			if(vocabularyID!=null)
				query += " AND target_id" + (vocabularyMappings?"=":"!=") + vocabularyID;

			// Retrieve the mappings
			rs = stmt.executeQuery(query);
			while(rs.next())
				mappings.add(new Mapping(rs.getInt("id"),projectID,rs.getInt("source_id"),rs.getInt("target_id")));
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) ProjectDataCalls:getMappings: "+e.getMessage()); }
		return mappings;		
	}
	
	/** Retrieves the specified mapping */
	public Mapping getMapping(Integer mappingID)
	{
		Mapping mapping = null;
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT project_id,source_id,target_id FROM mapping WHERE id="+mappingID);
			if(rs.next())
				mapping = new Mapping(mappingID,rs.getInt("project_id"),rs.getInt("source_id"),rs.getInt("target_id"));
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) ProjectDataCalls:getMapping: "+e.getMessage()); }
		return mapping;
	}
	
	/** Adds the specified mapping */
	public Integer addMapping(Mapping mapping)
	{
		Integer mappingID = 0;
		try {
			mappingID = getUniversalIDs(1);
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("INSERT INTO mapping(id,project_id,source_id,target_id) VALUES("+mappingID+","+mapping.getProjectId()+","+mapping.getSourceId()+","+mapping.getTargetId()+")");
			stmt.close();
			connection.commit();
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			mappingID = 0;
			System.out.println("(E) ProjectDataCalls:addMapping: "+e.getMessage());
		}
		return mappingID;
	}

	/** Deletes the specified mapping */
	public boolean deleteMapping(int mappingID)
	{
		boolean success = false;
		try {
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("DELETE FROM mapping_cell WHERE mapping_id="+mappingID);
			stmt.executeUpdate("DELETE FROM mapping WHERE id="+mappingID);
			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) ProjectDataCalls:deleteMapping: "+e.getMessage());
		}
		return success;
	}	
	
	//---------------------------------------
	// Handles Mapping Cells in the Database
	//---------------------------------------

	/** Returns a numeric list of the elements */
	private String getNumericList(List<AssociatedElement> elements)
	{
		StringBuffer numericList = new StringBuffer();
		for(AssociatedElement element : elements)
			numericList.append(element.getElementID() + ",");
		return numericList.subSequence(0, numericList.length()-1).toString();
	}

	/** Returns a string list of the elements */
	private String getStringList(List<AssociatedElement> elements)
	{
		StringBuffer stringList = new StringBuffer();
		for(AssociatedElement element : elements)
			stringList.append("'" + element.getElementID() + "',");
		return stringList.subSequence(0, stringList.length()-1).toString();
	}

	/** Returns a like list of the elements */
	private String getLikeList(List<AssociatedElement> elements)
	{
		StringBuffer likeList = new StringBuffer();
		for(AssociatedElement element : elements)
			likeList.append("input_ids LIKE '%" + element.getElementID() + "%' OR ");
		return likeList.subSequence(0, likeList.length()-4).toString();
	}
	
	/** Splits apart the mapping cell inputs */
	private MappingCellInput[] getInputs(String inputString)
	{
        ArrayList<MappingCellInput> inputs = new ArrayList<MappingCellInput>();
        for(String input : inputString.split(","))
        	inputs.add(MappingCellInput.parse(input.trim()));
        return inputs.toArray(new MappingCellInput[0]);
	}
	
	/** Retrieves the list of mapping cells in the repository for the specified mapping */
	public ArrayList<MappingCell> getMappingCells(Integer mappingID)
	{
		ArrayList<MappingCell> mappingCells = new ArrayList<MappingCell>();
		try {
            // Retrieve mapping cells
			Statement stmt = connection.getStatement();
            ResultSet rs = stmt.executeQuery("SELECT id,input_ids,output_id,score,function_id,author,modification_date,notes FROM mapping_cell WHERE mapping_id="+mappingID);
            while(rs.next())
            {            	
                // Retrieves the mapping cell info
                int cellID = rs.getInt("id");
                MappingCellInput[] inputs = getInputs(rs.getString("input_ids"));
                Integer output = rs.getInt("output_id");
                Double score = rs.getDouble("score");
                Integer functionID = rs.getString("function_id")==null?null:rs.getInt("function_id");
                String author = rs.getString("author");
                Date date = rs.getDate("modification_date");
                String notes = rs.getString("notes");
                
                // Store the mapping cell
                mappingCells.add(new MappingCell(cellID,mappingID,inputs,output,score,functionID,author,date,notes));
            }
            rs.close();

            // Close the statements
            stmt.close();
		}
		catch(SQLException e) { System.out.println("(E) ProjectDataCalls:getMappingCells: "+e.getMessage());}
		return mappingCells;
	}

	/** Retrieves the list of mapping cells containing the specific element and above the specified score */
	public ArrayList<MappingCell> getMappingCellsByElement(Integer projectID, List<AssociatedElement> elements, Double minScore)
	{
		ArrayList<MappingCell> mappingCells = new ArrayList<MappingCell>();
		try {

			// Generate hash of all existent elements
			HashSet<String> elementHash = new HashSet<String>();
			for(AssociatedElement element : elements)
				elementHash.add(element.getSchemaID() + "_" + element.getElementID());
			
			// Generate the query
			String query = "SELECT mapping_cell.id,mapping_id,source_id,target_id,input_ids,output_id,score,function_id,author,modification_date,notes " +
						   "FROM mapping_cell,mapping " +
						   "WHERE mapping_id=mapping.id AND project_id=" + projectID + " AND score>=" + minScore + " AND " +
						   "	  (output_id IN (" + getNumericList(elements) + ") OR " +
						   "       (input_ids NOT LIKE '%,%' AND input_ids IN (" + getStringList(elements) + ")) OR " +
						   "       (input_ids LIKE '%,%' AND (" + getLikeList(elements) + ")))";
				
            // Retrieve mapping cells
			Statement stmt = connection.getStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next())
            {            	
            	// Retrieve the mapping info
                int mappingID = rs.getInt("mapping_id");
                int sourceID = rs.getInt("source_id");
                int targetID = rs.getInt("target_id");
            	
            	// Retrieves the mapping cell info
                int cellID = rs.getInt("id");
                MappingCellInput[] inputs = getInputs(rs.getString("input_ids"));
                Integer output = rs.getInt("output_id");
                Double score = rs.getDouble("score");
                Integer functionID = rs.getString("function_id")==null?null:rs.getInt("function_id");
                String author = rs.getString("author");
                Date date = rs.getDate("modification_date");
                String notes = rs.getString("notes");

                // Check to make sure that mapping cell contains a referenced element
                boolean exists = false;
                if(elementHash.contains(targetID+"_"+output)) exists=true;
                for(MappingCellInput input : inputs)
                	if(elementHash.contains(sourceID+"_"+input.getElementID())) exists=true;
                if(!exists) continue;
                	
                // Store the mapping cell
                mappingCells.add(new MappingCell(cellID,mappingID,inputs,output,score,functionID,author,date,notes));
            }
            rs.close();

            // Close the statements
            stmt.close();
		}
		catch(SQLException e) { System.out.println("(E) ProjectDataCalls:getMappingCellsByElement: "+e.getMessage());}
		return mappingCells;
	}
	
	/** Retrieves the list of mapping cells connecting the specified elements in the specified project */
	public ArrayList<MappingCell> getAssociatedMappingCells(Integer projectID, List<AssociatedElement> elements)
	{
		ArrayList<MappingCell> mappingCells = new ArrayList<MappingCell>();
		try {

			// Generate hash of all existent elements
			HashSet<String> elementHash = new HashSet<String>();
			for(AssociatedElement element : elements)
				elementHash.add(element.getSchemaID() + "_" + element.getElementID());
			
			// Generate the query
			String query = "SELECT mapping_cell.id,mapping_id,source_id,target_id,input_ids,output_id,score,function_id,author,modification_date,notes " +
						   "FROM mapping_cell,mapping " +
						   "WHERE mapping_id=mapping.id AND project_id=" + projectID + " AND " +
						   "	  output_id IN (" + getNumericList(elements) + ") AND " +
						   "      ((input_ids NOT LIKE '%,%' AND input_ids IN (" + getStringList(elements) + ")) OR " +
						   "       (input_ids LIKE '%,%' AND (" + getLikeList(elements) + ")))";
			
            // Retrieve mapping cells
			Statement stmt = connection.getStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next())
            {            	
            	// Retrieve the mapping info
                int mappingID = rs.getInt("mapping_id");
                int sourceID = rs.getInt("source_id");
                int targetID = rs.getInt("target_id");
            	
                // Retrieves the mapping cell info
                int cellID = rs.getInt("id");
                MappingCellInput[] inputs = getInputs(rs.getString("input_ids"));
                Integer output = rs.getInt("output_id");
                Double score = rs.getDouble("score");
                Integer functionID = rs.getString("function_id")==null?null:rs.getInt("function_id");
                String author = rs.getString("author");
                Date date = rs.getDate("modification_date");
                String notes = rs.getString("notes");

                // Check to make sure that mapping cell only consists of referenced elements
                if(!elementHash.contains(targetID+"_"+output)) continue;
                for(MappingCellInput input : inputs)
                	if(!elementHash.contains(sourceID+"_"+input.getElementID())) continue;
                
                // Store the mapping cell
                mappingCells.add(new MappingCell(cellID,mappingID,inputs,output,score,functionID,author,date,notes));
            }
            rs.close();

            // Close the statements
            stmt.close();
		}
		catch(SQLException e) { System.out.println("(E) ProjectDataCalls:getAssociatedMappingCells: "+e.getMessage());}
		return mappingCells;
	}
	
	/** Adds the specified mapping cell */
	public Integer addMappingCells(List<MappingCell> mappingCells)
		{ return addMappingCells(mappingCells, true); }

	/** Adds the specified mapping cell */
	private Integer addMappingCells(List<MappingCell> mappingCells, boolean commit)
	{
        // Insert the mapping cells
        Integer mappingCellID = 0;
        try {
        	
        	// Fetch the IDs to use for the mapping cells
            mappingCellID = getUniversalIDs(mappingCells.size());
            
        	// Loop through the mapping cells
            PreparedStatement stmt = connection.getPreparedStatement("INSERT INTO mapping_cell(id, mapping_id, input_ids, output_id, score, function_id, author, modification_date, notes) VALUES (?,?,?,?,?,?,?,?,?)");
            for(int i=0; i<mappingCells.size(); i++)
            {
            	MappingCell mappingCell = mappingCells.get(i);
            	Date date = new Date(mappingCell.getModificationDate().getTime());

	            // Generate the input IDs
	            String inputIDs = "";
	            for(MappingCellInput input : mappingCell.getInputs())
	            	inputIDs += input.toString().replaceAll(",","&#44;") + ",";
	            inputIDs = substring(inputIDs, inputIDs.length()-1);

	            // Stores the validated mapping cell
	            stmt.setInt(1, mappingCellID+i);
	            stmt.setInt(2, mappingCell.getMappingId());
	            stmt.setString(3, inputIDs);
	            stmt.setInt(4, mappingCell.getOutput());
	            stmt.setDouble(5, mappingCell.getScore());
	            if(mappingCell.getFunctionID()==null) stmt.setNull(6, Types.INTEGER);
	            else stmt.setInt(6, mappingCell.getFunctionID());
	            stmt.setString(7, substring(mappingCell.getAuthor(),400));
	            stmt.setDate(8, date);
	            stmt.setString(9, substring(mappingCell.getNotes(),4096));
	            stmt.addBatch();
            }
            stmt.executeBatch();
            stmt.close();
	            
        	// Commit the mapping cell to the database
        	if(commit) connection.commit();
        }
        catch(SQLException e)
        {
            try { connection.rollback(); } catch(SQLException e2) {}
            mappingCellID = 0;
            System.out.println("(E) ProjectDataCalls:addMappingCell: "+e.getMessage());
        }

		return mappingCellID;
	}

	/** Updates the specified mapping cells */
	public boolean updateMappingCells(List<MappingCell> mappingCells)
	{
		// Get the list of mapping cell IDs
		ArrayList<Integer> mappingCellIDs = new ArrayList<Integer>();
		for(MappingCell mappingCell : mappingCells)
			mappingCellIDs.add(mappingCell.getId());
		
		boolean success = true;
		try {
            success = success && deleteMappingCells(mappingCellIDs, false);
            success = success && (!addMappingCells(mappingCells, false).equals(Integer.valueOf(0)));
			if (success) connection.commit();
            else { connection.rollback(); return false; }
        }
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) ProjectDataCalls:updateMappingCell: "+e.getMessage());
            return false;
		}
        return true;
	}

	/** Deletes the specified mapping cells */
	public boolean deleteMappingCells(List<Integer> mappingCellIDs)
		{ return deleteMappingCells(mappingCellIDs, true); }

    /** Deletes the specified mapping cells */
	private boolean deleteMappingCells(List<Integer> mappingCellIDs, boolean commit)
	{
		boolean success = false;
		try {
            PreparedStatement stmt = connection.getPreparedStatement("DELETE FROM mapping_cell WHERE id=?");
			for(Integer mappingCellID : mappingCellIDs)
				{ stmt.setInt(1,mappingCellID); stmt.addBatch(); }
			stmt.executeBatch();
			stmt.close();
            if(commit) connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) ProjectDataCalls:deleteMappingCells: "+e.getMessage());
		}
		return success;
	}
	
	//-------------------------------------------
	// Handles the vocabulary ID in the Database
	//-------------------------------------------
	
	/** Returns the project vocabulary schema id from the repository */
	public Integer getVocabularyID(Integer projectID)
	{
		Integer vocabularyID = null;
		try {			
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT vocabulary_id FROM project WHERE id="+projectID);
			if(rs.next()) vocabularyID = rs.getInt("vocabulary_id");
			if(vocabularyID!=null && vocabularyID.equals(0)) vocabularyID=null;
			stmt.close();
		}
		catch(SQLException e) { System.out.println("(E) VocabularyDataCalls:getVocabularyID: "+e.getMessage()); }
		return vocabularyID;
	}
	
	/** Sets the project vocabulary schema id in the repository */
	public boolean setVocabularyID(Integer projectID, Integer vocabularyID)
	{
		boolean success = false;
		try {			
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("UPDATE project SET vocabulary_id="+vocabularyID+" WHERE id="+projectID);	
			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) VocabularyDataCalls:setVocabularyID: "+e.getMessage());
		}
		return success;
	}

	
	/** Deletes the project vocabulary ID from the project in the repository */
	public boolean deleteVocabularyID(Integer projectID)
	{
		boolean success = false;
		try {			
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("UPDATE project SET vocabulary_id=NULL WHERE id="+projectID);	
			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) VocabularyDataCalls:deleteVocabularyID: "+e.getMessage());
		}
		return success;
	}
}