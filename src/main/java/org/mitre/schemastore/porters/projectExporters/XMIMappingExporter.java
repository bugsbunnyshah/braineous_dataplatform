package org.mitre.schemastore.porters.projectExporters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.Project;
import org.mitre.schemastore.model.ProjectSchema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;
import org.mitre.schemastore.porters.projectExporters.xmi.XMIExportable;
import org.mitre.schemastore.porters.projectExporters.xmi.XMILink;
import org.mitre.schemastore.porters.projectExporters.xmi.XMIModel;
import org.mitre.schemastore.porters.projectExporters.xmi.XMIModelElement;
import org.mitre.schemastore.porters.projectExporters.xmi.XMIWriter;


/**
 * MappingExporter for OpenII.  Takes a mapping and exports it in XMI 2.1 suitable for importing
 * into the Enterprise Architect tool.
 * 
 * <p>The EA tool has a very idiosyncratic XMI format.  Most of this exported model (but not all)
 * should be importable into other tools.  But no guarantees.
 * @author DMALLEN
 *
 */
public class XMIMappingExporter extends ProjectExporter {
	Hashtable <Integer, XMIModelElement> modelElements = new Hashtable <Integer,XMIModelElement> ();
	
	/** Match scores below this value are considered "weak" matches */
	public static final double WEAK_THRESHOLD = 0.4;
	/** Match scores below this value are considered "medium" matches */ 
	public static final double MEDIUM_THRESHOLD = 0.65;

	public void exportMapping(Project mapping,
			ArrayList<MappingCell> mappingCells, File file) throws IOException {
		XMIModel m = null;
		try { 
		m = new XMIModel("Class Model");  
		
		XMIModelElement [] pkgs = new XMIModelElement[2];
		
		/* Each schema needs to be added to the XMI in its entirety, not just the stuff that
		 * matches.  This is done by hashModel() below, which processes the model recursively. 
		 */
		ArrayList <XMIModelElement> xmiSchemas = new ArrayList <XMIModelElement> ();
		int x=0; 
		for(ProjectSchema mappingSchema : mapping.getSchemas()) {
			System.out.println("Getting hierarchical schema info for " + mappingSchema); 
			HierarchicalSchemaInfo hg = new HierarchicalSchemaInfo(client.getSchemaInfo(mappingSchema.getId()),
														 mappingSchema.geetSchemaModel());
			String pkgName = hg.getSchema().getName();
			if(pkgName == null || "".equals(pkgName)) pkgName = "Default package name";			
			pkgs[x] = new XMIModelElement(pkgName, XMIExportable.UML_PACKAGE);
			m.addChild(pkgs[x]); 
			hashModel(m, hg, pkgs[x]);
		} // End for
		
		/* Loop through each match between the two models, and add them as XMILinks.  These end up
		 * getting rendered as UML associations.
		 */
		for(MappingCell mc : mappingCells) { 
			System.out.println("Got mappingCell " + mc + ": " + 
					modelElements.get(mc.getElementInputIDs()[0]) + " => " + 
					modelElements.get(mc.getOutput())); 
										
			Integer [] inputs = mc.getElementInputIDs();
			Integer output = mc.getOutput();
			
			for(int y=0; y<inputs.length; y++) { 
				XMILink l = new XMILink(modelElements.get(inputs[y]),
						                modelElements.get(output), "matches");				
				l.setDocs("Harmony match: " + characterize(mc.getScore()) + " (" + mc.getScore() + ")");
				XMIModelElement owner = m.getPackageOwner(modelElements.get(inputs[y]));
				owner.addChild(l); 
			} // End for					
		} // End for
		} catch(Exception e) { 
			System.err.println("(E)XMIMappingExporter.exportMapping: Whoops...something went wrong."); 
			e.printStackTrace(); 
		}
		
		try { if(m != null) XMIWriter.write(m, file); }
		catch(Exception exc) { 
			System.out.println("(E)XMIMappingExporter.exportMapping - " + exc.getMessage());
		} // End catch
	} // End exportMapping

	/**
	 * Given a match score, determine whether it is "strong", "medium", or "weak"
	 * @param score a match score
	 * @return a characterization of that score.
	 * @see XMIMappingExporter#MEDIUM_THRESHOLD
	 * @see XMIMappingExporter#WEAK_THRESHOLD
	 */
	private String characterize(double score) { 
		if(score <= WEAK_THRESHOLD) return "weak";
		if(score <= MEDIUM_THRESHOLD) return "medium";
		return "strong";
	}
	
	/**
	 * Process a hierarchical schema info into an XMI model
	 * @param m the model to place the schema info objects into 
	 * @param hg the schema being processed
	 * @param pkg the enclosing "UML Package" model element to place the schema into.
	 */
	private void hashModel(XMIModel m, HierarchicalSchemaInfo hg, XMIModelElement pkg) {
		System.out.println("hashModel: Getting roots"); 
		ArrayList <SchemaElement> roots = hg.getRootElements(); 
		
		for(SchemaElement root : roots) {
			XMIModelElement el = recursivelyHash(m, root, hg, pkg, 1); 
		}
	} // end hashModel
	
	private XMIModelElement recursivelyHash(XMIModel m, SchemaElement elem, HierarchicalSchemaInfo owner, 
								  XMIModelElement container, int depth) {
		System.out.println("XMIMappingExporter: Recursing on " + elem + " depth " + depth);  
		XMIModelElement thisnode = new XMIModelElement(elem.getName());
		
		SchemaElement type = owner.getModel().getType(owner, elem.getId());
		String tName = "None or N/A";
		String desc = elem.getDescription();
		if(desc == null || "".equals(desc)) desc = "(None available)"; 
		
		if(type != null) tName = owner.getDisplayName(type.getId()); 
		thisnode.setDocs("<b>Type:</b> " + tName + "<br/>" + 
				         "<b>Description:</b> " + desc);
		
		// We will need this later for the mapping cells.
		modelElements.put(elem.getId(), thisnode); 
		container.addChild(thisnode);  // Add the child XMIModelElement to the parent.
		
		boolean hasChildren = false;
		ArrayList <SchemaElement> chillun = owner.getChildElements(elem.getId()); 
		for(int z=0; z<chillun.size(); z++) { 
			SchemaElement child = chillun.get(z);
			hasChildren = true;
			XMIModelElement sub = recursivelyHash(m, child, owner, thisnode, (depth + 1));
			sub.addGeneralization(thisnode); 
		}

		System.out.println("Finished " + chillun.size() + " children - stereotyping."); 
		// Stereotype with a custom label depending on depth.
		String schemaName = owner.getSchema().getName();
		String stName = schemaName + "_L" + depth;
		if(!hasChildren) stName = stName + "_LF";
		
		m.addStereotype(stName, thisnode);
		return thisnode; 
	} // End recurseHashModel
	
	public String getFileType() {
		return ".xml";
	}
	
	public String getDescription() {
		return "Exports XMI formatted XML suitable for importing into Enterprise Architect";
	}
	
	public String getName() {
		return "XMI Exporter";
	}

	@Override
	public void exportProject(Project project, HashMap<Mapping, ArrayList<MappingCell>> mappings, File file) throws IOException {
		// TODO Auto-generated method stub
		
	}
} // End XMIMappingExporter
