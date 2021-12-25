// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.client;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

import org.mitre.schemastore.data.database.DatabaseConnection;
import org.mitre.schemastore.model.Alias;
import org.mitre.schemastore.model.Annotation;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.DataSource;
import org.mitre.schemastore.model.DataType;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Function;
import org.mitre.schemastore.model.FunctionImp;
import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.Project;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.SchemaElementList;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.model.Tag;
import org.mitre.schemastore.model.Thesaurus;
import org.mitre.schemastore.model.schemaInfo.SchemaInfo;
import org.mitre.schemastore.model.terms.AssociatedElement;
import org.mitre.schemastore.model.terms.ThesaurusTerms;
import org.mitre.schemastore.model.terms.VocabularyTerms;
import org.mitre.schemastore.servlet.SchemaStore;
import org.mitre.schemastore.servlet.SchemaStoreProxy;

/**
 * Handles all communications to the schema store web service
 * @author CWOLF
 */
public class SchemaStoreClient
{
	/** Stores the object for calling SchemaStore */
	private Object schemaStore = null;

	/** Manages calls to the SchemaStore method */
	private Object callMethod(String name, Object args[]) throws RemoteException
	{
		// Create an array of types
		Class<?> types[] = new Class[args.length];
		for(int i=0; i<args.length; i++)
		{
			Class<?> type = args[i].getClass();
			if(type==Integer.class) type = Integer.TYPE;
			if(type==Double.class) type = Double.TYPE;
			if(type==Boolean.class) type = Boolean.TYPE;
			types[i] = type;
		}

		// Calls the SchemaStore method
		try {
			Method method = schemaStore.getClass().getDeclaredMethod(name, types);
			return method.invoke(schemaStore, args);
		} catch(Exception e) { return new RemoteException("Unable to call method " + name); }
	}

	/** Constructor for the Schema Store Client */
	public SchemaStoreClient() throws RemoteException
	{
		try {
			Constructor<?> constructor = SchemaStore.class.getConstructor(new Class<?>[]{});
			schemaStore = constructor.newInstance(new Object[]{});
		} catch(Exception e) { throw new RemoteException("(E) Failed to connect to SchemaStore: " + e.getMessage()); }
	}

	/** Constructor for the Schema Store Client */
	public SchemaStoreClient(Repository repository) throws RemoteException
	{
		try {			
			// Connects to a database or web service
			if(!repository.getType().equals(Repository.SERVICE))
			{
				Integer type = repository.getType().equals(Repository.POSTGRES) ? DatabaseConnection.POSTGRES : DatabaseConnection.DERBY;
				Class<?> types[] = new Class[] {Integer.class,String.class,String.class,String.class,String.class};
				Object args[] = new Object[] {type,repository.getURI().toString(),repository.getDatabaseName(),repository.getDatabaseUser(),repository.getDatabasePassword()};
				Constructor<?> constructor = SchemaStore.class.getConstructor(types);
				schemaStore = constructor.newInstance(args);
			}
			else schemaStore = new SchemaStoreProxy(repository.getURI().toString());

			// Verify connection
			boolean connected = (Boolean)callMethod("isConnected",new Object[] {});
			if(!connected) throw new Exception("Invalid database connection");
		}
		catch(Exception e) { throw new RemoteException("(E) Failed to connect to SchemaStore: " + e.getMessage()); }
	}
	
	/** Compresses the database */
	public Boolean compress() throws RemoteException
		{ return (Boolean)callMethod("compress",new Object[] {}); }

	//------------------
	// Schema Functions
	//------------------

	/** Gets the list of schemas from the web service */
	public ArrayList<Schema> getSchemas() throws RemoteException
	{
		Schema[] schemas = (Schema[])callMethod("getSchemas",new Object[] {});
		return schemas==null ? new ArrayList<Schema>() : new ArrayList<Schema>(Arrays.asList(schemas));
	}

	/** Gets the specified schema from the web service */
	public Schema getSchema(Integer schemaID) throws RemoteException
		{ return (Schema)callMethod("getSchema",new Object[] {schemaID}); }

	/** Adds the specified schema to the web service */
	public Integer addSchema(Schema schema) throws RemoteException
	{
		Integer schemaID = (Integer)callMethod("addSchema",new Object[] {schema});
		return schemaID==0 ? null : schemaID;
	}

	/** Extends the specified schema in the web service */
	public Schema extendSchema(Integer schemaID) throws RemoteException
		{ return (Schema)callMethod("extendSchema",new Object[] {schemaID}); }

