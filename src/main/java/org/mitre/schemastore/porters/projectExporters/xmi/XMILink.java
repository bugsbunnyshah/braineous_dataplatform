package org.mitre.schemastore.porters.projectExporters.xmi;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A link between two XMIModelElements.  Mostly, this is used to render UML Associations.
 * @author DMALLEN
 */
public class XMILink extends XMIModelElement {
	public XMIModelElement from;
	public XMIModelElement to;
	String id;
	String subID; 
	
	/** For XML serialization -- a bunch of default settings that most elements get */
	protected static final Hashtable <String,String> defaultAttributes;
	
	static { 
		defaultAttributes = new Hashtable<String,String>();
		defaultAttributes.put("visibility", "public");
		defaultAttributes.put("isOrdered", "false");
		defaultAttributes.put("isDerived", "false");
		defaultAttributes.put("isUnique", "true"); 
		defaultAttributes.put("isDerivedUnion", "false");
		defaultAttributes.put("aggregation", "none"); 		
	}

	private Element copyDefaults(Element elem) { 
		Enumeration <String>e = defaultAttributes.keys();
		while(e.hasMoreElements()) {  
			String k = e.nextElement();
			elem.setAttribute(k, defaultAttributes.get(k));
		}
		return elem;
	}
	
	public XMILink() { 
		super(); 
	}
	
	public XMILink(XMIModelElement from, XMIModelElement to) { 
		this(from, to, "DefaultLinkName"); 
	}
	
	public XMILink(XMIModelElement from, XMIModelElement to, String name) { 
		this.from = from;
		this.name = name;
		this.to = to;
		this.type = UML_ASSOCIATION; 
		String n = newID();
		this.id = "EAID_" + n;
		this.subID = n.substring(2); 
	} // End XMILink
	
	public void setId(String id) { 
		this.id = id;  
		System.out.println("New ID: " + id); 
		this.subID = id.substring(7); 
		System.out.println("New subID: " + subID); 
	}
	
	/**
	 * Documentation on XMILinks is rendered as part of the xmi:Extension section.
	 */
	public Element renderExtensionXML(XMIModel model, Document doc) {
		System.out.println("XMI: (link/extensions) " + name); 
		Element conn = doc.createElement("connector");
		conn.setAttribute("xmi:idref", id); 
		
		Element src = doc.createElement("source");
		src.setAttribute("xmi:idref", from.id); 
		Element dst = doc.createElement("target");
		dst.setAttribute("xmi:idref", to.id);
		
		conn.appendChild(src); 
		conn.appendChild(dst);
		
		Element mods = doc.createElement("modifiers"); 
		conn.appendChild(mods);
		mods.setAttribute("isRoot", "false"); 
		mods.setAttribute("isLeaf", "false"); 
		
		Element props = doc.createElement("properties"); 
		props.setAttribute("ea_type", "Association"); 
		props.setAttribute("direction", "Unspecified");
				
		Element docs = doc.createElement("documentation"); 
		docs.setAttribute("value", getDocs()); 
		conn.appendChild(docs); 

		return conn;
	}
	
	public Element renderAsXML(Document doc) { 
		/* SAMPLE TARGET FORMAT
		 	<packagedElement xmi:type="uml:Association" xmi:id="EAID_E9D0B000_06D2_486e_8CF2_4163F6CB598C" visibility="public">
				<memberEnd xmi:idref="EAID_dstD0B000_06D2_486e_8CF2_4163F6CB598C"/>
				<memberEnd xmi:idref="EAID_srcD0B000_06D2_486e_8CF2_4163F6CB598C"/>
				<ownedEnd xmi:type="uml:Property" xmi:id="EAID_srcD0B000_06D2_486e_8CF2_4163F6CB598C" 
				          visibility="public" association="EAID_E9D0B000_06D2_486e_8CF2_4163F6CB598C" 
				          isOrdered="false" isDerived="false" isDerivedUnion="false" aggregation="none">
					<type xmi:idref="EAID_A9AF5AF1_8006_4f25_A2A0_D4DB75C7FFFB"/>
				</ownedEnd>
				<ownedEnd xmi:type="uml:Property" xmi:id="EAID_dstD0B000_06D2_486e_8CF2_4163F6CB598C" 
				          visibility="public" association="EAID_E9D0B000_06D2_486e_8CF2_4163F6CB598C" 
				          isOrdered="false" isDerived="false" isDerivedUnion="false" aggregation="none">
					<type xmi:idref="EAID_A02C0914_3453_49dc_9D50_3E73C4AC4983"/>
				</ownedEnd>
			</packagedElement>
		 */
		System.out.println("XMI: (link/xml) " + name + " " + id); 
		Element e = doc.createElement("packagedElement");
		e.setAttribute("xmi:type", UML_ASSOCIATION); 
		// Castleberry didn't want link names for associations.
		//if(name != null) e.setAttribute("name", name);
		
		e.setAttribute("xmi:id", id);
		e.setAttribute("visibility", "public"); 
			
		String srcID = "EAID_src" + subID;
		String dstID = "EAID_dst" + subID; 
		
		Element memberEnd1 = doc.createElement("memberEnd"); 
		memberEnd1.setAttribute("xmi:idref", dstID); 
		 
		Element memberEnd2 = doc.createElement("memberEnd"); 
		memberEnd2.setAttribute("xmi:idref", srcID); 
		
		Element ownedEnd1 = doc.createElement("ownedEnd");
		//ownedEnd1.setAttribute("name", "mapsTo"); 
		ownedEnd1.setAttribute("xmi:type", UML_PROPERTY);  
		ownedEnd1.setAttribute("association", id); 
		ownedEnd1.setAttribute("xmi:id", srcID);  
		//if(name != null) ownedEnd1.setAttribute("name", name); 
		ownedEnd1 = copyDefaults(ownedEnd1); 
		Element t1 = doc.createElement("type"); 
		t1.setAttribute("xmi:idref", to.id); 
		ownedEnd1.appendChild(t1); 
		
		Element ownedEnd2 = doc.createElement("ownedEnd");
		ownedEnd2.setAttribute("xmi:type", UML_PROPERTY); 
		//ownedEnd2.setAttribute("name", "mapsTo");
		if(name != null) ownedEnd2.setAttribute("name", name); 
		ownedEnd2.setAttribute("association", id); 
		ownedEnd2.setAttribute("xmi:id", dstID);  
		ownedEnd2 = copyDefaults(ownedEnd2);
		Element t2 = doc.createElement("type");
		t2.setAttribute("xmi:idref", from.id); 
		ownedEnd2.appendChild(t2); 
		
		e.appendChild(memberEnd1);
		e.appendChild(memberEnd2);
		
		e.appendChild(ownedEnd1);
		e.appendChild(ownedEnd2);
			
		return e;
	} // End renderAsXML	
} // End XMILink
