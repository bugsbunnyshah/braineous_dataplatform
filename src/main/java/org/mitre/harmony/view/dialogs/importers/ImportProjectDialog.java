// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.harmony.view.dialogs.importers;

import java.net.URI;
import java.util.ArrayList;

import org.mitre.harmony.controllers.ProjectController;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.schemastore.model.Project;
import org.mitre.schemastore.porters.Importer;
import org.mitre.schemastore.porters.PorterType;
import org.mitre.schemastore.porters.projectImporters.ProjectImporter;

/** Class for displaying the project importer dialog */
public class ImportProjectDialog extends AbstractImportDialog
{	
	/** Constructs the importer dialog */
	public ImportProjectDialog(HarmonyModel harmonyModel)
		{ super(harmonyModel); setVisible(true); }
	
	/** Returns the type of importer being run */
	protected PorterType getImporterType() { return PorterType.PROJECT_IMPORTERS; }

	/** Returns the list of used project names */
	protected ArrayList<String> getUsedNames()
	{ 
		ArrayList<String> usedNames = new ArrayList<String>();
		for(Project project : harmonyModel.getProjectManager().getProjects())
			usedNames.add(project.getName());
		return usedNames;
	}
	
	/** Imports the specified schema locally */
	private Integer importItemLocally(Importer importer, String name, String author, String description, URI uri) throws Exception
	{
		ProjectImporter projectImporter = (ProjectImporter)selectionList.getSelectedItem();
		projectImporter.initialize(uri);
		return projectImporter.importProject(name, author, description);
	}
	
	/** Imports the currently specified project */
	protected void importItem(String name, String author, String description, URI uri) throws Exception
	{
		Integer projectID = null;
		
		// Import the project
		Importer importer = (Importer)selectionList.getSelectedItem();
		/*if(harmonyModel.getInstantiationType()==InstantiationType.WEBAPP)
			projectID = SchemaStoreManager.importData(importer, name, author, description, uri);
		else projectID = importItemLocally(importer, name, author, description, uri);*/

		// Load the imported project
		ProjectController.loadProject(harmonyModel,projectID);
		ProjectController.selectMappings(harmonyModel);
	}
}