	/** Updates the specified schema in the web service */
	public boolean updateSchema(Schema schema) throws RemoteException
		{ return (Boolean)callMethod("updateSchema",new Object[] {schema}); }

	/** Unlocks the specified schema in the web service */
	public boolean unlockSchema(Integer schemaID) throws RemoteException
		{ return (Boolean)callMethod("unlockSchema",new Object[] {schemaID}); }

	/** Locks the specified schema in the web service */
	public boolean lockSchema(Integer schemaID) throws RemoteException
		{ return (Boolean)callMethod("lockSchema",new Object[] {schemaID}); }

	/** Indicates that the schema is able to be deleted from the web service */
	public boolean isDeletable(Integer schemaID) throws RemoteException
		{ return (Boolean)callMethod("isDeletable",new Object[] {schemaID}); }

	/** Returns the list of deletable schemas from the web service */
	public ArrayList<Integer> getDeletableSchemas() throws RemoteException
	{
		ArrayList<Integer> deletableSchemas = new ArrayList<Integer>();
		int[] deletableSchemaArray = (int[])callMethod("getDeletableSchemas",new Object[] {});
		if(deletableSchemaArray!=null)
			for(Integer deletableSchema : deletableSchemaArray)
				deletableSchemas.add(deletableSchema);
		return deletableSchemas;
	}

	/** Delete the specified schema from the web service */
	public boolean deleteSchema(Integer schemaID) throws RemoteException
		{ return (Boolean)callMethod("deleteSchema",new Object[] {schemaID}); }

	//------------------------------
	// Handles thesaurus operations
	//------------------------------

	/** Web service to retrieve the list of thesauri */
	public ArrayList<Thesaurus> getThesauri() throws RemoteException
	{
		Thesaurus[] thesauri = (Thesaurus[])callMethod("getThesauri",new Object[] {});
		return thesauri==null ? new ArrayList<Thesaurus>() : new ArrayList<Thesaurus>(Arrays.asList(thesauri));
	}

	/** Web service to add a thesaurus */
	public int addThesaurus(Thesaurus thesaurus) throws RemoteException
	{
		Integer thesaurusID = (Integer)callMethod("addThesaurus",new Object[] {thesaurus});
		return thesaurusID==0 ? null : thesaurusID;
	}

	/** Web service to update the specified thesaurus */
	public boolean updateThesaurus(Thesaurus thesaurus) throws RemoteException
		{ return (Boolean)callMethod("updateThesaurus",new Object[] {thesaurus}); }
	
	/** Web service to delete the specified thesaurus */
	public boolean deleteThesaurus(int thesaurusID) throws RemoteException
		{ return (Boolean)callMethod("deleteThesaurus",new Object[] {thesaurusID}); }
	
	//----------------------
	// Schema Tag Functions
	//----------------------

	/** Get the list of tags from the web service */
	public ArrayList<Tag> getTags() throws RemoteException
	{
		Tag[] tags = (Tag[])callMethod("getTags",new Object[] {});
		return tags==null ? new ArrayList<Tag>() : new ArrayList<Tag>(Arrays.asList(tags));
	}

	/** Gets the specified tag from the web service */
	public Tag getTag(Integer tagID) throws RemoteException
		{ return (Tag)callMethod("getTag",new Object[] {tagID}); }

	/** Get the list of sub-categories for the specified tag from the web service */
	public ArrayList<Tag> getSubcategories(Integer tagID) throws RemoteException
	{
		Tag[] tags = (Tag[])callMethod("getSubcategories",new Object[] {tagID==null ? 0 : tagID});
		return tags==null ? new ArrayList<Tag>() : new ArrayList<Tag>(Arrays.asList(tags));
	}

	/** Add a tag to the web service */
	public Integer addTag(Tag tag) throws RemoteException
	{
		Integer tagID = (Integer)callMethod("addTag",new Object[] {tag});
		return tagID==0 ? null : tagID;
	}

	/** Update a tag in the web service */
	public boolean updateTag(Tag tag) throws RemoteException
		{ return (Boolean)callMethod("updateTag",new Object[] {tag}); }

	/** Delete a tag from the web service */
	public boolean deleteTag(Integer tagID) throws RemoteException
		{ return (Boolean)callMethod("deleteTag",new Object[] {tagID}); }

