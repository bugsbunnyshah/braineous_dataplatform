// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.harmony.model;

import java.applet.Applet;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.mitre.harmony.matchers.MatcherManager;
import org.mitre.schemastore.client.Repository;
import org.mitre.schemastore.client.SchemaStoreClient;
import org.mitre.schemastore.model.DataType;
import org.mitre.schemastore.model.Function;
import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.Project;
import org.mitre.schemastore.model.ProjectSchema;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.SchemaInfo;
import org.mitre.schemastore.porters.Exporter;
import org.mitre.schemastore.porters.Importer;
import org.mitre.schemastore.porters.Porter;
import org.mitre.schemastore.porters.PorterManager;
import org.mitre.schemastore.porters.PorterType;
import org.mitre.schemastore.porters.URIType;
import org.mitre.schemastore.porters.projectImporters.M3ProjectImporter;
import org.mitre.schemastore.porters.schemaImporters.SchemaImporter;

/**
 * Handles all communications to the database (via servlets)
 * @author CWOLF
 */
public class SchemaStoreManager
{
	/** Stores the code base used for accessing the database */
	static private URL codeBase;

	/** Stores the schema store client */
	static private SchemaStoreClient client;
	
	/** Initializes the database for use */
	static public void init(Applet applet)
	{
		try {
			if(applet.getCodeBase().toString().startsWith("file")) codeBase = new URL("http://localhost:8080/Harmony/");
			else codeBase = applet.getCodeBase();
		} catch(MalformedURLException e) {}
	}

	/** Sets the SchemaStore client for use by this manager */
	static public void setClient(Repository repository) throws Exception
		{ setClient(new SchemaStoreClient(repository)); }
	
	/** Sets the SchemaStore client for use by this manager */
	static public void setClient(SchemaStoreClient clientIn)
		{ client = clientIn; MatcherManager.setClient(client); }
	
	/** Connects to the specified servlet */
	static private URLConnection getServletConnection() throws MalformedURLException, IOException
	{
		URL urlServlet = new URL(codeBase.toString().replaceFirst("HarmonyApplet.*","")+"HarmonyServlet");
		URLConnection connection = urlServlet.openConnection();
		connection.setConnectTimeout(10800);
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		return connection;
	}

	/** Handles the call to the servlet */
	static private Object callServlet(String functionName,Object[] inputs) throws Exception
	{
		Object object = null;
		URLConnection connection = getServletConnection();
		ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
		out.writeObject(functionName);
		out.writeObject(inputs.length);
		for(Object input : inputs) out.writeObject(input);
		out.flush();
		out.close();
		ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
		object = in.readObject();
	    in.close();
		return object;
	}
	
	/** Manages calls to the SchemaStore client method */
	static private Object callClient(String name, Object inputs[]) throws RemoteException
	{
		// Create an array of types
		Class<?> types[] = new Class[inputs.length];
		for(int i=0; i<inputs.length; i++)
			types[i] = (inputs[i]==null) ? Integer.class : inputs[i].getClass();
			
		// Calls the SchemaStore method
		try {
			Method method = client.getClass().getDeclaredMethod(name, types);
			return method.invoke(client, inputs);
		} catch(Exception e) { return new RemoteException("Unable to call method " + name); }
	}

	/** Handles the call to the database (either through servlet or SchemaStore client) */
	static private Object callFunction(String functionName, Object[] inputs) 
	{
		try {
			if(codeBase!=null) return callServlet(functionName,inputs);
			if(client!=null) return callClient(functionName,inputs);
		}
		catch(Exception e)
		{
			System.out.println("(E)SchemaStoreManager.callFunction: Unable to call method " + functionName);
			System.out.println("    " + e.getMessage());
		}
		return null;
	}
	
	//------------------
	// Schema Functions
	//------------------
	
	/** Gets the list of schemas from the web service */ @SuppressWarnings("unchecked")
	static public ArrayList<Schema> getSchemas()
		{ return (ArrayList<Schema>)callFunction("getSchemas",new Object[] {}); }
	
