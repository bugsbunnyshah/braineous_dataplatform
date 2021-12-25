// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs.exporters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.SchemaStoreManager;
import org.mitre.harmony.model.project.ProjectMapping;
import org.mitre.schemastore.porters.Exporter;
import org.mitre.schemastore.porters.PorterType;
import org.mitre.schemastore.porters.mappingExporters.MappingExporter;

/**
 * Dialog for exporting the selected mapping
 * @author CWOLF
 */
public class ExportMappingDialog extends AbstractExportDialog
{
	/** Stores the mapping being exported */
	private ProjectMapping mapping;
	
	/** Constructs the Mapping Export dialog */
	public ExportMappingDialog(ProjectMapping mapping)
		{ this.mapping = mapping; }
	
	/** Declares the export type */
	protected PorterType getExporterType() { return PorterType.MAPPING_EXPORTERS; }
	
	/** Handles the export to the specified file */
	protected void export(HarmonyModel harmonyModel, Exporter exporter, File file) throws IOException
		{ ((MappingExporter)exporter).exportMapping(mapping, mapping.getMappingCells(), file); }

	/** Handles the export through a web service */
	protected String exportViaWebService(HarmonyModel harmonyModel, Exporter exporter)
	{
		ArrayList<Object> data = new ArrayList<Object>();
		data.add(mapping.copy()); data.add(mapping.getMappingCells());
		return SchemaStoreManager.exportData(exporter, data);
	}
}

