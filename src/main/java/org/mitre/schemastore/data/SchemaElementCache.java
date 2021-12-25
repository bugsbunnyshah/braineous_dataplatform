// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.mitre.schemastore.data.database.SchemaElementDataCalls;
import org.mitre.schemastore.model.Alias;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;

/** Class for managing the current list of schema elements in the schema repository */
public class SchemaElementCache extends DataCache
{
	/** Stores reference to the schema element data calls */
	private SchemaElementDataCalls dataCalls = null;
	
	/** Constructs the schema elements cache */
	SchemaElementCache(DataManager manager, SchemaElementDataCalls dataCalls)
		{ super(manager); this.dataCalls=dataCalls; }
	
	/** Returns the alias to use for the specified schema */
	private Alias getAlias(Integer schemaID, ArrayList<Alias> aliases)
	{
		// First, check to see if alias at this level
		Alias pickedAlias = null;
		for(Alias alias : aliases)
			if(alias.getBase().equals(schemaID) && (pickedAlias==null || alias.getId()<pickedAlias.getId()))
				pickedAlias = alias;
		if(pickedAlias!=null) return pickedAlias;
				
		// Next, determine alias based on parents
		ArrayList<Integer> parentSchemaIDs = getManager().getSchemaRelationshipCache().getParentsInternal(schemaID);
		if(parentSchemaIDs.size()==0) return null;
		for(Integer parentSchemaID : parentSchemaIDs)
		{
			Alias parentAlias = getAlias(parentSchemaID, aliases);
			if(parentAlias==null) continue;
			if(pickedAlias==null || parentAlias.getId()<pickedAlias.getId())
				pickedAlias = parentAlias;
		}
		return pickedAlias;
	}
	
	/** Filter out conflicting aliases from the element list */
	private ArrayList<SchemaElement> filterOutConflictingAliases(Integer schemaID, ArrayList<SchemaElement> elements)
	{
		// Generate a hash map to store all aliases
		HashMap<Integer,ArrayList<Alias>> aliasMap = new HashMap<Integer,ArrayList<Alias>>();
		for(SchemaElement element : elements)
			if(element instanceof Alias)
			{
				Integer elementID = ((Alias)element).getElementID();
				ArrayList<Alias> aliases = aliasMap.get(elementID);
				if(aliases==null) aliasMap.put(elementID, aliases = new ArrayList<Alias>());
				aliases.add((Alias)element);
			}

		// Only perform filtering if aliases exist
		for(Integer elementID : aliasMap.keySet())
		{
			ArrayList<Alias> aliases = aliasMap.get(elementID);
			Alias chosenAlias = getAlias(schemaID,aliases);
			for(Alias alias : aliases)
				if(chosenAlias==null || !chosenAlias.equals(alias))
					elements.remove(alias);
		}
		
		// Return filtered elements
		return elements;
	}

	/** Checks to see if any of the specified schema elements are locked */
	private boolean isLocked(ArrayList<SchemaElement> elements)
	{
		// Get the associated schema IDs
		HashSet<Integer> schemaIDs = new HashSet<Integer>();
		for(SchemaElement element : elements) schemaIDs.add(element.getBase());
		
		// Check to see if any of the schemas are locked
		for(Integer schemaID : schemaIDs)
			if(getManager().getSchemaCache().getSchema(schemaID).getLocked()) return true;
		return false;
	}
	
	/** Adds a schema element */
	public Integer addSchemaElement(SchemaElement element)
	{
		Schema schema = getManager().getSchemaCache().getSchema(element.getBase());
		if(schema!=null && !schema.getLocked())
			return dataCalls.addSchemaElement(element);
		return 0;
	}
	
	/** Adds the specified schema elements */
	public boolean addSchemaElements(ArrayList<SchemaElement> elements)
		{ return isLocked(elements) ? false : dataCalls.addSchemaElements(elements); }
	
	/** Updates a schema element */
	public boolean updateSchemaElement(SchemaElement element)
	{
		Schema schema = getManager().getSchemaCache().getSchema(element.getBase());
		if(schema!=null && !schema.getLocked())
		{
			ArrayList<SchemaElement> elements = new ArrayList<SchemaElement>();
			elements.add(element);
			return dataCalls.updateSchemaElements(elements);
		}
		return false;
	}
	
	/** Updates the specified schema elements */
	public boolean updateSchemaElements(ArrayList<SchemaElement> elements)
		{ return isLocked(elements) ? false : dataCalls.updateSchemaElements(elements); }
	
	/** Removes a schema element */
	public boolean deleteSchemaElement(Integer schemaElementID)
	{
		SchemaElement schemaElement = getSchemaElement(schemaElementID);
		Schema schema = getManager().getSchemaCache().getSchema(schemaElement.getBase());
		if(schema!=null && !schema.getLocked())
			return dataCalls.deleteSchemaElement(schemaElementID);
		return false;
	}

	/** Retrieves the specified schema element */
	public SchemaElement getSchemaElement(Integer schemaElementID)
		{ return dataCalls.getSchemaElement(schemaElementID); }
	
	/** Retrieves the schema element count for the specified schema */
	public Integer getSchemaElementCount(Integer schemaID)
		{ return getSchemaElements(schemaID).size(); }
	
	/** Retrieves the schema elements for the specified schema */
	public ArrayList<SchemaElement> getSchemaElements(Integer schemaID)
	{
		// Collect all schema elements
		ArrayList<SchemaElement> elements = new ArrayList<SchemaElement>();
		elements.addAll(dataCalls.getBaseElements(schemaID));
		for(Integer ancestorID : getManager().getSchemaRelationshipCache().getAncestors(schemaID))
			elements.addAll(dataCalls.getBaseElements(ancestorID));
		
		// Add in all used default domains
		for(Domain domain : dataCalls.getDefaultDomains())
			for(SchemaElement element : elements)
				if((element instanceof Containment && ((Containment)element).getChildID().equals(domain.getId())) ||
				   (element instanceof Attribute && ((Attribute)element).getDomainID().equals(domain.getId())))
					{ elements.add(domain); break; }

		// Filter out conflicting aliases as needed
		getManager().getSchemaRelationshipCache().recacheAsNeeded();
		elements = filterOutConflictingAliases(schemaID, elements);
		
		// Return the schema elements
		return elements;
	}

	/** Retrieves the schema elements for the specified schema containing the specified keyword */
	public ArrayList<SchemaElement> getSchemaElementsForKeyword(String keyword, ArrayList<Integer> tagIDs)
	{
		HashSet<Integer> schemaIDs = new HashSet<Integer>();
		for(Integer tagID : tagIDs)
			schemaIDs.addAll(getManager().getTagCache().getTagSchemas(tagID));
		return dataCalls.getSchemaElementsForKeyword(keyword, new ArrayList<Integer>(schemaIDs));
	}
	
	/** Retrieves the specified schema element type */
	public String getSchemaElementType(Integer schemaElementID)
	{
		SchemaElement schemaElement = getSchemaElement(schemaElementID);
		if(schemaElement instanceof Entity) return "Entity";
		else if(schemaElement instanceof Attribute) return "Attribute";
		else if(schemaElement instanceof Domain) return "Domain";
		else if(schemaElement instanceof DomainValue) return "DomainValue";
		else if(schemaElement instanceof Relationship) return "Relationship";
		else if(schemaElement instanceof Containment) return "Containment";
		else if(schemaElement instanceof Subtype) return "Subtype";
		else if(schemaElement instanceof Alias) return "Alias";
		return "";
	}
}