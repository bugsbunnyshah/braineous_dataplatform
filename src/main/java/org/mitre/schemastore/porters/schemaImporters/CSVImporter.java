package org.mitre.schemastore.porters.schemaImporters;

import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.ImporterException.ImporterExceptionType;
import org.mitre.schemastore.porters.URIType;

import au.com.bytecode.opencsv.CSVReader;

public class CSVImporter extends SchemaImporter {
	private ArrayList<String> attributeNames;
	private URI sourceURI;
	private HashMap<String, Entity> entities;
	private HashMap<String, Attribute> attributes;
	protected ArrayList<SchemaElement> schemaElements = new ArrayList<SchemaElement>();
	private HashMap<String, Domain> domainList = new HashMap<String, Domain>();
	protected String documentation = "";

	public CSVImporter() {
		loadDomains();
	}

	// get rid of characters
	protected String cleanup(String s) {
		return s.trim().replaceAll("'", "''").replaceAll("\"", "\\\"");
	}

	/**
	 * Derive the schema from the contents of a csv file
	 * This is where the magic happens!
	 */
	protected void generate() {
		// make sure we actually have data to process
		if (attributeNames == null || attributeNames.size() == 0) { return; }

		// create a new table entity
		String tblName = sourceURI.getPath();
		int tblNameStarting = tblName.lastIndexOf('/') + 1;
		int tblNameStopping = tblName.lastIndexOf('.');
		if (tblNameStopping == -1 || tblNameStopping < tblNameStarting) { tblNameStopping = tblName.length(); }
		tblName = tblName.substring(tblNameStarting, tblNameStopping); 
		Entity tblEntity = new Entity(nextId(), tblName, "", 0);
		entities.put(tblName, tblEntity);

		// look at the values in our list of headers and add them to our attributes list
		for (int i = 0; i < attributeNames.size(); i++) {
			if (attributeNames.get(i).length() > 0) {
				Domain domain = domainList.get(STRING);
				Attribute attribute = new Attribute(nextId(), attributeNames.get(i), documentation, tblEntity.getId(), domain.getId(), null, null, false, 0);
				attributes.put(attribute.getName(), attribute);
			}
		}
	}

	/** Generate the schema elements */
	public ArrayList<SchemaElement> generateSchemaElements() throws ImporterException {
		generate();
		for (Entity e : entities.values()) { schemaElements.add(e); }
		for (Attribute a : attributes.values()) { schemaElements.add(a); }
		return schemaElements;
	}

	/** Returns the importer URI type */
	public URIType getURIType() {
		return URIType.FILE;
	}

	/** Returns the importer name */
	public String getName() {
		return "CSV Importer";
	}

	/** Returns the importer description */
	public String getDescription() {
		return "Imports CSV formatted schema into the schema store.";
	}

	/** Returns the importer URI file types */
	public ArrayList<String> getFileTypes() {
		ArrayList<String> filetypes = new ArrayList<String>(3);
		filetypes.add("csv");
		return filetypes;
	}

	protected void initialize() throws ImporterException {
		try {
			entities = new HashMap<String, Entity>();
			attributes = new HashMap<String, Attribute>();

			// Do nothing if the excel sheet has been cached.
			if ((sourceURI != null) && sourceURI.equals(uri)) { return; }
			sourceURI = uri;

			// read in our csv file
			CSVReader reader = new CSVReader(new FileReader(sourceURI.toString()));
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				// if there is no data on this line, go to the next one
				if (nextLine.length == 0) { continue; }

				// read all the fields on this line and add them to our header details
				for (int i = 0; i < nextLine.length; i++) {
					if (nextLine[i].length() != 0) { attributeNames.add(nextLine[i]); }
				}

				// if we found headers then we are done processing this csv file
				if (attributeNames.size() > 0) { break; }
			}
			reader.close();

			// if we have no header values that's an error
			if (attributeNames.size() == 0) {
				throw new Exception("No header values found.");
			}
		} catch (Exception e) {
			throw new ImporterException(ImporterExceptionType.PARSE_FAILURE, e.getMessage());
		}
	}

	/**
	 * Function for loading the preset domains into the Schema and into a list for use during
	 * Attribute creation
	 */
	private void loadDomains() {
		Domain domain = new Domain(SchemaImporter.nextId(), INTEGER, "The Integer domain", 0);
		schemaElements.add(domain);
		domainList.put(INTEGER, domain);
		domain = new Domain(SchemaImporter.nextId(), REAL, "The Real domain", 0);
		schemaElements.add(domain);
		domainList.put(REAL, domain);
		domain = new Domain(SchemaImporter.nextId(), STRING, "The String domain", 0);
		schemaElements.add(domain);
		domainList.put(STRING, domain);
		domain = new Domain(SchemaImporter.nextId(), DATETIME, "The DateTime domain", 0);
		schemaElements.add(domain);
		domainList.put(DATETIME, domain);
		domain = new Domain(SchemaImporter.nextId(), BOOLEAN, "The Boolean domain", 0);
		schemaElements.add(domain);
		domainList.put(BOOLEAN, domain);
	}
}