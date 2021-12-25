package org.mitre.harmony.model;

import org.mitre.harmony.model.filters.FilterManager;
import org.mitre.harmony.model.preferences.PreferencesManager;
import org.mitre.harmony.model.project.MappingManager;
import org.mitre.harmony.model.project.ProjectManager;
import org.mitre.harmony.model.search.HarmonySearchManager;
import org.mitre.harmony.model.selectedInfo.SelectedInfoManager;

/** Class for monitoring for changes in the project */
public class HarmonyModel
{
	/** Stores Harmony instantiation types */
	static public enum InstantiationType {STANDALONE,WEBAPP,EMBEDDED};

	// Stores the managers associated with the currently displayed mapping
	protected SchemaManager schemaManager = new SchemaManager(this);
	protected ProjectManager projectManager = new ProjectManager(this);
	protected MappingManager mappingManager = new MappingManager(this);
	
	// Stores the various managers associated with the model
	protected FilterManager filterManager = new FilterManager(this);
	protected PreferencesManager preferencesManager = new PreferencesManager(this);
	protected SelectedInfoManager selectedInfoManager = new SelectedInfoManager(this);
	protected HarmonySearchManager searchManager = new HarmonySearchManager(this);

	/** Constructs the Harmony model */
	public HarmonyModel()
	{
		// Add listeners to the various model objects
		filterManager.addListener(selectedInfoManager);
		mappingManager.addListener(projectManager);
		mappingManager.addListener(filterManager);
		mappingManager.addListener(searchManager);
		mappingManager.addListener(selectedInfoManager);
		projectManager.addListener(preferencesManager);
	}

	/** Returns the filters */
	public FilterManager getFilters()
		{ return filterManager; }

	/** Returns the preferences */
	public PreferencesManager getPreferences()
		{ return preferencesManager; }
	
	/** Returns the selected info manager */
	public SelectedInfoManager getSelectedInfo()
		{ return selectedInfoManager; }

	/** Returns the search manager */
	public HarmonySearchManager getSearchManager()
		{ return searchManager; }
	
	/** Returns the schema manager */
	public SchemaManager getSchemaManager()
		{ return schemaManager; }

	/** Returns the project manager */
	public ProjectManager getProjectManager()
		{ return projectManager; }

	/** Returns the mapping manager */
	public MappingManager getMappingManager()
		{ return mappingManager; }
}