// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.porters.schemaImporters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.SchemaInfo;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.ImporterException.ImporterExceptionType;
import org.mitre.schemastore.porters.URIType;
import org.mitre.schemastore.porters.xml.ConvertFromXML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Importer for copying schemas from other repositories */
public class M3SchemaImporter extends SchemaImporter
{
	/** Private class for sorting schema elements */
	class SchemaElementComparator implements Comparator<SchemaElement>
	{
		public int compare(SchemaElement element1, SchemaElement element2)
		{
			// Retrieve the base schemas for the specified elements
			Integer base1 = element1.getBase(); if(base1==null) base1=-1;
			Integer base2 = element2.getBase(); if(base2==null) base2=-1;
			
			// Returns a comparator value for the compared elements
			if(!base1.equals(base2))
				return base1.compareTo(base2);
			if(element1.getClass()!=element2.getClass())
				return element1.getClass().toString().compareTo(element2.getClass().toString());
			return element1.getId().compareTo(element2.getId());
		}
	}
	
	/** Private class for sorting schema info */
	private class SchemaInfoComparator implements Comparator<SchemaInfo>
	{
		public int compare(SchemaInfo schemaInfo1, SchemaInfo schemaInfo2)
			{ return schemaInfo1.getSchema().getId().compareTo(schemaInfo2.getSchema().getId()); }
	}
	
	/** Stores the list of extended schemas */
	private ArrayList<Integer> extendedSchemaIDs = new ArrayList<Integer>();

	/** Stores the list of schema elements */
	private ArrayList<SchemaElement> schemaElements = null;
	
	/** Returns the importer name */
	public String getName()
		{ return "M3 Importer"; }
	
	/** Returns the importer description */
	public String getDescription()
		{ return "This importer can be used to download a schema in the M3 format"; }
		
	/** Returns the importer URI type */
	public URIType getURIType()
		{ return URIType.M3MODEL; }

	/** Returns the importer URI file types */
	public ArrayList<String> getFileTypes()
	{
		ArrayList<String> fileTypes = new ArrayList<String>();
		fileTypes.add(".m3s");
		return fileTypes;
	}
	
	/** Returns the schema from the specified URI */
	public Schema getSchema(URI uri) throws ImporterException
	{
		Schema schema = null;
		try {
			NodeList schemaList = getXMLNodeList(uri);
			for(int i=0 ; i<schemaList.getLength(); i++)
			{
				Element schemaXMLElement = 	(Element)schemaList.item(i);
				Schema currSchema = ConvertFromXML.getSchema(schemaXMLElement);
				if(schema==null || currSchema.getId()>schema.getId()) schema=currSchema;
			}
		} catch(Exception e) { throw new ImporterException(ImporterExceptionType.PARSE_FAILURE,e.getMessage()); }
		return schema;
	}