	/** Get list of schemas associated with tag in web service */
	public ArrayList<Integer> getTagSchemas(Integer tagID) throws RemoteException
	{
		ArrayList<Integer> tagSchemas = new ArrayList<Integer>();
		int[] tagSchemaArray = (int[])callMethod("getTagSchemas",new Object[] {tagID});
		if(tagSchemaArray!=null)
			for(Integer tagSchema : tagSchemaArray)
				tagSchemas.add(tagSchema);
		return tagSchemas;
	}

	/** Get list of tags associated with schema in the web service */
	public ArrayList<Integer> getSchemaTags(Integer schemaID) throws RemoteException
	{
		ArrayList<Integer> schemaTags = new ArrayList<Integer>();
		int[] schemaTagArray = (int[])callMethod("getSchemaTags",new Object[] {schemaID});
		if(schemaTagArray!=null)
			for(Integer schemaTag : schemaTagArray)
				schemaTags.add(schemaTag);
		return schemaTags;
	}

	/** Add a tag to a schema in the web service */
	public boolean addTagToSchema(Integer schemaID, Integer tagID) throws RemoteException
		{ return (Boolean)callMethod("addTagToSchema",new Object[] {schemaID,tagID}); }

	/** Remove a tag from a schema in the web service */
	public boolean removeTagFromSchema(Integer schemaID, Integer tagID) throws RemoteException
		{ return (Boolean)callMethod("removeTagFromSchema",new Object[] {schemaID,tagID}); }

	//-------------------------------
	// Schema Relationship Functions
	//-------------------------------

	/** Gets the list of parent schemas for the specified schema from the web service */
	public ArrayList<Integer> getParentSchemas(Integer schemaID) throws RemoteException
	{
		ArrayList<Integer> parentSchemas = new ArrayList<Integer>();
		int[] parentSchemaArray = (int[])callMethod("getParentSchemas",new Object[] {schemaID});
		if(parentSchemaArray!=null)
			for(Integer parentSchema : parentSchemaArray)
				parentSchemas.add(parentSchema);
		return parentSchemas;
	}

	/** Gets the list of child schemas for the specified schema from the web service */
	public ArrayList<Integer> getChildSchemas(Integer schemaID) throws RemoteException
	{
		ArrayList<Integer> childSchemas = new ArrayList<Integer>();
		int[] childSchemaArray = (int[])callMethod("getChildSchemas",new Object[] {schemaID});
		if(childSchemaArray!=null)
			for(Integer childSchema : childSchemaArray)
				childSchemas.add(childSchema);
		return childSchemas;
	}

	/** Gets the list of ancestor schemas for the specified schema from the web service */
	public ArrayList<Integer> getAncestorSchemas(Integer schemaID) throws RemoteException
	{
		ArrayList<Integer> ancestorSchemas = new ArrayList<Integer>();
		int[] ancestorSchemaArray = (int[])callMethod("getAncestorSchemas",new Object[] {schemaID});
		if(ancestorSchemaArray!=null)
			for(Integer ancestorSchema : ancestorSchemaArray)
				ancestorSchemas.add(ancestorSchema);
		return ancestorSchemas;
	}

	/** Gets the list of descendant schemas for the specified schema from the web service */
	public ArrayList<Integer> getDescendantSchemas(Integer schemaID) throws RemoteException
	{
		ArrayList<Integer> decendantSchemas = new ArrayList<Integer>();
		int[] decendantSchemaArray = (int[])callMethod("getDescendantSchemas",new Object[] {schemaID});
		if(decendantSchemaArray!=null)
			for(Integer decendantSchema : decendantSchemaArray)
				decendantSchemas.add(decendantSchema);
		return decendantSchemas;
	}

	/** Gets the list of schemas associated with the specified schema from the web service */
	public ArrayList<Integer> getAssociatedSchemas(Integer schemaID) throws RemoteException
	{
		ArrayList<Integer> associatedSchemas = new ArrayList<Integer>();
		int[] associatedSchemaArray = (int[])callMethod("getAssociatedSchemas",new Object[] {schemaID});
		if(associatedSchemaArray!=null)
			for(Integer associatedSchema : associatedSchemaArray)
				associatedSchemas.add(associatedSchema);
		return associatedSchemas;
	}

	/** Gets the root schema for the two specified schemas from the web service */
	public Integer getRootSchema(Integer schema1ID, Integer schema2ID) throws RemoteException
		{ return (Integer)callMethod("getRootSchema",new Object[] {schema1ID, schema2ID}); }

