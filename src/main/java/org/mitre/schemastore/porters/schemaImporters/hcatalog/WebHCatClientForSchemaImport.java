package org.mitre.schemastore.porters.schemaImporters.hcatalog;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.HiveDataType;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.parser.HCatalogParseException;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.parser.HCatalogRequestException;

public class WebHCatClientForSchemaImport {
	
	URI requestedURI;
	private boolean isTableSpecific = false;
	private boolean isDatabaseSpecific = false;
	private String database = null;
	private String table = null;
	private WebHCatClient hCatClient;
	private HiveDataType schema = null;
	
	public WebHCatClientForSchemaImport(URI uri) {
		requestedURI = uri;
		int databaseIndex = uri.getPath().indexOf("/database/");
		int pathLength = uri.getPath().length();
		if (databaseIndex != -1 && databaseIndex+ 10 < pathLength){
			isDatabaseSpecific = true;
			int startIndex = databaseIndex + 10;

			int endIndex = uri.getPath().indexOf("/", startIndex);
			if (endIndex == -1) {
				endIndex = pathLength;
			}
			database = uri.getPath().substring(startIndex, endIndex);
			int tableIndex = uri.getPath().indexOf("/table/");
			if (tableIndex != -1 && tableIndex + 7 < pathLength) {
				isTableSpecific = true;
				startIndex = tableIndex + 7;
				endIndex = uri.getPath().indexOf("/", startIndex);
				if (endIndex == -1) {
					endIndex = pathLength;
				}
				table = uri.getPath().substring(startIndex, endIndex);
			}
		}
		String host = uri.getHost();
		int port = uri.getPort();
		if (host == null){
			host = uri.getPath();
			int portIndex = uri.getPath().indexOf(":");
			int pathIndex = uri.getPath().indexOf("/");
			if (pathIndex == -1 ) {
				pathIndex = host.length() +1;
			}
			if (portIndex != -1) {
				port = Integer.parseInt(host.substring(portIndex + 1, pathIndex -1));
			}
			else
			{
				portIndex = pathIndex;
			}
			host = host.substring(0, portIndex - 1);
		}
		hCatClient = new WebHCatClient(System.getProperty("user.name"), host, port );
	}
	public boolean isTableSpecific() {
		return isTableSpecific;
	}
	public boolean isDatabaseSpecific() {
		return isDatabaseSpecific;
	}
	public boolean onlyDatabaseSpecific() {
		return isDatabaseSpecific && !isTableSpecific;
	}
	public String getDatabase() {
		return database;
	}
	public String getTable() {
		return table;
	}
	public URI getRequestedURI() {
		return requestedURI;
	}
	public static List<URI> getURIsForDatabases(String uriString) throws HCatalogRequestException, HCatalogParseException, URISyntaxException {
		URI uri = new URI(addHttpProtocol(uriString));
		WebHCatClientForSchemaImport client = new WebHCatClientForSchemaImport(uri);
		List<URI> uris = new ArrayList<URI>();
		if (client.isTableSpecific || client.isDatabaseSpecific) {    	
			uris.add(uri);
		}else {
			List<String> databaseNames = client.hCatClient.listDatabaseNamesByPattern(null);

			for (String database : databaseNames){
				uris.add(new URI(client.hCatClient.getBaseURI().toString() +"database/" + database));
			}
		}
		return uris;

	}
	private static String addHttpProtocol(String uriString) {
		if (uriString.startsWith("http")) {
			return uriString;
		}
		else
		{
			return "http://" + uriString; 
		}
	}
	public static List<URI> getURIsForTables(String uriString) throws HCatalogRequestException, HCatalogParseException, URISyntaxException {
		URI uri = new URI(addHttpProtocol(uriString));
		WebHCatClientForSchemaImport client = new WebHCatClientForSchemaImport(uri);
		List<URI> uris = new ArrayList<URI>();
		if (client.isTableSpecific)
		{
			uris.add(uri);
		}
		else {
			List<String> databaseNames = null;
			if (client.isDatabaseSpecific){
				databaseNames = new ArrayList<String>();
				databaseNames.add(client.database);
			}

			else {
				databaseNames = client.hCatClient.listDatabaseNamesByPattern(null);
			}

			for (String database : databaseNames) {
				String databaseString = client.hCatClient.getBaseURI().toString() +"database/" + database + "/table/";
				List<String> tableNames = client.hCatClient.listTableNamesByPattern(database, null);
				for (String table : tableNames) {
					uris.add(new URI(databaseString + table));
				}
			}
		}
		return uris;
	}
	public HiveDataType getSchema() throws HCatalogRequestException, HCatalogParseException {
		if (schema == null) {

			if (onlyDatabaseSpecific()) {

					schema = hCatClient.getDatabase(database);
				
			}
			else if (isTableSpecific()) {
				schema = hCatClient.getTable(database, table);
			}
			else {
				throw new HCatalogRequestException("URI " + requestedURI + " does not request a schema for a table or a database");
			}
			
		}
		return schema;
		
	}
	
	

}
