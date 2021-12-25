// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.porters.schemaImporters;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;

import org.exolab.castor.xml.schema.Annotated;
import org.exolab.castor.xml.schema.Annotation;
import org.exolab.castor.xml.schema.AnyType;
import org.exolab.castor.xml.schema.AttributeDecl;
import org.exolab.castor.xml.schema.AttributeGroupReference;
import org.exolab.castor.xml.schema.ComplexType;
import org.exolab.castor.xml.schema.Documentation;
import org.exolab.castor.xml.schema.ElementDecl;
import org.exolab.castor.xml.schema.Facet;
import org.exolab.castor.xml.schema.Group;
import org.exolab.castor.xml.schema.Schema;
import org.exolab.castor.xml.schema.SimpleType;
import org.exolab.castor.xml.schema.Union;
import org.exolab.castor.xml.schema.Wildcard;
import org.exolab.castor.xml.schema.XMLType;
import org.exolab.castor.xml.schema.reader.SchemaReader;
import org.mitre.schemastore.client.Repository;
import org.mitre.schemastore.client.SchemaStoreClient;
import org.mitre.schemastore.model.Alias;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.model.Tag;
import org.mitre.schemastore.model.schemaInfo.SchemaInfo;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.ImporterException.ImporterExceptionType;
import org.mitre.schemastore.porters.URIType;


/**
 * XSDMergedImporter: Class for importing XSD files into the M3 Format
 * 
 * @author DBURDICK
 */

public class XSDImporterMerged extends SchemaImporter
{
	
