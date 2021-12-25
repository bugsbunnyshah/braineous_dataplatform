// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.porters.schemaImporters;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


import org.mitre.schemastore.client.Repository;
import org.mitre.schemastore.client.SchemaStoreClient;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.ImporterException.ImporterExceptionType;
import org.mitre.schemastore.porters.URIType;

import com.hp.hpl.jena.ontology.CardinalityRestriction;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.MaxCardinalityRestriction;
import com.hp.hpl.jena.ontology.MinCardinalityRestriction;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.DC;

/**
 * Imports an OWL into the Yggdrasil repository
 * 
 * Caveat: -- The converter doesn't handle multiple imported repository --
 * Convert object properties with multiple domains and ranges by connecting
 * whatever relationship available -- Does not handle restrictions
 * 
 * @author HAOLI
 * 
 */

public class OWLImporter extends SchemaImporter implements RDFErrorHandler {
	// Defines the additional owl domain types
	public static final String FLOAT = "Float";
	public static final String DATE = "Date";
	public static final String TIME = "Time";

	private OntModel _ontModel;
	private ArrayList<SchemaElement> _schemaElements = new ArrayList<SchemaElement>();
	private HashMap<String, Entity> _entityList = new HashMap<String, Entity>();
	private HashMap<String, Domain> _domainList = new HashMap<String, Domain>(); // <owl
	private HashMap<String, PropertyWrapper> _cardinality = new HashMap<String, PropertyWrapper>();																				// domain,
																					// ygg
																					// domain>
	/** testing main **/ 
	public static void main(String[] args) throws URISyntaxException, ImporterException{
		OWLImporter owlImporter = new OWLImporter();
		String basePath = "file:/Users/mgreer/share/schemas/SA-TT/";
		String filePath = basePath + "SA-TT_Ontology_V1-1_20140219.owl";
	//	String filePath = basePath + "test.owl";
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
			repository = new Repository(Repository.DERBY,new URI("C:/Temp/"), "org/mitre/schemastore","postgres","postgres");
		} catch (URISyntaxException e2) {
			e2.printStackTrace();
		}		
		try {
			owlImporter.client = new SchemaStoreClient(repository);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
			
		// Initialize the importer
		owlImporter.uri = new URI(filePath);
		//xsdImporter.uri = new URI("C:/tempSchemas/niem-2.1/niem/domains/maritime/2.1/maritime.xsd");
		owlImporter.initialize();
		Collection<SchemaElement> elems = owlImporter.generateSchemaElements();
	/*	for (SchemaElement elem : elems)
		{
			System.out.println(elem.getId() + " " +elem.getName() + " " + elem.getDescription() + " " + ((elem instanceof Attribute)?((Attribute)elem).getDomainID():""));
		}*/
		
	}
	public OWLImporter () {
		super();
		baseDomains = new String[][] {{ANY.toLowerCase(), "The Any wildcard domain"},
		{INTEGER.toLowerCase(), "The Integer domain"},
		{FLOAT.toLowerCase(), "The Float domain"},
		{STRING.toLowerCase(), "The String domain"},
		{BOOLEAN.toLowerCase(), "The Boolean domain"},
		{DATETIME.toLowerCase(), "The DateTime domain"},
		{DATE.toLowerCase(), "The Date domain"},
		{TIME.toLowerCase(), "The Time domain"}};
		
	}
	/** Returns the importer name */
	public String getName() {
		return "OWL Importer";
	}

	/** Returns the importer description */
	public String getDescription() {
		return "This importer can be used to import schemas from an owl format";
	}

	/** Returns the importer URI type */
	public URIType getURIType() {
		return URIType.FILE;
	}

	/** Returns the importer URI file types */
	public ArrayList<String> getFileTypes() {
		ArrayList<String> fileTypes = new ArrayList<String>();
		fileTypes.add(".owl");
		fileTypes.add(".rdf");
		fileTypes.add(".rdfs");
		return fileTypes;
	}

	/** Initializes the importer for the specified URI */
	protected void initialize() throws ImporterException {
		_schemaElements = new ArrayList<SchemaElement>();
		_entityList = new HashMap<String, Entity>();
		_domainList = new HashMap<String, Domain>();

		try {
			loadDomains();
			initializeOntModel(uri);

		} catch (Exception e) {
			throw new ImporterException(ImporterExceptionType.PARSE_FAILURE, e.getMessage());
		}
	}

	@Override
	/** Returns the schema elements from the specified URI */
	protected ArrayList<SchemaElement> generateSchemaElements() throws ImporterException
	{
		linearGen();
		return _schemaElements;
	}

