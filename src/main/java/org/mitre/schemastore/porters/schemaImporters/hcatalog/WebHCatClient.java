/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.schemastore.porters.schemaImporters.hcatalog;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import de.susebox.jtopas.TokenizerException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.Databases;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.HCatRestColumn;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.HCatRestDatabase;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.HCatRestTable;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.HCatRestTableProperties;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.Tables;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.parser.HCatalogParseException;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.parser.HCatalogRequestException;

/**
 *
 * @author mgreer
 */
public class WebHCatClient {

	private URI baseURI = null;
	private Client client = ClientBuilder.newClient();
	private String user = null;
	public static final int DEFAULT_WEBHCAT_PORT = 50111;
	public static final String DEFAULT_WEBHCAT_HOST = "localhost";
	public static final int OK = 200;

	private boolean isClosed = false;
	public WebHCatClient() {
		this(null, null, 0);
	}

	private enum HCatRequestType {
			DATABASE_LIST("database", 0,  "like" , null), 
			DATABASE_DESC("database/{0}", 1), 
			TABLE_LIST("database/{0}/table", 1, "like", null),
			TABLE_DESC("database/{0}/table/{1}", 2, "format", "extended" ), 
			PARTITION_LIST("database/{0}/table/{1}/partition", 2),
			PARTITION_DESC("database/{0}/table/{1},partition/{2}", 3), 
			COLUMN_LIST("database/{0}/table/{1}/column", 2),
			COLUMN_DESC("database/{0}/table/{1}/column/{2}", 3), 
			PROPERTY_LIST("database/{0}/table/{1}/property", 2),
			PROPERTY_DESC("database/{0}/table/{1}/property/{2}", 3);
		
		String parameter;
		String allowedValueForParm;
		String restString;
		int numberOfArguments;
		
		private HCatRequestType(String restString, int num) {
			this(restString, num, (String) null, (String) null);
		}
		private HCatRequestType(String restString, int num, String parm, String allowedValueForParm){
			this.restString = restString;
			numberOfArguments = num;
			this.parameter = parm;
			this.allowedValueForParm = allowedValueForParm;
		}
		public String getParameter() {
			return parameter;
		}
		public String getAllowedValueForParameter() {
			return allowedValueForParm;
		}
		public String getRestString() {
			return restString;
		}
		public int getNumberOfArguments() {
			return numberOfArguments;
		}
		
		
	}
	public WebHCatClient(String user, String host, int port ) {
		if (user == null) {
			this.user = System.getProperty("user.name");
		}
		else {
			this.user = user;
		}
		if (host == null) {
			host = DEFAULT_WEBHCAT_HOST;
		}
		if (port <= 0) {
			port = DEFAULT_WEBHCAT_PORT;
		}
		try {
		baseURI = new URI("http://" + host + ":" + port +"/templeton/v1/ddl/");
		}
		catch (URISyntaxException e) {
			baseURI = null;
		}
	}
	URI getBaseURI() {
		return baseURI; 
	}

	
	public List<String> listDatabaseNamesByPattern(String string) throws HCatalogRequestException, HCatalogParseException {
		String optParm = null;
		String optParmValue = null;
		if (string != null && !string.equals("*")) {
			optParm = HCatRequestType.DATABASE_LIST.getParameter();
			optParmValue = string;
		}
		String respString = getJsonResponse(HCatRequestType.DATABASE_LIST, optParm, optParmValue);
		Gson gson = new Gson();
		try
		{
			return (gson.fromJson(respString, Databases.class)).getDatabases();
		}
		catch (JsonSyntaxException e) {
			throw new HCatalogParseException("Error parsing database name return for connection " + baseURI.toString()+" and user " + user + ": " + e.getMessage() + "\nReturned json: " + respString);
		}
	}
	public HCatRestDatabase getDatabase(String string) throws HCatalogRequestException, HCatalogParseException {
		ArrayList<String> requiredArgs = new ArrayList<String>();
		requiredArgs.add(string);
		String respString = getJsonResponse(HCatRequestType.DATABASE_DESC, requiredArgs);
		Gson gson = new Gson();
		HCatRestDatabase db = null;
		try{
			db = gson.fromJson(respString, HCatRestDatabase.class);
		}
		catch (JsonSyntaxException e) {
			throw new HCatalogParseException("Error parsing database information for connection " + baseURI.toString() +"/" + string + " and user " + user + ": " + e.getMessage() + "\nReturned json: " + respString);
		}
		db.setClient(this);
		return db;

	}

