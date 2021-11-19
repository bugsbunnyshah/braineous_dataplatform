package org.mitre.harmony.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.SchemaStoreManager;
import org.mitre.harmony.model.project.MappingManager;
import org.mitre.harmony.model.project.ProjectManager;
import org.mitre.harmony.model.project.ProjectMapping;
import org.mitre.harmony.view.dialogs.project.ProjectDialog;
import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.Project;
import org.mitre.schemastore.model.ProjectSchema;
import org.mitre.schemastore.model.mappingInfo.MappingInfo;

/** Class for various project controls */
public class ProjectController
{
	/** Creates a new project */
	static public void newProject(HarmonyModel harmonyModel)
	{
		// Clear out all old project settings
		harmonyModel.getPreferences().unmarkAllFinished();
		harmonyModel.getMappingManager().removeAllMappings();

		// Set up a new project
		Project project = new Project();
		//project.setAuthor(harmonyModel.getUserName());
		harmonyModel.getProjectManager().setProject(project);
		harmonyModel.getProjectManager().setModified(false);
	}
	
	/** Loads the specified project */
	static public boolean loadProject(HarmonyModel harmonyModel, Integer projectID)
	{
		// Retrieve project information from repository
		Project project = null;
		ArrayList<MappingInfo> mappings = new ArrayList<MappingInfo>();
		try {
			project = SchemaStoreManager.getProject(projectID);
			for(Mapping mapping : SchemaStoreManager.getMappings(projectID))
			{
				ArrayList<MappingCell> mappingCells = SchemaStoreManager.getMappingCells(mapping.getId());
				for(MappingCell mappingCell : mappingCells)
					mappingCell.setId(null);
				mappings.add(new MappingInfo(mapping, mappingCells));
			}
		}
		catch(Exception e) { System.out.println("(E) ProjectController:loadProject - " + e.getMessage()); return false; }
		
		// Clear out all old project settings
		harmonyModel.getPreferences().unmarkAllFinished();
		harmonyModel.getMappingManager().removeAllMappings();
			
		// Sets the project information
		harmonyModel.getProjectManager().setProject(project);

		// Sets the mapping information
		MappingManager mappingManager = harmonyModel.getMappingManager();
		for(MappingInfo mapping : mappings)
		{
			ProjectMapping projectMapping = mappingManager.addMapping(mapping.getMapping());
			projectMapping.setMappingCells(mapping.getMappingCells().get());
		}
		
		// Indicates that the project was successfully loaded
		harmonyModel.getProjectManager().setModified(false);
		return true;
	}
	
	/** Loads the specified mapping */
	static public boolean loadMapping(HarmonyModel harmonyModel, MappingInfo mappingInfo)
	{
		try {
			// Create an array of the project schemas
			ArrayList<ProjectSchema> schemas = new ArrayList<ProjectSchema>();
			for(Integer schemaID : new Integer[]{mappingInfo.getSourceID(),mappingInfo.getTargetID()})
				schemas.add(new ProjectSchema(schemaID, SchemaStoreManager.getSchema(schemaID).getName(), null));
			
			// Generate a new project to hold the mapping
			newProject(harmonyModel);		
			harmonyModel.getProjectManager().setSchemas(schemas);
			
			// Sets the mapping information
			ProjectMapping projectMapping = harmonyModel.getMappingManager().addMapping(mappingInfo.getMapping());
			projectMapping.setMappingCells(mappingInfo.getMappingCells().get());
			
			// Indicates that the project was successfully loaded
			harmonyModel.getProjectManager().setModified(false);
			return true;
		}
		catch(Exception e) { System.out.println("(E) ProjectController:loadProject - " + e.getMessage()); return false; }
	}
	
	/** Automatically selects the mappings to display */
	static public void selectMappings(HarmonyModel harmonyModel)
	{
		// Checks to see if all mappings can be displayed simultaneously
		boolean displayAll = true;
		HashSet<Integer> sourceIDs = new HashSet<Integer>();
		HashSet<Integer> targetIDs = new HashSet<Integer>();
		for(ProjectMapping mapping : harmonyModel.getMappingManager().getMappings())
		{
			sourceIDs.add(mapping.getSourceId());
			targetIDs.add(mapping.getTargetId());
			if(sourceIDs.contains(mapping.getTargetId()) || targetIDs.contains(mapping.getSourceId()))
				{ displayAll = false; break; }
		}
		
		// Display all mappings if possible
		if(displayAll)
			for(ProjectMapping mapping : harmonyModel.getMappingManager().getMappings())
				mapping.setVisibility(true);
		else new ProjectDialog(harmonyModel);
	}
	
	/** Saves the specified project */
	static public boolean saveProject(HarmonyModel harmonyModel, Project project)
	{
		try {
			// Set the project schemas
			project.setSchemas(harmonyModel.getProjectManager().getProject().getSchemas());
			
			// Save the project
			if(project.getId()!=null)
			{
				// Collect keys for the current project mappings
				HashSet<String> mappingKeys = new HashSet<String>();
				for(ProjectMapping mapping : harmonyModel.getMappingManager().getMappings())
					mappingKeys.add(mapping.getSourceId() + "_" + mapping.getTargetId());
				
				// Remove mappings which no longer can be supported
				for(Mapping mapping : SchemaStoreManager.getMappings(project.getId()))
					if(!mappingKeys.contains(mapping.getSourceId() + "_" + mapping.getTargetId()))
						if(!SchemaStoreManager.deleteMapping(mapping.getId()))
							throw new Exception("Failed to delete mapping " + mapping.getId());
				
				// Update the project information
				if(!SchemaStoreManager.updateProject(project))
					throw new Exception("Failed to update project");
				harmonyModel.getProjectManager().setProject(project);
			}
			else
			{
				Integer projectID = SchemaStoreManager.addProject(project);
				if(projectID!=null) project.setId(projectID);
				else throw new Exception("Failed to create project");
			}

			// Identify the current project mappings
			HashMap<String,Integer> mappingIDs = new HashMap<String,Integer>();
			for(Mapping mapping : SchemaStoreManager.getMappings(project.getId()))
				mappingIDs.put(mapping.getSourceId() + "_" + mapping.getTargetId(),mapping.getId());
			
			// Save the project mappings
			for(ProjectMapping mapping : harmonyModel.getMappingManager().getMappings())
			{
				// Identify the mapping where to save the 
				Integer mappingID = mappingIDs.get(mapping.getSourceId() + "_" + mapping.getTargetId());
				if(mappingID==null)
				{
					Mapping newMapping = mapping.copy();
					newMapping.setProjectId(project.getId());
					mapping.setId(SchemaStoreManager.addMapping(newMapping));
				}
				else mapping.setId(mappingID);
				if(mapping.getId()==null) throw new Exception("Failed to create mapping");

				// Save the mapping cells to the mapping
				SchemaStoreManager.saveMappingCells(mapping.getId(), mapping.getMappingCells());
			}
			
			// Updates the mapping has reflect the changes
			ProjectManager projectManager = harmonyModel.getProjectManager();
			projectManager.setProjectInfo(project.getId(), project.getName(), project.getAuthor(), project.getDescription());
			projectManager.setModified(false);
			return true;
		}
		catch(Exception e) { System.out.println("(E) ProjectController.saveProject - " + e.getMessage()); return false; }
	}
}