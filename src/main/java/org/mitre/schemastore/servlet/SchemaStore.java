// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.servlet;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

import org.mitre.schemastore.data.DataManager;
import org.mitre.schemastore.data.database.Database;
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
import org.mitre.schemastore.model.Synonym;
import org.mitre.schemastore.model.Tag;
import org.mitre.schemastore.model.Thesaurus;
import org.mitre.schemastore.model.terms.AssociatedElement;
import org.mitre.schemastore.model.terms.ThesaurusTerms;
import org.mitre.schemastore.model.terms.VocabularyTerms;

/**
 * Web service for retrieving schema information from SchemaStore
 * @author CWOLF
 */
public class SchemaStore
{
	/** Stores a static reference to the database connection for schema store run as a web service */
	static private DatabaseConnection connection = null;
	
	/** Stores the data manager */
	private DataManager manager;
	
	/** Converts Integer array into int array */
	private int[] convertArray(ArrayList<Integer> oldArray)
	{
		int[] array = new int[oldArray.size()];
		for(int i=0; i<oldArray.size(); i++)
			array[i] = oldArray.get(i);
		return array;
	}
	
	/** Converts int array into Integer array */
	private ArrayList<Integer> convertArray(int oldArray[])
	{
		ArrayList<Integer> array = new ArrayList<Integer>();
		if(oldArray!=null)
			for(int i=0; i<oldArray.length; i++)
				array.add(oldArray[i]);
		return array;
	}
	
	/** Gets the manager */
	private DataManager getManager()
	{
		if(manager==null) manager= new DataManager(connection);
		return manager;
	}
	
	/** Constructs SchemaStore */
	public SchemaStore()
		{ if(connection==null) connection = new DatabaseConnection(); }
	
	/** Constructs SchemaStore to specified database */
	public SchemaStore(Integer type, String uri, String database, String user, String password)
		{ connection = new DatabaseConnection(type,uri,database,user,password); }
	
	/** Indicates if SchemaStore is connected to a database */
	public boolean isConnected()
		{ return new Database(connection).isConnected(); }
	
	/** Handles the compression of the database */
	public boolean compress()
		{ return new Database(connection).compress(); }
	
	//---------------------------
	// Handles schema operations 
	//---------------------------
	
	/** Web service to retrieve the list of schemas */
	public Schema[] getSchemas()
		{ return getManager().getSchemaCache().getSchemas(Schema.class).toArray(new Schema[0]); }
	
	/** Web service to retrieve the specified schema */
	public Schema getSchema(int schemaID)
		{ return getManager().getSchemaCache().getSchema(schemaID); }
	
	/** Web service to add the specified schema */
	public int addSchema(Schema schema)
		{ return getManager().getSchemaCache().addSchema(schema); }
	
	/** Web service to extend the specified schema */
	public Schema extendSchema(int schemaID)
		{ return getManager().getSchemaCache().extendSchema(schemaID); }

	/** Web service to update the specified schema */
	public boolean updateSchema(Schema schema)
		{ return getManager().getSchemaCache().updateSchema(schema); }

	/** Web service to unlock the specified schema */
	public boolean unlockSchema(int schemaID)
		{ return getManager().getSchemaCache().unlockSchema(schemaID); }
	
	/** Web service to lock the specified schema */
	public boolean lockSchema(int schemaID)
		{ return getManager().getSchemaCache().lockSchema(schemaID); }

	/** Web service to indicate if the schema can be deleted */
	public boolean isDeletable(int schemaID)
		{ return getManager().getSchemaCache().isDeletable(schemaID); }

	/** Web service to get the deletable schemas */
	public int[] getDeletableSchemas()
		{ return convertArray(getManager().getSchemaCache().getDeletableSchemas()); }
	
	/** Web service to delete the specified schema */
	public boolean deleteSchema(int schemaID)
		{ return getManager().getSchemaCache().deleteSchema(schemaID); }
	
	//------------------------------
	// Handles thesaurus operations
	//------------------------------

	/** Web service to retrieve the list of thesauri */
	public Thesaurus[] getThesauri()
	{
		ArrayList<Thesaurus> thesauri = new ArrayList<Thesaurus>();
		for(Schema schema : getManager().getSchemaCache().getSchemas(Thesaurus.class))
			thesauri.add(new Thesaurus(schema.getId(),schema.getName(),schema.getDescription()));
		return thesauri.toArray(new Thesaurus[0]);
	}