	/** Gets the schema path between the specified root and schema from the web service */
	public ArrayList<Integer> getSchemaPath(Integer rootID, Integer schemaID) throws RemoteException
	{
		ArrayList<Integer> schemaPath = new ArrayList<Integer>();
		int[] schemaPathArray = (int[])callMethod("getSchemaPath",new Object[] {rootID,schemaID});
		if(schemaPathArray!=null)
			for(Integer schemaPathItem : schemaPathArray)
				schemaPath.add(schemaPathItem);
		return schemaPath;
	}

	/** Sets the parent schemas for the specified schema from the web service */
	public boolean setParentSchemas(Integer schemaID, ArrayList<Integer> parentIDs) throws RemoteException
	{
		int[] parentIDArray = new int[parentIDs.size()];
		for(int i=0; i<parentIDs.size(); i++)
			parentIDArray[i] = parentIDs.get(i);
		return (Boolean)callMethod("setParentSchemas",new Object[] {schemaID,parentIDArray});
	}

	//--------------------------
	// Schema Element Functions
	//--------------------------

	/** Adds the specified schema element to the web service */
	public Integer addSchemaElement(SchemaElement schemaElement) throws RemoteException
	{
		Integer schemaElementID = 0;
		if(schemaElement instanceof Entity) schemaElementID = (Integer)callMethod("addEntity",new Object[] {(Entity)schemaElement});
		else if(schemaElement instanceof Attribute) schemaElementID = (Integer)callMethod("addAttribute",new Object[] {(Attribute)schemaElement});
		else if(schemaElement instanceof Domain) schemaElementID = (Integer)callMethod("addDomain",new Object[] {(Domain)schemaElement});
		else if(schemaElement instanceof DomainValue) schemaElementID = (Integer)callMethod("addDomainValue",new Object[] {(DomainValue)schemaElement});
		else if(schemaElement instanceof Relationship) schemaElementID = (Integer)callMethod("addRelationship",new Object[] {(Relationship)schemaElement});
		else if(schemaElement instanceof Containment) schemaElementID = (Integer)callMethod("addContainment",new Object[] {(Containment)schemaElement});
		else if(schemaElement instanceof Subtype) schemaElementID = (Integer)callMethod("addSubtype",new Object[] {(Subtype)schemaElement});
		else if(schemaElement instanceof Alias) schemaElementID = (Integer)callMethod("addAlias",new Object[] {(Alias)schemaElement});
		return schemaElementID==0 ? null : schemaElementID;
	}

	/** Updates the specified schema element on the web service */
	public Boolean updateSchemaElement(SchemaElement schemaElement) throws RemoteException
	{
		if(schemaElement instanceof Entity) return (Boolean)callMethod("updateEntity",new Object[] {(Entity)schemaElement});
		else if(schemaElement instanceof Attribute) return (Boolean)callMethod("updateAttribute",new Object[] {(Attribute)schemaElement});
		else if(schemaElement instanceof Domain) return (Boolean)callMethod("updateDomain",new Object[] {(Domain)schemaElement});
		else if(schemaElement instanceof DomainValue) return (Boolean)callMethod("updateDomainValue",new Object[] {(DomainValue)schemaElement});
		else if(schemaElement instanceof Relationship) return (Boolean)callMethod("updateRelationship",new Object[] {(Relationship)schemaElement});
		else if(schemaElement instanceof Containment) return (Boolean)callMethod("updateContainment",new Object[] {(Containment)schemaElement});
		else if(schemaElement instanceof Subtype) return (Boolean)callMethod("updateSubtype",new Object[] {(Subtype)schemaElement});
		else if(schemaElement instanceof Alias) return (Boolean)callMethod("updateAlias",new Object[] {(Alias)schemaElement});
		return null;
	}

	/** Deletes the specified schema element from the web service */
	public Boolean deleteSchemaElement(Integer schemaElementID) throws RemoteException
	{
		String type = (String)callMethod("getSchemaElementType",new Object[] {schemaElementID});
		if(type.equals("Entity")) return (Boolean)callMethod("deleteEntity",new Object[] {schemaElementID});
		else if(type.equals("Attribute")) return (Boolean)callMethod("deleteAttribute",new Object[] {schemaElementID});
		else if(type.equals("Domain")) return (Boolean)callMethod("deleteDomain",new Object[] {schemaElementID});
		else if(type.equals("DomainValue")) return (Boolean)callMethod("deleteDomainValue",new Object[] {schemaElementID});
		else if(type.equals("Relationship")) return (Boolean)callMethod("deleteRelationship",new Object[] {schemaElementID});
		else if(type.equals("Containment")) return (Boolean)callMethod("deleteContainment",new Object[] {schemaElementID});
		else if(type.equals("Subtype")) return (Boolean)callMethod("deleteSubtype",new Object[] {schemaElementID});
		else if(type.equals("Alias")) return (Boolean)callMethod("deleteAlias",new Object[] {schemaElementID});
		return null;
	}

