package org.mitre.schemastore.porters.mappingExporters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.model.schemaInfo.SchemaInfo;


public class ObjectMappingExporter extends MappingExporter {



	/** Private class used to flag an array when items are deleted */ @SuppressWarnings("serial")
	private class FlaggedArrayList extends ArrayList<SchemaElement>
	{
		boolean flag = false;
		private FlaggedArrayList() { super(); }
		private FlaggedArrayList(SchemaElement element) { super(); add(element); }
		public boolean remove(Object o) { flag = true; 	return super.remove(o); }
		private boolean isFlagged() { return flag; }
	}
	
	/** Stores the mapping being exported. */
	private Mapping mapping;
	
	/** Stores the source schema used in the mapping */
	private SchemaInfo sourceInfo;
	
	/** Stores the target schema used in the mapping */
	private SchemaInfo targetInfo;
	
	/** Maps the target "containers" to the mapping cells that reference the container */
	private HashMap<SchemaElement, ArrayList<MappingCell>> mappingCellHash;
	
	/** Maps the "containers" to the elements in the container that have yet to be matched */
	private HashMap<Integer, FlaggedArrayList> containerHash;
	
	/** Stores the extra fields stored with the mapping cells */
	private ArrayList<String> extraFields = new ArrayList<String>();
	
	private HashMap<Integer, Subtype> childLinks = new HashMap<Integer, Subtype>();
	
	/** Boolean to return all unmatched elements rather than just those in containers with a matched element */
	private boolean includeAllUnmatched = true;
	
	/** Returns the exporter name */
	public String getName()
		{ return "Object Mapping Exporter"; }
	
	/** Returns the exporter description */
	public String getDescription()
		{ return "This exporter generates an Excel(csv) containing object mapping. Unmapped elements are also exported"; }
	
	/** Returns the file types associated with this converter */
	public String getFileType()
		{ return ".csv"; }
	
	/** Generates a data dictionary for this project */
	public void exportMapping(Mapping mapping, ArrayList<MappingCell> mappingCells, File file) throws IOException
	{
		// Initialize the hashes used in exporting the mapping
		this.mapping = mapping;
		initialize(mappingCells);

		// Export the mapping
		PrintWriter out = new PrintWriter(new FileWriter(file));
		generateHeader(out);
		exportMatchedElements(out);
		exportUnmatchedElements(out);
    	out.close();
	}
	
	/** Populates the hash maps that map containers to a) their contents and b) their mapping cells */
	private void initialize(ArrayList<MappingCell> cells) throws RemoteException
	{
		// Initialize the source and target schemas
		sourceInfo = client.getSchemaInfo(mapping.getSourceId());
		targetInfo = client.getSchemaInfo(mapping.getTargetId());

		// Initialize the containers
		containerHash = new HashMap<Integer,FlaggedArrayList>();		
		for(SchemaInfo schemaInfo : new SchemaInfo[]{sourceInfo,targetInfo})
			initializeContainers(schemaInfo);
		
		// Initialize the mapping cells
		mappingCellHash = new HashMap<SchemaElement, ArrayList<MappingCell>>();
		for(MappingCell cell : cells)
		{
			// Associate the mapping cells with the containers
			SchemaElement containingElement = getContainingElement(cell.getOutput());
			ArrayList<MappingCell> mappingCells = mappingCellHash.get(containingElement);
			if(mappingCells==null) mappingCellHash.put(containingElement, mappingCells = new ArrayList<MappingCell>());
			mappingCells.add(cell);

			// Identify any extra fields provided in the mapping cells
			if(cell.getNotes()!=null && !cell.getNotes().equals(""))
			{
				String fieldString = "<([^>]*)>.*</\\1>";
				Pattern fieldPattern = Pattern.compile(fieldString);
				Matcher fieldMatcher = fieldPattern.matcher(cell.getNotes());
				while(fieldMatcher.find())
				{
					String extraField = fieldMatcher.group(1);
					if(!extraFields.contains(extraField)) extraFields.add(extraField);
				}
			}
		}
	}