	/** Web service to add a thesaurus */
	public int addThesaurus(Thesaurus thesaurus)
		{ return getManager().getSchemaCache().addSchema(thesaurus); }

	/** Web service to update the specified thesaurus */
	public boolean updateThesaurus(Thesaurus thesaurus)
		{ return getManager().getSchemaCache().updateSchema(thesaurus); }
	
	/** Web service to delete the specified thesaurus */
	public boolean deleteThesaurus(int thesaurusID)
		{ return getManager().getSchemaCache().deleteSchema(thesaurusID); }	
	
	//-------------------------------
	// Handles schema tag operations
	//-------------------------------

	/** Web service to get the list of schema tags */
	public Tag[] getTags()
		{ return getManager().getTagCache().getTags().toArray(new Tag[0]); }

	/** Web service to get the specified tag */
	public Tag getTag(int tagID)
		{ return getManager().getTagCache().getTag(tagID); }

	/** Web service to get the sub-categories for the specified tag */
	public Tag[] getSubcategories(int tagID)
		{ return getManager().getTagCache().getSubcategories(tagID==0 ? null : tagID).toArray(new Tag[0]); }
	
	/** Web service to add a tag */
	public int addTag(Tag tag)
		{ return getManager().getTagCache().addTag(tag); }

	/** Web service to update a tag */
	public boolean updateTag(Tag tag)
		{ return getManager().getTagCache().updateTag(tag); }
	
	/** Web service to delete a tag */
	public boolean deleteTag(int tagID)
		{ return getManager().getTagCache().deleteTag(tagID); }
	
	/** Web service to get list of schemas associated with tag */
	public int[] getTagSchemas(int tagID)
		{ return convertArray(getManager().getTagCache().getTagSchemas(tagID)); }	
	
	/** Web service to get list of tags associated with schema */
	public int[] getSchemaTags(int schemaID)
		{ return convertArray(getManager().getTagCache().getSchemaTags(schemaID)); }
		
	/** Web service to add a tag to a schema */
	public boolean addTagToSchema(int schemaID, int tagID)
		{ return getManager().getTagCache().addTagToSchema(schemaID, tagID); }
	
	/** Web service to remove a tag from a schema */
	public boolean removeTagFromSchema(int schemaID, int tagID)
		{ return getManager().getTagCache().removeTagFromSchema(schemaID, tagID); }
	
	//----------------------------------------
	// Handles schema relationship operations
	//----------------------------------------
	
	/** Web service to get the parent schemas for the specified schema */
	public int[] getParentSchemas(int schemaID)
		{ return convertArray(getManager().getSchemaRelationshipCache().getParents(schemaID)); }
	
	/** Web service to get the child schemas for the specified schema */
	public int[] getChildSchemas(int schemaID)
		{ return convertArray(getManager().getSchemaRelationshipCache().getChildren(schemaID)); }

	/** Web service to get the ancestor schemas for the specified schema */
	public int[] getAncestorSchemas(int schemaID)
		{ return convertArray(getManager().getSchemaRelationshipCache().getAncestors(schemaID)); }

	/** Web service to get the descendant schemas for the specified schema */
	public int[] getDescendantSchemas(int schemaID)
		{ return convertArray(getManager().getSchemaRelationshipCache().getDescendants(schemaID)); }

	/** Web service to get the associated schemas of the specified schema */
	public int[] getAssociatedSchemas(int schemaID)
		{ return convertArray(getManager().getSchemaRelationshipCache().getAssociatedSchemas(schemaID)); }

	/** Web service to get the root schema of the two specified schemas */
	public int getRootSchema(int schema1ID, int schema2ID)
		{ return getManager().getSchemaRelationshipCache().getRootSchema(schema1ID, schema2ID); }	
	
	/** Web service to get the schema path between the specified root and schema */
	public int[] getSchemaPath(int rootID, int schemaID)
		{ return convertArray(getManager().getSchemaRelationshipCache().getSchemaPath(rootID, schemaID)); }	
	
	/** Web service to set the parent schemas for the specified schema */
	public boolean setParentSchemas(int schemaID, int[] parentIDs)
	{
		ArrayList<Integer> parentIDArray = new ArrayList<Integer>();
		if(parentIDs!=null) parentIDArray = convertArray(parentIDs);
		return getManager().getSchemaRelationshipCache().setParents(schemaID,parentIDArray);
	}
	
	//-----------------------------------
	// Handles Schema Element Operations
	//-----------------------------------