	/** Gets the descendant schemas from the web service for the specified schema */ @SuppressWarnings("unchecked")
	static public ArrayList<Integer> getDescendantSchemas(Integer schemaID)
		{ return (ArrayList<Integer>)callFunction("getDescendantSchemas",new Object[] {schemaID}); }
	
	/** Gets the list of deletable schemas from the web service */ @SuppressWarnings("unchecked")
	static public ArrayList<Integer> getDeletableSchemas() throws RemoteException
		{ return (ArrayList<Integer>)callFunction("getDeletableSchemas",new Object[] {}); }
	
	/** Gets the specified schema from the web service */
	static public Schema getSchema(Integer schemaID)
		{ return (Schema)callFunction("getSchema",new Object[] {schemaID}); }

	/** Deletes the specified schema from the web service */
	static public boolean deleteSchema(Integer schemaID) throws RemoteException
		{ return (Boolean)callFunction("deleteSchema",new Object[] {schemaID}); }
	
	//--------------------------
	// Schema Element Functions
	//--------------------------
	
	/** Retrieves the schema info for the specified schema from the web service */
	static public SchemaInfo getSchemaInfo(Integer schemaID)
		{ return (SchemaInfo)callFunction("getSchemaInfo",new Object[] {schemaID}); }	

	/** Retrieves the specified schema element from the web service */
	static public SchemaElement getSchemaElement(Integer schemaElementID)
		{ return (SchemaElement)callFunction("getSchemaElement",new Object[] {schemaElementID}); }	
	
	//-------------------
	// Project Functions
	//-------------------

	/** Retrieves the list of all projects from the web service */ @SuppressWarnings("unchecked")
	static public ArrayList<Project> getProjects() throws RemoteException
		{ return (ArrayList<Project>)callFunction("getProjects",new Object[] {}); }
	
	/** Retrieves the specified project from the web service */
	static public Project getProject(Integer projectID) throws RemoteException
		{ return (Project)callFunction("getProject",new Object[] {projectID}); }
	
	/** Adds the specified project to the web service */
	static public Integer addProject(Project project) throws RemoteException
		{ return (Integer)callFunction("addProject",new Object[] {project}); }
	
	/** Modifies the specified project in the web service */
	static public boolean updateProject(Project project) throws RemoteException
		{ return (Boolean)callFunction("updateProject",new Object[] {project}); }
	
	/** Deletes the specified project from the web service */
	static public boolean deleteProject(Integer projectID) throws RemoteException
		{ return (Boolean)callFunction("deleteProject",new Object[] {projectID}); }
	
	/** Retrieves the list of all mappings for the specified project from the web service */ @SuppressWarnings("unchecked")
	static public ArrayList<Mapping> getMappings(Integer projectID) throws RemoteException
		{ return (ArrayList<Mapping>)callFunction("getMappings",new Object[] {projectID}); }
	
	/** Adds the specified mapping to the web service */
	static public Integer addMapping(Mapping mapping) throws RemoteException
		{ return (Integer)callFunction("addMapping",new Object[] {mapping}); }
	
	/** Retrieves the mapping cells for the specified mapping from the web service */ @SuppressWarnings("unchecked")
	static public ArrayList<MappingCell> getMappingCells(Integer mappingID) throws RemoteException
		{ return (ArrayList<MappingCell>)callFunction("getMappingCells",new Object[] {mappingID}); }
	
	/** Saves the specified mapping to the web service */
	static public boolean saveMappingCells(Integer mappingID, ArrayList<MappingCell> mappingCells) throws RemoteException
		{ return (Boolean)callFunction("saveMappingCells",new Object[] {mappingID,mappingCells}); }
	
	/** Deletes the specified mapping from the web service */
	static public boolean deleteMapping(Integer mappingID) throws RemoteException
		{ return (Boolean)callFunction("deleteMapping",new Object[] {mappingID}); }
	
	//--------------------
	// Function Functions
	//--------------------
	
