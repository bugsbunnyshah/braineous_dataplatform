// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.porters.mappingImporters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.mitre.schemastore.model.Function;
import org.mitre.schemastore.model.FunctionImp;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.ProjectSchema;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.ImporterException.ImporterExceptionType;
import org.mitre.schemastore.porters.URIType;
import org.mitre.schemastore.porters.xml.ConvertFromXML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Importer for copying mappings from other repositories */
public class M3MappingImporter extends MappingImporter
{	
	/** Stores the M3 document to be imported */
	private Element element;
	
	/** Returns the importer name */
	public String getName()
		{ return "M3 Mapping Importer"; }
	
	/** Returns the importer description */
	public String getDescription()
		{ return "This importer can be used to download a mapping in the M3 format"; }
		
	/** Returns the importer URI type */
	public URIType getURIType()
		{ return URIType.FILE; }
	
	/** Returns the importer URI file types */
	public ArrayList<String> getFileTypes()
	{
		ArrayList<String> fileTypes = new ArrayList<String>();
		fileTypes.add(".m3m");
		return fileTypes;
	}

	/** Initializes the importer for the specified Document */
	final public void initialize(Document document) throws ImporterException
		{ element = document.getDocumentElement(); }
	
	/** Initialize the importer */
	protected void initialize() throws ImporterException
	{
		try
		{
			// Uncompresses the file
			ZipInputStream zipIn = new ZipInputStream(new FileInputStream(new File(uri)));
			File tempFile = File.createTempFile("M3M", ".xml");
			FileOutputStream out = new FileOutputStream(tempFile);
			zipIn.getNextEntry();
			byte data[] = new byte[100000]; int count;
			while((count = zipIn.read(data)) > 0)
				out.write(data, 0, count);
			zipIn.close();
			out.close();
			
			// Retrieve the XML document element
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(tempFile);
			element = document.getDocumentElement();
			tempFile.delete();
		}
		catch(Exception e)
			{ throw new ImporterException(ImporterExceptionType.IMPORT_FAILURE, e.getMessage()); }
	}

	/** Returns the source schema in the mapping */
	public ProjectSchema getSourceSchema() throws ImporterException
	{
		try { return ConvertFromXML.getSourceSchema(element); }
		catch(Exception e) { throw new ImporterException(ImporterExceptionType.IMPORT_FAILURE, e.getMessage()); }
	}

	/** Returns the target schema in the mapping */
	public ProjectSchema getTargetSchema() throws ImporterException
	{
		try { return ConvertFromXML.getTargetSchema(element); }
		catch(Exception e) { throw new ImporterException(ImporterExceptionType.IMPORT_FAILURE, e.getMessage()); }
	}

	/** Retrieves the elements with the specified label */
	private ArrayList<Element> getElements(String label)
	{
		ArrayList<Element> elements = new ArrayList<Element>();
		NodeList elementList = element.getElementsByTagName(label);
		if (elementList != null)
		{
			for (int i = 0; i < elementList.getLength(); i++)
			{
				Node node = elementList.item(i);
				if (node instanceof Element)
					elements.add((Element)node);
			}
		}
		return elements;
	}
	
	/** Returns the imported functions */
	public HashMap<Function,ArrayList<FunctionImp>> getFunctions() throws ImporterException
	{
		try 
		{
			HashMap<Function,ArrayList<FunctionImp>> functions = new HashMap<Function,ArrayList<FunctionImp>>();
			for (Element element : getElements("Function"))
			{
				Function function = ConvertFromXML.getFunction(element);
				ArrayList<FunctionImp> functionImps = ConvertFromXML.getFunctionImps(element);
				functions.put(function, functionImps);
			}
			return functions;
		}
		catch(Exception e) { throw new ImporterException(ImporterExceptionType.IMPORT_FAILURE, e.getMessage()); }
	}
	
	/** Returns the imported mapping cells */
	public ArrayList<MappingCell> getMappingCells() throws ImporterException
	{
		// Clear out the unidentified mapping cells
		unidentifiedMappingCellPaths.clear();
		
		try
		{
			// Get the source and target info
			HierarchicalSchemaInfo sourceInfo = new HierarchicalSchemaInfo(client.getSchemaInfo(source.getId()), getSourceSchema().geetSchemaModel());
			HierarchicalSchemaInfo targetInfo = new HierarchicalSchemaInfo(client.getSchemaInfo(target.getId()), getTargetSchema().geetSchemaModel());
			
			// Generate the list of mapping cells
			ArrayList<MappingCell> mappingCells = new ArrayList<MappingCell>();
			for (Element element : getElements("MappingCell"))
			{
				// Gets the imported mapping cell
				MappingCell mappingCell = ConvertFromXML.getMappingCell(element, sourceInfo, targetInfo);
				if (mappingCell==null)
					unidentifiedMappingCellPaths.add(ConvertFromXML.getMappingCellPaths(element));
				else mappingCells.add(mappingCell);
			}
			return mappingCells;
		}
		catch(Exception e) { throw new ImporterException(ImporterExceptionType.IMPORT_FAILURE, e.getMessage()); }
	}
}