	/** Web service to add the specified entity */
	public int addEntity(Entity entity)
		{ return getManager().getSchemaElementCache().addSchemaElement(entity); }

	/** Web service to add the specified attribute */
	public int addAttribute(Attribute attribute)
		{ return getManager().getSchemaElementCache().addSchemaElement(attribute); }

	/** Web service to add the specified domain */
	public int addDomain(Domain domain)
		{ return getManager().getSchemaElementCache().addSchemaElement(domain); }

	/** Web service to add the specified domain value */
	public int addDomainValue(DomainValue domainValue)
		{ return getManager().getSchemaElementCache().addSchemaElement(domainValue); }

	/** Web service to add the specified relationship */
	public int addRelationship(Relationship relationship)
		{ return getManager().getSchemaElementCache().addSchemaElement(relationship); }

	/** Web service to add the specified containment */
	public int addContainment(Containment containment)
		{ return getManager().getSchemaElementCache().addSchemaElement(containment); }

	/** Web service to add the specified subtype */
	public int addSubtype(Subtype subtype)
		{ return getManager().getSchemaElementCache().addSchemaElement(subtype); }

	/** Web service to add the specified synonym */
	public int addSynonym(Synonym synonym)
		{ return getManager().getSchemaElementCache().addSchemaElement(synonym); }

	/** Web service to add the specified alias */
	public int addAlias(Alias alias)
		{ return getManager().getSchemaElementCache().addSchemaElement(alias); }
	
	/** Web service to update the specified entity */
	public boolean updateEntity(Entity entity)
		{ return getManager().getSchemaElementCache().updateSchemaElement(entity); }

	/** Web service to update the specified attribute */
	public boolean updateAttribute(Attribute attribute)
		{ return getManager().getSchemaElementCache().updateSchemaElement(attribute); }

	/** Web service to update the specified domain */
	public boolean updateDomain(Domain domain)
		{ return getManager().getSchemaElementCache().updateSchemaElement(domain); }

	/** Web service to update the specified domain value */
	public boolean updateDomainValue(DomainValue domainValue)
		{ return getManager().getSchemaElementCache().updateSchemaElement(domainValue); }

	/** Web service to update the specified relationship */
	public boolean updateRelationship(Relationship relationship)
		{ return getManager().getSchemaElementCache().updateSchemaElement(relationship); }

	/** Web service to update the specified containment */
	public boolean updateContainment(Containment containment)
		{ return getManager().getSchemaElementCache().updateSchemaElement(containment); }

	/** Web service to update the specified subtype */
	public boolean updateSubtype(Subtype subtype)
		{ return getManager().getSchemaElementCache().updateSchemaElement(subtype); }

	/** Web service to update the specified synonym */
	public boolean updateSynonym(Synonym synonym)
		{ return getManager().getSchemaElementCache().updateSchemaElement(synonym); }

	/** Web service to update the specified alias */
	public boolean updateAlias(Alias alias)
		{ return getManager().getSchemaElementCache().updateSchemaElement(alias); }
	
	/** Web service to delete the specified entity */
	public boolean deleteEntity(int entityID)
		{ return getManager().getSchemaElementCache().deleteSchemaElement(entityID); }

	/** Web service to delete the specified attribute */
	public boolean deleteAttribute(int attributeID)
		{ return getManager().getSchemaElementCache().deleteSchemaElement(attributeID); }

	/** Web service to delete the specified domain */
	public boolean deleteDomain(int domainID)
		{ return getManager().getSchemaElementCache().deleteSchemaElement(domainID); }

	/** Web service to delete the specified domain value */
	public boolean deleteDomainValue(int domainValueID)
		{ return getManager().getSchemaElementCache().deleteSchemaElement(domainValueID); }

	/** Web service to delete the specified relationship */
	public boolean deleteRelationship(int relationshipID)
		{ return getManager().getSchemaElementCache().deleteSchemaElement(relationshipID); }

	/** Web service to delete the specified containment */
	public boolean deleteContainment(int containmentID)
		{ return getManager().getSchemaElementCache().deleteSchemaElement(containmentID); }

	/** Web service to delete the specified subtype */
	public boolean deleteSubtype(int subtypeID)
		{ return getManager().getSchemaElementCache().deleteSchemaElement(subtypeID); }

	/** Web service to delete the specified synonym */
	public boolean deleteSynonym(int synonymID)
		{ return getManager().getSchemaElementCache().deleteSchemaElement(synonymID); }

