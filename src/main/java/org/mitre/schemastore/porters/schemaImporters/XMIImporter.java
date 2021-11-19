package org.mitre.schemastore.porters.schemaImporters;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.mitre.schemastore.client.Repository;
import org.mitre.schemastore.client.SchemaStoreClient;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.ImporterException.ImporterExceptionType;
import org.mitre.schemastore.porters.URIType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Importer for XMI models exported from the EA tool.
 * 
 * @author DMALLEN, HAOLI
 * 
 *         THIS IS AN INITIAL COMMIT ONLY. THIS CODE DOES NOT YET WORK -- STILL
 *         NEED TO TALK TO OPENII FOLKS TO FIX A NUMBER OF PROBLEMS.
 * 
 *         Please keep this in CVS for the moment so that John Castleberry and I
 *         can collaborate on getting this right.
 */
public class XMIImporter extends SchemaImporter {
	private HashMap<String, SchemaElement> elements = new HashMap<String, SchemaElement>();
	// private HashMap<String, Association> associations = new HashMap<String,
	// Association>();
	private HashMap<String, Domain> domains = new HashMap<String, Domain>();

	private void processDomain(Node n) throws Exception {
		String nodeName = n.getNodeName();
		String ref = n.getAttributes().getNamedItem("base_Class").getNodeValue();
		SchemaElement referentElement = elements.get(ref);
		String domainName = null;

		if (referentElement == null) {
			System.err.println("No referent for domain " + nodeName + " with base_Class " + ref);
			return;
		}

		if (nodeName.contains("thecustomprofile")) domainName = nodeName.substring(nodeName.indexOf(":") + 1);
		else if (nodeName.contains("EAUML:table")) domainName = referentElement.getName();
		else return; 
		
		System.out.println( " === DOMAIN for " + referentElement.getName() + " = " + domainName); 
		
		// Entities cannot have 
		String description = referentElement.getDescription(); 
		String newDescrString = (description.length()==0)? "" : description.concat("\n"); 
		referentElement.setDescription( newDescrString.concat("(Value type: " + domainName + ")") );

		
		
		// The following code doesn't work because an Entity for a vocabulary
		// cannot have domain types.
		Domain domain = domains.get(domainName);
		if (domain == null) {
			domain = new Domain(nextId(), domainName, "UML Stereotype imported from EA", 0);
			domains.put(domainName, domain);
			// There is no way to define a domain type for an entity.
		}

	} // End processDomain