	/** Retrieves the mapping cell functions from the web service */ @SuppressWarnings("unchecked")
	static public ArrayList<Function> getFunctions() throws RemoteException
		{ return (ArrayList<Function>)callFunction("getFunctions",new Object[] {}); }	
	
	/** Retrieves the mapping cell data types from the web service */ @SuppressWarnings("unchecked")
	static public ArrayList<DataType> getDataTypes() throws RemoteException
		{ return (ArrayList<DataType>)callFunction("getDataTypes",new Object[] {}); }
	
	/** Add a mapping cell functions from the web service */
	static public Integer addFunction(Function function) throws RemoteException
		{ return (Integer)callFunction("addFunction",new Object[] {function}); }

	//--------------------
	// Importer Functions
	//--------------------

	/** Returns the specified list of porters */
	static private ArrayList<Porter> getLocalPorters(PorterType type)
	{
		// For project importers, only use the M3 Project Importer
		if(type.equals(PorterType.PROJECT_IMPORTERS))
		{
			Porter porter = (Porter)new PorterManager(client).getPorter(M3ProjectImporter.class);
			ArrayList<Porter> porters = new ArrayList<Porter>();
			porters.add(porter);
			return porters;
		}
		
		// Get the specified list of porters
		ArrayList<Porter> porters = new PorterManager(client).getPorters(type);

		// For schema importers, filter out unavailable importers
		if(type.equals(PorterType.SCHEMA_IMPORTERS))
			for(Porter porter : new ArrayList<Porter>(porters))
			{
				URIType uriType = ((SchemaImporter)porter).getURIType();
				if(uriType==URIType.SCHEMA)
					porters.remove(porter);
			}

		// Return the list of porters
		return porters;
	}
	
	/** Returns the specified list of porters */ @SuppressWarnings("unchecked")
	static public <T extends Porter> ArrayList<T> getPorters(PorterType type)
	{
		try{
			if(client!=null) return (ArrayList<T>)getLocalPorters(type);
			if(codeBase!=null) return (ArrayList<T>)callFunction("getPorters",new Object[] {type});
		}
		catch(Exception e) { e.printStackTrace(); }
		
		return null;
	}

	/** Returns the URI list for the specific porter */ @SuppressWarnings("unchecked")
	static public ArrayList<URI> getImporterURIList(Importer importer)
	{
		try{
			if(client!=null) return importer.getList();
			if(codeBase!=null) return (ArrayList<URI>)callFunction("getImporterURIList",new Object[] {importer});
		}
		catch(Exception e) { e.printStackTrace(); }
		
		return null;
	}
	
	/** Retrieves the schema generated by the importer */
	static public Schema getSchemaFromImporter(Importer importer, URI uri)
		{ return (Schema)callFunction("getSchemaFromImporter",new Object[] {importer,uri}); }
	
	/** Imports the data through the specified importer */
	static public Integer importData(Importer importer, String name, String author, String description, URI uri)
		{ return (Integer)callFunction("importData",new Object[] {importer,name,author,description,uri}); }
	
	/** Retrieves the suggested schemas for the specified mapping */ @SuppressWarnings("unchecked")
	static public ArrayList<ProjectSchema> getSuggestedSchemas(Importer importer, URI uri)
		{ return (ArrayList<ProjectSchema>)callFunction("getSuggestedSchemas",new Object[] {importer,uri}); }
	
	/** Returns imported mapping cells through the specified importer */ @SuppressWarnings("unchecked")
	static public ArrayList<MappingCell> getImportedMappingCells(Importer importer, Integer sourceID, Integer targetID, URI uri)
		{ return (ArrayList<MappingCell>)callFunction("getImportedMappingCells",new Object[] {importer,sourceID,targetID,uri}); }
	
	/** Exports the data through the specified exporter */
	static public String exportData(Exporter exporter, ArrayList<Object> data)
		{ return (String)callFunction("exportData",new Object[] {exporter,data}); }
}