	/** Retrieves the specified schema element from the web service */
	public SchemaElement getSchemaElement(Integer schemaElementID) throws RemoteException
	{
		String type = (String)callMethod("getSchemaElementType",new Object[] {schemaElementID});
		if(type.equals("Entity")) return (SchemaElement)callMethod("getEntity",new Object[] {schemaElementID});
		else if(type.equals("Attribute")) return (SchemaElement)callMethod("getAttribute",new Object[] {schemaElementID});
		else if(type.equals("Domain")) return (SchemaElement)callMethod("getDomain",new Object[] {schemaElementID});
		else if(type.equals("DomainValue")) return (SchemaElement)callMethod("getDomainValue",new Object[] {schemaElementID});
		else if(type.equals("Relationship")) return (SchemaElement)callMethod("getRelationship",new Object[] {schemaElementID});
		else if(type.equals("Containment")) return (SchemaElement)callMethod("getContainment",new Object[] {schemaElementID});
		else if(type.equals("Subtype")) return (SchemaElement)callMethod("getSubtype",new Object[] {schemaElementID});
		else if(type.equals("Alias")) return (SchemaElement)callMethod("getAlias",new Object[] {schemaElementID});
		return null;
	}

	/** Retrieves the schemas elements referencing the specified keyword */
	public ArrayList<SchemaElement> getSchemaElementsForKeyword(String keyword, ArrayList<Integer> tags) throws RemoteException
	{
		int tagList[] = new int[0];
		if(tags!=null)
		{
			tagList = new int[tags.size()];
			for(int i=0; i<tags.size(); i++) tagList[i] = tags.get(i);
		}
		SchemaElement[] schemaElements = ((SchemaElementList)callMethod("getSchemaElementsForKeyword",new Object[] {keyword, tagList})).geetSchemaElements();
		return schemaElements==null ? new ArrayList<SchemaElement>() : new ArrayList<SchemaElement>(Arrays.asList(schemaElements));
	}

	/** Retrieves the number of schema elements for the specified schema from the web service */
	public Integer getSchemaElementCount(Integer schemaID) throws RemoteException
		{ return (Integer)callMethod("getSchemaElementCount",new Object[] {schemaID}); }

	/** Retrieves the schema info for the specified schema from the web service */
	public SchemaInfo getSchemaInfo(Integer schemaID) throws RemoteException
	{
		SchemaElement[] schemaElements = ((SchemaElementList)callMethod("getSchemaElements",new Object[] {schemaID})).geetSchemaElements();
		ArrayList<SchemaElement> elements = schemaElements==null ? new ArrayList<SchemaElement>() : new ArrayList<SchemaElement>(Arrays.asList(schemaElements));
		return new SchemaInfo(getSchema(schemaID),getParentSchemas(schemaID),elements);
	}

	//-----------------------
	// GraphData Source Functions
	//-----------------------

	/** Gets the list of data sources from the web service */
	public ArrayList<DataSource> getDataSources(Integer schemaID) throws RemoteException
	{
		DataSource[] dataSources = (schemaID==null ? (DataSource[])callMethod("getAllDataSources",new Object[] {}) : (DataSource[])callMethod("getDataSources",new Object[] {schemaID}));
		return dataSources==null ? new ArrayList<DataSource>() : new ArrayList<DataSource>(Arrays.asList(dataSources));
	}

	/** Gets the specified data source from the web service */
	public DataSource getDataSource(Integer dataSourceID) throws RemoteException
		{ return (DataSource)callMethod("getDataSource",new Object[] {dataSourceID}); }

	/** Adds the specified data source to the web service */
	public Integer addDataSource(DataSource dataSource) throws RemoteException
	{
		Integer dataSourceID = (Integer)callMethod("addDataSource",new Object[] {dataSource});
		return dataSourceID==0 ? null : dataSourceID;
	}

	/** Updates the specified data source in the web service */
	public boolean updateDataSource(DataSource dataSource) throws RemoteException
		{ return (Boolean)callMethod("updateDataSource",new Object[] {dataSource}); }