	private void processExtensionElement(Node element) throws Exception {
		NodeList children = element.getChildNodes();
		String xmiID = element.getAttributes().getNamedItem("xmi:idref").getNodeValue();
		SchemaElement schemaElement = elements.get(xmiID);
		// System.out.println("Extension element " + xmiID);
		if (schemaElement == null) {
			System.err.println("No schema element for XMI ID " + xmiID);
			return;
		}

		for (int x = 0; x < children.getLength(); x++) {
			Node n = children.item(x);
			System.out.println("ExtElement child " + n.getNodeName());
			if (n.getNodeName().equals("properties")) {
				try {
					String docs = n.getAttributes().getNamedItem("documentation").getNodeValue();
					System.out.println("DOCUMENTATION for " + n.getNodeName() + ": " + docs);
					String existingDescription = schemaElement.getDescription(); 
					String newDescription = (existingDescription.length()==0) ? "" : existingDescription.concat("\n"); 
					schemaElement.setDescription(newDescription.concat(docs));
				} catch (NullPointerException e) {
					;
				}
			} else if (n.getNodeName().equals("links")) {
				NodeList links = n.getChildNodes();
				for (int y = 0; y < links.getLength(); y++) {
					Node linkNode = links.item(y);
					// <Aggregation> for containment relationships
					if ("Aggregation".equals(linkNode.getNodeName())) {
						String aggId = linkNode.getAttributes().getNamedItem("xmi:id").getNodeValue();
						String parentID = linkNode.getAttributes().getNamedItem("end").getNodeValue();
						String childID = linkNode.getAttributes().getNamedItem("start").getNodeValue();

						SchemaElement parent = elements.get(parentID);
						SchemaElement child = elements.get(childID);

						if (parent == null || child == null) {
							System.err.println("MISSING SCHEMA ELEMENTS: parent=" + parent + " child=" + child);
							continue;
						}

						if (elements.get(aggId) == null) {
							elements.put(aggId, new Containment(nextId(), "[" + parent.getName() + "->" + child.getName() + "]", "xmi:id=" + aggId, parent.getId(), child.getId(), 0, 1, 0));
							// remove the element with a [root]->elmeent containment relationship 
							if (elements.containsKey(childID + ".containment")) elements.remove(childID + ".containment");
						}
					} else if ("Generalization".equals(linkNode.getNodeName())) {
						// <generalizaton> tags
						String genId = linkNode.getAttributes().getNamedItem("xmi:id").getNodeValue();
						String parentID = linkNode.getAttributes().getNamedItem("end").getNodeValue();
						String childID = linkNode.getAttributes().getNamedItem("start").getNodeValue();

						SchemaElement parent = elements.get(parentID);
						SchemaElement child = elements.get(childID);

						if (parent == null || child == null) {
							System.err.println("MISSING SCHEMA ELEMENTS: parent=" + parent + " child=" + child);
							continue;
						}
						// Create a subtype for generalization
						if (elements.get(genId) == null) elements.put(genId, new Subtype(nextId(), parent.getId(), child.getId(), 0));

					}
				}
			}
		}
	}

	private SchemaElement processClass(Node pe, SchemaElement parent) {
		String xmiID = pe.getAttributes().getNamedItem("xmi:id").getNodeValue();
		String name = pe.getAttributes().getNamedItem("name").getNodeValue();
		String documentation = "";
		Entity entity = new Entity(nextId(), name, documentation, 0);
		if (!elements.containsKey(xmiID)) elements.put(xmiID, entity);

		// For the elements with no parent, make them be contained under the
		// root
		if (parent == null && (!elements.containsKey(xmiID + ".containment"))) elements.put(xmiID + ".containment", new Containment(nextId(), "[" + name + "]", "", null, entity.getId(), 0, 1, 0));

		return entity;
	}

	// private SchemaElement processAssociation(Node pe) {
	// SchemaElement result = null;
	// // String associationID =
	// pe.getAttributes().getNamedItem("xmi:id").getNodeValue();
	//
	// // Association association = associations.get(associationID);
	// // if (association == null) associations.put(associationID, association =
	// new Association(associationID, null, null));
	//
	// ArrayList<String> memberEndIDs = new ArrayList<String>();
	// NodeList nl = pe.getChildNodes();
	// for (int x = 0; x < nl.getLength(); x++) {
	// Node n = nl.item(x);
	//
	// if (n.getNodeName().equals("memberEnd")) {
	// String xmiID =
	// n.getAttributes().getNamedItem("xmi:idref").getNodeValue();
	// memberEndIDs.add(xmiID);
	// } else if (n.getNodeName().equals("ownedEnd")) {
	// // String srcID =
	// n.getChildNodes().item(1).getAttributes().getNamedItem("xmi:idref").getNodeValue();
	// // association.parentXmiId = srcID;
	// }
	// }
	//
	// return result;
	// } // End processAssociation

	private void processPackagedElement(Node pe, SchemaElement parent, String saughtType) throws Exception {
		SchemaElement here = null;
		String nodeType = pe.getAttributes().getNamedItem("xmi:type").getNodeValue();
		if (!nodeType.equals("uml:Package") && !nodeType.equals(saughtType)) return;

		if (saughtType.equals("uml:Class") && nodeType.equals(saughtType)) here = processClass(pe, parent);
		// else if (saughtType.equals("uml:Association")) here =
		// processAssociation(pe);

		for (int x = 0; x < pe.getChildNodes().getLength(); x++) {
			Node child = pe.getChildNodes().item(x);
			if (child.getNodeName().equals("packagedElement")) {
				processPackagedElement(child, here, saughtType);
			}
		}
	}

