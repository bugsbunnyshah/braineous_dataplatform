// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.mitre.schemastore.data.database.SchemaRelationshipsDataCalls;
import org.mitre.schemastore.data.database.SchemaRelationshipsDataCalls.Extension;

/** Class for managing the current list of schema relationships */
public class SchemaRelationshipCache extends DataCache
{	
	/** Private class for managing schema linkages */
	private class SchemaLinks
	{
		// Stores the linkages
		private HashMap<Integer,ArrayList<Integer>> links = new HashMap<Integer,ArrayList<Integer>>();

		/** Adds a link to the specified schema */
		void addLink(Integer schemaID, Integer linkID)
		{
			ArrayList<Integer> linkList = links.get(schemaID);
			if(linkList==null) links.put(schemaID,linkList = new ArrayList<Integer>());
			linkList.add(linkID);			
		}
		
		/** Gets links */
		private ArrayList<Integer> getLinks(Integer schemaID)
		{
			ArrayList<Integer> linkList = links.get(schemaID);
			if(linkList==null) linkList = new ArrayList<Integer>();
			return linkList;
		}
	}

	/** Stores reference to the schema relationship data calls */
	private SchemaRelationshipsDataCalls dataCalls = null;
	
	/** Stores the validation number */
	private Integer validationNumber = 0;
	
	/** Stores a mapping of schema parents */
	private SchemaLinks parents = new SchemaLinks();
	
	/** Stores a mapping of schema children */
	private SchemaLinks children = new SchemaLinks();
	
	/** Refreshes the schema relationships */
	void recacheAsNeeded()
	{
		// Check to see if the schema relationships have changed any
		Integer newValidationNumber = dataCalls.getSchemaExtensionsValidationNumber();
		if(!newValidationNumber.equals(validationNumber))
		{
			validationNumber = newValidationNumber;
			
			// Clears the cached schema relationships
			parents.links.clear();
			children.links.clear();
			
			// Caches the schema relationships
			for(Extension extension : dataCalls.getSchemaExtensions())
			{
				parents.addLink(extension.getExtensionID(),extension.getSchemaID());
				children.addLink(extension.getSchemaID(),extension.getExtensionID());
			}
		}
	}
	
	/** Constructs the schema elements cache */
	SchemaRelationshipCache(DataManager manager, SchemaRelationshipsDataCalls dataCalls)
		{ super(manager); this.dataCalls=dataCalls; }
	
	//------------------
	// Public Functions
	//------------------
	
	/** Returns the parents for the specified schema */
	public ArrayList<Integer> getParents(Integer schemaID)
		{ recacheAsNeeded(); return getParentsInternal(schemaID); }
	
	/** Returns the children for the specified schema */
	public ArrayList<Integer> getChildren(Integer schemaID)
		{ recacheAsNeeded(); return getChildrenInternal(schemaID); }
	
	/** Returns the ancestors for the specified schema */
	public ArrayList<Integer> getAncestors(Integer schemaID)
		{ recacheAsNeeded(); return getAncestorsInternal(schemaID); }
	
	/** Returns the descendants of the specified schema */
	public ArrayList<Integer> getDescendants(Integer schemaID)
		{ recacheAsNeeded(); return getDescendantsInternal(schemaID); }
	
	/** Returns the schemas associated with the specified schema */
	public ArrayList<Integer> getAssociatedSchemas(Integer schemaID)
		{ recacheAsNeeded(); return getAssociatedSchemasInternal(schemaID); }		

	/** Returns the root of the specified schemas */
	public Integer getRootSchema(Integer schema1ID, Integer schema2ID)
		{ recacheAsNeeded(); return getRootSchemaInternal(schema1ID, schema2ID); }
	
	/** Returns the path from the specified root schema and this schema */
	public ArrayList<Integer> getSchemaPath(Integer rootID, Integer schemaID)
		{ recacheAsNeeded(); return getSchemaPathInternal(rootID, schemaID); }
	
	/** Sets the parent schemas for the specified schema */
	public boolean setParents(Integer schemaID, ArrayList<Integer> parentIDs)
		{ return dataCalls.setSchemaParents(schemaID,parentIDs); }

	//-------------------------------------------------
	// Internal instantiations of the public functions
	//-------------------------------------------------
	
