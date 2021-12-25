// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.schemastore.porters.projectExporters;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.Project;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.porters.Exporter;

/** Export Class - An exporter enables the exporting of projects */
public abstract class ProjectExporter extends Exporter
{
	/** Exports the project to the specified file */
	abstract public void exportProject(Project project, HashMap<Mapping,ArrayList<MappingCell>> mappings, File file) throws IOException;
	
	/** Generates a hash map of all schema elements contained within the project */
	protected HashMap<Integer,SchemaElement> getSchemaElements(List<Integer> schemaIDs) throws RemoteException
	{
		HashMap<Integer,SchemaElement> elements = new HashMap<Integer,SchemaElement>();
		for(Integer schemaID : schemaIDs)
			for(SchemaElement element : client.getSchemaInfo(schemaID).getElements(null))
				elements.put(element.getId(),element);
		return elements;
	}
	
	/** Returns the list of mapping cells which contain the specified element */
	protected ArrayList<MappingCell> getMappingCellsByElement(Integer elementID, ArrayList<MappingCell> mappingCells)
	{
		ArrayList<MappingCell> mappingCellsWithElement = new ArrayList<MappingCell>();
		for(MappingCell mappingCell : mappingCells)
			if(Arrays.asList(mappingCell.getElementInputIDs()).contains(elementID) || mappingCell.getOutput().equals(elementID))
				mappingCellsWithElement.add(mappingCell);
		return mappingCellsWithElement;
	}
}