	/** Initializes the containers with their associated schema elements */
	private void initializeContainers(SchemaInfo schemaInfo)
	{	
		// Stores entities in their own containers
		for (SchemaElement entity : schemaInfo.getElements(Entity.class))
			containerHash.put(entity.getId(), new FlaggedArrayList(entity));
		containerHash.put(null, new FlaggedArrayList(null));
		
		// Store attributes in the associated entity container
		for (SchemaElement attribute : schemaInfo.getElements(Attribute.class))
			containerHash.get(((Attribute)attribute).getEntityID()).add(attribute);

		// Stores containments in the associated entity container
		for (SchemaElement containment : schemaInfo.getElements(Containment.class))
			containerHash.get(((Containment)containment).getParentID()).add(containment);
			
		// Stores domains and their associated values in domain containers
		for (SchemaElement domain : schemaInfo.getElements(Domain.class))
		{
			FlaggedArrayList children = new FlaggedArrayList();
			children.add(domain);
			children.addAll(schemaInfo.getDomainValuesForDomain(domain.getId()));
			containerHash.put(domain.getId(), children);
		}
		
		// Stores relationships in their own containers
		for (SchemaElement relationship : schemaInfo.getElements(Relationship.class))
			containerHash.get(((Relationship)relationship).getLeftID()).add(relationship);
		for (SchemaElement subtype : schemaInfo.getElements(Subtype.class)){
			Subtype s = (Subtype)subtype;
			childLinks.put(s.getChildID(), s);
		}
	}

	/** Generates the header */
	private void generateHeader(PrintWriter out)
	{
		out.print("Source,,,,,,Target,,,,,");
		for (String extraField: extraFields) out.print(",");
		out.println();
		out.print("M3NodeType,Schema Name,Entity Name,Attribute/Relationship,Description, Supertype/Domain/Right Element,");
		out.print("M3NodeType,Schema Name,Entity Name,Attribute/Relationship,Description, Supertype/Domain/Right Element");
		out.print(",Score,Note");
		for(String extraField : extraFields) out.print(","+extraField);
		out.println();
	}
	
	/** Exports all of the pairs of matched elements */
	private void exportMatchedElements(PrintWriter out)
	{
		// Iterate over each mapping cell pairing (grouped by element container)
		for(SchemaElement targetBase : mappingCellHash.keySet())
			for(MappingCell mappingCell : mappingCellHash.get(targetBase))
				for(Integer inputID : mappingCell.getElementInputIDs())
				{
					// Retrieve the source and target element
					SchemaElement sourceElement = findElementByID(inputID);
					SchemaElement sourceBase = getContainingElement(inputID);
					SchemaElement targetElement = findElementByID(mappingCell.getOutput());

					// Parse out extra field information
					String note = scrub(mappingCell.getNotes());
					ArrayList<String> extraValues = new ArrayList<String>();
					for(String extraField : extraFields)
					{
						String fieldString = "<"+extraField+">(.*?)</"+extraField+">";
						Pattern fieldPattern = Pattern.compile(fieldString);
						Matcher fieldMatcher = fieldPattern.matcher(note);
						extraValues.add(fieldMatcher.find() ? fieldMatcher.group(1).trim() : "");
						note = note.replaceAll(fieldString, "");
					}
					
					// Output the results
					out.print(getElementString(sourceInfo, sourceBase, sourceElement) + "," +
							  getElementString(targetInfo, targetBase, targetElement) + "," +							
							  mappingCell.getScore() + ",\"" + note + "\"");
					for(String extraValue : extraValues) out.print(",\""+extraValue+"\"");
					out.println();
					
					// Remove the source and target elements from the container hash
					containerHash.get(getId(targetBase)).remove(targetElement);
					containerHash.get(getId(sourceBase)).remove(sourceElement);
				}
	}
	
	/** Exports all unmatched elements, provided the container has at least one exported match */
	private void exportUnmatchedElements(PrintWriter out) 
	{
		// Identify all target containers
		HashSet<Integer> targetContainers = new HashSet<Integer>();
		for(SchemaElement targetContainer : mappingCellHash.keySet())
			targetContainers.add(getId(targetContainer));
		
		// Cycle through all containers to identify unmatched elements
		for(Integer baseID : containerHash.keySet())
		{
			if (baseID != null) {
			// Make sure that container was associated with at least one match
			FlaggedArrayList elements = containerHash.get(baseID);
			if(!elements.isFlagged() && !includeAllUnmatched) continue;
			
			// Determine if the container is part of the source or target schema	
			boolean isSource = targetInfo.getElement(baseID) == null;
			SchemaElement base = isSource ? sourceInfo.getElement(baseID) : targetInfo.getElement(baseID);

			// Display the unmatched elements which make up the container
			for(SchemaElement child : containerHash.get(baseID))
			{
				if (!(child instanceof Domain)  || containerHash.get(baseID).size()>1) {
					String sourceString = isSource ? getElementString(sourceInfo, base, child) : ",,,,,";
					String targetString = isSource ? ",,,,," : getElementString(targetInfo, base, child);
					out.println(sourceString + "," + targetString);
				}
			}
			}
		}
	}
	
