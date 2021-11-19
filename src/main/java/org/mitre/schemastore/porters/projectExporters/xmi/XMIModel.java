package org.mitre.schemastore.porters.projectExporters.xmi;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * An XMI model.  Typically, these are modeled as UML Packages.  Basically, these are just
 * containers for other XMIModelElements, and they hold and track UML steroetypes.
 * @author DMALLEN
 */
public class XMIModel extends XMIModelElement {
	Hashtable <String,ArrayList <XMIModelElement>> stereotypes; 
	private static int EA_LOCALID = 20;  
	
	public XMIModel(String name) { 
		super(name, UML_PACKAGE); 
		stereotypes = new Hashtable <String,ArrayList<XMIModelElement>>();
		id = "EAPK_" + newID(); 
	}
	
	public static int getEA_LOCALID() { 
		return EA_LOCALID++; 
	}
	
	public boolean hasStereotypes() { 
		return stereotypes.size() > 0; 
	}
	
	public String lookupStereotype(XMIModelElement elem) { 
		Enumeration <String>en = stereotypes.keys();
		
		while(en.hasMoreElements()) {
			String k = en.nextElement();
			ArrayList <XMIModelElement> arr = stereotypes.get(k); 
			if(arr.contains(elem)) return k;
		}
		
		return null;
	}
	
	public void addStereotype(String sName, XMIModelElement target) { 
		ArrayList <XMIModelElement> l = stereotypes.get(sName);
		if(l == null) { 
			l = new ArrayList <XMIModelElement>();
			stereotypes.put(sName, l); 
		}
		
		l.add(target); 
	} // End addStereotype
	
	/**
	 * Testing method.  Generates a simple model containing three hierarchies with mappings.
	 * @return a sample model.
	 */
	public static XMIModel generateSimple() { 
		XMIModel m = new XMIModel("Class Model");
		
		XMIModelElement core = new XMIModelElement("Core", UML_PACKAGE); 
		XMIModelElement humint = new XMIModelElement("HUMINT", UML_PACKAGE);
		XMIModelElement ted = new XMIModelElement("TED", UML_PACKAGE); 

		m.addChild(core);
		m.addChild(humint); 
		m.addChild(ted); 
		
		XMIModelElement corePerson = new XMIModelElement("Person");  core.addChild(corePerson); 
		XMIModelElement humintInd = new XMIModelElement("Individual");  humint.addChild(humintInd); 
		XMIModelElement tedPerson = new XMIModelElement("Person"); ted.addChild(tedPerson); 
				
		XMIModelElement coreEvil = new XMIModelElement("EvilPerson"); corePerson.addChild(coreEvil); 
		XMIModelElement humintEvil = new XMIModelElement("EvilIndividual"); humintInd.addChild(humintEvil); 
		XMIModelElement tedEvil = new XMIModelElement("EvilPerson"); tedPerson.addChild(tedEvil); 
		
		String def = "<b>Definition:</b> foo";
		corePerson.setDocs(def);
		coreEvil.setDocs(def);
		humintInd.setDocs(def);
		humintEvil.setDocs(def);
		tedPerson.setDocs(def);
		tedEvil.setDocs(def); 
		
		XMIModelElement [][] links = new XMIModelElement [][] { 
				{ corePerson, tedPerson },
				{ corePerson, humintInd },
				{ coreEvil, tedEvil },
				{ coreEvil, humintEvil }
		};
		
		for(int x=0; x<links.length; x++) { 
			XMILink l = new XMILink(links[x][0], links[x][1], "Good Match");
			l.setDocs("Some <a href='http://slashdot.org'>link</a> documentation for link from " + links[x][0].name + 
					" to " + links[x][1].name); 			
			core.addChild(l); 
		}
		
		coreEvil.addGeneralization(corePerson); 
		humintEvil.addGeneralization(humintInd); 
		tedEvil.addGeneralization(tedPerson); 
		
		m.addStereotype("CORE_L1", corePerson);
		m.addStereotype("CORE_L2", coreEvil); 
		m.addStereotype("HUMINT_L1", humintInd); 
		m.addStereotype("HUMINT_L2", humintEvil); 
		m.addStereotype("TED_L1", tedPerson); 
		m.addStereotype("TED_L2", tedEvil); 
		
		return m;		
	} // End generateSimple
		
	public XMIModelElement getPackageOwner(XMIModelElement em) { 
		for(int x=0; x<children.size(); x++) { 
			if(UML_PACKAGE.equals(children.get(x).type) && 
			   children.get(x).contains(em)) return children.get(x); 
		}
		return null;
	} // End getPackageOwner
	
