// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.schemaInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.mitre.schemastore.model.SchemaElement;

/** Class for representing a filtered schema */
public class FilteredSchemaInfo extends HierarchicalSchemaInfo implements SchemaInfoListener
{
	/** Stores the filtered root element */
	private ArrayList<Integer> filteredRootIDs = null;
	
	/** Stores the minimum depth */
	private Integer minDepth = null;
	
	/** Stores the maximum depth */
	private Integer maxDepth = null;
	
	/** Stores the list of specified hidden schema elements */
	private HashSet<Integer> hiddenElements = new HashSet<Integer>();
	
	/** Stores a listing of all filtered schema elements */
	private HashSet<Integer> filteredElements = null;
	
	/** Fills in the depth hash for use in identifying the filtered elements */
	private void findFilteredElements(Integer elementID, ArrayList<Integer> depths)
	{
		// Don't proceed if not visible
		boolean descendantsVisible = false;
		for(Integer depth : depths)
		{
			if(maxDepth!=null && depth>maxDepth) continue;
			if(minDepth==null || depth>=minDepth)
				{ if(!hiddenElements.contains(elementID)) filteredElements.add(elementID); }
			descendantsVisible=true;
		}
		if(!descendantsVisible) return;
		
		// Calculate child depths
		ArrayList<Integer> childDepths = new ArrayList<Integer>();
		for(Integer depth : depths)
			childDepths.add(depth+1);
		
		// Proceed to fill in depth hash with child elements
		for(SchemaElement element : getChildElements(elementID))
			if(!filteredElements.contains(element.getId()))
				findFilteredElements(element.getId(),childDepths);
	}
	
	/** Generates the filtered elements */
	private void generateFilteredElements()
	{
		// Identify filtered roots if none given
		if(filteredRootIDs==null)
		{
			filteredRootIDs = new ArrayList<Integer>();
			for(SchemaElement element : getRootElements())
				filteredRootIDs.add(element.getId());
		}
			
		// Find all of the filtered elements
		filteredElements = new HashSet<Integer>();
		for(Integer elementID : filteredRootIDs)
		{
			ArrayList<Integer> depths = getDepths(elementID);
			findFilteredElements(elementID,depths);
		}	
	}

	/** Resets the filters */
	private void reset()
		{ filteredElements = null; filteredRootIDs = null; }
	
	/** Constructs the filtered schema */
	public FilteredSchemaInfo(HierarchicalSchemaInfo schemaInfo)
		{ super(schemaInfo,schemaInfo.getModel()); }
	
	/** Copy the filtered schema info */
	public FilteredSchemaInfo copy()
	{
		FilteredSchemaInfo schema = new FilteredSchemaInfo(this);
		schema.minDepth = minDepth;
		schema.maxDepth = maxDepth;
		schema.hiddenElements = new HashSet<Integer>(hiddenElements);
		return schema;
	}
	
	/** Returns the list of hidden elements */
	public ArrayList<Integer> getHiddenElements()
		{ return new ArrayList<Integer>(hiddenElements); }
	
	/** Sets the filtered roots */
	public void setFilteredRoots(ArrayList<Integer> filteredRootIDs)
		{ this.filteredRootIDs = filteredRootIDs; reset(); }
	
	/** Sets the filter minimum depth */
	public void setMinDepth(Integer minDepth)
		{ this.minDepth = minDepth; reset(); }

	/** Sets the filter maximum depth */
	public void setMaxDepth(Integer maxDepth)
		{ this.maxDepth = maxDepth; reset(); }	

	/** Sets the filter visible elements */
	public void setVisibleElements(Collection<Integer> visibleElements)
	{
		// Generate a list of hidden elements
		HashSet<Integer> visibleHash = new HashSet<Integer>(visibleElements);
		ArrayList<Integer> hiddenElements = new ArrayList<Integer>();
		for(SchemaElement element : getElements(null))
			if(!visibleHash.contains(element.getId()))
				hiddenElements.add(element.getId());
		
		// Set the hidden elements
		setHiddenElements(hiddenElements);
	}
	
	/** Sets the filter hidden elements */
	public void setHiddenElements(Collection<Integer> hiddenElements)
		{ this.hiddenElements = new HashSet<Integer>(hiddenElements); reset(); }
	
	/** Returns if the specified element is visible */
	public boolean isVisible(Integer elementID)
	{
		if(filteredElements==null) generateFilteredElements();
		return filteredElements.contains(elementID);
	}
	
	/** Returns the root elements in this schema */
	public ArrayList<SchemaElement> getFilteredRootElements()
	{
		if(filteredRootIDs==null) generateFilteredElements();
		ArrayList<SchemaElement> filteredRoots = new ArrayList<SchemaElement>();
		for(Integer filteredRootID : filteredRootIDs)
			filteredRoots.add(getElement(filteredRootID));
		return filteredRoots;
	}
	
	/** Returns the filtered parent elements of the specified element in this schema */
	public ArrayList<SchemaElement> getFilteredParentElements(Integer elementID)
	{
		ArrayList<SchemaElement> filteredParents = new ArrayList<SchemaElement>();
		for(SchemaElement element : getParentElements(elementID))
			if(isVisible(element.getId())) filteredParents.add(element);
		return filteredParents;
	}
	
	/** Returns the filtered children elements of the specified element in this schema */
	public ArrayList<SchemaElement> getFilteredChildElements(Integer elementID)
	{
		ArrayList<SchemaElement> filteredChildren = new ArrayList<SchemaElement>();
		for(SchemaElement element : getParentElements(elementID))
			if(isVisible(element.getId())) filteredChildren.add(element);
		return filteredChildren;		
	}
	
	/** Returns the list of all filtered elements in this schema */
	public ArrayList<SchemaElement> getFilteredElements()
	{
		ArrayList<SchemaElement> filteredElements = new ArrayList<SchemaElement>();
		for(SchemaElement element : getHierarchicalElements())
			if(isVisible(element.getId())) filteredElements.add(element);
		return filteredElements;
	}

	/** Clear the cached filtered elements if the schema has been modified */
	public void schemaElementAdded(SchemaElement schemaElement) { reset(); }

	/** Clears the cached filtered elements if the schema has been modified */
	public void schemaElementRemoved(SchemaElement schemaElement) { reset(); }
}