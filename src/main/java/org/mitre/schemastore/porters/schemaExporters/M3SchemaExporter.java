// Copyright (C) The MITRE Corporation 2008
// ALL RIGHTS RESERVED

package org.mitre.schemastore.porters.schemaExporters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import org.mitre.schemastore.client.Repository;
import org.mitre.schemastore.client.SchemaStoreClient;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.porters.xml.ConvertToXML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Class for moving SchemaStore format between SchemaStore Instances
 * Given schemaID, exporter finds parent types, and other schema dependencies and exports these too.
 *  
 * @author MDMORSE
 */
public class M3SchemaExporter extends SchemaExporter
{	
	/** Returns the exporter name */
	public String getName()
		{ return "M3 Exporter"; }
	
	/** Returns the exporter description */
	public String getDescription()
		{ return "This exporter can be used to export the M3 format of a schema"; }
	
	/** Returns the exporter file type */
	public String getFileType()
		{ return ".m3s"; }

	public static void main(String[] args){
		
		Integer schemaId1 = 16220;
		Integer schemaId2 = 16159;
		
		M3SchemaExporter schemaExporter = new M3SchemaExporter();
		try {
			Repository repository = new Repository(Repository.POSTGRES,new URI("bmd.org.mitre.org"), "org/mitre/schemastore", "org/mitre/schemastore","bmdSchemastore");
			String md5_1 = schemaExporter.getMD5(repository, schemaId1);
			System.out.println(md5_1);
			String md5_2 = schemaExporter.getMD5(repository, schemaId2);
			System.out.println(md5_2);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Returns the MD5 hash of the schema for a given schemaId found in "org.org.mitre.schemastore".
	 * This method is only useful if the RepositoryManager is available to return the
	 * appropriate Repository.
	 * 
	 * @param schemaId
	 * @return
	 * @throws IOException
	 */
/*	public String getMD5(Integer schemaId) throws IOException{

		List<Repository> repos = RepositoryManager.getRepositories();
		Iterator<Repository> iter = repos.iterator();
		while(iter.hasNext()){
			Repository repo = iter.next();
			if (repo.getDatabaseName().equalsIgnoreCase("org.org.mitre.schemastore")){
				RepositoryManager.setSelectedRepository(repo);
				return getMD5(repo, schemaId);
			}
		}
		return null;
	}
*/
	/**
	 * Returns the MD5 hash of the schema for a given schemaId found in the given repository.
	 * 
	 * @param repo
	 * @param schemaId
	 * @return
	 * @throws IOException
	 */
	public String getMD5(Repository repo, Integer schemaId) throws IOException{
		
		File file = File.createTempFile(String.valueOf(schemaId), ".m3s");
		exportSchema(repo, schemaId, file);
		
		FileInputStream fis = new FileInputStream(file);
		String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
		fis.close();
		return md5;
	}
	
	/**
	 * Exports the schema and schema elements from a given Schema in the given
	 * repository to an SchemaStore ordered, non-archive file.
	 */
	public void exportSchema(Repository repo, Integer schemaId, File file) throws IOException {
		
		client = new SchemaStoreClient(repo);
		Schema schema = client.getSchema(schemaId);
		ArrayList<SchemaElement> schemaElements = client.getSchemaInfo(schemaId).getElements(null);
		exportSchema(schema, schemaElements, file, true);
	}

	/** Exports the schema and schema elements to an SchemaStore archive file */
	public void exportSchema(Schema schema, ArrayList<SchemaElement> schemaElements, File file) throws IOException
	{
		exportSchema(schema, schemaElements, file, false);
	}
	
	/** Exports the schema and schema elements to an SchemaStore archive file */
	public void exportSchema(Schema schema, ArrayList<SchemaElement> schemaElements, File file, boolean ordered) throws IOException
	{
		try {
			// Generates the XML document
			Document document = generateXMLDocument(schema.getId(), ordered);
			DOMSource domSource = new DOMSource(document);
			File tempFile = File.createTempFile("M3S", ".xml");
			StreamResult streamResult = new StreamResult(tempFile);
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
			serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
					"4");
			serializer.transform(domSource, streamResult);
	/*		DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
			LSSerializer writer = impl.createLSSerializer();
			String str = writer.writeToString(document);
			// Save the XML document to a temporary file
			File tempFile = File.createTempFile("M3S", ".xml");
			FileWriter out = new FileWriter(tempFile);
			out.write(str);
			//deprecated
	/*		OutputFormat format = new OutputFormat(document);
			format.setIndenting(true);
			XMLSerializer serializer = new XMLSerializer(out, format);
			serializer.serialize(document);
			out.close();
			*/
			// Generate a zip file for the specified file
			// If you are ordering it is almost certainly to have a consistent
			// hash, which is no longer consistent if you zip it.
			if(!ordered){
				ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(file));
				FileInputStream in = new FileInputStream(tempFile);
				byte data[] = new byte[100000]; int count;
				zipOut.putNextEntry(new ZipEntry("schemas.xml"));
				while((count = in.read(data)) > 0)
					zipOut.write(data, 0, count);
				zipOut.closeEntry();
				zipOut.close();
				in.close();
			
				// Remove the temporary file
				tempFile.delete();
			}
		}
		catch(Exception e) { System.out.println("(E)M3SchemaExporter.exportSchema - " + e.getMessage()); }
	}
	
