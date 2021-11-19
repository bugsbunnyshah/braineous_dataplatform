// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mitre.schemastore.data.database.ProjectDataCalls;
import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.Project;
import org.mitre.schemastore.model.terms.AssociatedElement;

/** Class for managing the projects in the schema repository */
public class ProjectCache extends DataCache
{
	/** Stores reference to the project data calls */
	private ProjectDataCalls dataCalls = null;
	
	/** Constructs the project cache */
	ProjectCache(DataManager manager, ProjectDataCalls dataCalls)
		{ super(manager); this.dataCalls=dataCalls; }

	/** Returns a listing of all projects */
	public ArrayList<Project> getProjects()
		{ return dataCalls.getProjects(); }

	/** Retrieve the specified project */
	public Project getProject(Integer projectID)
		{ return dataCalls.getProject(projectID); }

	/** Retrieves the list of projects associated with the specified schema */
	public ArrayList<Integer> getSchemaProjectIDs(Integer schemaID)
		{ return dataCalls.getSchemaProjectIDs(schemaID); }
	
	/** Add the specified project */
	public Integer addProject(Project project)
		{ return dataCalls.addProject(project); }

	/** Update the specified project */
	public Boolean updateProject(Project project)
	{
		ArrayList<Integer> schemaIDs = new ArrayList<Integer>(Arrays.asList(project.getSchemaIDs()));
		for(Mapping mapping : getMappings(project.getId()))
			if(!schemaIDs.contains(mapping.getSourceId()) || !schemaIDs.contains(mapping.getTargetId())) return false;
		return dataCalls.updateProject(project);
	}

	/** Delete the specified project */
	public Boolean deleteProject(Integer projectID)
		{ return dataCalls.deleteProject(projectID); }

	/** Returns a listing of mappings for the specified project */
	public ArrayList<Mapping> getMappings(Integer projectID)
		{ return dataCalls.getMappings(projectID,false); }

	/** Returns a listing of the vocabulary mappings for the specified project */
	public ArrayList<Mapping> getVocabularyMappings(Integer projectID)
		{ return dataCalls.getMappings(projectID,true); }
	
	/** Retrieve the specified mapping */
	public Mapping getMapping(Integer mappingID)
		{ return dataCalls.getMapping(mappingID); }
	
	/** Add the specified mapping */
	public Integer addMapping(Mapping mapping)
	{
		Integer mappingID = 0;
		try {
			// Get the project and vocabulary IDs
			Integer projectID = mapping.getProjectId();
			Integer vocabularyID = dataCalls.getVocabularyID(projectID);

			// Verify that the mapping is legal
			ArrayList<Integer> schemaIDs = new ArrayList<Integer>(Arrays.asList(dataCalls.getProject(projectID).getSchemaIDs()));
			if(vocabularyID!=null) schemaIDs.add(vocabularyID);
			if(mapping.getSourceId().equals(mapping.getTargetId())) return 0;
			if(!schemaIDs.contains(mapping.getSourceId()) || !schemaIDs.contains(mapping.getTargetId())) return 0;

			// Generate the mapping
			mappingID = dataCalls.addMapping(mapping);
		}
		catch(Exception e) {}
		return mappingID;
	}

	/** Delete the specified mapping */
	public Boolean deleteMapping(Integer mappingID)
		{ return dataCalls.deleteMapping(mappingID); }
	
	/** Get the mapping cells for the specified mapping */
	public ArrayList<MappingCell> getMappingCells(Integer mappingID)
		{ return dataCalls.getMappingCells(mappingID); }

	/** Get all mapping cells containing the specific element and above the specified score */
	public ArrayList<MappingCell> getMappingCellsByElement(Integer projectID, List<AssociatedElement> elements, Double minScore)
		{ return dataCalls.getMappingCellsByElement(projectID, elements, minScore); }
	
	/** Get all mapping cells connecting the specified elements in the specified project */
	public ArrayList<MappingCell> getAssociatedMappingCells(Integer projectID, List<AssociatedElement> elements)
		{ return dataCalls.getAssociatedMappingCells(projectID, elements); }
	
	/** Add the specified mapping cell */
	public Integer addMappingCells(List<MappingCell> mappingCells)
		{ return dataCalls.addMappingCells(mappingCells); }

	/** Update the specified mapping cells */
	public Boolean updateMappingCells(List<MappingCell> mappingCells)
		{ return dataCalls.updateMappingCells(mappingCells); }

	/** Delete the specified mapping cells */
	public Boolean deleteMappingCells(List<Integer> mappingCellIDs)
		{ return dataCalls.deleteMappingCells(mappingCellIDs); }

	/** Gets the vocabulary ID for the specified project */
	public Integer getVocabularyID(Integer projectID)
		{ return dataCalls.getVocabularyID(projectID); }

	/** Sets the project vocabulary id in the repository */
	public boolean setVocabularyID(Integer projectID, Integer vocabularyID)
		{ return dataCalls.setVocabularyID(projectID, vocabularyID); }
	
	/** Deletes the project vocabulary ID from the project in the repository */
	public boolean deleteVocabularyID(Integer projectID)
		{ return dataCalls.deleteVocabularyID(projectID); }
}