	/** Deletes the specified data source from the web service */
	public boolean deleteDataSource(Integer dataSourceID) throws RemoteException
		{ return (Boolean)callMethod("deleteDataSource",new Object[] {dataSourceID}); }

	//--------------------
	// Function Functions
	//--------------------

	/** Web service to retrieve the list of data types */
	public ArrayList<DataType> getDataTypes() throws RemoteException
	{
		DataType[] dataTypes = (DataType[])callMethod("getDataTypes",new Object[] {});
		return dataTypes==null ? new ArrayList<DataType>() : new ArrayList<DataType>(Arrays.asList(dataTypes));
	}
	
	/** Gets the list of functions from the web service */
	public ArrayList<Function> getFunctions() throws RemoteException
	{
		Function[] functions = (Function[])callMethod("getFunctions",new Object[] {});
		return functions==null ? new ArrayList<Function>() : new ArrayList<Function>(Arrays.asList(functions));
	}
	
	/** Gets the specified function from the web service */
	public Function getFunction(Integer functionID) throws RemoteException
		{ return (Function)callMethod("getFunction",new Object[] {functionID}); }

	/** Gets the functions referenced by the specified function from the web service */
	public ArrayList<Function> getReferencedFunctions(Integer functionID) throws RemoteException
	{
		Function[] functions = (Function[])callMethod("getReferencedFunctions",new Object[] {functionID});
		return functions==null ? new ArrayList<Function>() : new ArrayList<Function>(Arrays.asList(functions));
	}
	
	/** Adds the specified function to the web service */
	public Integer addFunction(Function function) throws RemoteException
	{
		Integer functionID = (Integer)callMethod("addFunction",new Object[] {function});
		return functionID==0 ? null : functionID;
	}

	/** Returns the list of deletable functions from the web service */
	public ArrayList<Integer> getDeletableFunctions() throws RemoteException
	{
		ArrayList<Integer> deletableFunctions = new ArrayList<Integer>();
		int[] deletableFunctionArray = (int[])callMethod("getDeletableFunctions",new Object[] {});
		if(deletableFunctionArray!=null)
			for(Integer deletableFunction : deletableFunctionArray)
				deletableFunctions.add(deletableFunction);
		return deletableFunctions;
	}

	/** Deletes the specified function from the web service */
	public boolean deleteFunction(Integer functionID) throws RemoteException
		{ return (Boolean)callMethod("deleteFunction",new Object[] {functionID}); }

	/** Gets the list of function implementations from the web service */
	public ArrayList<FunctionImp> getFunctionImps() throws RemoteException
	{
		FunctionImp[] functionImps = (FunctionImp[])callMethod("getAllFunctionImps",new Object[] {});
		return functionImps==null ? new ArrayList<FunctionImp>() : new ArrayList<FunctionImp>(Arrays.asList(functionImps));
	}

	/** Gets the list of function implementations for the specified function from the web service */
	public ArrayList<FunctionImp> getFunctionImps(Integer functionID) throws RemoteException
	{
		FunctionImp[] functionImps = (FunctionImp[])callMethod("getFunctionImps",new Object[] {functionID});
		return functionImps==null ? new ArrayList<FunctionImp>() : new ArrayList<FunctionImp>(Arrays.asList(functionImps));
	}
	
	/** Sets the specified function implementation in the web service */
	public boolean setFunctionImp(FunctionImp functionImp) throws RemoteException
		{ return (Boolean)callMethod("setFunctionImp",new Object[] {functionImp}); }

	/** Deletes the specified function implementation from the web service */
	public boolean deleteFunctionImp(FunctionImp functionImp) throws RemoteException
		{ return (Boolean)callMethod("deleteFunctionImp",new Object[] {functionImp}); }
	
	//-------------------
	// Project Functions
	//-------------------

	/** Gets the list of projects from the web service */
	public ArrayList<Project> getProjects() throws RemoteException
	{
		Project[] projects = (Project[])callMethod("getProjects",new Object[] {});
		return projects==null ? new ArrayList<Project>() : new ArrayList<Project>(Arrays.asList(projects));
	}

	/** Gets the specified project from the web service */
	public Project getProject(Integer projectID) throws RemoteException
		{ return (Project)callMethod("getProject",new Object[] {projectID}); }

	/** Adds the specified project to the web service */
	public Integer addProject(Project project) throws RemoteException
	{
		Integer projectID = (Integer)callMethod("addProject",new Object[] {project});
		return projectID==0 ? null : projectID;
	}