	/**
	 * Get a flattened list of all objects that have a child relationship to this model.
	 * @return an array list of XMIModelElement objects.
	 */
	public ArrayList <XMIModelElement> getCatalog() { 
		ArrayList <XMIModelElement> arr = new ArrayList <XMIModelElement> ();
		
		arr.addAll(children);
		int lastIDX = 0;
		int inspected = 0; 
		while(true) {
			boolean didSomething = false;
			if(lastIDX >= arr.size()) break; 
			
			for(int x=lastIDX; x<arr.size(); x++) { 
				XMIModelElement e = arr.get(x); 
				ArrayList <XMIModelElement> s = e.children;
				for(XMIModelElement e2 : s) { 
					if(arr.contains(e2)) continue;
					else { 
						didSomething = true; 
						arr.add(e2); 
					}
				}
				inspected = x;
			}
			lastIDX = inspected;
			if(!didSomething) break;
		}
		
		return arr;
	} // End getCatalog()
	
	public static String xmlName(String name) { 
		return name.replace(" ", "_"); 
	}
	
	public Element [] renderExtensionsElement(Document doc) {
		System.out.println("XMI: (model/extensions) " + name); 
		Element [] results = new Element [2];
		Element elements = doc.createElement("elements");
		results[0] = elements;
		Element connectors = doc.createElement("connectors");
		results[1] = connectors; 
		
		ArrayList <XMIModelElement> catalog = getCatalog(); 
		int t=0;
		for(XMIModelElement elem : catalog) {
			if(!elem.hasDocs()) continue;
			if(elem instanceof XMILink) {
				Element conn = elem.renderExtensionXML(this, doc); 
				connectors.appendChild(conn); 
			} else {  
				Element e = elem.renderExtensionXML(this, doc);
				elements.appendChild(e); 
			} // End else 
			
			System.out.println("XMIModel: Rendered extension " + ++t + " of " + catalog.size());
		} // End for
		
		return results; 
	} // End renderExtensionsXML
	
	public Element renderAsXML(String tagName, Document doc) {
		Element e = super.renderAsXML(tagName, doc); 
		System.out.println("XMI: (model/xml) " + name);
		Enumeration <String>en = stereotypes.keys();
		while(en.hasMoreElements()) { 
			String k = en.nextElement();
			ArrayList <XMIModelElement> items = stereotypes.get(k); 
			
			for(XMIModelElement i : items) { 
				Element st = doc.createElement("thecustomprofile:" + xmlName(k));
				st.setAttribute("base_Class", i.id); 
				e.appendChild(st); 
			} // End for
		} // End while
		
		Element prof = doc.createElement("uml:Profile"); 
		e.appendChild(prof); 
		
		prof.setAttribute("xmi:version", "2.1"); 
		prof.setAttribute("xmlns:uml", "http://schema.omg.org/spec/UML/2.1/");
		prof.setAttribute("xmi:id", "thecustomprofile"); 
		prof.setAttribute("nsPrefix", "thecustomprofile"); 
		prof.setAttribute("name", "thecustomprofile"); 
		prof.setAttribute("metamodelReference", "mmref01"); 
		 
		Element oc = doc.createElement("ownedComment");
		oc.setAttribute("xmi:type", UML_COMMENT); 
		oc.setAttribute("xmi:id", "comment01"); 
		oc.setAttribute("annotatedElement", "thecustomprofile");
		Element ocb = doc.createElement("body"); 
		ocb.appendChild(doc.createTextNode(" Version:1.0"));
		oc.appendChild(ocb); 
		prof.appendChild(oc); 

		Element pi = doc.createElement("packageImport"); 
		pi.setAttribute("xmi:id", "mmref01"); 
		prof.appendChild(pi); 
		Element pi_ip = doc.createElement("importedPackage");
		pi.appendChild(pi_ip); 
		pi_ip.setAttribute("href", "http://schema.omg.org/spec/UML/2.1/");
		
		Enumeration <String>sNames = stereotypes.keys();
		while(sNames.hasMoreElements()) { 
			String sName = sNames.nextElement(); 
			Element pe = doc.createElement("packagedElement"); 
			pe.setAttribute("xmi:type", UML_STEREOTYPE); 
			pe.setAttribute("xmi:id", xmlName(sName)); 
			pe.setAttribute("xmi:name", xmlName(sName));
			prof.appendChild(pe); 
			Element oa = doc.createElement("ownedAttribute");
			oa.setAttribute("xmi:type", UML_PROPERTY); 
			oa.setAttribute("xmi:id", xmlName(sName) + "-base_Class");
			oa.setAttribute("name", "base_Class");
			oa.setAttribute("association", "Class_" + xmlName(sName)); 
						
			Element oatype = doc.createElement("type");
			oatype.setAttribute("href", "http://schema.omg.org/spec/UML/2.1/#Class");
			oa.appendChild(oatype); 
			
			Element peExt = doc.createElement("packagedElement");
			prof.appendChild(peExt); 
			peExt.setAttribute("xmi:type", UML_EXTENSION); 
			peExt.setAttribute("xmi:id", "Class_" + xmlName(sName)); 			
			peExt.setAttribute("name", "A_Class_" + xmlName(sName));
			peExt.setAttribute("memberEnd", "extension_" + xmlName(sName) + " " + xmlName(sName) + 
							   "-base_Class"); 
		} // End while
		
		return e;
	} // End renderAsXML	
} // End XMIModel
