// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.model.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.mitre.harmony.model.AbstractManager;
import org.mitre.harmony.model.HarmonyConsts;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.SchemaStoreManager;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.Project;
import org.mitre.schemastore.model.ProjectSchema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.model.SchemaModel;

/**
 * Class used to manage the current project
 * @author CWOLF
 */
public class ProjectManager extends AbstractManager<ProjectListener> implements MappingListener
{	
	/** Stores the current project */
	private Project project = new Project();	
	
	/** Indicates if the project has been modified */
	private boolean modified = false;
	
	/** Constructs the project manager */
	public ProjectManager(HarmonyModel harmonyModel)
		{ super(harmonyModel); }
	
	/** Returns a list of all projects */
	public ArrayList<Project> getProjects()
	{
		ArrayList<Project> projects = new ArrayList<Project>();
		try {
			projects = SchemaStoreManager.getProjects();
		} catch(Exception e) {
			System.err.println("Error getting projects from SchemaStoreConnection");
			e.printStackTrace();
		}
		return projects;
	}
	
	/** Indicates if the project has been modified */
	public boolean isModified()
		{ return modified; }
	
	/** Sets the project as modified */
	public void setModified(boolean modified)
	{
		this.modified = modified;
		for(ProjectListener listener : getListeners())
			listener.projectModified();
	}
	
	/** Gets the project info */
	public Project getProject()
		{ return project.copy(); }

	/** Sets the project info */
	public void setProject(Project project)
	{
		// Set the schema information
		setSchemas(new ArrayList<ProjectSchema>());
		if(project.getSchemas()!=null)
			setSchemas(new ArrayList<ProjectSchema>(Arrays.asList(project.getSchemas())));

		// Set the project information
		this.project = project;
		setModified(true);
	}
	
	/** Gets the project schemas */
	public ArrayList<ProjectSchema> getSchemas()
	{
		if(project.getSchemas()==null) return new ArrayList<ProjectSchema>();
		return new ArrayList<ProjectSchema>(Arrays.asList(project.getSchemas()));
	}
	
	/** Gets a specific project schema */
	public ProjectSchema getSchema(Integer schemaID)
	{
		for (ProjectSchema schema : getSchemas())
			if (schema.getId() == schemaID) return schema;
		return null;
	}
	
	/** Gets the project schema IDs */
	public ArrayList<Integer> getSchemaIDs()
		{ return new ArrayList<Integer>(Arrays.asList(project.getSchemaIDs())); }

	/** Returns the schemas displayed on the specified side of the mapping */
	public HashSet<Integer> getSchemaIDs(Integer side)
	{
		HashSet<Integer> schemaIDs = new HashSet<Integer>();
		for(ProjectMapping mapping : getModel().getMappingManager().getMappings())
			if(mapping.isVisible())
			{
				if(side.equals(HarmonyConsts.LEFT)) schemaIDs.add(mapping.getSourceId());
				else schemaIDs.add(mapping.getTargetId());
			}
		return schemaIDs;
	}
	
	/** Returns all elements displayed on the specified side of the mapping */
	public HashSet<SchemaElement> getSchemaElements(Integer side)
	{
		HashSet<SchemaElement> elements = new HashSet<SchemaElement>();
		for(Integer schemaID : getSchemaIDs(side))
			elements.addAll(getModel().getSchemaManager().getSchemaInfo(schemaID).getHierarchicalElements());
		return elements;
	}

	/** Returns all element IDs displayed on the specified side of the mapping */
	public HashSet<Integer> getSchemaElementIDs(Integer side)
	{
		HashSet<Integer> elementIDs = new HashSet<Integer>();
		for(SchemaElement element : getSchemaElements(side))
			elementIDs.add(element.getId());
		return elementIDs;
	}

	/** Returns the schema model for the specified schema */
	public SchemaModel getSchemaModel(Integer schemaID)
	{
		for(ProjectSchema schema : getSchemas())
			if(schema.getId().equals(schemaID)) return schema.geetSchemaModel();
		return null;
	}
	