	/** Handles the loading of the specified domain */
	private void loadDomain(String name, String description) {
		Domain domain = new Domain(nextId(), name, description, 0);
		_schemaElements.add(domain);
		_domainList.put(name, domain);
	}

	/** Handles the loading of all default domains */
	private void loadDomains() {
		for (int i = 0; i < baseDomains.length; i++) {
			loadDomain(baseDomains[i][0], baseDomains[i][1]);
		}
	}

	/** Initializes the ontology model */
	private void initializeOntModel(URI uri) throws MalformedURLException, IOException {
		// Determine what version of owl to use
		String language = "";
		if (uri.toString().endsWith(".owl")) language = "http://www.w3.org/2002/07/owl#";
		else if (uri.toString().endsWith(".rdf")) language = "http://www.w3.org/2000/01/rdf-schema#";
		else if (uri.toString().endsWith(".rdfs")) language = "http://www.w3.org/2000/01/rdf-schema#";

		// Create a stream to read from and a model to read into.
		InputStream ontologyStream = uri.toURL().openStream();
		_ontModel = ModelFactory.createOntologyModel(language);
		String proxyHost = System.getProperty("http.proxyHost");
		String proxyPort = System.getProperty("http.proxyPort");
		if (proxyHost != null) {
			System.setProperty("proxySet", "true");
			System.setProperty("proxyHost", proxyHost);
			System.setProperty("proxyPort", proxyPort);
		}
		else {
			System.setProperty("proxySet", "false");
		}
		

		_ontModel.setDynamicImports(true);

		// Use Jena to read from the stream into the model.
		RDFReader reader = _ontModel.getReader();

		reader.setErrorHandler(this);
		reader.read(_ontModel, ontologyStream, uri.toString());
		ontologyStream.close();
	}

	private void linearGen() {
		convertClasses();
		getCardinalities();
		convertDatatypeProperties();
		convertObjectProperties();
		convertIsARelationships();
	}

	private void getCardinalities() {
        
        for ( ExtendedIterator<Restriction> rs = 
_ontModel.listRestrictions();
rs.hasNext() ; ) {
                Restriction r = rs.next();
                String pName = r.getOnProperty().getNameSpace() + r.getOnProperty().getLocalName();
                ExtendedIterator iter = null;
                Integer[] cards = new Integer[2];
                if ( r.isMaxCardinalityRestriction() ) {
                        MaxCardinalityRestriction mcr = 
r.asMaxCardinalityRestriction();
                        cards[1] = mcr.getMaxCardinality();
                        
                       
                        iter = mcr.getOnProperty().listDomain();
                       
                }
                else if ( r.isMinCardinalityRestriction() ) {
                    MinCardinalityRestriction mcr = 
r.asMinCardinalityRestriction();
                    cards[0] = mcr.getMinCardinality();
                    
                    
                    iter = mcr.getOnProperty().listDomain();
            }
                else if ( r.isCardinalityRestriction() ) {
                    CardinalityRestriction mcr = 
r.asCardinalityRestriction();
                    cards[0] = mcr.getCardinality();
                    cards[1] = mcr.getCardinality();
                    
                    
                    iter = mcr.getOnProperty().listDomain();
               
                    
            }
                if (iter != null) {
                	PropertyWrapper wrapper = _cardinality.get(pName);
                	if (wrapper == null) {
                		wrapper = new PropertyWrapper(pName);
                		_cardinality.put(pName, wrapper);
                	}
                	while (iter.hasNext()){
                		OntClass domainClass = (OntClass) iter.next();
                		String dName = null;
                		if (domainClass.getLocalName() != null) {
                			dName = domainClass.getNameSpace() + domainClass.getLocalName();
            
                		}
                		wrapper.addCardinalityForClass(dName, cards[0], cards[1]);
                	}
                }
        }

		
	}
	private void convertIsARelationships() {
		// Iterate through all classes
		ExtendedIterator classes = _ontModel.listClasses();
		while (classes.hasNext()) {
			OntClass ontClass = (OntClass) classes.next();

			// stop if ontClass is null
			if (ontClass.getLocalName() == null) continue;

			// create a subType edge for for each direct parent class
			Entity entity = _entityList.get(ontClass.getLocalName());
			ExtendedIterator subClasses = ontClass.listSubClasses(true);
			while (subClasses.hasNext()) {
				OntClass child = (OntClass) subClasses.next();
				Entity childEntity = _entityList.get(child.getLocalName());
				if (childEntity == null) continue;
//				System.out.println(entity.getName() + ": " + childEntity.getName());
				Subtype subtype = new Subtype(nextId(), entity.getId(), childEntity.getId(), childEntity.getBase());
				_schemaElements.add(subtype);
			}
		}
	}