	/** Web service to delete the specified alias */
	public boolean deleteAlias(int aliasID)
		{ return getManager().getSchemaElementCache().deleteSchemaElement(aliasID); }
	
	/** Web service to get the specified entity */
	public Entity getEntity(int entityID)
		{ return (Entity)getManager().getSchemaElementCache().getSchemaElement(entityID); }

	/** Web service to get the specified attribute */
	public Attribute getAttribute(int attributeID)
		{ return (Attribute)getManager().getSchemaElementCache().getSchemaElement(attributeID); }

	/** Web service to get the specified domain */
	public Domain getDomain(int domainID)
		{ return (Domain)getManager().getSchemaElementCache().getSchemaElement(domainID); }

	/** Web service to get the specified domain value */
	public DomainValue getDomainValue(int domainValueID)
		{ return (DomainValue)getManager().getSchemaElementCache().getSchemaElement(domainValueID); }
	
	/** Web service to get the specified relationship */
	public Relationship getRelationship(int relationshipID)
		{ return (Relationship)getManager().getSchemaElementCache().getSchemaElement(relationshipID); }
	
	/** Web service to get the specified containment */
	public Containment getContainment(int containmentID)
		{ return (Containment)getManager().getSchemaElementCache().getSchemaElement(containmentID); }
	
	/** Web service to get the specified subtype */
	public Subtype getSubtype(int subtypeID)
		{ return (Subtype)getManager().getSchemaElementCache().getSchemaElement(subtypeID); }
	
	/** Web service to get the specified synonym */
	public Synonym getSynonym(int synonymID)
		{ return (Synonym)getManager().getSchemaElementCache().getSchemaElement(synonymID); }

	/** Web service to get the specified alias */
	public Alias getAlias(int aliasID)
		{ return (Alias)getManager().getSchemaElementCache().getSchemaElement(aliasID); }
	
	/** Web service to get the number of schema elements associated with the specified schema */
	public int getSchemaElementCount(int schemaID)
		{ return getManager().getSchemaElementCache().getSchemaElementCount(schemaID); }
	
	/** Web service to get the schema elements for the specified schema */
	public SchemaElementList getSchemaElements(int schemaID)
		{ return new SchemaElementList(getManager().getSchemaElementCache().getSchemaElements(schemaID).toArray(new SchemaElement[0])); }

	/** Web service to get the schemas elements referencing the specified keyword */
	public SchemaElementList getSchemaElementsForKeyword(String keyword, int[] tagIDs)
	{
		ArrayList<Integer> tagList = convertArray(tagIDs);
		return new SchemaElementList(getManager().getSchemaElementCache().getSchemaElementsForKeyword(keyword,tagList).toArray(new SchemaElement[0]));
	}
	
	/** Web service to get the schema element type */
	public String getSchemaElementType(int schemaElementID)
		{ return getManager().getSchemaElementCache().getSchemaElementType(schemaElementID); }
	
	//--------------------------------
	// Handles GraphData Source Operations
	//--------------------------------
	
	/** Web service to get a list of all data sources */
	public DataSource[] getAllDataSources()
		{ return getManager().getDataSourceCache().getDataSources(null).toArray(new DataSource[0]); }
			
	/** Web service to get a list of data sources for the specified schema */
	public DataSource[] getDataSources(int schemaID)
		{ return getManager().getDataSourceCache().getDataSources(schemaID).toArray(new DataSource[0]); }

	/** Web service to get the specified data source */
	public DataSource getDataSource(int dataSourceID)
		{ return getManager().getDataSourceCache().getDataSource(dataSourceID); }

	/** Web service to add the specified data source */
	public int addDataSource(DataSource dataSource)
		{ return getManager().getDataSourceCache().addDataSource(dataSource); }

	/** Web service to update the specified data source */
	public boolean updateDataSource(DataSource dataSource)
		{ return getManager().getDataSourceCache().updateDataSource(dataSource); }

	/** Web service to delete the specified data source */
	public boolean deleteDataSource(int dataSourceID)
		{ return getManager().getDataSourceCache().deleteDataSource(dataSourceID); }
	
	//-----------------------------
	// Handles Function Operations
	//-----------------------------

	/** Web service to retrieve the list of data types */
	public DataType[] getDataTypes()
		{ return getManager().getFunctionCache().getDataTypes().toArray(new DataType[0]); }

