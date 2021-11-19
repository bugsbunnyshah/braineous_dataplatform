package org.mitre.schemastore.porters.projectExporters.xmi;

import org.w3c.dom.*;
import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;

/**
 * A class that writes XMIModels to files.
 * @author DMALLEN
 */
public class XMIWriter {	
	public static void write(XMIModel xmimodel, File f) throws Exception {
		DocumentBuilderFactory factory =
		    DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(false); 

		DocumentBuilder builder = factory.newDocumentBuilder();
		Document d = builder.newDocument();

		Element root = d.createElement("xmi:XMI");
		root.setAttribute("xmlns:xmi", "http://schema.omg.org/spec/XMI/2.1");
		root.setAttribute("xmlns:uml", "http://schema.omg.org/spec/UML/2.1");
		if(xmimodel.hasStereotypes()) root.setAttribute("xmlns:thecustomprofile", "http://www.sparxsystems.com/profiles/thecustomprofile/1.0"); 
		root.setAttribute("xmi:version", "2.1");

		d.appendChild(root); 
		
		Element doc = d.createElement("xmi:Documentation");
		doc.setAttribute("exporter", "Enterprise Architect"); 
		doc.setAttribute("exporterVersion", "6.5"); 
		root.appendChild(doc);
		
		Element model = d.createElement(XMIExportable.UML_MODEL);
		model.setAttribute("xmi:type", XMIExportable.UML_MODEL);  
		model.setAttribute("name", "EA_Model"); 
		model.setAttribute("visibility", "public"); 
		root.appendChild(model);
				
		System.out.println("XMIWriter: rendering base model..."); 
		model.appendChild(xmimodel.renderAsXML(d)); 

		Element ext = d.createElement("xmi:Extension"); 
		ext.setAttribute("extender", "Enterprise Architect"); 
		ext.setAttribute("extenderID", "6.5");
		ext.appendChild(d.createTextNode("\n")); 
		root.appendChild(ext); 
		
		System.out.println("XMIWriter: rendering extensions..."); 
		Element [] extensions = xmimodel.renderExtensionsElement(d);
		System.out.println("XMIWriter: adding extensions..."); 
		for(int y=0; y<extensions.length; y++) ext.appendChild(extensions[y]);		
		
		System.out.println("XMIWriter: starting transformation and write..."); 
		// Can you believe that you have to go through this many acrobatics to print XML????
		TransformerFactory tfactory = TransformerFactory.newInstance();
		Transformer xform = tfactory.newTransformer();
			
		// These two lines are magic incantations that enable "pretty printing" of XML (indented elements, rather than
		// the whole document in one line.	
		xform.setOutputProperty(OutputKeys.INDENT, "yes");
		xform.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			
		// Then wrap the DOM into a javax.xml.transform.Source.
		Source src = new DOMSource(d);

		System.out.println("XMIWriter: transforming..."); 
		// Now create a java.io.StringWriter to receive the output and wrap it 
		// into a javax.xml.transform.stream.StreamResult.
		StringWriter writer = new StringWriter();
		Result result = new javax.xml.transform.stream.StreamResult(writer);

		// Finally use empty transform to read from the source (your XML document in DOM format), 
		// apply a transform (a do nothing transform) and write the result (to your StreamResult 
		// which in turn is based on a StringWriter).
		xform.transform(src, result);

		// Now jump on one foot, while patting your head and rubbing your stomach.
		// Just kidding.
		// Last is to extract the DOM as a text string by using the toString method on the 
		// StringWriter that we created.
		System.out.println("XMIWriter: writing..."); 
		BufferedWriter r = new BufferedWriter(new FileWriter(f)); 
		r.write(writer.toString());
		r.close();		
		System.out.println("XMIWriter: finished."); 
	} // End write
	
	/**
	 * Sample test main.  Generate sample model.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String [] args) throws Exception { 
		XMIModel m = XMIModel.generateSimple();
		XMIWriter.write(m, new File("c:\\documents and settings\\dmallen\\Desktop\\jc model\\simple.xml"));
		System.out.println("Finished."); 
	} // End main
} // End XMIWriter