	/** Updates the specified project in the web service */
	public boolean updateProject(Project project) throws RemoteException
		{ return (Boolean)callMethod("updateProject",new Object[] {project}); }

	/** Deletes the specified project from the web service */
	public boolean deleteProject(Integer projectID) throws RemoteException
		{ return (Boolean)callMethod("deleteProject",new Object[] {projectID}); }

	/** Gets the list of mappings for the specified project from the web service */
	public ArrayList<Mapping> getMappings(Integer projectID) throws RemoteException
	{
		Mapping[] mappings = (Mapping[])callMethod("getMappings",new Object[] {projectID});
		return mappings==null ? new ArrayList<Mapping>() : new ArrayList<Mapping>(Arrays.asList(mappings));
	}

	/** Gets the specified mapping from the web service */
	public Mapping getMapping(Integer mappingID) throws RemoteException
		{ return (Mapping)callMethod("getMapping",new Object[] {mappingID}); }

	/** Adds the specified mapping to the web service */
	public Integer addMapping(Mapping mapping) throws RemoteException
	{
		Integer mappingID = (Integer)callMethod("addMapping",new Object[] {mapping});
		return mappingID==0 ? null : mappingID;
	}

	/** Deletes the specified mapping from the web service */
	public boolean deleteMapping(Integer mappingID) throws RemoteException
		{ return (Boolean)callMethod("deleteMapping",new Object[] {mappingID}); }

	/** Adds the specified mapping cell to the web service */
	public Integer addMappingCells(ArrayList<MappingCell> mappingCells) throws RemoteException
	{
		Integer mappingCellID = (Integer)callMethod("addMappingCells",new Object[] {mappingCells.toArray(new MappingCell[0])});
		return mappingCellID==0 ? null : mappingCellID;
	}

	/** Updates the specified mapping cell in the web service */
	public boolean updateMappingCells(ArrayList<MappingCell> mappingCells) throws RemoteException
		{ return (Boolean)callMethod("updateMappingCells",new Object[] {mappingCells.toArray(new MappingCell[0])}); }

	/** Deletes the specified mapping from the web service */
	public boolean deleteMappingCells(ArrayList<Integer> mappingCellIDs) throws RemoteException
	{
		int idList[] = new int[0];
		if(mappingCellIDs!=null)
		{
			idList = new int[mappingCellIDs.size()];
			for(int i=0; i<mappingCellIDs.size(); i++) idList[i] = mappingCellIDs.get(i);
		}
		return (Boolean)callMethod("deleteMappingCells",new Object[] {idList});
	}

	/** Gets the list of mapping cells for the specified mapping from the web service */
	public ArrayList<MappingCell> getMappingCells(Integer mappingID) throws RemoteException
	{
		MappingCell[] mappingCells = (MappingCell[])callMethod("getMappingCells",new Object[] {mappingID});
		return mappingCells==null ? new ArrayList<MappingCell>() : new ArrayList<MappingCell>(Arrays.asList(mappingCells));
	}

	/** Gets the list of mapping cells containing the at least one of the specified elements and the specified score */
	public ArrayList<MappingCell> getMappingCellsByElement(int projectID, ArrayList<AssociatedElement> elements, double minScore) throws RemoteException
	{
		MappingCell[] mappingCells = (MappingCell[])callMethod("getMappingCellsByElement",new Object[] {projectID, elements.toArray(new AssociatedElement[0]), minScore});
		return mappingCells==null ? new ArrayList<MappingCell>() : new ArrayList<MappingCell>(Arrays.asList(mappingCells));
	}

	/** Gets the list of mapping cells connecting only the specified elements in the specified project */
	public ArrayList<MappingCell> getAssociatedMappingCells(int projectID, ArrayList<AssociatedElement> elements) throws RemoteException
	{
		MappingCell[] mappingCells = (MappingCell[])callMethod("getAssociatedMappingCells",new Object[] {projectID, elements.toArray(new AssociatedElement[0])});
		return mappingCells==null ? new ArrayList<MappingCell>() : new ArrayList<MappingCell>(Arrays.asList(mappingCells));
	}
	
	/** Indicates if the specified project has a vocabulary from the web service */
	public boolean hasVocabulary(Integer projectID) throws RemoteException
		{ return (Boolean)callMethod("hasVocabulary",new Object[] {projectID}); }
	
	//----------------------
	// Annotation Functions
	//----------------------