	/** Web service to retrieve the list of functions */
	public Function[] getFunctions()
		{ return getManager().getFunctionCache().getFunctions().toArray(new Function[0]); }

	/** Web service to retrieve the specified function */
	public Function getFunction(int functionID)
		{ return getManager().getFunctionCache().getFunction(functionID); }

	/** Returns the functions referenced by the specified function */
	public Function[] getReferencedFunctions(int functionID)
		{ return getManager().getFunctionCache().getReferencedFunctions(functionID).toArray(new Function[0]); }
	
	/** Web service to add the specified function */
	public int addFunction(Function function)
		{ return getManager().getFunctionCache().addFunction(function); }
	
	/** Web service to get the deletable functions */
	public int[] getDeletableFunctions()
		{ return convertArray(getManager().getFunctionCache().getDeletableFunctions()); }
	
	/** Web service to delete the specified function */
	public boolean deleteFunction(int functionID)
		{ return getManager().getFunctionCache().deleteFunction(functionID); }
	
	/** Web service to retrieve the list of function implementations */
	public FunctionImp[] getAllFunctionImps()
		{ return getManager().getFunctionCache().getFunctionImps().toArray(new FunctionImp[0]); }
	
	/** Web service to retrieve the list of function implementations for the specified function */
	public FunctionImp[] getFunctionImps(int functionID)
		{ return getManager().getFunctionCache().getFunctionImps(functionID).toArray(new FunctionImp[0]); }
	
	/** Web service to set the specified function implementation */
	public boolean setFunctionImp(FunctionImp functionImp)
		{ return getManager().getFunctionCache().setFunctionImp(functionImp); }
	
	/** Web service to delete the specified function implementation */
	public boolean deleteFunctionImp(FunctionImp functionImp)
		{ return getManager().getFunctionCache().deleteFunctionImp(functionImp); }
	
	//----------------------------
	// Handles Project Operations
	//----------------------------

	/** Web service to retrieve the list of projects */
	public Project[] getProjects()
		{ return getManager().getProjectCache().getProjects().toArray(new Project[0]); }
		
	/** Web service to retrieve the specified project */
	public Project getProject(int projectID)
		{ return getManager().getProjectCache().getProject(projectID); }
	
	/** Web service to add the specified project */
	public int addProject(Project project)
		{ return getManager().getProjectCache().addProject(project); }

	/** Web service to update the specified project */
	public boolean updateProject(Project project)
		{ return getManager().getProjectCache().updateProject(project); }

	/** Web service to delete the specified project */
	public boolean deleteProject(int projectID)
		{ return getManager().getProjectCache().deleteProject(projectID); }

	/** Web service to retrieve the list of mappings for the specified project */
	public Mapping[] getMappings(int projectID)
		{ return getManager().getProjectCache().getMappings(projectID).toArray(new Mapping[0]); }
		
	/** Web service to retrieve the specified mapping */
	public Mapping getMapping(int mappingID)
		{ return getManager().getProjectCache().getMapping(mappingID); }
	
	/** Web service to add the specified mapping */
	public int addMapping(Mapping mapping)
		{ return getManager().getProjectCache().addMapping(mapping); }

	/** Web service to delete the specified mapping */
	public boolean deleteMapping(int mappingID)
		{ return getManager().getProjectCache().deleteMapping(mappingID); }
	
	/** Web service to add the specified mapping cell */
	public int addMappingCells(MappingCell[] mappingCells)
		{ return getManager().getProjectCache().addMappingCells(Arrays.asList(mappingCells)); }

	/** Web service to update the specified mapping cells */
	public boolean updateMappingCells(MappingCell[] mappingCells)
		{ return getManager().getProjectCache().updateMappingCells(Arrays.asList(mappingCells)); }
	
	/** Web service to delete the specified mapping cells */
	public boolean deleteMappingCells(int[] mappingCellIDs)
	{
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for(int mappingCellID : mappingCellIDs) ids.add(mappingCellID);
		return getManager().getProjectCache().deleteMappingCells(ids);
	}

	/** Web service to get the mapping cells for the specified mapping */
	public MappingCell[] getMappingCells(int mappingID)
		{ return getManager().getProjectCache().getMappingCells(mappingID).toArray(new MappingCell[0]); }

	/** Web service to get all mapping cells containing the specific element and having above the specified score */
	public MappingCell[] getMappingCellsByElement(int projectID, AssociatedElement elements[], double minScore)
		{ return getManager().getProjectCache().getMappingCellsByElement(projectID, Arrays.asList(elements), minScore).toArray(new MappingCell[0]); }

