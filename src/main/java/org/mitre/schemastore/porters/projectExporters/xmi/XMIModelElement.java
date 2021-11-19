package org.mitre.schemastore.porters.projectExporters.xmi;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An element in an XMIModel.  Typically used for UML classes.
 * @author DMALLEN
 */
public class XMIModelElement extends XMIExportable {
	String name;
	String id; 	
	String type;	
	String documentation;
	HashMap <String,String> metadata = new HashMap<String,String>(); 
	ArrayList <XMIModelElement> children;
	ArrayList <XMIModelElement> generalizations; 
	
	public XMIModelElement() { 
		this(XMIModel.randomName());
	}
	
	public XMIModelElement(String name) { 
		this(name, UML_CLASS); 
	} // End XMIModelElement
	
	public XMIModelElement(String name, String type) { 
		this.name = name;
		this.type = type;
		this.documentation = null;
		this.id = "EAID_" + newID();
		
		generalizations = new ArrayList <XMIModelElement> ();
		children = new ArrayList <XMIModelElement> (); 
	} // End XMIModelElement
	
	public void setDocs(String documentation) { 
		this.documentation = documentation;
	}
	
	public boolean contains(XMIModelElement elem) { 
		if(children.contains(elem)) return true;
		
		for(int x=0; x<children.size(); x++) { 
			if(children.get(x).contains(elem)) return true;
		}
		
		return false;
	} // End contains
	
	public String toString() { return name + " (" + type + ")"; } 
	public String getDocs() { return documentation; } 	
	public String getType() { return type; } 
	public boolean hasDocs() { 
		return documentation != null && !"".equals(documentation);
	}
	
	public void setId(String id) { 
		this.id = id; 
	}
	
	public XMIModelElement getChild(int idx) { 
		return children.get(idx); 
	}
	
	public void addChild(XMIModelElement child) { 
		children.add(child);
	}
	
	public XMIModelElement getGeneralization(int idx) { 
		return generalizations.get(idx); 
	}
	
	public void addGeneralization(XMIModelElement gen) { 
		generalizations.add(gen);
	}
	
	/**
	 * @see XMIExportable#renderAsXML(Document)
	 */
	public Element renderAsXML(Document doc) { 
		return renderAsXML("packagedElement", doc); 
	}
	
	/**
	 * @see XMIExportable#renderExtensionXML(XMIModel, Document)
	 */
	public Element renderExtensionXML(XMIModel model, Document doc) {
		System.out.println("XMI: (element/extensions) " + name + " " + id);
		Element e = doc.createElement("element"); 
		e.setAttribute("xmi:idref", id); 
		e.setAttribute("xmi:type", type); 
		e.setAttribute("name", name); 
		e.setAttribute("scope", "public"); 

		Element md = doc.createElement("model");
		md.setAttribute("package", "");
		md.setAttribute("tpos", "0");
		md.setAttribute("ea_eleType", "element"); 
		XMIModelElement pkg = model.getPackageOwner(this); 
		if(pkg != null) md.setAttribute("package", pkg.id); 
		else md.setAttribute("package", "null");

		// Store this for later...
		int eaid = XMIModel.getEA_LOCALID();
		metadata.put("ea_localid", ""+eaid);  
		md.setAttribute("ea_localid", ""+eaid); 
		
		e.appendChild(md); 
					
		Element props = doc.createElement("properties"); 
		props.setAttribute("documentation", getDocs());
		props.setAttribute("isSpecification", "false");
		props.setAttribute("sType", type.substring(type.indexOf(":")+1));
		props.setAttribute("nType", "0");
		props.setAttribute("scope", "public");
		props.setAttribute("isRoot", "false");
		props.setAttribute("isLeaf", "false");
		props.setAttribute("isAbstract", "false"); 
		props.setAttribute("isActive", "false"); 
		String stName = model.lookupStereotype(this); 
		if(stName != null) props.setAttribute("stereotype", XMIModel.xmlName(stName));
		e.appendChild(props);
		
		return e; 
	} // End renderExtensionXML
	
	public Element renderAsXML(String tagName, Document doc) { 
		Element e = doc.createElement(tagName);
		
		System.out.println("XMI: (element/xml) " + name + " " + id);
		
		e.setAttribute("xmi:type", type);
		e.setAttribute("xmi:id", id); 		 
		e.setAttribute("name", name); 
		e.setAttribute("visibility", "public"); 
		
		for(XMIModelElement gen : generalizations) {
			//<generalization xmi:type="uml:Generalization" xmi:id="(X)" general="(Y)"/>
			Element g = doc.createElement("generalization");
			g.setAttribute("xmi:type", "uml:Generalization");
			g.setAttribute("xmi:id", "EAID_" + newID());
			g.setAttribute("general", gen.id); 
			e.appendChild(g); 
		} // End for
		
		for(XMIModelElement child : children) {
			if(UML_CLASS.equals(type))
				e.appendChild(child.renderAsXML("nestedClassifier", doc));
			else e.appendChild(child.renderAsXML(doc)); 
		} // End for
		
		return e;
	} // End renderAsXML	
} // End XMIModelElement
