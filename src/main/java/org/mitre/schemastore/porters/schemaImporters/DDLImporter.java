// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.porters.schemaImporters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;

import org.mitre.schemastore.client.Repository;
import org.mitre.schemastore.client.SchemaStoreClient;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.ImporterException.ImporterExceptionType;
import org.mitre.schemastore.porters.URIType;
import org.mitre.schemastore.porters.schemaImporters.ddl.parser.Parser;
import org.mitre.schemastore.porters.schemaImporters.ddl.parser.SchemaBuilder;
import org.mitre.schemastore.porters.schemaImporters.ddl.parser.Tables;

public class DDLImporter extends SchemaImporter {
	/** Returns the importer name */
	public String getName() {
		return "SQL/DDL Importer";
	}

	/** Returns the importer description */
	public String getDescription() {
		return "This importer can be used to import schemas from a DDL format.";
	}

	/** Returns the importer URI type */
	public URIType getURIType() {
		return URIType.FILE;
	}

	/** Returns the importer URI file types */
	public ArrayList<String> getFileTypes() {
		ArrayList<String> fileTypes = new ArrayList<String>();
		fileTypes.add(".ddl");
		fileTypes.add(".sql");
		return fileTypes;
	}

	/** Returns the schema elements from the specified URI */
	public ArrayList<SchemaElement> generateSchemaElements() throws ImporterException {
		try {
			// load the file
			String line = null;
			StringBuffer ddl = new StringBuffer();
			BufferedReader in = new BufferedReader(new FileReader(new File(uri)));
			while ((line = in.readLine()) !=  null) { ddl.append(line + "\n"); }
			in.close();

			// get a list of commands from the file
			ArrayList<ArrayList<String>> commands = Parser.parseForCommands(ddl.toString());

			// create a DDL parser and send it the commands
			Parser parser = new Parser();
			for (int i = 0; i < commands.size(); i++) {
				parser.parse(commands.get(i));
			}

			// get our tables that we've parsed out
			Tables tableObj = parser.getTables();

			// do something with the tables we've parsed out
			SchemaBuilder builder = new SchemaBuilder(tableObj);
			return builder.getSchemaObjects();
		} catch (Exception e) {
			throw new ImporterException(ImporterExceptionType.PARSE_FAILURE, e.getMessage());
		}
	}

	public static void main(String[] args) throws Exception {
//		File ddlFile = new File(args[0]);
		File ddlFile = new File("C:\\Users\\mgreer\\share\\My Documents\\CRS_PROD_schema_export.sql");
		DDLImporter tester = new DDLImporter();
		Repository repository = new Repository(Repository.DERBY,new URI("/home/plockaby"), "org/mitre/schemastore","postgres","postgres");
		SchemaStoreClient client = new SchemaStoreClient(repository);
		tester.setClient(client);
		tester.importSchema(ddlFile.getName(), "", "", ddlFile.toURI());
	} 
}