	public List<String> listTableNamesByPattern(String string, String string1) throws HCatalogRequestException, HCatalogParseException {
		String optParm = null;
		String optParmValue = null;
		if (string1 != null && !string1.equals("*")) {
			optParm = HCatRequestType.TABLE_LIST.getParameter();
			optParmValue = string1;
		}
		ArrayList<String> requiredArgs = new ArrayList<String>();
		requiredArgs.add(string);
		String respString = getJsonResponse(HCatRequestType.TABLE_LIST, requiredArgs, optParm, optParmValue);
		Gson gson = new Gson();
		try {
			return gson.fromJson(respString, Tables.class).getTables();
		}
		catch (JsonSyntaxException e) {
			throw new HCatalogParseException("Error parsing table list return for connection " + baseURI.toString() +"/" + string + "/table and user " + user + ": " + e.getMessage() + "\nReturned json: " + respString);
		}
	}


	public HCatRestTable getTable(String string, String string1) throws HCatalogRequestException, HCatalogParseException {
		ArrayList<String> requiredArgs = new ArrayList<String>();
		requiredArgs.add(string);
		requiredArgs.add(string1);
		String respString = getJsonResponse(HCatRequestType.TABLE_DESC, requiredArgs, "format", "extended");
		Gson gson = new Gson();
		HCatRestTable table = null;
		try {
			table =  gson.fromJson(respString, HCatRestTable.class);
			table.setClient(this);
			table.isExtended(true);
			for (HCatRestColumn column : table.getAllColumns()) {
				try {
					column.getHiveType();
				} catch (TokenizerException ex) {
					throw new HCatalogParseException("Error parsing columns for table " + string1 + ": " + ex.getMessage());
				}
			}
		}
		catch (JsonSyntaxException e) {
			throw new HCatalogParseException("Error parsing table return for connection " + baseURI.toString() +"/" + string + "/table/" + string1 + " and user " + user + ": " + e.getMessage() + "\nReturned json: " + respString);
		}
		return table;
	}
	private String getJsonResponse(HCatRequestType requestType, String optParm, String optParmValue) throws HCatalogRequestException {
		return getJsonResponse(requestType, null, optParm, optParmValue);
	}
	private String getJsonResponse(HCatRequestType requestType, ArrayList<String> requiredArgs) throws HCatalogRequestException {
		return getJsonResponse(requestType, requiredArgs, null, null);
	}
	private String getJsonResponse(HCatRequestType requestType, ArrayList<String> requiredArgs, String optParm, String optParmValue ) throws HCatalogRequestException {
		String requestString = baseURI.toString() + requestType.getRestString();
		if (optParm != null && ((requestType.getParameter() == null || 
				!requestType.getParameter().equals(optParm)) || (optParmValue == null  || 
				(requestType.getAllowedValueForParameter() != null && 
				!optParmValue.equals(requestType.getAllowedValueForParameter()))))) {
			throw new HCatalogRequestException("Incorrect parameter sent for request type");
		}
		if (requestType.getNumberOfArguments()>0 ) {
			if (requiredArgs == null || requiredArgs.size() != requestType.getNumberOfArguments()) {
		
				throw new HCatalogRequestException("Incorrect number of arguments sent for request type");
			}
		
		for (int i = 0; i < requestType.getNumberOfArguments(); i++) {
			String requiredArg = requiredArgs.get(i);
			if (requiredArg == null || requiredArg.isEmpty()) {
				throw new HCatalogRequestException("Empty or null argument sent for required argument");
			}
			requestString = requestString.replace("{" + i + "}", requiredArg);
		}
		}
		WebTarget target = getClient().target(requestString);
		if (user != null) {
			target = target.queryParam("user.name", user);
		}
		if (optParm != null) {
			target = target.queryParam(optParm, optParmValue);
		}
		Response response = target.request("application/json").accept("application/json").get();

		String respString = response.readEntity(String.class);
		if (response.getStatus() != OK) {

			JSONObject json = new JSONObject(respString);
			throw new HCatalogRequestException("Error requesting "+ requestString + ": " + response.getStatus() + ": " + json.getString("error"));
		}
		return respString;
		
		
		
	}
	private Client getClient() throws HCatalogRequestException {
		if (!isClosed) {
			return client;
		}
		else {
			throw new HCatalogRequestException("Client is closed.");
		}
		
	}

	public HCatRestTableProperties getTableProperties(String database, String table) throws HCatalogRequestException, HCatalogParseException {
		ArrayList<String> requiredArgs = new ArrayList<String>();
		requiredArgs.add(database);
		requiredArgs.add(table);
		String respString = getJsonResponse(HCatRequestType.PROPERTY_LIST, requiredArgs);
		Gson gson = new Gson();
		HCatRestTableProperties properties = null;
		try {
			properties =  gson.fromJson(respString, HCatRestTableProperties.class);
		}
		catch (JsonSyntaxException e) {
			throw new HCatalogParseException("Error parsing table properties return for connection " + baseURI.toString() +"/" + database + "/table/" + table + "/property" +" and user " + user + ": " + e.getMessage() + "\nReturned json: " + respString);
		}
		return properties;

	} 
	
	public void close() throws HCatalogRequestException {
		client.close();
		client = null;
		isClosed = true;
	}
	
}