	public URIType getURIType() {
		return URIType.FILE;
	}

	public String getDescription() {
		return "This importer can be used to import schemas from an XMI format";
	}

	public String getName() {
		return "XMI Importer";
	}

	public ArrayList<String> getFileTypes() {
		ArrayList<String> fileTypes = new ArrayList<String>();
		fileTypes.add(".xmi");
		fileTypes.add(".xml");
		return fileTypes;
	}

	protected void initialize() throws ImporterException {
		elements = new HashMap<String, SchemaElement>();

		try {
			// Build XML document
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new File(uri));

			// define root element,
			Node docRoot = doc.getChildNodes().item(0);
			Node model = null;
			Node ext = null;

			// define model and extension nodes
			NodeList nl = docRoot.getChildNodes();
			for (int x = 0; x < nl.getLength(); x++) {
				Node n = nl.item(x);
				if (n.getNodeName().equals("uml:Model")) model = n;
				if (n.getNodeName().equals("xmi:Extension")) ext = n;

				if (model != null && ext != null) break;
			}

			// Process packagedElements, which contain both classes and domains
			NodeList pes = model.getChildNodes();
			for (int x = 0; x < pes.getLength(); x++) {
				Node n = pes.item(x);
				if (n.getNodeName().equals("packagedElement")) {
					processPackagedElement(n, null, "uml:Class");
				} else if (n.getNodeName().contains("thecustomprofile") || n.getNodeName().equals("EAUML:table")) {
					processDomain(n);
				}
			}

			System.out.println("processed " + elements.size() + " so far");

			// Process association
			// for (int x = 0; x < pes.getLength(); x++) {
			// Node n = pes.item(x);
			// if (n.getNodeName().equals("packagedElement")) {
			// processPackagedElement(n, null, "uml:Association");
			// }
			// }

			// System.out.println("processed " + elements.size() + " so far");

			// Process Extension elements
			NodeList extE = ext.getChildNodes();
			for (int x = 0; x < extE.getLength(); x++) {
				Node extNode = extE.item(x);
				if (extNode.getNodeName().equals("elements")) {
					// Process element only
					NodeList sub = extNode.getChildNodes();
					for (int y = 0; y < sub.getLength(); y++) {
						Node s = sub.item(y);
						if (s.getNodeName().equals("element")) processExtensionElement(s);
					}
				}
			}
			System.out.println("processed " + elements.size() + " so far");

		} catch (Exception e) {
			e.printStackTrace();
			throw new ImporterException(ImporterExceptionType.PARSE_FAILURE, e.getMessage());
		}
	}

	@Override
	protected ArrayList<SchemaElement> generateSchemaElements() throws ImporterException {
		System.out.println("Generating schema elements for XMI...");

		return new ArrayList<SchemaElement>(elements.values());
	}

	public static void main(String[] args) throws Exception {
		// Display the schemas found within the repository
		try {
			Repository repository = new Repository(Repository.POSTGRES,new URI("ygg.org.mitre.org"),"d3","postgres","postgres");
			SchemaStoreClient client = new SchemaStoreClient(repository);
			
			XMIImporter importer = new XMIImporter(); 
			importer.setClient(client); 
			importer.importSchema("TED", "haoli", "test xmi importer", new URI("file:///C:/TEDVocabulary.xml")); 
		}
		catch(Exception e) { e.printStackTrace(); }
	}
}

class Association {
	String xmiId;
	String parentXmiId; // container
	String childXmiId; // child

	Association(String assocID, String parentID, String childID) {
		this.xmiId = assocID;
		this.parentXmiId = parentID;
		this.childXmiId = childID;
	}

	public String toString() {
		return new String("(XMI_ID: "+xmiId + ")" );
	}
}
