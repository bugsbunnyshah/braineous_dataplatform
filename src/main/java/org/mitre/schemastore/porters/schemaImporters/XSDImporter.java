// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.porters.schemaImporters;

import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.exolab.castor.xml.schema.writer.SchemaWriter;
import org.mitre.schemastore.client.Repository;
import org.mitre.schemastore.client.SchemaStoreClient;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.ImporterException.ImporterExceptionType;
import org.mitre.schemastore.porters.URIType;


/**
 * XSDImporter: Class for importing XSD files into the M3 Format
 * 
 * @author DBURDICK
 */

public class XSDImporter extends SchemaImporter
{
	
	/** testing main 
	 * @throws ClassNotFoundException **/ 
	public static void main(String[] args) throws URISyntaxException, ImporterException, ClassNotFoundException{
		XSDImporter xsdImporter = new XSDImporter();
		/** set the web proxy to import schemas on internet (if needed) **/
		 try {
	        String proxyHost = new String("gatekeeper.org.mitre.org");
	        String proxyPort = new String("80");
           System.getProperties().put( "http.proxyHost",proxyHost );
           System.getProperties().put( "http.proxyPort",proxyPort );
          
	     }catch (Exception e) {
	     
	          	String message = new String("[E] XSDImporter -- " + 
	          			"Likely a security exception - you " +
	                		"must allow modification to system properties if " +
	                		"you want to use the proxy");
	          	e.printStackTrace();
	          	
				throw new ImporterException(ImporterExceptionType.PARSE_FAILURE,message);		 
	     }

		Repository repository = null;
		try {
			repository = new Repository(Repository.DERBY,new URI("C:/Users/mgreer"), "org/mitre/schemastore","postgres","postgres");
		} catch (URISyntaxException e2) {
			e2.printStackTrace();
		}		
		try {
			xsdImporter.client = new SchemaStoreClient(repository);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
			
		// Initialize the importer
		xsdImporter.uri = new URI("C:/Users/mgreer/share/schemas/niem/niem-core/3.0/niem-core.xsd");
		//xsdImporter.uri = new URI("C:/Users/mgreer/share/schemas/niem/domains/maritime/3.0/maritime.xsd");
		//xsdImporter.uri = new URI("C:/Users/mgreer/share/devel/contlearn/accumulo/config/xsd/PanoptesAccumuloConf.xsd");
		xsdImporter.initialize();
	/*	for (SchemaElement elem : xsdImporter.generateSchemaElements()) {
			System.out.println(elem.getName() + ": " + elem.getDescription() + " - " + elem.getClass().toString());
		}*/
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
	
	/** Stores the unique "Any" entity **/
	private Entity anyEntity;

	private static HashSet<Integer> _seenAttrsInAttrGroup = new HashSet<Integer>();

	private static HashMap<String,Entity> _attrGroupEntitySet = new HashMap<String,Entity>();
	private static HashSet<String> _baseDomainSet;
	
	public XSDImporter() {
		super();
		baseDomains = new String[][]{{ANY + " ", "The Any wildcard domain"},
		{INTEGER + " ","The Integer domain"},
		{REAL + " ","The Real domain"},
		{STRING + " ","The String domain"},
		{"string" + " ","The string domain"},
		{DATETIME + " ","The DateTime domain"},
		{BOOLEAN + " ","The Boolean domain"}};
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
			_attrGroupEntitySet = new HashMap<String,Entity>();
			_seenAttrsInAttrGroup = new HashSet<Integer>();
			_baseDomainSet = new HashSet<String>();
			
			/** Preset domains and then process this schema **/
			loadDomains();
			
			/** create DOM tree for main schema **/
			SchemaReader xmlSchemaReader = new SchemaReader(uri.toString());
			Schema mainSchema = xmlSchemaReader.read();
			Writer stringWriter = new StringWriter();
		//	Writer fileWriter = new FileWriter("C:\\Users\\mgreer\\test.xsd");
			SchemaWriter xmlSchemaWriter = new SchemaWriter(stringWriter);
			xmlSchemaWriter.write(mainSchema);
		/*	int i =0;
			String output = stringWriter.toString();
		
			while (i< output.length()) {
				String substring;
			
				if (i + 80 >output.length()){
					substring = output.substring(i);
				}else {
					substring = output.substring(i, i + 80);
				}
				int j = substring.lastIndexOf(' ');
				if (j != -1) {
					System.out.println(substring.substring(0,j)); 
				}
				else {
					j=79;
					System.out.println(substring);
				}
				i += j+1;
			} */
		//	System.out.println(stringWriter.toString());
			
			getRootElements(mainSchema);
			

		}
		catch(Exception e) { 			
			e.printStackTrace();
			throw new ImporterException(ImporterExceptionType.PARSE_FAILURE,e.getMessage()); 
		}
	}

	/*************************************************************************
	 * Rest of XSDImporter 
	 * ***********************************************************************
	 */
	
	/** Returns the importer name */
	public String getName()
		{ return "XSD Importer"; }
	
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
		for  (SimpleType simpleType :simpleTypes)
			processSimpleType(simpleType, null);
		
		// Each root ComplexType should be translated into an Entity
		Collection<ComplexType> complexTypes = xmlSchema.getComplexTypes();
		for (ComplexType complexType : complexTypes)
			processComplexType(complexType, null);
		
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
		String typeName = ANY + " ";
		if ((passedType != null) && (passedType.getName() != null) && (passedType.getName().length() > 0)) 
			typeName = passedType.getName() + " ";
		
		// handle "Any" type
		if (passedType != null && passedType instanceof AnyType)
			typeName = ANY + " ";
		
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
				Domain anyDomain = _domainList.get(ANY + " ");
				Containment containment = new Containment(nextAutoInc(),ANY, this.getDocumentation((Annotated)obj), parent.getId(), anyDomain.getId(), 0, 1, 0);
				_schemaElementsHS.put(containment.hashCode(), containment);
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
		
	
		Containment containment = new Containment(nextAutoInc(),elementDecl.getName(),this.getDocumentation(elementDecl),((parent != null) ? parent.getId() : null),-1,origMin,origMax,0);
	
		if (_schemaElementsHS.containsKey(origHashcode) == false){
			_schemaElementsHS.put(origHashcode, containment);
			
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
	
		
	} // end method

	
	/**
	 * getDocumentation: Get the documentation associated with specified element
	 * @param element element to get documentation about
	 * @return The documentation associated with a specific element
	 */
	private String getDocumentation(Annotated element) {
		
		StringBuffer documentation = new StringBuffer("");
		documentation.append(appendDocumentation(element));
		
		// post-process documentation string to remove 
		String retVal = documentation.toString();
		retVal = retVal.replaceAll("\\s+", " ");
		
		return retVal;
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