	/** Initialize the importer */
	protected void initialize() throws ImporterException
	{	
		try {
			// Extract out schema info from the XML document
			ArrayList<SchemaInfo> schemaInfoList = parseDocument();

			// Create ID translation table between file and repository
			HashMap<Integer,Integer> translationTable = new HashMap<Integer,Integer>();
			
			// Transfer all schemas from which this schema is extended
			ArrayList<Schema> availableSchemas = client.getSchemas();
			for(SchemaInfo schemaInfo : schemaInfoList.subList(0, schemaInfoList.size()-1))
			{
				// Get the schema and schema ID
				Schema schema = schemaInfo.getSchema();
				Integer schemaID = schema.getId();

				// Find if schema already exists in repository
				Integer newSchemaID = getMatchedSchema(availableSchemas, schemaInfo);
				
				// Translate all IDs in the schema
				for(Integer key : translationTable.keySet())
					schemaInfo.updateElementID(key, translationTable.get(key));
				
				// Create extended schema if does not yet exist
				if(newSchemaID==null)
					newSchemaID = importParentSchema(schemaInfo);
				if(newSchemaID==null) throw new Exception("Failure to import schema " + schemaInfo.getSchema().getName());
				
				// Update references to the schema ID
				for(SchemaInfo currSchemaInfo : schemaInfoList)
				{
					ArrayList<Integer> parentSchemaIDs = currSchemaInfo.getParentSchemaIDs();
					if(parentSchemaIDs.contains(schemaID))
						{ parentSchemaIDs.remove(schemaID); parentSchemaIDs.add(newSchemaID); }
				}
				
				// Collect and sort the elements from the two repositories
				ArrayList<SchemaElement> origElements = schemaInfo.getBaseElements(null);
				ArrayList<SchemaElement> newElements = client.getSchemaInfo(newSchemaID).getBaseElements(null);				
				Collections.sort(origElements,new SchemaElementComparator());
				Collections.sort(newElements,new SchemaElementComparator());

				// Add items to the translation table
				for(int i=0; i<origElements.size(); i++)
				{
					Integer origID = origElements.get(i).getId();
					Integer newID = newElements.get(i).getId();
					translationTable.put(origID, newID);
				}
			}
			
			// Get the schema being imported
			SchemaInfo schemaInfo = schemaInfoList.get(schemaInfoList.size()-1);

			// Throw an exception if schema already exists in repository
			Integer schemaID = getMatchedSchema(availableSchemas, schemaInfo);
			if(schemaID!=null)
				throw new ImporterException(ImporterExceptionType.DUPLICATION_FAILURE,"Schema already exists in repository ("+schemaID+")");
			
			// Store the information for the schema being imported
			for(Integer key : translationTable.keySet())
				schemaInfo.updateElementID(key, translationTable.get(key));
			extendedSchemaIDs = schemaInfo.getParentSchemaIDs();
			schemaElements = schemaInfo.getBaseElements(null);
		}
		catch(ImporterException e) { throw e; }
		catch(Exception e) { throw new ImporterException(ImporterExceptionType.PARSE_FAILURE,e.getMessage()); }
	}
	
	/** Returns the list of schemas which this schema extends */
	protected ArrayList<Integer> generateExtendedSchemaIDs() throws ImporterException
		{ return extendedSchemaIDs; }
	
	/** Returns the schema elements from the specified URI */
	protected ArrayList<SchemaElement> generateSchemaElements() throws ImporterException 
		{ return schemaElements; }

	/** Connects to the XML document */
	private NodeList getXMLNodeList(URI uri) throws Exception
	{
		// Uncompresses the file
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(new File(uri)));
		File tempFile = File.createTempFile("M3S", ".xml");
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
		Element element = document.getDocumentElement();
		tempFile.delete();
		