	/** Sets the project info */
	public void setProjectInfo(Integer id, String name, String author, String description)
	{
		// Only save information if changes made
		if(!id.equals(project.getId()) || !name.equals(project.getName()) || !author.equals(project.getAuthor()) || !description.equals(project.getDescription()))
		{
			// Sets the mapping
			project.setId(id);
			project.setName(name);
			project.setAuthor(author);
			project.setDescription(description);
			
			// Indicates that the project has been modified
			for(ProjectListener listener : getListeners())
				listener.projectModified();
		}
	}

	/** Sets the project schemas */
	public void setSchemas(ArrayList<ProjectSchema> schemas)
	{		
		boolean changesOccured = false;
		
		// Create hash table for the old schema
		HashMap<Integer, ProjectSchema> oldSchemas = new HashMap<Integer, ProjectSchema>();
		for(ProjectSchema schema : getSchemas())
			oldSchemas.put(schema.getId(), schema);

		// Create hash table for the new schema
		HashMap<Integer, ProjectSchema> newSchemas = new HashMap<Integer, ProjectSchema>();
		for(ProjectSchema schema : schemas)
			newSchemas.put(schema.getId(), schema);
		
		// Set the project schemas
		project.setSchemas(schemas.toArray(new ProjectSchema[0]));
		
		// Inform listeners of any schemas that were added
		for(Integer newSchemaID : newSchemas.keySet())
			if(!oldSchemas.containsKey(newSchemaID))
			{
				for(ProjectListener listener : getListeners())
					listener.schemaAdded(newSchemaID);
				changesOccured = true;
			}
				
		// Inform listeners of any schemas that were removed
		for(Integer oldSchemaID : oldSchemas.keySet())
			if(!newSchemas.containsKey(oldSchemaID))
			{
				for(ProjectListener listener : getListeners())
					listener.schemaRemoved(oldSchemaID);
				changesOccured = true;
			}
				
		// Inform listeners of any schemas that were modified
		for(Integer newSchemaID : newSchemas.keySet())
			if(oldSchemas.containsKey(newSchemaID))
			{
				ProjectSchema oldSchema = oldSchemas.get(newSchemaID);
				ProjectSchema newSchema = newSchemas.get(newSchemaID);
	
				// Determines if the schema model was modified
				boolean schemaModelModified = oldSchema.getModel()==null && newSchema.getModel()!=null;
				schemaModelModified |= oldSchema.getModel()!=null && !oldSchema.getModel().equals(newSchema.getModel());

				// If the schema model was modified, update schema and inform listeners
				if(schemaModelModified)
				{
					// Update schema model
					SchemaModel schemaModel = getSchemaModel(newSchemaID);
					getModel().getSchemaManager().getSchemaInfo(newSchemaID).setModel(schemaModel);

					// Inform schema listeners
					for(ProjectListener listener : getListeners())
						listener.schemaModelModified(newSchemaID);
					changesOccured = true;
				}
			}
		
		// Set the mapping as being modified
		if(changesOccured) setModified(true);
	}
	
	/** Sets the specified schema's schema model */
	public void setSchemaModel(Integer schemaID, SchemaModel schemaModel)
	{
		for(ProjectSchema schema : getSchemas())
			if(schema.getId().equals(schemaID))
			{
				schema.seetSchemaModel(schemaModel);
				getModel().getSchemaManager().getSchemaInfo(schemaID).setModel(schemaModel);
				for(ProjectListener listener : getListeners())
					listener.schemaModelModified(schemaID);		
				return;
			}
	}
	
	/** Deletes the specified project */
	public boolean deleteProject(Integer projectID)
	{
		// Delete the project
		try {
			SchemaStoreManager.deleteProject(projectID);
			return true;
		}
		catch(Exception e) { System.out.println("(E) MappingManager:deleteProject - " + e.getMessage()); }
		return false;
	}

	// Mark the project as modified whenever the mappings change
	public void mappingAdded(Integer mappingID) { setModified(true); }
	public void mappingRemoved(Integer mappingID) { setModified(true); }
	public void mappingVisibilityChanged(Integer mappingID) {}
	public void mappingCellsAdded(Integer mappingID, List<MappingCell> mappingCells) { setModified(true); }
	public void mappingCellsModified(Integer mappingID,	List<MappingCell> oldMappingCells, List<MappingCell> newMappingCells) { setModified(true); }
	public void mappingCellsRemoved(Integer mappingID, List<MappingCell> mappingCells) { setModified(true); }
}
