package org.mitre.schemastore.porters.projectExporters.xmi;

import java.util.Random;
import java.util.UUID;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An object that is exportable as XMI.
 * @author DMALLEN
 */
public abstract class XMIExportable {
	public static final String UML_PROPERTY = "uml:Property";
	public static final String UML_CLASS = "uml:Class";
	public static final String UML_PACKAGE = "uml:Package";
	public static final String UML_MODEL = "uml:Model"; 
	public static final String UML_COMMENT = "uml:Comment";
	public static final String UML_STEREOTYPE = "uml:Stereotype";
	public static final String UML_EXTENSION = "uml:Extension";
	public static final String UML_ASSOCIATION = "uml:Association";
		
	/**
	 * Render the exportable item as XMI, suitable for the xmi:Model portion.
	 * @param doc an XML document
	 * @return an element representing this XMIExportable. 
	 */
	public abstract Element renderAsXML(Document doc); 
	/**
	 * Render the exportable item as an XMI extension, suitable for xmi:Extension.
	 * @param model the model that owns this XMIExportable.
	 * @param doc an XML document
	 * @return the element representing the XMI extension serialization.
	 */
	public abstract Element renderExtensionXML(XMIModel model, Document doc);
	
	/**
	 * Method used for testing only.
	 * @return random string
	 */
	public static String randomName() { 
		String [] toks = new String [] { 
			"foo", "bar", "baz", "quux", "blizzens", "blargo", "florko",
			"snuge", "blorfo", "blark", "korth", "frog", "hat", "snork",
			"plugh", "pork", "chicken", "dog", "cat", "parrot"
		};
		
		String name = "";
		Random r = new Random();
		for(int x=0; x<3; x++) { 
			int i = r.nextInt() % toks.length;
			if(i < 0) i *= -1; 
			name = name + toks[i];
			if(x != 2) name = name + "_";
		}
		
		return name;
	} // End randomName
	
	/**
	 * Generate a new ID for an XMI element.  IDs are basically string formatted UUIDs, but with their
	 * third component in lowercase hexidecimal (all other portions uppercase hexidecimal).  Odd, but
	 * this seems to make a difference for importability into some tools.
	 * @return a new unique ID
	 */
	public String newID() { 
		String s = UUID.randomUUID().toString();
		String [] arr = s.split("-");
		
		String agg = "";
		
		for(int x=0; x<arr.length; x++) { 
			if(x == 2) arr[x]=arr[x].toLowerCase();
			else arr[x]=arr[x].toUpperCase();
			
			agg = agg + arr[x];
			if(x < (arr.length - 1)) agg = agg + "_";
		} // End for
		
		return agg; 		
	} // End newID()
} // End XMIExportable