		// Returns the XML node list		
		return element.getElementsByTagName("Schema");
	}
	
	/** Parse out schema from XML document */
	private ArrayList<SchemaInfo> parseDocument() throws Exception
	{	
		// Create a map to temporarily hold schema elements until after the entire file is parsed
		HashMap<Integer,ArrayList<SchemaElement>> elementMap = new HashMap<Integer,ArrayList<SchemaElement>>();
		
		// Gather up all schema information from the XML document
		ArrayList<SchemaInfo> schemaInfoList = new ArrayList<SchemaInfo>();
		NodeList schemaList = getXMLNodeList(uri);
		if(schemaList!=null)
			for(int i=0 ; i<schemaList.getLength(); i++)
			{	
				// Get the schema element
				Element schemaXMLElement = (Element)schemaList.item(i);
				
				// Extract schema and parent schema information from the element
				Schema schema = ConvertFromXML.getSchema(schemaXMLElement);
				ArrayList<Integer> parentIDs = ConvertFromXML.getParentSchemaIDs(schemaXMLElement);
				
				// Extract schema element information from the element
				ArrayList<SchemaElement> schemaElements = new ArrayList<SchemaElement>();
				NodeList elementNodeList = schemaXMLElement.getChildNodes();
				if(elementNodeList != null)
					for(int j=0; j<elementNodeList.getLength(); j++)
					{
						Node node = elementNodeList.item(j);
						if(node instanceof Element)
						{
							SchemaElement schemaElement = ConvertFromXML.getSchemaElement((Element)node);
							if(schemaElement!=null) schemaElements.add(schemaElement);
						}
					}
						
				// Add the schema info to the list of schema info
				elementMap.put(schema.getId(), schemaElements);
				schemaInfoList.add(new SchemaInfo(schema, parentIDs, new ArrayList<SchemaElement>()));
			}

		// Add schema elements to the generated schema info
		for(SchemaInfo schemaInfo : schemaInfoList)
		{
			Integer schemaID = schemaInfo.getSchema().getId();
			HashSet<SchemaElement> elements = new HashSet<SchemaElement>(elementMap.get(schemaID));
			for(Integer extendedSchemaID : getExtendedSchemas(schemaID,schemaInfoList))
				elements.addAll(elementMap.get(extendedSchemaID));
			boolean success = schemaInfo.addElements(new ArrayList<SchemaElement>(elements));
			if(!success) throw new Exception("Failure to generate the schema from the M3 schema file");
		}
		
		// Sort schemas by schema ID and then return
		Collections.sort(schemaInfoList,new SchemaInfoComparator());
		return schemaInfoList;
	}
	
	/** Returns the list of extended schemas for the specified schema ID */
	private ArrayList<Integer> getExtendedSchemas(Integer schemaID, ArrayList<SchemaInfo> schemaInfoList)
	{
		// Get the schema info associated with the specified schema ID
		SchemaInfo schemaInfo = null;
		for(SchemaInfo currSchemaInfo : schemaInfoList)
			if(currSchemaInfo.getSchema().getId().equals(schemaID))
				{ schemaInfo=currSchemaInfo; break; }
		if(schemaInfo==null) return new ArrayList<Integer>();
		
		// Generate the list of extended schemas
		ArrayList<Integer> extendedSchemas = new ArrayList<Integer>(schemaInfo.getParentSchemaIDs());
		for(Integer parentSchemaID : schemaInfo.getParentSchemaIDs())
			extendedSchemas.addAll(getExtendedSchemas(parentSchemaID, schemaInfoList));
		return extendedSchemas;
	}
	
	/** Identifies the matching schema in the repository if one exists */
	private Integer getMatchedSchema(ArrayList<Schema> availableSchemas, SchemaInfo schemaInfo) throws Exception
	{
		// Search through available schemas to find if one matches
		for(Schema possSchema : availableSchemas)
			if(possSchema.getName().equals(schemaInfo.getSchema().getName()))
			{
				// Generate schema info for both schemas
				SchemaInfo possSchemaInfo = client.getSchemaInfo(possSchema.getId());
				
				// Check to see if number of elements matches
				ArrayList<SchemaElement> elements = schemaInfo.getElements(null);
				ArrayList<SchemaElement> possElements = possSchemaInfo.getElements(null);
				if(elements.size()==possElements.size())
				{
					// Sort the elements in each list
					Collections.sort(elements,new SchemaElementComparator());
					Collections.sort(possElements,new SchemaElementComparator());

					// Validate that all of the elements are indeed the same
					boolean match = true;
					for(int i=0; i<elements.size(); i++)
					{
						SchemaElement s1 = elements.get(i);
						SchemaElement s2 = possElements.get(i);
						if((s1.getClass().equals(s2.getClass()) && s1.getName().equals(s2.getName()))==false)
							{ match = false; break; }
					}
					if(match) return possSchema.getId();					
				}
		}

		// Indicates that no matched schema was found
		return null;
	}
	
	/** Import the schema to the repository */
	private Integer importParentSchema(SchemaInfo schemaInfo)
	{
		boolean success = false;
		
		// Import the schema
		Integer schemaID = null;
		try {
			schemaID = client.importSchema(schemaInfo.getSchema(), schemaInfo.getBaseElements(null));
			success = client.setParentSchemas(schemaID, schemaInfo.getParentSchemaIDs());
			if(success) client.lockSchema(schemaID);
		} catch(Exception e) {}

		// Delete the imported schema if failure occurred
		if(!success && schemaID!=null)
		{
			try { client.deleteSchema(schemaID); } catch(Exception e) {};
			schemaID=null;
		}
	
		// Return the created schema ID
		return schemaID;
	}
}