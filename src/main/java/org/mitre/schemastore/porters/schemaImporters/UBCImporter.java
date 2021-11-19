package org.mitre.schemastore.porters.schemaImporters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.ImporterException.ImporterExceptionType;
import org.mitre.schemastore.porters.URIType;

import au.com.bytecode.opencsv.CSVReader;

/** Imports barcode information */
public class UBCImporter extends SchemaImporter
{
	/** Returns the importer URI type */
	public URIType getURIType()
		{ return URIType.FILE; }

	/** Returns the importer name */
	public String getName()
		{ return "UBC Importer"; }

	/** Returns the importer description */
	public String getDescription()
		{ return "Imports a list of UBC supplies as a schema"; }

	/** Returns the importer URI file types */
	public ArrayList<String> getFileTypes()
		{ return new ArrayList<String>(Arrays.asList(new String[]{"csv"})); }
	
	/** Retrieves the description for the item */
	private String getUBCDescription(String ubc)
	{
		// Extract the description
		try {			
			URL url = new URL("http://localhost:8080/SchemaStoreWebServices/ubcDescriptions.jsp?ubc="+ubc);
			URLConnection conn = url.openConnection();
			conn.connect();
			BufferedReader data = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			// Convert file into string
			StringBuffer in = new StringBuffer();
			String line;
			while((line=data.readLine())!=null) in.append(line);
			data.close();
			return in.toString();
		}
		catch(IOException e) { System.out.println("(E) URLInfo: "+e.getMessage()); }
		return "";
	}
	
	/** Generate the schema elements */
	public ArrayList<SchemaElement> generateSchemaElements() throws ImporterException
	{
		ArrayList<SchemaElement> elements = new ArrayList<SchemaElement>();
		try {
			CSVReader reader = new CSVReader(new FileReader(new File(uri)));
			List<String[]> lines = reader.readAll();
			for(int i=1; i<lines.size(); i++)
			{
				// Get the line in the csv file
				String[] line = lines.get(i);
				if(line.length==0) continue;
				try { Thread.sleep((int)(500+4500*Math.random())); } catch(Exception e) {}
				
				// Generate an entity based on the line
				try {
					String name = line[1];
					String description = getUBCDescription(line[0]);
					elements.add(new Entity(nextId(),name,description,null));
				} catch(Exception e) {}
			}				
		}
		catch (Exception e) { throw new ImporterException(ImporterExceptionType.PARSE_FAILURE, e.getMessage()); }
		return elements;
	}
}