	// convert all datatypeProperties
	private void convertDatatypeProperties() {
		ExtendedIterator dataProperties = _ontModel.listDatatypeProperties();
		while (dataProperties.hasNext()) {
			DatatypeProperty dataProp = (DatatypeProperty) dataProperties.next();
			String pName = dataProp.getNameSpace() + dataProp.getLocalName();
			PropertyWrapper propCardinality = _cardinality.get(pName);
			/*		if (dataProp.getLocalName().equalsIgnoreCase("hasproblemtype")||dataProp.getLocalName().equalsIgnoreCase("hasproblemcategory")||dataProp.getLocalName().equalsIgnoreCase("hassanotifyid")) {
				System.out.println(dataProp.getLocalName());
				System.out.println(dataProp.toString());
				System.out.println(dataProp.getDomain());
				System.out.println(dataProp.listRange());
			}*/


			// create an attribute for each domain the data property belongs to
			// DEBUG jean doesn't seem to return a valid domain for multiple
			// domains
			//		ExtendedIterator containerClsItr = dataProp.listDeclaringClasses(true);
			//		if (!containerClsItr.hasNext()){
			ExtendedIterator		containerClsItr = dataProp.listDomain();


			//	}

			if (containerClsItr.hasNext()) {
				Domain domain = convertRangeToDomain(dataProp);

				while (containerClsItr.hasNext()) {

					OntClass containerCls = (OntClass) containerClsItr.next();
					Entity entity = _entityList.get(containerCls.getLocalName());
					String dName = null;
					if (containerCls.getLocalName()!= null) {
						dName = containerCls.getNameSpace() +containerCls.getLocalName();
					}

					Statement description = dataProp.getProperty(DC.description);
					String comment = dataProp.getComment(null);

					if (description == null){
						if (comment==null) comment= "";
					}
					else{
						comment = description.getString();
					}

					if (entity != null) {
						Attribute attribute = new Attribute(nextId(), dataProp.getLocalName(), comment, entity.getId(), domain.getId(), getMinCardinality(propCardinality, dName), getMaxCardinality(propCardinality, dName), false, 0);
						_schemaElements.add(attribute);
					}
				}

			}
		}
	}

	// convert all objectProperties to relationships
	private void convertObjectProperties() {
		ExtendedIterator objProperties = _ontModel.listObjectProperties();

		while (objProperties.hasNext()) {
			ObjectProperty objProp = (ObjectProperty) objProperties.next();
			String pName = objProp.getNameSpace() + objProp.getLocalName();
			PropertyWrapper propCardinality = _cardinality.get(pName);
			/*
			 * If an object property has multiple classes defined in its range,
			 * the range is taken as an conjunction of all the classes (as
			 * opposed to independent classes). Jena returns a null named class
			 * for the collection. Here we create a new class that's named by
			 * concatenating all classes names. DEBUG This may need to be
			 * changed to conjunctive relationships
			 */
			ExtendedIterator objPropDomainItr = objProp.listDomain();
			while (objPropDomainItr.hasNext()) {
				// LEFT entity

				OntClass leftOntClass = (OntClass) objPropDomainItr.next();
				if (leftOntClass == null || leftOntClass.getLocalName() == null) {
					System.err.println("\t &&&& null left entity for " + objProp.getLocalName());
					continue;
				}

				Entity leftEntity = _entityList.get(leftOntClass.getLocalName());
				if (leftEntity == null) {
					System.out.println("Entity not found: " + leftOntClass.getLocalName());
					continue;
				}
        		//System.out.println("\tleft Entity for " + objProp.getLocalName() + " = " + leftOntClass.getLocalName());
        		String dName = leftOntClass.getNameSpace() + leftOntClass.getLocalName();
        		Integer minCard = getMinCardinality(propCardinality, dName);
        		Integer maxCard = getMaxCardinality(propCardinality, dName);
				// RIGHT entity
				ExtendedIterator objPropRangeItr = objProp.listRange();
				while (objPropRangeItr.hasNext()) {
					OntResource rightOntResource = (OntResource) objPropRangeItr.next();
					if (rightOntResource == null || !rightOntResource.canAs(OntClass.class)) {
						System.out.println("\t" + objProp.getLocalName() + " is an objectProperty but its range is null :( ");
						continue;
					}
					OntClass rightCls = objProp.getRange().asClass();
					Entity rightEntity = _entityList.get(rightOntResource.getLocalName());
					if (rightEntity == null) {
						System.out.println("Entity not found: " + rightOntResource.getLocalName());
						continue;
					}
				//	System.out.println("\tright = " + rightCls.getLocalName() + "=>" + rightCls.getLocalName());
					
					

					int relMin = minCard == null?0:minCard;
					int relMax = maxCard == null?-1:maxCard;
			
					//System.out.println("cardinality = " + relMin);
					//System.out.println("cardinality = " + relMax);

					Integer leftClsMin = null;
					Integer leftClsMax = null;
					Integer rightClsMin = relMin;
					Integer rightClsMax = relMax;
					Statement statement = objProp.getProperty(DC.description);
					String comment = statement==null?"":statement.getString();
					Relationship rel = new Relationship(nextId(), objProp==null?"":objProp.getLocalName(), comment, leftEntity.getId(), leftClsMin, leftClsMax, rightEntity.getId(), rightClsMin, rightClsMax, 0);
					_schemaElements.add(rel);
				}
			}
		}
	}