	/** Sets the annotation for the specified element and attribute */
	public boolean setAnnotation(Integer elementID, Integer groupID, String attribute, String value) throws RemoteException
		{ return (Boolean)callMethod("setAnnotation",new Object[] {elementID,groupID==null?0:groupID,attribute,value}); }

	/** Sets the specified list of annotations */
	public boolean setAnnotations(ArrayList<Annotation> annotations) throws RemoteException
		{ return (Boolean)callMethod("setAnnotations",new Object[] {annotations.toArray(new Annotation[0])}); }

	/** Gets the annotation for the specified element and attribute */
	public String getAnnotation(Integer elementID, Integer groupID, String attribute) throws RemoteException
		{ return (String)callMethod("getAnnotation",new Object[] {elementID,groupID==null?0:groupID,attribute}); }

	/** Get the requested annotations */
	public ArrayList<Annotation> getAnnotations(Integer elementID, String attribute) throws RemoteException
	{
		Annotation[] annotations = (Annotation[])callMethod("getAnnotations",new Object[] {elementID,attribute});
		return annotations==null ? new ArrayList<Annotation>() : new ArrayList<Annotation>(Arrays.asList(annotations));
	}
	
	/** Get the requested annotations by group */
	public ArrayList<Annotation> getAnnotationsByGroup(Integer groupID, String attribute) throws RemoteException
	{
		Annotation[] annotations = (Annotation[])callMethod("getAnnotationsByGroup",new Object[] {groupID,attribute});
		return annotations==null ? new ArrayList<Annotation>() : new ArrayList<Annotation>(Arrays.asList(annotations));
	}

	/** Clears the annotation for the specified element and attribute */
	public boolean clearAnnotation(Integer elementID, Integer groupID, String attribute) throws RemoteException
		{ return (Boolean)callMethod("clearAnnotation",new Object[] {elementID,groupID==null?0:groupID,attribute}); }
	
	/** Clears the requested annotations by group */
	public boolean clearAnnotations(Integer groupID, String attribute) throws RemoteException
		{ return (Boolean)callMethod("clearAnnotations",new Object[] {groupID,attribute}); }
	
	//-------------------
	// Derived Functions
	//-------------------

	/** Imports the specified schema into the web services */
	public Integer importSchema(Schema schema, ArrayList<SchemaElement> schemaElements) throws RemoteException
	{
		// Adjust the schema elements to avoid non-ASCII characters and reference the proper base elements
		for(SchemaElement element : schemaElements)
		{
			element.setName(element.getName().replaceAll("[^\\p{ASCII}]","#"));
			element.setDescription(element.getDescription().replaceAll("[^\\p{ASCII}]","#"));
		}

		// Import the schema
		Integer schemaID = (Integer)callMethod("importSchema",new Object[] {schema, new SchemaElementList(schemaElements.toArray(new SchemaElement[0]))});
		return schemaID==0 ? null : schemaID;
	}

	/** Saves the mapping cells to the web service */
	public boolean saveMappingCells(Integer mappingID, ArrayList<MappingCell> mappingCells) throws RemoteException
		{ return (Boolean)callMethod("saveMappingCells",new Object[] {mappingID,mappingCells.toArray(new MappingCell[0])}); }

	/** Gets the specified vocabulary from the web service */
	public VocabularyTerms getVocabularyTerms(Integer projectID) throws RemoteException
		{ return (VocabularyTerms)callMethod("getVocabulary",new Object[] {projectID}); }
	
	/** Saves the vocabulary to the web service */
	public VocabularyTerms saveVocabularyTerms(VocabularyTerms vocabulary) throws RemoteException
		{ return (VocabularyTerms)callMethod("saveVocabulary",new Object[] {vocabulary}); }
	
	/** Deletes the specified vocabulary from the web service */
	public boolean deleteVocabulary(Integer projectID) throws RemoteException
		{ return (Boolean)callMethod("deleteVocabulary",new Object[] {projectID}); }

	/** Web service to retrieve a thesaurus */
	public ThesaurusTerms getThesaurusTerms(int thesaurusID) throws RemoteException
		{ return (ThesaurusTerms)callMethod("getThesaurusTerms",new Object[] {thesaurusID}); }
	
	/** Web service to save the thesaurus */
	public boolean saveThesaurusTerms(ThesaurusTerms terms) throws RemoteException
		{ return (Boolean)callMethod("saveThesaurusTerms",new Object[] {terms}); }
}