	/** testing main **/ 
	public static void main(String[] args) throws URISyntaxException, ImporterException{
		XSDImporterMerged xsdImporter = new XSDImporterMerged();
		
		Repository repository = null;
		try {
			repository = new Repository(Repository.DERBY,new URI("C:/Temp/"), "org/mitre/schemastore","postgres","postgres");
		} catch (URISyntaxException e2) {
			e2.printStackTrace();
		}		
		try {
			xsdImporter.client = new SchemaStoreClient(repository);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
			
		// Initialize the importer
		xsdImporter.uri = new URI("C:/ecf-v4.0-spec/xsd/message/ECF-4.0-CoreFilingMessage.xsd");
		//xsdImporter.uri = new URI("C:/tempSchemas/niem-2.1/niem/domains/maritime/2.1/maritime.xsd");
		xsdImporter.initialize();
	}
		

	
	/************************* class variables ********************************/
	
	// Stores the M3 schema elements (entities, attributes, domain, relationships, etc.) 
	private static Integer _autoInc = 10;
	
	private static Integer nextAutoInc(){
		return _autoInc++;
	}

	private static HashMap<Integer, SchemaElement> _schemaElementsHS = new HashMap<Integer, SchemaElement>();
	
	/** stores the list of domains seen (used to import elements) **/
	private static HashMap<String,Domain> _domainList = new HashMap<String,Domain>();
	
	/** Stores the Castor SchemaElems **/
	private static HashMap<Integer,Object> _schemaElems = new HashMap<Integer,Object>();
	
	/** Stores the unique "Any" entity **/
	private Entity anyEntity;

	/** Store the parentID arrays for each schema by schemaID **/
	private static HashMap<Integer, HashSet<Integer>> _parentIDsBySchemaID  = new HashMap<Integer,HashSet<Integer>>();
	
	/** Stores merge sets (sets of schemaIDs that are merged to a 
		single schema because they form a cycle in the extends graph  **/
	private static HashMap<Integer,HashSet<Integer>> _mergeSets = new HashMap<Integer,HashSet<Integer>>();
	
	private static HashMap<Integer,String> _NSprefixBySchemaID = new HashMap<Integer,String>();
	
	private static HashMap<String, org.mitre.schemastore.model.Schema> _schemasByNSPrefix = new HashMap<String,org.mitre.schemastore.model.Schema>();
	
	/** Store the schemaElements by namespace prefix **/
	private static HashMap<String, HashSet<SchemaElement>> _schemaElementsByNSPrefix = new HashMap<String,HashSet<SchemaElement>>();
	
	/** Store the namespace prefix by schema element ID **/
	private static HashMap<Integer, String> _NSPrefixByElementID = new HashMap<Integer,String>();
	
	/** Store the namespace prefix associated with schema element **/
	private static HashMap<String,String> _nsPreByNS = new HashMap<String,String>();
	
	/** used in cycle detection **/
	private static ArrayList<Integer> _activeSet = new ArrayList<Integer>();
	
	private static HashMap<Integer,Integer> _translationTable = new HashMap<Integer,Integer>();
	
	private static HashSet<SchemaElement> _masterElementList = new HashSet<SchemaElement>();
	
	private static HashMap<Integer,Integer> _reverseTempTranslationTable = new HashMap<Integer,Integer>();
	
	private static HashSet<String> _baseDomainSet;
	
	private static HashSet<Integer> _seenAttrsInAttrGroup = new HashSet<Integer>();

	private static Tag _tagForSchemas = null;
	
	private static Integer _tagID = null;
	
	private static final String EXCLUDED_NAMESPACE_M3 = "http://openintegration.org/M3";
	private static final String EXCLUDED_NAMESPACE_M3_DOM = "http://openintegration.org/M3Dom";
	private static final String EXCLUDED_NAMESPACE_M3_ENTITY = "http://openintegration.org/M3Entity";
	
	public XSDImporterMerged() {
		super();

		baseDomains = new String[][]{{ANY + " ", "The Any wildcard domain"},
		{INTEGER + " ","The Integer domain"},
		{REAL + " ","The Real domain"},
		{STRING + " ","The String domain"},
		{"string" + " ","The string domain"},
		{DATETIME + " ","The DateTime domain"},
		{BOOLEAN + " ","The Boolean domain"},
		{"StringDef ", "The default string domain"}};

	}
	
	/** Initializes the importer for the specified URI 
	 * @throws ImporterException 
	 * @throws URISyntaxException */
	protected void initialize() throws ImporterException
	{	
		
		

		try {

			/** reset the Importer **/
			_schemaElementsHS = new HashMap<Integer, SchemaElement>();
			_domainList = new HashMap<String, Domain>();
			_schemaElems = new HashMap<Integer,Object>();
			_parentIDsBySchemaID  = new HashMap<Integer,HashSet<Integer>>();
			_mergeSets = new HashMap<Integer,HashSet<Integer>>();
			_schemasByNSPrefix = new HashMap<String,org.mitre.schemastore.model.Schema>();
			_schemaElementsByNSPrefix = new HashMap<String,HashSet<SchemaElement>>();
			_NSPrefixByElementID = new HashMap<Integer,String>();
			_nsPreByNS = new HashMap<String,String>();
			_activeSet = new ArrayList<Integer>();
			_translationTable = new HashMap<Integer,Integer>();
			_masterElementList = new HashSet<SchemaElement>();
			_attrGroupEntitySet = new HashMap<String,Entity>();
			_reverseTempTranslationTable = new HashMap<Integer,Integer>();
			_seenAttrsInAttrGroup = new HashSet<Integer>();
			_baseDomainSet = new HashSet<String>();
			_tagForSchemas = null;
			_tagID = null;
			
			/** Preset domains and then process this schema **/
			loadDomains();
			
			/** create DOM tree for main schema **/
			SchemaReader xmlSchemaReader = new SchemaReader(uri.toString());
			Schema mainSchema = xmlSchemaReader.read();
			getRootElements(mainSchema);
			

			
			/** verify correctness of imported schema **/
			org.mitre.schemastore.model.Schema schema = new org.mitre.schemastore.model.Schema(0,"foo","","","","",false);
			ArrayList<Integer> parentSet = new ArrayList<Integer>();
			ArrayList<SchemaElement> ses = new ArrayList<SchemaElement>();
			ses.addAll(_schemaElementsHS.values());
			SchemaInfo schemaInfo = new SchemaInfo(schema,parentSet,ses);
			
			if (schemaInfo.getBaseElements(null).size() != _schemaElementsHS.size()){
				System.out.println("[E] xsdMergedImporter -- imported schema does not have valid format ");
				throw new Exception();
			}
			
			/** process the imported schema elements to create graph **/
			processSchemaElements();

		}
		catch(Exception e) { 			
			e.printStackTrace();
			throw new ImporterException(ImporterExceptionType.PARSE_FAILURE,e.getMessage()); 
		}
	}

	/**
	 * processSchemaElements(): processes the schemaElements by importing 
	 * them into separate schemas one per namespace
	 * 
	 * @throws Exception
	 */
	private void processSchemaElements() throws Exception {

		/** find appropriate namespace for each schemaElement **/
		for (Integer key : _schemaElementsHS.keySet())
			processElement(key);
			
		/** create hashtable mapping schemaElementID --> namespace prefix **/
		for (String nsPre : _schemaElementsByNSPrefix.keySet()){
			
			for (SchemaElement se : _schemaElementsByNSPrefix.get(nsPre))
				_NSPrefixByElementID.put(se.getId(), nsPre);
		}
			
		/**
		 * BEGIN Remove the subtypes from the m3 namespace schema
		 */
		ArrayList<SchemaElement> m3ElementsCopy = new ArrayList<SchemaElement>();
		m3ElementsCopy.addAll(_schemaElementsByNSPrefix.get("m3"));
		for (SchemaElement sub : m3ElementsCopy){
			if (sub instanceof Subtype){
				_schemaElementsByNSPrefix.get("m3").remove(sub);
				String childNSPre = _NSPrefixByElementID.get(((Subtype)sub).getChildID());
				_NSPrefixByElementID.put(sub.getId(), childNSPre);
				_schemaElementsByNSPrefix.get(childNSPre).add(sub);
			}
		}
		/**
		 * END Remove subtypes from m3 namespace
		 */
		
		/** remove empty schemas here (for now just "m3" schema) **/
		ArrayList<String> keySetCopy = new ArrayList<String>();
		keySetCopy.addAll(_schemaElementsByNSPrefix.keySet());
		
		for (String nsPre : keySetCopy){
			
			if (_schemaElementsByNSPrefix.get(nsPre).size() == 0){
				_schemaElementsByNSPrefix.remove(nsPre);
				
				/** remove empty schema from list of namespace prefix by namespaces */
				ArrayList<String> keySetCopy2 = new ArrayList<String>();
				keySetCopy2.addAll(_nsPreByNS.keySet());
				for (String ns : keySetCopy2){
					if (_nsPreByNS.get(ns).equals(nsPre)){
						_nsPreByNS.remove(ns);
					}
				}
				
				/** remove empty schema from list of parentIDs by SchemaID */
				ArrayList<Integer> keySetCopy3 = new ArrayList<Integer>();
				keySetCopy3.addAll(_NSprefixBySchemaID.keySet());
				for (Integer schemaID : keySetCopy3){
					if (_NSprefixBySchemaID.get(schemaID).equals(nsPre)){
						_NSprefixBySchemaID.remove(schemaID);
						_parentIDsBySchemaID.remove(schemaID);
					}
				}
		
				/** remove empty schema from list of schemas for each namespace prefix */
				_schemasByNSPrefix.remove(nsPre);

			}
		}
		
		/** build the extension graph **/
		buildExtensionGraph();
		
		// DEBUG -- dump graph
		System.out.println("***** dump graph *****");
		for (Integer id : _parentIDsBySchemaID.keySet()){
			System.out.print(id + ":" + _NSprefixBySchemaID.get(id));
			for (Integer parentID : _parentIDsBySchemaID.get(id)){
				System.out.print(" " + parentID + ":" + _NSprefixBySchemaID.get(parentID));
			}
			System.out.println();
		}
		// END DEBUG
		
		 /** detects and removes cycles in the extension graph*/
		detectCycles();
		
		/** identify topological sort of extends graph */
		ArrayList<Integer> sortedSchemaIDs = findTopologicalSchemaIDSort();
		
		// import the schemas in topological order
		HashMap<Integer,Integer> translatedSchemaIds = new HashMap<Integer,Integer>();
		for (Integer oldID : sortedSchemaIDs){
			
			Integer newID = nextAutoInc();
			
			/** topologically sort the schemaElements **/
			ArrayList<SchemaElement> sortedTranslatedElements = topologicalSortSchemaElements(newID, _schemaElementsByNSPrefix.get(_NSprefixBySchemaID.get(oldID)));
			
			/** insert the schema into repository **/
			org.mitre.schemastore.model.Schema  schema = _schemasByNSPrefix.get(_NSprefixBySchemaID.get(oldID));
			schema.setId(newID);
			
			/** translate the parent schema list to ACTUAL ids in repository **/
			ArrayList<Integer> translatedParentList = new ArrayList<Integer>();
			for (Integer oldParentID : _parentIDsBySchemaID.get(oldID))
				translatedParentList.add(translatedSchemaIds.get(oldParentID));
			Collections.sort(translatedParentList);
			
			/** search the repository for the existing schema **/
			boolean alreadyImported = false;
			for (org.mitre.schemastore.model.Schema currSchema : client.getSchemas()){
				if (!currSchema.getDescription().equals(EXCLUDED_NAMESPACE_M3) &&
						!currSchema.getDescription().equals(EXCLUDED_NAMESPACE_M3) &&
						!currSchema.getDescription().equals(EXCLUDED_NAMESPACE_M3) &&
						currSchema.getDescription().equals(schema.getDescription()))
				{

					 newID = currSchema.getId();
					alreadyImported = true;
				}
			}
			
			if (alreadyImported)
			{
				
				System.out.println("already imported schema " + schema.getName() + "---" + schema.getDescription());
				
				// add new parents to existing parents
				SchemaInfo existingSchemaInfo = client.getSchemaInfo(newID);
				
				HashSet<Integer> completeParentHS = new HashSet<Integer>();
				completeParentHS.addAll(translatedParentList);
				completeParentHS.addAll(existingSchemaInfo.getParentSchemaIDs());
				
				ArrayList<Integer> parentIDs = new ArrayList<Integer>();
				parentIDs.addAll(completeParentHS);
				Collections.sort(parentIDs);
				client.setParentSchemas(newID, parentIDs);
				
				// insertElements
				insertElements(newID,sortedTranslatedElements);
				
			}
			else
			{
				newID = importParentSchema(schema, translatedParentList, sortedTranslatedElements);
			}
			translatedSchemaIds.put(oldID, newID);
		
			
		}
	}  // end method processElements
	
	
	private String buildKey (SchemaElement se, Integer newID) throws Exception
	{
		String key = null;
		if (se.getName() != null && se.getName().length() > 0)
			key = se.getName() + "---" + se.getDescription();
		else
		{
			
			if (client.getSchemaInfo(newID) == null){
				throw new Exception();
			}
			
			if (se instanceof Entity)
			{
				ArrayList<SchemaElement> referencingElements = client.getSchemaInfo(newID).getReferencingElements(se.getId());
				Collections.sort(referencingElements,new SchemaElementComparator());
				key = new String();
				for (SchemaElement se2 : referencingElements)
				{
					if (se2 == null) 
						return null;
					key = key + se2.getName() + "---" + se2.getDescription() + "---";
				}
			}
			else if (se instanceof Subtype)
			{
				
				SchemaElement parentElem = client.getSchemaInfo(newID).getElement(((Subtype)se).getParentID());
				SchemaElement childElem = client.getSchemaInfo(newID).getElement(((Subtype)se).getChildID());
				
				if (parentElem == null || childElem == null){
					return null;
				}
				
				key = client.getSchemaInfo(newID).getElement(((Subtype)se).getParentID()).getName()  + "---" +
					client.getSchemaInfo(newID).getElement(((Subtype)se).getParentID()).getDescription() + "---" +
					client.getSchemaInfo(newID).getElement(((Subtype)se).getChildID()).getName()   + "---" +
					client.getSchemaInfo(newID).getElement(((Subtype)se).getChildID()).getDescription(); 
			}
		}  // end else -- key generation
		return key;
	}
	
	/**
	 * 
	 * @param schemaID
	 * @param sortedTranslatedElements
	 * @throws Exception 
	 */
	private void insertElements(Integer schemaID, ArrayList<SchemaElement> sortedTranslatedElements) throws Exception
	{
		for (SchemaElement se : sortedTranslatedElements)
			se.setBase(schemaID);
	
		ArrayList<SchemaElement> masterListCopy = new ArrayList<SchemaElement>(); 
		masterListCopy.addAll(_masterElementList);
		masterListCopy.addAll(sortedTranslatedElements);
		
		ArrayList<SchemaElement> existingBaseSchemaElements = client.getSchemaInfo(schemaID).getBaseElements(null);
		HashMap<String,SchemaElement> existsTable = new HashMap<String,SchemaElement>(); 
		
		for (SchemaElement se : existingBaseSchemaElements){
			if (buildKey(se,schemaID) == null)
				throw new Exception();
			
			existsTable.put(buildKey(se,schemaID), se);
		}
		
		HashMap<Integer,Integer> tempTranslationTable = new HashMap<Integer,Integer>();
		
		for (SchemaElement newElement : sortedTranslatedElements)
		{
			String key = buildKey(newElement, schemaID);
			
			Integer newID = null;
			Integer oldID = newElement.getId();
			
			if (existsTable.containsKey(key))
			{
				newID = existsTable.get(key).getId();
				tempTranslationTable.put(oldID, newID);
				
			}
			else 
			{		
				// use the translation table to look
			//	translateElement(newElement, tempTranslationTable);
				
				newID = client.addSchemaElement(newElement);
				tempTranslationTable.put(oldID, newID);	
			}
			Integer origID = _reverseTempTranslationTable.get(oldID);
			if (origID == null)
			{
				System.out.println("[E] xsdMergedImporter -- cannot find orignial element in sortedTranslatedElements");
				throw new Exception();
			}
			_translationTable.put(origID, newID);	
			_masterElementList.add(newElement);

		} // end for each new element
	} // end method insertElements
	
	
	private static void translateElement(SchemaElement se, HashMap<Integer,Integer> tempTranslationTable) throws Exception
	{
		
		if (se instanceof DomainValue)
		{	 	
			DomainValue domValue = (DomainValue)se;
			Integer newID = tempTranslationTable.get(domValue.getDomainID());
			if (newID == null) 
				newID = _translationTable.get(domValue.getDomainID());
			if (newID == null) 
				throw new Exception("[E] xsdMergedImporter:translateElement -- domain value with underfined left element ref");
			domValue.setDomainID(newID);	
		}
		
		else if (se instanceof Attribute)
		{	
			Attribute attr = (Attribute)se;
			Integer newID = tempTranslationTable.get(attr.getDomainID());			
			if (newID == null) 
				newID = _translationTable.get(attr.getDomainID());	
			if (newID == null) 
				throw new Exception("[E] xsdMergedImporter:translateElement -- attr with underfined domain ref");
			attr.setDomainID(newID);
			
			newID = tempTranslationTable.get(attr.getEntityID());
			if (newID == null) 
				newID = _translationTable.get(attr.getEntityID());
			if (newID == null) 
				throw new Exception("[E] xsdMergedImporter:translateElement -- attr with underfined entity ref");
			attr.setEntityID(newID);
		}
		
		else if (se instanceof Containment)
		{	
			Containment cont = (Containment)se;
			Integer newID = null;
			if (cont.getParentID() == null) 
				newID = null;
			else {
				newID = tempTranslationTable.get(cont.getParentID());
				if (newID == null) 
					newID = _translationTable.get(cont.getParentID());
			}
			if (newID == null && cont.getParentID() != null) 
				throw new Exception("[E] xsdMergedImporter:translateElement -- cont with underfined parent ref");
			cont.setParentID(newID);
			
			newID = tempTranslationTable.get(cont.getChildID());
			if (newID == null)
				newID = _translationTable.get(cont.getChildID());
			if (newID == null)
				throw new Exception("[E] xsdMergedImporter:translateElement-- cont with underfined child ref");	
			cont.setChildID(newID);
		}
		
		else if (se instanceof Subtype)
		{	
			Subtype subtype = (Subtype)se;
			Integer newID = tempTranslationTable.get(subtype.getParentID());
			if (newID == null)
				newID = _translationTable.get(subtype.getParentID());
			if (newID == null) 
				throw new Exception("[E] xsdMergedImporter:translateElement -- subtype with underfined left element ref");
			subtype.setParentID(newID);
			
			newID = tempTranslationTable.get(subtype.getChildID());
			if (newID == null)
				newID = _translationTable.get(subtype.getChildID());
			if (newID == null) 
				throw new Exception("[E] xsdMergedImporter:translateElement-- subtype with underfined right element ref");
			subtype.setChildID(newID);
		}
		
		else if (se instanceof Relationship)
		{	
			Relationship rel = (Relationship)se;
			Integer newID = tempTranslationTable.get(rel.getLeftID());
			if (newID == null)
				newID = _translationTable.get(rel.getLeftID());
			if (newID == null) 
				throw new Exception("[E] xsdMergedImporter:translateElement -- rel with underfined left element ref");
			rel.setLeftID(newID);
			
			newID = tempTranslationTable.get(rel.getRightID());
			if (newID == null)
				newID = _translationTable.get(rel.getRightID());
			if (newID == null) 
				throw new Exception("[E] xsdMergedImporter:translateElement-- rel with underfined right element ref");
			rel.setRightID(newID);
		}
	
	}
	
	/**
	 * processElement:  process each element in the imported element set
	 * for SchemaStore
	 * 
	 * @param elemID  element id
	 * 
	 * @throws Exception
	 */
	private void processElement(Integer elemID) throws Exception 
	{			
		Object elemDecl = _schemaElems.get(elemID);
		String targetNSPrefix = null, targetNS = null;
		
		/** check if the element was created for SchemaStore indicated by
		 * elementDecl (from Castor tree) being NULL**/
		if (elemDecl ==	null){
			targetNSPrefix = "m3";
			targetNS = EXCLUDED_NAMESPACE_M3;
			SchemaElement se = _schemaElementsHS.get(elemID);
			
			if (se instanceof Entity){
				targetNSPrefix = "m3Entity";
				targetNS = EXCLUDED_NAMESPACE_M3_ENTITY;
				
			}
			else if (se instanceof Domain){
				targetNSPrefix = "m3Dom";
				targetNS = EXCLUDED_NAMESPACE_M3_DOM;
				String newName = se.getName() + " ";
				se.setName(newName);
			}
		}
		
		/** attempt to find target namespace and target namespace prefix **/
		else if (elemDecl instanceof XMLType){	
			targetNS = (((XMLType)elemDecl).getSchema().getTargetNamespace() == null) ? ((XMLType)elemDecl).getSchema().getSchemaNamespace() : ((XMLType)elemDecl).getSchema().getTargetNamespace();
			targetNS = (targetNS == null || targetNS.equals("null")) ? "" : targetNS;
			targetNSPrefix = ((XMLType)elemDecl).getSchema().getNamespacePrefix(targetNS);	
			targetNSPrefix = (targetNSPrefix == null || targetNSPrefix.length() == 0 || targetNSPrefix.equals("null")) ? "ns" + nextAutoInc() : targetNSPrefix;
			
			/** update mapping of nsPre --> NS **/
			if (_nsPreByNS.get(targetNS) == null)
				_nsPreByNS.put(targetNS,targetNSPrefix);
			targetNSPrefix = _nsPreByNS.get(targetNS);
		}
		
		else if (elemDecl instanceof AttributeDecl){ 
			targetNS = (((AttributeDecl)elemDecl).getSchema().getTargetNamespace() == null) ? ((AttributeDecl)elemDecl).getSchema().getSchemaNamespace() : ((AttributeDecl)elemDecl).getSchema().getTargetNamespace() ;
			targetNS = (targetNS == null || targetNS.equals("null")) ? "" : targetNS;	
			targetNSPrefix = ((AttributeDecl)elemDecl).getSchema().getNamespacePrefix(targetNS);	
			targetNSPrefix = (targetNSPrefix == null || targetNSPrefix.length() == 0 || targetNSPrefix.equals("null")) ? "ns" + nextAutoInc() : targetNSPrefix;
		
			/** update mapping of nsPre --> NS **/
			if (_nsPreByNS.get(targetNS) == null)
				_nsPreByNS.put(targetNS,targetNSPrefix);
			
			else 
				if (!(_nsPreByNS.get(targetNS).equals(targetNSPrefix)))
					System.err.println("[W] already saw " + targetNS + " with " + targetNSPrefix + " NOT " +_nsPreByNS.get(targetNS));
			
			targetNSPrefix = _nsPreByNS.get(targetNS);
		}
		
		else if (elemDecl instanceof ElementDecl){	
			targetNS = (((ElementDecl)elemDecl).getSchema().getTargetNamespace() == null) ? ((ElementDecl)elemDecl).getSchema().getSchemaNamespace() : ((ElementDecl)elemDecl).getSchema().getTargetNamespace() ;
			targetNS = (targetNS == null || targetNS.equals("null")) ? "" : targetNS;	
			targetNSPrefix = ((ElementDecl)elemDecl).getSchema().getNamespacePrefix(targetNS);	
			targetNSPrefix = (targetNSPrefix == null || targetNSPrefix.length() == 0 || targetNSPrefix.equals("null")) ? "ns" + nextAutoInc() : targetNSPrefix;
			
			/** update mapping of nsPre --> NS **/
			if (_nsPreByNS.get(targetNS) == null)
				_nsPreByNS.put(targetNS,targetNSPrefix);
			targetNSPrefix = _nsPreByNS.get(targetNS);	
		}
		
		else {
			System.out.println("[E] xsdMergedImporter -- attempting to assign namespace to unknown type " + elemDecl.getClass());
			throw new Exception();
		}
	
		/** add the element to the appropriate schema.  Create new schema if necessary **/
		if (_schemasByNSPrefix.get(targetNSPrefix) == null){
			
			// search for the existing 
			
			
			
			org.mitre.schemastore.model.Schema newSchema = new org.mitre.schemastore.model.Schema(nextAutoInc(),targetNSPrefix,"",targetNS,"",targetNS,false); 
			_schemasByNSPrefix.put (targetNSPrefix, newSchema); 
			
			_schemaElementsByNSPrefix.put(targetNSPrefix, new HashSet<SchemaElement>());
			_parentIDsBySchemaID.put(newSchema.getId(), new HashSet<Integer>());
			_NSprefixBySchemaID.put(newSchema.getId(), targetNSPrefix);
		}
		
		HashSet<SchemaElement> schemaElements = _schemaElementsByNSPrefix.get(targetNSPrefix);
		if (_schemaElementsHS.get(elemID) != null)
			schemaElements.add(_schemaElementsHS.get(elemID));
		else 
			throw new Exception("[E] xsdMergedImporter:processElement -- attempting to process non-existent element");
		
	}  // end processElement
	
	
	/**
	 * buildExtensionGraph: add edges to extension graph
	 * @throws Exception
	 */
	private void buildExtensionGraph() throws Exception{
	
		for (String nsPre : _schemaElementsByNSPrefix.keySet()){
			
			for (SchemaElement se : _schemaElementsByNSPrefix.get(nsPre)){
				
				/** add edges to extension graph for containment **/
				if (se instanceof Containment){
					
					String parentNSPre = _NSPrefixByElementID.get(((Containment)se).getParentID());
					String childNSPre  = _NSPrefixByElementID.get(((Containment)se).getChildID());
				
					if (childNSPre == null)
						throw new Exception("[E] parent or child of containment " + se.getName() + " undefined"); 
					
					else if (parentNSPre != null && !parentNSPre.equals(nsPre)){
						try {
							Integer otherSchemaID = _schemasByNSPrefix.get(parentNSPre).getId(); 
							_parentIDsBySchemaID.get(_schemasByNSPrefix.get(nsPre)).add(otherSchemaID);
						} catch(Exception e) { 
							throw new Exception("[E] xsdMergedImporter -- parent of containment " + se + " undefined"); 
						}
					}				
					else if (!childNSPre.equals(nsPre)){
						try {
							Integer otherSchemaID = _schemasByNSPrefix.get(childNSPre).getId(); 
							_parentIDsBySchemaID.get(_schemasByNSPrefix.get(nsPre)).add(otherSchemaID);
						} catch(Exception e){
							System.out.println("[E] xsdImporter:buildGraph -- child of containment " + se + " undefined");
							new Exception();
						}
					}
				}
				
				/** add edges to extension graph for subtype **/
				else if (se instanceof Subtype){
					
					String parentNSPre = _NSPrefixByElementID.get(((Subtype)se).getParentID());
					String childNSPre  = _NSPrefixByElementID.get(((Subtype)se).getChildID());
				
					if (childNSPre == null)
						throw new Exception("[E] parent or child of subtype " + se.getId() + " undefined"); 
					
					if (parentNSPre != null && !parentNSPre.equals(nsPre)){
						// add edge to schema for parentNSPre
						Integer otherSchemaID = (_schemasByNSPrefix.get(parentNSPre) == null) ? -1 : _schemasByNSPrefix.get(parentNSPre).getId(); 
						HashSet<Integer> parentIDs = _parentIDsBySchemaID.get(_schemasByNSPrefix.get(nsPre));
						if (otherSchemaID == -1) 
							throw new Exception("[E] xsdMergedImporter -- parent of subtype " + se + " undefined"); 
						parentIDs.add(otherSchemaID);	
					}
					
					if (!childNSPre.equals(nsPre)){
						// add edge to schema for parentNSPre
						Integer otherSchemaID = (_schemasByNSPrefix.get(childNSPre) == null) ? -1 : _schemasByNSPrefix.get(childNSPre).getId(); 
						HashSet<Integer> parentIDs = _parentIDsBySchemaID.get(_schemasByNSPrefix.get(nsPre));
						if (otherSchemaID == -1)
							throw new Exception(" [E] xsdMergedImporter -- child of containment " + se.getName() + " undefined");
						parentIDs.add(otherSchemaID);
					}
				}
				
				/** add edges to extension graph for  relationship **/
				else if (se instanceof Relationship){
					
					String parentNSPre = _NSPrefixByElementID.get(((Relationship)se).getLeftID());
					String childNSPre  = _NSPrefixByElementID.get(((Relationship)se).getRightID());
				
					if (childNSPre == null)
						throw new Exception("[E] parent or child of containment " + se.getName() + " undefined"); 
					
					if (parentNSPre != null && !parentNSPre.equals(nsPre)){
						// add edge to schema for parentNSPre
						Integer otherSchemaID = (_schemasByNSPrefix.get(parentNSPre) == null) ? -1 : _schemasByNSPrefix.get(parentNSPre).getId(); 
						HashSet<Integer> parentIDs = _parentIDsBySchemaID.get(_schemasByNSPrefix.get(nsPre));
						if (otherSchemaID == -1) 
							throw new Exception("[E] xsdMergedImporter -- parent of containment " + se + " undefined"); 
						parentIDs.add(otherSchemaID);
					}
					
					if (!childNSPre.equals(nsPre)){
						try {
							Integer otherSchemaID = _schemasByNSPrefix.get(childNSPre).getId(); 
							_parentIDsBySchemaID.get(_schemasByNSPrefix.get(nsPre)).add(otherSchemaID);;
						} catch (Exception e){
								throw new Exception(" [E] xsdMergedImporter -- child of containment " + se.getName() + " undefined");
						}
					}
				}
				
				/** add edges to extension graph for attribute  **/
				else if (se instanceof Attribute){
					
					String domainNSPre = _NSPrefixByElementID.get(((Attribute)se).getDomainID());
					String entityNSPre = _NSPrefixByElementID.get(((Attribute)se).getEntityID());
					
					if (domainNSPre == null || entityNSPre == null) 
						throw new Exception("[E] xsdMergedImporter -- domain or entity for attribute " + se.getName() + " has undefined namespace");
					
					if (!domainNSPre.equals(nsPre)){
						// add edge to the other schema
						
						try {
							Integer otherSchemaID =  _schemasByNSPrefix.get(domainNSPre).getId(); 
							_parentIDsBySchemaID.get(_schemasByNSPrefix.get(nsPre)).add(otherSchemaID);
						} catch (Exception e){
							e.printStackTrace();
							throw new Exception();
						}
					}
					
					if (!entityNSPre.equals(nsPre)){
						// add edge to the other schema
						try {
							Integer otherSchemaID = _schemasByNSPrefix.get(entityNSPre).getId(); 
							_parentIDsBySchemaID.get(_schemasByNSPrefix.get(nsPre)).add(otherSchemaID);	;
						} catch (Exception e) { 
							throw new Exception(" [E] xsdMergedImporter -- entity of attribute " + se.getName() + " undefined");
						}
					}
				}
				
				/** add edge to extension graph for domain value **/
				else if (se instanceof DomainValue){
					// check domain is still in same
					String domainNSPre = _NSPrefixByElementID.get(((DomainValue)se).getDomainID());
					
					if (domainNSPre == null)
						throw new Exception("[E] xsdMergedImporter -- domain for domainValue has undefined namespace");
					
					if (!domainNSPre.equals(nsPre)){
						// add edge to the other schema
						try {
							Integer otherSchemaID = _schemasByNSPrefix.get(domainNSPre).getId(); 
							_parentIDsBySchemaID.get(_schemasByNSPrefix.get(nsPre)).add(otherSchemaID);	
						} catch (Exception e) {
								throw new Exception(" [E] parent of containment undefined");
						}
					}	
				}
			}
		}		
	}  // end buildExtensionGraph
	
	/**
	 * topologicalSortSchemaElements:  sort the elements into topological order. 
	 * Return value from this 
	 * NOTE: modifies the original 
	 * 
	 * @param translatedBase base id for new schema
	 * 
	 * @param unsortedElements schemaElements for new schema in unsorted order
	 * 
	 * @return schemaElements copy of unsortedElements sorted into topological order
	 * @throws Exception
	 */
	private static ArrayList<SchemaElement> topologicalSortSchemaElements(Integer translatedBase, HashSet<SchemaElement> unsortedElements) throws Exception{
			
		/** sort schema elements **/
		ArrayList<Entity> entities = new ArrayList<Entity>();
		ArrayList<Domain> domains = new ArrayList<Domain>();
		ArrayList<DomainValue> domainValues = new ArrayList<DomainValue>();
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		ArrayList<Containment> containments = new ArrayList<Containment>();
		ArrayList<Subtype> subtypes = new ArrayList<Subtype>();
		ArrayList<Relationship> relationships = new ArrayList<Relationship>();
		ArrayList<Alias> aliases = new ArrayList<Alias>();
		
		for (SchemaElement se : unsortedElements){
			se.setBase(translatedBase);
			
			if (se instanceof Entity) 		entities.add((Entity)se);			
			else if (se instanceof Domain) 	domains.add((Domain)se);
			else if (se instanceof DomainValue)	domainValues.add((DomainValue)se);
			else if (se instanceof Attribute)	attributes.add((Attribute)se);
			else if (se instanceof Containment)	containments.add((Containment)se);
			else if (se instanceof Subtype)	subtypes.add((Subtype)se);
			else if (se instanceof Relationship)	relationships.add((Relationship)se);
			else if (se instanceof Alias)	aliases.add((Alias)se);
		}
		
		/** set newID to be larger than any existing value in translation table **/
		
		Integer newID = nextAutoInc();
		for (Integer translatedID : _translationTable.values())
			if (translatedID > newID) 
				newID = translatedID;	
		for (Integer translatedID : _translationTable.keySet())
			if (translatedID > newID) 
				newID = translatedID;
		
		/** 
		 * reset reverseTempTranslationTable (will be used in subsequent call to importParentSchemas)
		 * 
		 * tempTranslationTable handles translation for elements referenced in CURRENT schema 
		 * being imported
		 * 
		 */
		
		_reverseTempTranslationTable = new HashMap<Integer,Integer>();
		HashMap<Integer,Integer> tempTranslationTable = new HashMap<Integer,Integer>();
		
		for (Entity entity : entities)
		{
			newID = nextAutoInc(); 
			tempTranslationTable.put(entity.getId(),newID);
			_reverseTempTranslationTable.put(newID, entity.getId());
			entity.setId(newID);	
		}
		
		for (Domain domain : domains)
		{
			newID = nextAutoInc(); 
			tempTranslationTable.put(domain.getId(),newID); 
			_reverseTempTranslationTable.put(newID, domain.getId());
			domain.setId(newID);
		}
		
		for (DomainValue domValue : domainValues)
		{	
			newID = nextAutoInc(); 
			tempTranslationTable.put(domValue.getId(),newID); 
			_reverseTempTranslationTable.put(newID,domValue.getId());
			domValue.setId(newID);
			
			newID = tempTranslationTable.get(domValue.getDomainID());
			if (newID == null) 
				newID = _translationTable.get(domValue.getDomainID());
			if (newID == null) 
				throw new Exception("[E] xsdMergedImporter:topologicalSortSchemaElements -- domain value with underfined left element ref");
			domValue.setDomainID(newID);	
		}
		
		for (Attribute attr : attributes)
		{	
			newID = nextAutoInc(); 
			tempTranslationTable.put(attr.getId(),newID); 
			_reverseTempTranslationTable.put(newID, attr.getId());
			attr.setId(newID);		
			
			newID = tempTranslationTable.get(attr.getDomainID());			
			if (newID == null) 
				newID = _translationTable.get(attr.getDomainID());	
			if (newID == null) 
				throw new Exception("[E] xsdMergedImporter:topologicalSortSchemaElements -- attr with underfined domain ref");
			attr.setDomainID(newID);
			
			newID = tempTranslationTable.get(attr.getEntityID());
			if (newID == null) 
				newID = _translationTable.get(attr.getEntityID());
			if (newID == null) 
				throw new Exception("[E] xsdMergedImporter:topologicalSortSchemaElements -- attr with underfined entity ref");
			attr.setEntityID(newID);
		}
		
		for (Containment cont : containments)
		{	
			newID = nextAutoInc(); 
			tempTranslationTable.put(cont.getId(),newID); 
			_reverseTempTranslationTable.put(newID, cont.getId());
			cont.setId(newID);
			
			if (cont.getParentID() == null) 
				newID = null;
			else {
				newID = tempTranslationTable.get(cont.getParentID());
				if (newID == null) 
					newID = _translationTable.get(cont.getParentID());
			}
			if (newID == null && cont.getParentID() != null) 
				throw new Exception("[E] xsdMergedImporter:topologicalSortSchemaElements -- cont with underfined parent ref");
			cont.setParentID(newID);
			
			newID = tempTranslationTable.get(cont.getChildID());
			if (newID == null)
				newID = _translationTable.get(cont.getChildID());
			if (newID == null)
				throw new Exception("[E] xsdMergedImporter:topologicalSortSchemaElements -- cont with underfined child ref");	
			cont.setChildID(newID);
		}
		
		for (Subtype subtype : subtypes)
		{	
			newID = nextAutoInc(); 
	
			tempTranslationTable.put(subtype.getId(),newID); 
			_reverseTempTranslationTable.put(newID, subtype.getId());
			subtype.setId(newID);

			newID = tempTranslationTable.get(subtype.getParentID());
			if (newID == null)
				newID = _translationTable.get(subtype.getParentID());
			if (newID == null) 
				throw new Exception("[E] xsdMergedImporter:topologicalSortSchemaElements -- subtype with underfined left element ref");
			subtype.setParentID(newID);
			
			newID = tempTranslationTable.get(subtype.getChildID());
			if (newID == null)
				newID = _translationTable.get(subtype.getChildID());
			if (newID == null) 
				throw new Exception("[E] xsdMergedImporter:topologicalSortSchemaElements -- subtype with underfined right element ref");
			subtype.setChildID(newID);
		}
		
		for (Relationship rel : relationships)
		{	
			newID = nextAutoInc();	
			tempTranslationTable.put(rel.getId(),newID); 
			_reverseTempTranslationTable.put(newID, rel.getId());
			rel.setId(newID);	
			
			newID = tempTranslationTable.get(rel.getLeftID());
			if (newID == null)
				newID = _translationTable.get(rel.getLeftID());
			if (newID == null) 
				throw new Exception("[E] xsdMergedImporter:topologicalSortSchemaElements -- rel with underfined left element ref");
			rel.setLeftID(newID);
			
			newID = tempTranslationTable.get(rel.getRightID());
			if (newID == null)
				newID = _translationTable.get(rel.getRightID());
			if (newID == null) 
				throw new Exception("[E] xsdMergedImporter:topologicalSortSchemaElements -- rel with underfined right element ref");
			rel.setRightID(newID);
		}
		
		for (Alias alias : aliases)
		{	
			newID = nextAutoInc(); 
			tempTranslationTable.put(alias.getId(),newID);	
			_reverseTempTranslationTable.put(newID, alias.getId());
			alias.setId(newID);
			
			newID = tempTranslationTable.get(alias.getElementID());
			if (newID == null)
				newID = _translationTable.get(alias.getElementID());
			if (newID == null) throw new Exception("[E] xsdMergedImporter:topologicalSortSchemaElements -- alias with underfined element ref");
			alias.setElementID(newID);
		}
		
		ArrayList<SchemaElement> retVal = new ArrayList<SchemaElement>();
		retVal.addAll(entities); retVal.addAll(domains); 
		retVal.addAll(domainValues); retVal.addAll(attributes);
		retVal.addAll(containments); retVal.addAll(subtypes); 
		retVal.addAll(aliases);
		
		return retVal;
		
	} // end method topologicalSortSchemaElements
	
	
	/**
	 * findTopologicalSchemaIDSort:  find topological sort of schemaIDs which are nodes in extension graph
	 * @return list of schemaIDs sorted in topological order
	 * @throws Exception
	 */
	private static ArrayList<Integer> findTopologicalSchemaIDSort() throws Exception{
		
		// create copy of graph to destructively modify
		HashMap<Integer, HashSet<Integer>> graphCopy = new HashMap<Integer, HashSet<Integer>>(); 
		for (Integer key : _parentIDsBySchemaID.keySet()){
			HashSet<Integer> edgeCopy = new HashSet<Integer>(); 
			edgeCopy.addAll(_parentIDsBySchemaID.get(key));
			graphCopy.put(key, edgeCopy);
		}
		
		ArrayList<Integer> sortedNodes = new ArrayList<Integer>();
		boolean stillProgress = true;
		
		while(graphCopy.size() > 0 && stillProgress){
			stillProgress = false;
			
			HashSet<Integer> nodeSet = new HashSet<Integer>();
			nodeSet.addAll(graphCopy.keySet());
			for (Integer currNode : nodeSet){
				HashSet<Integer> currEdges = graphCopy.get(currNode);
				if (currEdges != null && currEdges.size() == 0){
					stillProgress = true;
					sortedNodes.add(currNode);
					graphCopy.remove(currNode);
					for (Integer node : graphCopy.keySet())
						graphCopy.get(node).remove(currNode);
				}
			}		
		}
		if (graphCopy.size() > 0){
			throw new Exception("[E]  xsdMergedImporter -- extension graph has " +
					"cycle and no topolological sort exists");
		}
	
		return sortedNodes;
	}

	

	
	/** Import the schema to the repository 
	 * @throws Exception */
	private Integer importParentSchema(org.mitre.schemastore.model.Schema schema, 
									   ArrayList<Integer> translatedParentList, 
									   ArrayList<SchemaElement> sortedTranslatedElements) throws Exception
	{
		boolean success = false;
		
		/** build out the set of parent elements -- includes elements 
		 * being extended in sortedTranslatedElements and elements already 
		 * in repository (stored in _masterElementList). masterListCopy is 
		 * ONLY used for this call **/
		ArrayList<SchemaElement> masterListCopy = new ArrayList<SchemaElement>();
		 
		masterListCopy.addAll(_masterElementList);
		masterListCopy.addAll(sortedTranslatedElements);
		
		/** find the set of elements that will actually be inserted into repository 
		 * for the passed schema **/
		SchemaInfo schemaInfo = new SchemaInfo(schema,translatedParentList,masterListCopy);
		ArrayList<SchemaElement> insertedElems = schemaInfo.getBaseElements(null);
		Collections.sort(insertedElems, new SchemaElementComparator());
		Collections.sort(sortedTranslatedElements, new SchemaElementComparator());
		
		/**
		 * sortedTranslatedElements will superset of elements that are actually
		 * inserted into schema (stored in insertedElems).  We only should be 
		 * dropping the "special" Domains -- anything else should throw an error 
		 */
		ArrayList<SchemaElement> sortedTranslatedElementsCopy 
										= new ArrayList<SchemaElement>();
		
		HashSet<Integer> knownIds = new HashSet<Integer>();
		for (SchemaElement se : insertedElems)
			knownIds.add(se.getId());
		
		for (SchemaElement se : sortedTranslatedElements)
		{
			if (knownIds.contains(se.getId()))
			{
				sortedTranslatedElementsCopy.add(se);
			}
			else if (!(se instanceof Domain))
			{
				//System.out.println("[E] XSDMergedImporter -- not inserting more than Domains --- " + se.getName() + " --- " + se.getClass());
				//throw new Exception();
			}
		}
		
		sortedTranslatedElements = sortedTranslatedElementsCopy;
			
		// Import the schema
		Integer schemaID = null;
	
		try {
					
			schemaID = client.importSchema(schemaInfo.getSchema(), schemaInfo.getBaseElements(null));
			success = client.setParentSchemas(schemaID, schemaInfo.getParentSchemaIDs());
		
			if (success && schemaID != null)
			{
				/** create tag for schemas that will be imported for XSD **/
				/** ensure that tag is unique by appending ID of first new schema in group **/
			
				if (_tagForSchemas == null)
				{
					String tagName = uri.toString();
					boolean tagNameExists = false;
					for (Tag tag : client.getTags())
						if (tagName.equals(tag.getName()))
							tagNameExists = true;
				
					if (tagNameExists)
						tagName = tagName + "-" + schemaID;		
				
					_tagForSchemas = new Tag(nextAutoInc(),tagName,0);
					_tagID = client.addTag(_tagForSchemas);
				}
				client.addTagToSchema(schemaID, _tagID);
			}
			
			// Delete the imported schema if failure occurred
			if(!success && schemaID!=null)
			{
				try { client.deleteSchema(schemaID); } catch(Exception e) {};
				schemaID=null;
			}
			
			insertedElems = client.getSchemaInfo(schemaID).getBaseElements(null);
		
		} catch(Exception e) {
			e.printStackTrace();
		}
	
		/** make sure that everything was actually inserted **/ 
		Collections.sort(insertedElems,new SchemaElementComparator());
		if (insertedElems.size() != sortedTranslatedElements.size())
		{	
			System.out.println("insertedElems.size: " + insertedElems.size() + " " + "sortedTranslatedElems.size: " + sortedTranslatedElements.size());
			throw new Exception("[E] xsdMergedImporter -- sorted elements not same size as inserted schema ");
		}
		
		/** add the translated elements to the translation table **/
		else 
		{
			for (int i=0; i< insertedElems.size(); i++)
			{	
				if (sortedTranslatedElements.get(i).getName().equals(insertedElems.get(i).getName()) == false)
				{
					System.out.println("[E] importParentSchema -- insertedElems not the same as sortedTranslatedElems");
					throw new Exception();
				}
				if (sortedTranslatedElements.get(i).getClass().equals(insertedElems.get(i).getClass()) == false)
				{
					System.out.println("[E] importParentSchema -- insertedElems not the same as sortedTranslatedElems");
					throw new Exception();
				}
			
				Integer origID = _reverseTempTranslationTable.get(sortedTranslatedElements.get(i).getId());
				if (origID == null)
				{
					System.out.println("[E] xsdMergedImporter -- cannot find orignial element in sortedTranslatedElements");
					throw new Exception();
				}
				_translationTable.put(origID, insertedElems.get(i).getId());	
				_masterElementList.add(insertedElems.get(i));
			}
		}		

		/** Return the created schema ID **/
		return schemaID;
	}

	
	/*************************************************************************
	 * Cycle detection code
	 *************************************************************************/
	
	private static void detectCycles(){
	
		// initialize mergeSet
		for (Integer schemaID : _parentIDsBySchemaID.keySet()){
			HashSet<Integer> mergeSet = new HashSet<Integer>();
			mergeSet.add(schemaID);
			_activeSet.add(schemaID);
			_mergeSets.put(schemaID, mergeSet);
		}
		
		while (_activeSet.size() > 0){
			Integer currID = _activeSet.remove(0);
			ArrayList<Integer> visitedIDs = new ArrayList<Integer>();
			visitedIDs.add(currID);
			visitNode(currID, visitedIDs);
		}
	
		// BEGIN DEBUG --
		System.out.println("***** Dumping mergesets *****");
		for (Integer id : _mergeSets.keySet()){
			System.out.print(id + ": " );
			for (Integer mergeId : _mergeSets.get(id)){
				System.out.print(mergeId + " ");
			}
			System.out.println();
		}
		// END DEBUG
		
		// merge together schemaElements
		for (Integer id : _mergeSets.keySet())
			for (Integer mergeId : _mergeSets.get(id))
				_schemaElementsByNSPrefix.get(_NSprefixBySchemaID.get(id)).addAll(_schemaElementsByNSPrefix.get(_NSprefixBySchemaID.get(mergeId)));
	}
	
	
	private static void visitNode(Integer currID, ArrayList<Integer> visitedIDs){
		
		
		// initialize childSet
		HashSet<Integer> childSet = new HashSet<Integer>();			
		childSet.addAll(_parentIDsBySchemaID.get(currID));
		
		//System.out.print("-------------- visiting node " + currID + " VISITED-IDS: " + visitedIDs);
		//System.out.println(" --- CHILD-SET: " + childSet );
		
		ArrayList<Integer> origVisitedIDs = new ArrayList<Integer>();
		origVisitedIDs.addAll(visitedIDs);
		
		while (childSet.size() > 0){
			
			Integer child = childSet.iterator().next();
			childSet.remove(child);
			visitedIDs.clear(); 
			visitedIDs.addAll(origVisitedIDs);
			for (int i = 0; i<visitedIDs.size();i++){
				if (child.equals(visitedIDs.get(i))){
				
					// handle cycle -- replace with 1st node in cycle
					HashSet<Integer> mergedNodes = new HashSet<Integer>();
					for (int j = i+1; j < visitedIDs.size(); j++)
						mergedNodes.add(visitedIDs.get(j));
					
					HashSet<Integer> mergedEdges = new HashSet<Integer>();
					for (int j = i; j < visitedIDs.size(); j++){
						if (_parentIDsBySchemaID.get(visitedIDs.get(j)) != null){
							mergedEdges.addAll(_parentIDsBySchemaID.get(visitedIDs.get(j)));
						}	
					}
					// remove the child
					mergedEdges.remove(child);
					_parentIDsBySchemaID.put(child, mergedEdges);
					
					// remove all edges referring to merged set
					for (Integer id : _parentIDsBySchemaID.keySet()){
						boolean containsMerged = false;
						for (Integer parentID : _parentIDsBySchemaID.get(id)){
							if (mergedNodes.contains(parentID)){
								containsMerged = true;
							}
						}
						if (containsMerged){
							_parentIDsBySchemaID.get(id).removeAll(mergedNodes);
							if (!id.equals(child))
								_parentIDsBySchemaID.get(id).add(child);
						}
					}
					
					// remove merged nodes from graph and mergedSets
					for (Integer id : mergedNodes){
						System.out.println("** removing from _parentIDsBySchemaID: " + id);
						_parentIDsBySchemaID.remove(id);
					}
					
					HashSet<Integer> mergeSetUnion = new HashSet<Integer>();
					for (Integer id : mergedNodes){
						if (_mergeSets.get(id) != null){
							mergeSetUnion.addAll(_mergeSets.get(id));
							_mergeSets.remove(id);
						}
					}
					
					mergeSetUnion.remove(child);
					if (_mergeSets.get(child) != null)
						_mergeSets.get(child).addAll(mergeSetUnion);
					
					_activeSet.removeAll(mergedNodes);
					visitedIDs.removeAll(mergedNodes);
					visitedIDs.remove(child);
					childSet.remove(child);
					_activeSet.add(child);
					
				}
				
			} // end for visitedIDs
			
			HashSet<Integer> childSetCopy = new HashSet<Integer>();
			childSetCopy.addAll(childSet);
			for (Integer cid : childSetCopy){
				if (!_parentIDsBySchemaID.keySet().contains(cid))
					childSet.remove(cid);
			}
		
			if (_parentIDsBySchemaID.keySet().contains(child)){
				visitedIDs.add(child);
				visitNode(child,visitedIDs);
			}
			
		} // end while -- all children processed
	} // end visitNode
	
		
	
	
	/*************************************************************************
	 * Rest of XSDImporter 
	 * ***********************************************************************
	 */
	
	/** Returns the importer name */
	public String getName()
		{ return "XSD Importer Merged"; }
	
	/** Returns the importer description */
	public String getDescription()
		{ return "This importer can be used to import schemas from an xsd format"; }
	
	/** Returns the importer URI type */
	public URIType getURIType()
		{ return URIType.FILE; }
	
	/** Returns the importer URI file types */
	public ArrayList<String> getFileTypes()
	{
		ArrayList<String> fileTypes = new ArrayList<String>();
		fileTypes.add(".xsd");
		return fileTypes;
	}

	/** Returns the schema elements from the specified URI */
	public ArrayList<SchemaElement> generateSchemaElements() throws ImporterException
		{ return new ArrayList<SchemaElement>(_schemaElementsHS.values()); }


	/**
	 * getRootElements:  Processes the SimpleTypes, ComplexTypes, and Elements 
	 * defined at the "root" (Schema) level.
	 * 
	 * @param xmlSchema schema to be processed
	 */
	public void getRootElements(Schema xmlSchema) {
		
		// Each root SimpleType should be translated into a Domain
		Collection<SimpleType> simpleTypes = xmlSchema.getSimpleTypes();
		for (SimpleType simpleType : simpleTypes)
			processSimpleType(simpleType, null);
		
		// Each root ComplexType should be translated into an Entity
		Collection<ComplexType> complexTypes = xmlSchema.getComplexTypes();
		for (ComplexType complexType : complexTypes)
			processComplexType( complexType, null);
		
		// Each root Element should be translated into a Containment (with schema as parent)
		Collection<ElementDecl> elements = xmlSchema.getElementDecls();
		for (ElementDecl element : elements)
			processElement( element, null);
	}
	
	/**
	 * processSimpleType: creates M3 Domain for the passed SimpleType 
	 * (or finds references to existing Domain if type seen before)
	 * and adds this domain as child of passed Containment or Attribute
	 * 
	 * @param passedType XML SimpleType which needs to either be processed 
	 * or referenced if already seen
	 * @param parent M3 Containment or Attribute to which domain for passed 
	 * simpleType should be added as child
	 */
	public void processSimpleType (XMLType passedType, SchemaElement parent)
	{		
		// assign the default type of String
		String typeName = "StringDef" + " ";
		if ((passedType != null) && (passedType.getName() != null) && (passedType.getName().length() > 0)) 
			typeName = passedType.getName() + " ";
		
		// handle "Any" type
		if (passedType != null && passedType instanceof AnyType)
			typeName = "Any" + " ";
		
		// handle IDREF / IDREFS -- generate relationship to "Any" entity
		if (parent instanceof Attribute && (typeName.equals("IDREF") || typeName.equals("IDREFS"))){
		
			if (this.anyEntity == null)
				this.anyEntity = new Entity(nextAutoInc(),"ANY","ANY ENTITY",0);
			_schemaElementsHS.put(this.anyEntity.hashCode(),this.anyEntity);
			
			Integer rightMax = ( typeName.equals("IDREFS") ) ? null : 1;   
			Relationship rel = new Relationship(nextAutoInc(),parent.getName(),"",((Attribute)parent).getEntityID(),0,1,this.anyEntity.getId(),0,rightMax,0);
			_schemaElementsHS.put(rel.hashCode(),rel);
			
			/** remove the attribute if type ANY is involved **/
			_schemaElementsHS.remove(parent.getId());
			_schemaElems.remove(parent.getId());
			
		}
		else {
	
			// find Domain for SimpleType (generated if required)
			Domain domain = _domainList.get(typeName);

			boolean isEnumeration = passedType != null && passedType instanceof SimpleType && !(passedType instanceof Union) && ((SimpleType)passedType).hasFacet("enumeration");
			boolean isBaseDomain =  domain != null && _baseDomainSet.contains(typeName.toLowerCase());
			if (domain == null || (isEnumeration && isBaseDomain)) {
				if (isEnumeration && (passedType.getName()== null || passedType.getName().isEmpty())) {
					typeName = "";
				}
				 domain = new Domain(nextAutoInc(), typeName, (passedType == null ? "" : this.getDocumentation(passedType)), 0);
				 if (!isBaseDomain) {
					 _domainList.put(domain.getName(),domain);
				 }
				_schemaElementsHS.put(domain.hashCode(), domain);
				
				if (isEnumeration){
					// create DomainValues (if specified for SimpleType)
					Enumeration<?> facets = ((SimpleType)passedType).getFacets("enumeration");

					while (facets.hasMoreElements()) {
						Facet facet = (Facet) facets.nextElement();
						DomainValue domainValue = new DomainValue(nextAutoInc(), facet.getValue(), this.getDocumentation(facet), domain.getId(), 0);
						_schemaElementsHS.put(domainValue.hashCode(), domainValue);
					}
				}
				
				// TODO: process Union Types
				else if (passedType != null && passedType instanceof Union){
					Union passedUnion = (Union)passedType;
					Enumeration<?> memberTypes = passedUnion.getMemberTypes();
					while (memberTypes.hasMoreElements()){
						SimpleType childType = (SimpleType)memberTypes.nextElement();
						
						// create a subtype to capture union
						Subtype subtype = new Subtype(nextAutoInc(),domain.getId(),-1,0);
						_schemaElementsHS.put(subtype.hashCode(), subtype);
						processSimpleType(childType,subtype);
					}
				}
			}
	 

			if (parent instanceof Attribute)
				((Attribute)parent).setDomainID(domain.getId());
			else if (parent instanceof Containment)
				((Containment)parent).setChildID(domain.getId());
			else if (parent instanceof Subtype)
				((Subtype)parent).setChildID(domain.getId());
		}
	} // end method processSimpleType


	private static HashMap<String,Entity> _attrGroupEntitySet = new HashMap<String,Entity>();
	
	/**
	 * processComplexType: creates M3 Entity for the passed ComplexType 
	 * (or finds references to existing Entity if type seen before)
	 * and adds this entity as child of passed Containment or Subtype
	 * 
	 * NOTE:  This method can support handling Attributes which are 
	 * simpleContent by creating an additional simpleContent attribute.
	 * The necessary code for handling simpleContent is currently commented
	 * out.
	 * 
	 * @param passedType XML ComplexType which needs to either be processed 
	 * or referenced if already seen
	 * 
	 * @param parent M3 Containment or Subtype to which entity for passed 
	 * complexType should be added as child
	 */
	public void processComplexType (ComplexType passedType, SchemaElement parent)
	{
		
		// check to see if entity has been created for passed complex type
		// create new Entity if none has been created 
		Entity entity = new Entity(nextAutoInc(), passedType.getName(), this.getDocumentation(passedType), 0);
		
		if (_schemaElementsHS.containsKey(passedType.hashCode()) == false) 
		{
			_schemaElementsHS.put(passedType.hashCode(), entity);
			_schemaElems.put(passedType.hashCode(), passedType);
				
			try 
			{
				// get Attributes for current complexType
				Enumeration<?> attrGroupReferences = passedType.getAttributeGroupReferences();
				
				while (attrGroupReferences.hasMoreElements())
				{					
					AttributeGroupReference attrGroupRef = (AttributeGroupReference)attrGroupReferences.nextElement();
					Entity attrGroupEntity = new Entity(nextAutoInc(),attrGroupRef.getReference(),"attr group",0);
				
					if (_attrGroupEntitySet.containsKey(attrGroupEntity.getName()) == false)
					{
						_attrGroupEntitySet.put(attrGroupEntity.getName(), attrGroupEntity);
						_schemaElementsHS.put(attrGroupEntity.getId(), attrGroupEntity);
						
						Enumeration<?> attrs = attrGroupRef.getAttributes();
					
					//	boolean sawSimpleContentVal = false;
						
						while (attrs.hasMoreElements()){
						
							AttributeDecl attrDecl = (AttributeDecl)attrs.nextElement();
							_seenAttrsInAttrGroup.add(attrDecl.hashCode());
							try {
								while (attrDecl != null && attrDecl.isReference() == true && attrDecl.getReference() != null)
								attrDecl = attrDecl.getReference();
							} catch(IllegalStateException e){} // handle malformed XSDs that do not have parent set (depreciated attrs as parents)
						
							boolean containsID = attrDecl.getSimpleType() != null && attrDecl.getSimpleType().getName() != null && attrDecl.getSimpleType().getName().equals("ID");
						
							Integer attrID = nextAutoInc();
							Attribute attr = new Attribute(attrID,(attrDecl.getName() == null ? "" : attrDecl.getName()),getDocumentation(attrDecl),attrGroupEntity.getId(),-1,(attrDecl.isRequired()? 1 : 0), 1, containsID, 0); 
					//		if (attr.getName().equalsIgnoreCase("simpleContentValue")){
					//			sawSimpleContentVal = true;
					//		}
						
							_schemaElementsHS.put(attrID, attr);
							_schemaElems.put(attrID, attrDecl);
						
							processSimpleType(attrDecl.getSimpleType(), attr);
						
					
						} // while attrs left
					
					//	/** process simpleContent by creating special attr **/
					//	if (passedType.isSimpleContent()){
					//		Integer attrID = nextAutoInc();
					//		Attribute simpleContentAttr = new Attribute(attrID,(sawSimpleContentVal ? "simpleContentValue2" : "simpleContentValue"),"added attribute to handle simpleContent",attrGroupEntity.getId(),-1, 0, 1, false, 0);  
					//		_schemaElementsHS.put(attrID, simpleContentAttr);
					//		_schemaElems.put(attrID, passedType);
					//		processSimpleType(null, simpleContentAttr);
					//	}
						
					} // end if -- processing simple content
					
					/** create subtype **/
					attrGroupEntity = _attrGroupEntitySet.get(attrGroupEntity.getName());
					Integer subTypeID = nextAutoInc();
					Subtype subType = new Subtype(subTypeID,attrGroupEntity.getId(),entity.getId(),0);
					_schemaElementsHS.put(subTypeID, subType);
					
				} // while attr groups left
			
				Enumeration<?> attrDecls = passedType.getAttributeDecls(); 
				// boolean sawSimpleContentVal = false;
				while (attrDecls.hasMoreElements()){
				
					AttributeDecl attrDecl = (AttributeDecl)attrDecls.nextElement();
					
					/** check to see if attributes have already been processed **/
					if (!_seenAttrsInAttrGroup.contains(attrDecl.hashCode())){
					
						try {
							while (attrDecl != null && attrDecl.isReference() == true && attrDecl.getReference() != null)
								attrDecl = attrDecl.getReference();
						} catch(IllegalStateException e){} // handle malformed XSDs that do not have parent set (depreciated attrs as parents)
						
						boolean containsID = attrDecl.getSimpleType() != null && attrDecl.getSimpleType().getName() != null && attrDecl.getSimpleType().getName().equals("ID");
						
						Integer attrID = nextAutoInc();
						Attribute attr = new Attribute(attrID,(attrDecl.getName() == null ? "" : attrDecl.getName()),getDocumentation(attrDecl),entity.getId(),-1,(attrDecl.isRequired()? 1 : 0), 1, containsID, 0); 
					//	if (attr.getName().equalsIgnoreCase("simpleContentValue")){
					//		sawSimpleContentVal = true;
					//	}
				
						_schemaElementsHS.put(attrID, attr);
						_schemaElems.put(attrID, attrDecl);
						
						processSimpleType(attrDecl.getSimpleType(), attr);
					}
					
				//	if (passedType.isSimpleContent()){
				//		Integer attrID = nextAutoInc();
				//		Attribute simpleContentAttr = new Attribute(attrID,(sawSimpleContentVal ? "simpleContentValue2" : "simpleContentValue"),"added attribute to handle simpleContent",entity.getId(),-1, 0, 1, false, 0);  
				//		_schemaElementsHS.put(attrID, simpleContentAttr);
				//		_schemaElems.put(attrID, passedType);
				//		processSimpleType(null, simpleContentAttr);
				//	}
				}	
			} catch (IllegalStateException e){}
			
			
			/** get Elements for current complexType **/
			Enumeration<?> elementDecls = passedType.enumerate();
			while (elementDecls.hasMoreElements()) {
				Group group = (Group)elementDecls.nextElement();
				processGroup(group, entity);
			}
		
			/** get SuperTypes for current complexType **/
			if (passedType.getBaseType() != null){
				XMLType baseType = passedType.getBaseType();
				
				/** process simpleType supertype here -- create a "special" Entity **/
				if (baseType instanceof SimpleType){
					Subtype subtype = new Subtype(nextAutoInc(),-1,entity.getId(),0);
					_schemaElementsHS.put(subtype.hashCode(), subtype);
					
					Entity simpleSuperTypeEntity = new Entity(nextAutoInc(), (baseType.getName() == null ? "" : baseType.getName()), this.getDocumentation(baseType), 0);
					if (_schemaElementsHS.get(baseType.hashCode()) == null){
						_schemaElementsHS.put(baseType.hashCode(), simpleSuperTypeEntity);
						_schemaElems.put(baseType.hashCode(), baseType);
					}
					simpleSuperTypeEntity = (Entity)_schemaElementsHS.get(baseType.hashCode());
					subtype.setParentID(simpleSuperTypeEntity.getId());
				}
				else if (baseType instanceof ComplexType){
					Subtype subtype = new Subtype(nextAutoInc(),-1, entity.getId(),0);
					_schemaElementsHS.put(subtype.hashCode(), subtype);
					processComplexType((ComplexType)baseType, subtype);
				}	
			}	
		}
		
		/** add Entity for complexType as child of passed containment or subtype **/ 
		entity = (Entity)_schemaElementsHS.get(passedType.hashCode());
		
		if (parent instanceof Containment && parent != null)
			((Containment)parent).setChildID(entity.getId());
		else if (parent instanceof Subtype && parent != null)
			((Subtype)parent).setParentID(entity.getId());
				
	} // end method	
		
	
	/**
	 * processGroup:  Processes a grouping of elements in a ComplexType. 
	 * The Elements in a ComplexType are contained in 1 or more Groups, 
	 * each of which is processed by this method.
	 * 
	 * @param group Element Group to be processed 
	 * @param parent Entity corresponding to complexType
	 */
	public void processGroup (Group group, Entity parent){
		
		// step through item in a group
		Enumeration<?> e = group.enumerate();
		while (e.hasMoreElements()) {
				
			Object obj = e.nextElement();
			
			// For WildCard, create containment child to "Any" domain
			if (obj instanceof Wildcard){
				Domain anyDomain = _domainList.get("Any");
				Containment containment = new Containment(nextAutoInc(),"Any", this.getDocumentation((Annotated)obj), parent.getId(), anyDomain.getId(), 0, 1, 0);
				_schemaElementsHS.put(containment.hashCode(), containment);
				_schemaElems.put(containment.hashCode(),obj);
				
			}
			// process Group item
			else if (obj instanceof Group)
				processGroup((Group)obj, parent);	
			
			// process Element item
			else if (obj instanceof ElementDecl)  
				processElement((ElementDecl)obj, parent);
			
			else
				System.err.println("(E) XSDImporter:processGroup -- Encountered object named " + obj.toString() + " with unknown type " + obj.getClass());
							
		}
	} // end method

	
	/**
	 * processElement:  Creates an M3 Containment corresponding to the Element declaration in
	 * a ComplexType.  Parent of containment will be passed Entity, and the child will be either 
	 * M3 Entity for specified complexType or M3 Domain for specified simpleType.
	 * 
	 * @param elementDecl Element declaration in XSD ComplexType
	 * @param parent Entity corresponding to complexType containing elementDecl
	 */
	public void processElement(ElementDecl elementDecl, Entity parent)
	{
		/** dereference xs:ref until we find actual element declarations **/
		Integer origMin = elementDecl.getMinOccurs();
		Integer origMax = elementDecl.getMaxOccurs();
		Integer origHashcode = elementDecl.hashCode();
		ElementDecl origElementDecl = elementDecl;
		try {
			while (elementDecl.isReference() && elementDecl.getReference() != null)
				elementDecl = elementDecl.getReference();
		} catch (IllegalStateException e) {}{}
		
		/** if containment references elements in same namespace, leave alone **/
		if (origElementDecl.getSchema().getTargetNamespace().equals(
				elementDecl.getSchema().getTargetNamespace()))
		{
			Containment containment = new Containment(nextAutoInc(),elementDecl.getName(),this.getDocumentation(elementDecl),((parent != null) ? parent.getId() : null),-1,origMin,origMax,0);
		
			if (_schemaElementsHS.containsKey(origHashcode) == false){
				_schemaElementsHS.put(origHashcode, containment);
				_schemaElems.put(origHashcode,elementDecl);
			}

			XMLType childElementType = null;
			try { 
				childElementType = elementDecl.getType();
			} catch (IllegalStateException e){} 
			if ((childElementType == null) || (childElementType instanceof SimpleType) || (childElementType instanceof AnyType)) 				
				processSimpleType(childElementType, containment);
	
			else if (childElementType instanceof ComplexType)
				processComplexType((ComplexType)childElementType,containment);

			else 
				System.err.println("(E) XSDImporter:processElement -- Encountered object named " 
					+ elementDecl.getName() + " with unknown type " 
					+  ((childElementType == null)? null : childElementType.getClass()));
		}
		else {
			
			/** create Containment for Element **/  		
			Containment origContainment = new Containment(nextAutoInc(),origElementDecl.getName(),this.getDocumentation(elementDecl),((parent != null) ? parent.getId() : null),-1,origMin,origMax,0);
			if (_schemaElementsHS.containsKey(origContainment.hashCode()) == false)
			{
				_schemaElementsHS.put(origContainment.hashCode(), origContainment);
				_schemaElems.put(origContainment.hashCode(),origElementDecl);
			}

			XMLType childElementType = null;
			try { 
				childElementType = origElementDecl.getType();
			} catch (IllegalStateException e){} 
			
			if ((childElementType == null) || (childElementType instanceof SimpleType) || (childElementType instanceof AnyType)) 				
				processSimpleType(childElementType, origContainment);
	
			else if (childElementType instanceof ComplexType)
				processComplexType((ComplexType)childElementType,origContainment);

			else 
				System.err.println("(E) XSDImporter:processElement -- Encountered object named " 
					+ origElementDecl.getName() + " with unknown type " 
					+  ((childElementType == null)? null : childElementType.getClass()));
					
			/** This assumes the referenced element is a top-level element **/
			Containment refContainment = new Containment(nextAutoInc(),elementDecl.getName(),this.getDocumentation(elementDecl),null,-1,origMin,origMax,0);
			if (_schemaElementsHS.containsKey(refContainment.hashCode()) == false)
			{
				_schemaElementsHS.put(refContainment.hashCode(), refContainment);
				_schemaElems.put(refContainment.hashCode(),elementDecl);
			}

			XMLType childElementType2 = null;
			try { 
				childElementType2 = elementDecl.getType();
			} catch (IllegalStateException e){} 
			
			if ((childElementType2 == null) || (childElementType2 instanceof SimpleType) || (childElementType2 instanceof AnyType)) 				
				processSimpleType(childElementType2, refContainment);
	
			else if (childElementType2 instanceof ComplexType)
				processComplexType((ComplexType)childElementType2,refContainment);

			else 
				System.err.println("(E) XSDImporter:processElement -- Encountered object named " 
					+ elementDecl.getName() + " with unknown type " 
					+  ((childElementType2 == null)? null : childElementType2.getClass()));
			
			/** create subtype **/
			//Subtype subtype = new Subtype(nextAutoInc(),origContainment.getId(),refContainment.getId(),0);
			//_schemaElementsHS.put(subtype.hashCode(), subtype);
		}
		
	} // end method

	
	/**
	 * getDocumentation: Get the documentation associated with specified element
	 * @param element element to get documentation about
	 * @return The documentation associated with a specific element
	 */
	private String getDocumentation(Annotated element) {
		
		StringBuffer documentation = new StringBuffer("");
		documentation.append(appendDocumentation(element));
		return documentation.toString();
	}

	/**
	 * appendDocumentation: Get the documentation associated with specified type
	 * @param type type to get documentation about
	 * @return The documentation associated with a specific element
	 */
	@SuppressWarnings("unchecked")
	private StringBuffer appendDocumentation(Annotated type) {
		StringBuffer documentation = new StringBuffer();
		if (type != null) {
			Enumeration annotations = type.getAnnotations();
			while (annotations.hasMoreElements()) {
				Annotation annotation = (Annotation) annotations.nextElement();
				Enumeration docs = annotation.getDocumentation();
				while (docs.hasMoreElements()) {
					Documentation doc = (Documentation) docs.nextElement();
					if (doc.getContent() != null)
						documentation.append(doc.getContent().replaceAll("<",
								"&lt;").replaceAll(">", "&gt;").replaceAll("&",
								"&amp;"));
				}
			}
		}
		return documentation;
	}

	
	/**
	 * Function for loading the preset domains into the Schema and into a list
	 * for use during Attribute creation
	 */
	private void loadDomains() {
		for (int i = 0; i < baseDomains.length; i++){
			Domain domain = new Domain(nextAutoInc(), baseDomains[i][0], baseDomains[i][1], 0);
			_schemaElementsHS.put(domain.hashCode(), domain);
			_domainList.put(baseDomains[i][0].trim(),  domain);		
			_baseDomainSet.add(baseDomains[i][0].toLowerCase());

		}

	}
	
} // end XSDImporter class

/** Private class for sorting schema elements */
//class SchemaElementComparator implements Comparator<SchemaElement>
//{
//	public int compare(SchemaElement element1, SchemaElement element2)
//	{
//		// Retrieve the base schemas for the specified elements
//		Integer base1 = element1.getBase(); if(base1==null) base1=-1;
//		Integer base2 = element2.getBase(); if(base2==null) base2=-1;
//		
//		// Returns a comparator value for the compared elements
//		if(!base1.equals(base2))
//			return base1.compareTo(base2);
//		if(element1.getClass()!=element2.getClass())
//			return element1.getClass().toString().compareTo(element2.getClass().toString());
//		return element1.getId().compareTo(element2.getId());
//	}
//}

