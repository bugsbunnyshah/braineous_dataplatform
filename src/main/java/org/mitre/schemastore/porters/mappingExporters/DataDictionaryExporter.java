// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.schemastore.porters.mappingExporters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.SchemaElement;

/**
 * Class for exporting a data dictionary from the mapping
 * @author CWOLF
 */
public class DataDictionaryExporter extends MappingExporter
{
	/** Returns the exporter name */
	public String getName()
		{ return "GraphData Dictionary Exporter"; }
	
	/** Returns the exporter description */
	public String getDescription()
		{ return "This exporter is used to export all pairings of terms within the mapping"; }
	
	/** Returns the file types associated with this converter */
	public String getFileType()
		{ return ".csv"; }
	
	/** Generates a data dictionary for this mapping */
	public void exportMapping(Mapping mapping, ArrayList<MappingCell> mappingCells, File file) throws IOException
	{
		// Prepare to export source and target node information
		BufferedWriter out = new BufferedWriter(new FileWriter(file));

		// Generate a list of all schema elements
		HashMap<Integer,SchemaElement> elements = new HashMap<Integer,SchemaElement>();
		for(Integer schemaID : new Integer[]{mapping.getSourceId(),mapping.getTargetId()})
			for(SchemaElement element : client.getSchemaInfo(schemaID).getElements(null))
				elements.put(element.getId(),element);

    	// First, output all user selected node pairings
		HashSet<Integer> usedElements = new HashSet<Integer>();
		for(MappingCell mappingCell : mappingCells)
    		if(mappingCell.getAuthor().equals("User") && mappingCell.getScore()>0)
    		{
    			// Gets the elements associated with the mapping cell
    			SchemaElement inputElement = elements.get(mappingCell.getElementInputIDs()[0]);
    			SchemaElement outputElement = elements.get(mappingCell.getOutput());
    			
    			// Display the pairing of schema elements
    			String inputName = inputElement.getName();
    			String inputDescription = inputElement.getDescription();
    			String outputName = outputElement.getName();
    			String outputDescription = outputElement.getDescription();
    			out.write(inputName+",\""+inputDescription+"\","+outputName+",\""+outputDescription+"\"\n");
    
    			// Mark the elements as used
    			usedElements.add(inputElement.getId());
    			usedElements.add(outputElement.getId());
    		}

    	// Then output all source nodes with no links
		for(SchemaElement element : elements.values())
			if(!usedElements.contains(element.getId()))
  				out.write(element.getName()+",\""+element.getDescription().replace('\n',' ').replaceAll("\"","\"\"")+"\",,\n");
 
    	out.close();
	}
}