	/** Generates the XML document */
	private Document generateXMLDocument(Integer schemaID, boolean ordered) throws Exception
	{
		// Generate the list of all schemas that must be exported to completely define the specified schema
		ArrayList<Integer> schemaIDs = new ArrayList<Integer>(); 
		schemaIDs.add(schemaID);
		schemaIDs.addAll(client.getAncestorSchemas(schemaID));
		if(ordered){
			orderSchemas(schemaIDs);
		}

		// Initialize the XML document
		Document document = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		document = db.newDocument();

		// Create the XML document
		Element rootXMLElement = document.createElement("Schemas");
		for(Integer currSchemaID : schemaIDs)
		{
			// Generate XML for the schema
			Schema currSchema = client.getSchema(currSchemaID);
			ArrayList<Integer> parentSchemaIDs = client.getParentSchemas(currSchemaID);
			if(ordered){
				orderSchemas(parentSchemaIDs);
			}
			Element schemaXMLElement = ConvertToXML.generate(currSchema, parentSchemaIDs, document);

			// Generate XML for the various schema elements
			ArrayList<SchemaElement> elements = client.getSchemaInfo(currSchemaID).getBaseElements(null);
			if(ordered){
				elements = orderSchemaElements(elements);
			}
			for(SchemaElement schemaElement : elements)
			{
				Element schemaElementXMLElement = ConvertToXML.generate(schemaElement, document);
				schemaXMLElement.appendChild(schemaElementXMLElement);
			}
			rootXMLElement.appendChild(schemaXMLElement);
		}
		document.appendChild(rootXMLElement);

		// Returns the generated document
		return document;
	}
	
	private ArrayList<SchemaElement> orderSchemaElements(ArrayList<SchemaElement> elements) {

		SortedMap<String, SchemaElement> schemaElements = new TreeMap<String, SchemaElement>();
		for (SchemaElement element : elements){
			schemaElements.put(element.getName(), element);
		}
		return new ArrayList<SchemaElement>(schemaElements.values());
	}

	private Collection<Integer> orderSchemas(ArrayList<Integer> schemaIDs) throws RemoteException {
		Schema tempSchema;
		Map<Integer, String> schemaNames = new TreeMap<Integer, String>();
		for (Integer schemaID : schemaIDs){
			tempSchema = client.getSchema(schemaID);
			tempSchema.getName();
			schemaNames.put(schemaID, tempSchema.getName());
		}
		TreeMap<Integer, String> sortedSchemaNames = new TreeMap<Integer, String>();
		sortedSchemaNames.putAll(schemaNames);
		return sortedSchemaNames.keySet();
	}
}