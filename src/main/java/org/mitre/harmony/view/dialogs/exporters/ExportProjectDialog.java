// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs.exporters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.SchemaStoreManager;
import org.mitre.harmony.model.project.ProjectMapping;
import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.Project;
import org.mitre.schemastore.porters.Exporter;
import org.mitre.schemastore.porters.PorterType;
import org.mitre.schemastore.porters.projectExporters.ProjectExporter;

/**
 * Dialog for exporting the current project
 * @author CWOLF
 */
public class ExportProjectDialog extends AbstractExportDialog
{
	/** Declares the export type */
	protected PorterType getExporterType() { return PorterType.PROJECT_EXPORTERS; }
	
	/** Retrieves the project */
	private Project getProject(HarmonyModel harmonyModel)
		{ return harmonyModel.getProjectManager().getProject(); }

	
	/** Retrieves the mappings */
	private HashMap<Mapping,ArrayList<MappingCell>> getMappings(HarmonyModel harmonyModel)
	{
		HashMap<Mapping,ArrayList<MappingCell>> mappings = new HashMap<Mapping,ArrayList<MappingCell>>();
		for(ProjectMapping mapping : harmonyModel.getMappingManager().getMappings())
			mappings.put(mapping, mapping.getMappingCells());	
		return mappings;
	}
	
	/** Handles the export to the specified file */
	protected void export(HarmonyModel harmonyModel, Exporter exporter, File file) throws IOException
		{ ((ProjectExporter)exporter).exportProject(getProject(harmonyModel), getMappings(harmonyModel), file); }
	
	/** Handles the export through a web service */
	protected String exportViaWebService(HarmonyModel harmonyModel, Exporter exporter)
	{		
		// Retrieve the mappings
		HashMap<Mapping,ArrayList<MappingCell>> completeMappings = getMappings(harmonyModel);
		ArrayList<Mapping> mappings = new ArrayList<Mapping>();
		ArrayList<ArrayList<MappingCell>> mappingCells = new ArrayList<ArrayList<MappingCell>>();
		for(Mapping mapping : completeMappings.keySet())
		{
			mappings.add(mapping.copy());
			mappingCells.add(completeMappings.get(mapping));
		}
		
		// Export the data
		ArrayList<Object> data = new ArrayList<Object>();	
		data.add(getProject(harmonyModel)); data.add(mappings); data.add(mappingCells);
		return SchemaStoreManager.exportData(exporter, data);
	}
}