	/** Returns the ID for the specified schema element */
	private Integer getId(SchemaElement element)
		{ return element==null ? null : element.getId(); }
	
	/** Generates a string for the specified base and element */
	private String getElementString(SchemaInfo info, SchemaElement base, SchemaElement element)
	{
		String className = element == null?"":element.getClass().getSimpleName();
		return "\"" + className + "\",\"" + info.getSchema().getName() + "\",\"" + getDisplayName(base) +
				"\",\"" + getDisplayName(base, element) + "\",\"" + (element == null?"":scrub(element.getDescription()))
				+ "\",\"" + getExtendedInfo(base, element) + "\"";

	}
	
	/** Scrubs the specified string to replace quotation marks */
	private String scrub(String text)
		{ return text.replaceAll("\"", "\"\""); }
	
	/** Finds a given schema element by iterating through all available schemata */
	private SchemaElement findElementByID(Integer elementID)
	{
		if(elementID == null) return null;
		if(sourceInfo.containsElement(elementID)) return sourceInfo.getElement(elementID);
		if(targetInfo.containsElement(elementID)) return targetInfo.getElement(elementID);
		return null;
	}
	/** Set includeAllUnmatchedElements so can decide if to export all unmatched elements or just those with
	 * a match in the container
	 */
	protected void setIncludeAllUnmatched(boolean choice)
	{
		includeAllUnmatched = choice;
	}
	/** Finds the display name (which accounts for anonymous elements) by iterating through all available schemas */
	private String getDisplayName(SchemaElement root, SchemaElement element)
	{
		if((element==null && root==null) || element.equals(root)) return "-";
		return getDisplayName(element);
	}
	/** Finds the display name (which accounts for anonymous elements) by iterating through all available schemas */
	private String getExtendedInfo(SchemaElement root, SchemaElement element)
	{
		if((element==null && root==null) || element.equals(root)) {
			if (element instanceof Entity) {
				Subtype subtype =childLinks.get(element.getId());
				if (subtype != null) {
					SchemaElement parent = findElementByID(subtype.getParentID());
					return getDisplayName(parent, false);
				}
			}
		}
		else if (element instanceof Relationship) {
			SchemaElement link = findElementByID(((Relationship)element).getRightID());
			return getDisplayName(link, false);
			
		}
		else if (element instanceof Attribute) {
			SchemaElement domain = findElementByID(((Attribute)element).getDomainID());
			return getDisplayName(domain, false);
		}
		return "-";
	}

	private String getDisplayName(SchemaElement element) {
		return getDisplayName(element, true);
	}
	/** Finds the display name (which accounts for anonymous elements) by iterating through all available schemas */
	private String getDisplayName(SchemaElement element, boolean nullIsRoot)
	{
		if(element == null){
			if (nullIsRoot) return "[root]";
			else return "-";
		}
		if(sourceInfo.containsElement(element.getId())) return scrub(sourceInfo.getDisplayName(element.getId()));
		if(targetInfo.containsElement(element.getId())) return scrub(targetInfo.getDisplayName(element.getId()));
		return null;
	}
	
	/**
	 * Finds the container for a given element: attributes and containments are stored with the parent entity, domain values are
	 * stored with their domains, all other elements are stored in themselves (entities, relationships, and domains).
	 */
	private SchemaElement getContainingElement(Integer elementID)
	{
		if(elementID == null) return null;
		SchemaElement base = findElementByID(elementID);
		if(base instanceof DomainValue)
			return findElementByID(((DomainValue)base).getDomainID());
		if(base instanceof Attribute)
			return findElementByID(((Attribute)base).getEntityID());
		if(base instanceof Containment)
			return findElementByID(((Containment)base).getParentID());
		if (base instanceof Relationship)
			return findElementByID(((Relationship)base).getLeftID());
		return base;
	}
}


