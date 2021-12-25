// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.schemastore.porters.mappingExporters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

/**
 * Class for exporting a mapping formatted in csv
 * @author cwolf
 */
public class RescueSuppliesExporter extends MappingExporter
{		
	/** Returns the exporter name */
	public String getName()
		{ return "Rescue Supplies Exporter"; }
	
	/** Returns the exporter description */
	public String getDescription()
		{ return "This exporter generates a csv file which outputs the rescue supplies list tagged with UN commerce IDs"; }
	
	/** Returns the file types associated with this converter */
	public String getFileType()
		{ return ".csv"; }
	
	/** Generates a data dictionary for this project */
	public void exportMapping(Mapping mapping, ArrayList<MappingCell> mappingCells, File file) throws IOException
	{
		// Get the mapped schemas
		HierarchicalSchemaInfo sourceInfo = new HierarchicalSchemaInfo(client.getSchemaInfo(mapping.getSourceId()));
		HierarchicalSchemaInfo targetInfo = new HierarchicalSchemaInfo(client.getSchemaInfo(mapping.getTargetId()));

		// Don't proceed if neither schema is the taxonomy
		boolean sourceIsTaxonomy = sourceInfo.getSchema().getName().startsWith("UNSPSC");
		boolean targetIsTaxonomy = targetInfo.getSchema().getName().startsWith("UNSPSC");
		if(!sourceIsTaxonomy && !targetIsTaxonomy) throw new IOException("One of the mapped schemas must the UNSPSC taxonomy");
		
		// Export the mapping
		PrintWriter out = new PrintWriter(new FileWriter(file));
		out.println("Supplies,Amount,UNSPSC ID");
		for(MappingCell mappingCell : mappingCells)
			if(mappingCell.isValidated())
			{
				// Get the source and target elements
				SchemaElement sourceElement = sourceInfo.getElement(mappingCell.getElementInputIDs()[0]);
				SchemaElement targetElement = targetInfo.getElement(mappingCell.getOutput());

				// Identify the labels
				String item = sourceIsTaxonomy ? targetElement.getName() : sourceElement.getName();
				String amount = sourceIsTaxonomy ? targetElement.getDescription() : sourceElement.getDescription();
				String id = sourceIsTaxonomy ? sourceElement.getDescription() : targetElement.getDescription();
	
				// Generate the taxonomy
				String taxonomy = "";
				ArrayList<SchemaElement> path = sourceIsTaxonomy ? sourceInfo.getPaths(sourceElement.getId()).get(0) : targetInfo.getPaths(targetElement.getId()).get(0);
				for(SchemaElement element : path)
					taxonomy += element.getName() + " -> ";
				if(taxonomy.length()>4) taxonomy = taxonomy.substring(0,taxonomy.length()-4);

				// Output the spreadsheet row
				out.println("\"" + scrub(item) + "\",\"" + scrub(amount) + "\",\"" + scrub(id) + " (" + scrub(taxonomy) + ")\"");
			}
    	out.close();
	}

	/** Scrubs the label to eliminate quotes */
	private String scrub(String value)
		{ return value.replaceAll("\"", "\"\""); }
}