	// convert all classes to entities
	private void convertClasses() {
		ExtendedIterator classes = _ontModel.listClasses();
		while (classes.hasNext()) {
			OntClass ontClass = (OntClass) classes.next();
			if (ontClass.getLocalName() == null) continue;
			String comment = ontClass.getComment(null);
			Statement desc = ontClass.getProperty(DC.description);
			if ( desc == null) {
				if (comment == null) {
					comment = "";
				}
			}
			else comment = desc.getString();

			Entity entity = new Entity(nextId(), ontClass.getLocalName(), comment, 0);
			_entityList.put(entity.getName(), entity);
			_schemaElements.add(entity);

		}
	}

	// converts a data property range to domain.
	private Domain convertRangeToDomain(OntProperty dataProp) {
		OntResource propType = dataProp.getRange();
		Domain domain = null;
		if (propType != null){
		if (propType.getLocalName() == null) {
			
		//	System.out.println(propType.getRDFType().getLocalName());
			if (propType.asClass().isEnumeratedClass()) {
				domain = new Domain(nextId(),"","",0);
				_schemaElements.add(domain);


				ExtendedIterator<RDFNode> iter = propType.asClass().asEnumeratedClass().getOneOf().iterator();

				while (iter.hasNext()) {
					RDFNode enumType = iter.next();

					DomainValue d = new DomainValue(nextId(),enumType.toString(), "", domain.getId(),0);
					
					_schemaElements.add(d);
				}
				
			}
		
		}
		else {
			domain = _domainList.get(propType.getLocalName().toLowerCase());
			if (domain == null) {
				Statement description = propType.getProperty(DC.description);
				String comment = propType.getComment(null);
				
				if (description == null){
					if (comment==null) comment= "";
				}
				else{
					comment = description.getString();
				}

				domain = new Domain(nextId(), propType.getLocalName(), comment, 0);
				_schemaElements.add(domain);
				_domainList.put(domain.getName().toLowerCase(), domain);
			}
		}
		}
		if (domain == null) domain = _domainList.get(ANY.toLowerCase());
	//	System.out.println(domain.getName());
		return domain;
	}

	// Handles RDF Errors
	public void warning(Exception e) {}

	public void error(Exception e) {
		System.err.println(e.getMessage());
	}

	public void fatalError(Exception e) {
		System.err.println(e.getMessage());
	}
	
	private int getMinCardinality(PropertyWrapper wrapper, String dName) {
		if (wrapper == null) {
			return 0;
		}

		return wrapper.getMinCardinality(dName);
	}
	private int getMaxCardinality(PropertyWrapper wrapper, String dName) {
		if (wrapper == null) {
			return -1;
		}
		return wrapper.getMaxCardinality(dName);
	}
	private static class PropertyWrapper {
		private String propName;
		private HashMap<String, Integer> minCardinality = new HashMap<String, Integer>();
		private HashMap<String, Integer> maxCardinality = new HashMap<String, Integer>();
		private int defaultMinCardinality = 0;
		private int defaultMaxCardinality = -1;
		public PropertyWrapper(String propName) {
			this.propName = propName;
		}
		public PropertyWrapper(String propName, int min, int max) {
			this(propName);
			defaultMinCardinality = min;
			defaultMaxCardinality = max;
		}
		public void addCardinalityForClass(String className, Integer min, Integer max) {
			if (className == null) {
				if (min != null) {
					defaultMinCardinality = min.intValue();
				}
				if (max != null) {
					defaultMaxCardinality = max.intValue();
				}
			}
			else {
			if (min != null){
				minCardinality.put(className, min);
			}
			if (max != null) {
				maxCardinality.put(className, max);
			}
			}
		}
		public int getMinCardinality(String className) {
			Integer min = minCardinality.get(className);
			if (min == null) {
				return defaultMinCardinality;
			}
			return min;
		}
		public int getMaxCardinality(String className) {
			Integer max = maxCardinality.get(className);
			if (max == null) {
				return defaultMaxCardinality;
			}
			return max;
		}
		public String getPropertyName() {
			return propName;
		}
		
	}
}