	/** Returns the parents for the specified schema */
	ArrayList<Integer> getParentsInternal(Integer schemaID)
		{ return parents.getLinks(schemaID); }
	
	/** Returns the children for the specified schema */
	ArrayList<Integer> getChildrenInternal(Integer schemaID)
		{ return children.getLinks(schemaID); }
	
	/** Returns the ancestors for the specified schema */
	ArrayList<Integer> getAncestorsInternal(Integer schemaID)
	{
		HashSet<Integer> ancestors = new HashSet<Integer>();
		for(Integer parentSchemaID : parents.getLinks(schemaID))
			if(!ancestors.contains(parentSchemaID))
			{
				ancestors.add(parentSchemaID);
				ancestors.addAll(getAncestorsInternal(parentSchemaID));
			}
		return new ArrayList<Integer>(ancestors);
	}
	
	/** Returns the descendants of the specified schema */
	ArrayList<Integer> getDescendantsInternal(Integer schemaID)
	{
		HashSet<Integer> descendants = new HashSet<Integer>();
		for(Integer childSchemaID : children.getLinks(schemaID))
			if(!descendants.contains(childSchemaID))
			{
				descendants.add(childSchemaID);
				descendants.addAll(getDescendantsInternal(childSchemaID));
			}
		return new ArrayList<Integer>(descendants);
	}
	
	/** Returns the schemas associated with the specified schema */
	ArrayList<Integer> getAssociatedSchemasInternal(Integer schemaID)
	{		
		// Initialize a list to track all schemas that need to be examined
		LinkedList<Integer> list = new LinkedList<Integer>();
		list.addFirst(schemaID);
		
		// Trace through list to identify all associated schemas
		HashSet<Integer> associatedSchemas = new HashSet<Integer>();
		while(list.size()>0)
		{
			Integer currSchemaID = list.removeLast();
			if(!associatedSchemas.contains(currSchemaID))
			{
				associatedSchemas.add(currSchemaID);
				for(Integer parentSchemaID : getParentsInternal(currSchemaID))
					list.addFirst(parentSchemaID);
				for(Integer childSchemaID : getChildrenInternal(currSchemaID))
					list.addFirst(childSchemaID);
			}
		}
		return new ArrayList<Integer>(associatedSchemas);
	}

	/** Returns the root of the specified schemas */
	Integer getRootSchemaInternal(Integer schema1ID, Integer schema2ID)
	{
		// Create a list of all ancestors (including self) of the specified schema
		Collection<Integer> ancestors = getAncestorsInternal(schema1ID);
		ancestors.add(schema1ID);
		
		// Trace through all possible roots until a shared root is found
		LinkedList<Integer> possibleRoots = new LinkedList<Integer>();
		possibleRoots.addLast(schema2ID);
		while(possibleRoots.size()>0)
		{
			Integer possibleRootID = possibleRoots.removeFirst();
			if(ancestors.contains(possibleRootID)) return possibleRootID;
			for(Integer parent : getParentsInternal(possibleRootID))
				possibleRoots.addLast(parent);
		}
		return null;
	}
	
	/** Returns the path from the specified root schema and this schema */
	ArrayList<Integer> getSchemaPathInternal(Integer rootID, Integer schemaID)
	{
		ArrayList<Integer> path = new ArrayList<Integer>();
		if(!rootID.equals(schemaID))
		{
			// Build up the list of elements that might exist within the path 
			ArrayList<Integer> possiblePathElements = new ArrayList<Integer>();
			possiblePathElements.add(schemaID);
			for(int i=0; i<possiblePathElements.size(); i++)
			{
				Integer possiblePathElement = possiblePathElements.get(i);
				ArrayList<Integer> parentSchemas = getParentsInternal(possiblePathElement);
				if(parentSchemas.contains(rootID)) continue;
				for(Integer parent :parentSchemas)
					possiblePathElements.add(parent);
			}
			
			// Build path from the specified root schema to this schema
			path.add(rootID);
			for(int i=possiblePathElements.size()-1; i>=0; i--)
			{
				Integer possiblePathElement = possiblePathElements.get(i);
				if(getParentsInternal(possiblePathElement).contains(path.get(path.size()-1)))
					path.add(possiblePathElement);
			}
		}
		else path.add(schemaID);
		return path;
	}
}