	/** Web service to get all mapping cells connecting the specified elements in the specified project */
	public MappingCell[] getAssociatedMappingCells(int projectID, AssociatedElement elements[])
		{ return getManager().getProjectCache().getAssociatedMappingCells(projectID, Arrays.asList(elements)).toArray(new MappingCell[0]); }
	
	/** Web service indicating if a project has a vocabulary */
	public boolean hasVocabulary(int projectID)
		{ return getManager().getProjectCache().getVocabularyID(projectID)!=null; }
	
	//-------------------------------
	// Handles Annotation Operations
	//-------------------------------

	/** Web service to set an annotation */
	public boolean setAnnotation(int elementID, int groupID, String attribute, String value)
		{ return getManager().getAnnotationCache().setAnnotation(elementID, groupID, attribute, value.equals("")?null:value); }

	/** Web service to set annotations */
	public boolean setAnnotations(Annotation[] annotations)
		{ return getManager().getAnnotationCache().setAnnotations(Arrays.asList(annotations)); }
	
	/** Web service to get an annotation for a specified group */
	public String getAnnotation(int elementID, int groupID, String attribute)
		{ return getManager().getAnnotationCache().getAnnotation(elementID, groupID, attribute); }
	
	/** Web service to get the requested annotations */
	public Annotation[] getAnnotations(int elementID, String attribute)
		{ return getManager().getAnnotationCache().getAnnotations(elementID, attribute).toArray(new Annotation[0]); }
	
	/** Web service to get the requested annotations */
	public Annotation[] getAnnotationsByGroup(int groupID, String attribute)
		{ return getManager().getAnnotationCache().getAnnotationsByGroup(groupID, attribute).toArray(new Annotation[0]); }

	/** Web service to clear an annotation */
	public boolean clearAnnotation(int elementID, int groupID, String attribute)
		{ return getManager().getAnnotationCache().clearAnnotation(elementID, groupID, attribute); }
	
	/** Web service to clear the specified group annotations */
	public boolean clearAnnotations(int groupID, String attribute)
		{ return getManager().getAnnotationCache().clearAnnotations(groupID, attribute); }
	
	//--------------------
	// Derived Operations
	//--------------------
	
	/** Web service to import schemas */
	public int importSchema(Schema schema, SchemaElementList schemaElementList) throws RemoteException
	{
		ArrayList<SchemaElement> elements = new ArrayList<SchemaElement>();
		for(SchemaElement element : Arrays.asList(schemaElementList.geetSchemaElements()))
			elements.add(element.copy());
		return ImportSchema.importSchema(this, getManager(), schema, elements);
	}
	
	/** Web service to save the mapping and mapping cells */
	public boolean saveMappingCells(int mappingID, MappingCell[] mappingCells) throws RemoteException
	{
		ArrayList<MappingCell> mappingCellArray = new ArrayList<MappingCell>();
		if(mappingCells!=null)
			for(MappingCell mappingCell : Arrays.asList(mappingCells))
				mappingCellArray.add(mappingCell.copy());
		return SaveMappingCells.saveMappingCells(getManager(),mappingID,mappingCellArray);
	}

	/** Web service to retrieve a project vocabulary */
	public VocabularyTerms getVocabulary(int projectID) throws RemoteException
		{ return GetVocabularyTerms.getVocabularyTerms(getManager(),projectID); }
	
	/** Web service to save the vocabulary */
	public VocabularyTerms saveVocabulary(VocabularyTerms vocabulary) throws RemoteException
		{ return SaveVocabularyTerms.saveVocabularyTerms(getManager(),vocabulary.copy()); }

	/** Web service to delete the vocabulary */
	public boolean deleteVocabulary(int projectID) throws RemoteException
		{ return DeleteVocabulary.deleteVocabulary(getManager(),projectID); }

	/** Web service to retrieve a thesaurus */
	public ThesaurusTerms getThesaurusTerms(int thesaurusID) throws RemoteException
		{ return GetThesaurusTerms.getThesaurusTerms(getManager(),thesaurusID); }
	
	/** Web service to save the thesaurus */
	public boolean saveThesaurusTerms(ThesaurusTerms terms) throws RemoteException
		{ return SaveThesaurusTerms.saveThesaurusTerms(getManager(),terms.copy()); }
}