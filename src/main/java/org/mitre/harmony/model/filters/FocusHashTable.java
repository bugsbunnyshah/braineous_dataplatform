// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.model.filters;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.tree.DefaultMutableTreeNode;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.schemaTree.SchemaTree;

/**
 * Keeps an updated listing of the items in focus
 * @author CWOLF
 */
class FocusHashTable
{
	
	/** Defines the private node hash class */
	private class NodeHash extends HashMap<DefaultMutableTreeNode,Boolean> {};

	/** Stores reference to the org.org.mitre.harmony model */
	private HarmonyModel harmonyModel = null;
	
	/** Stores all focus filter settings */
	private ArrayList<Focus> foci = new ArrayList<Focus>();
	
	/** Indicates if all items are currently in focus */
	private Boolean focusExists = false;
	
	/** Stores a hash of nodes and if they are in focus */
	private NodeHash nodeHash = new NodeHash();

	/** Constructs the Focus Hash Table */
	FocusHashTable(HarmonyModel harmonyModel)
		{ this.harmonyModel = harmonyModel; }
	
	/** Return the list of foci on the specified side */
	ArrayList<Focus> getFoci()
		{ return new ArrayList<Focus>(foci); }
	
	/** Returns the focus associated with the specified schema */
	Focus getFocus(Integer schemaID)
	{
		for(Focus focus : foci)
			if(focus.getSchemaID().equals(schemaID))
				return focus;
		return null;
	}
	
	/** Indicates if the specified schema is in focus */
	boolean inFocus(Integer schemaID)
	{
		// Determine if focus exists if needed
		if(focusExists==null)
		{
			focusExists = false;
			for(Focus focus : foci)
				if(focus.getFocusedPaths().size()>0)
					{ focusExists=true; break; }
		}
		
		// Determine if the schema is within focus
		if(!focusExists) return true;
		Focus focus = getFocus(schemaID);
		return focus!=null && focus.getFocusedPaths().size()>0;
	}
	
	/** Indicates if the specified element is in focus */
	boolean inFocus(Integer schemaID, Integer elementID)
	{
		if(!inFocus(schemaID)) return false;
		Focus focus = getFocus(schemaID);
		return focus==null || focus.contains(elementID);
	}
	
	/** Indicates if the specified node is in focus */
	boolean inFocus(DefaultMutableTreeNode node)
	{
		// Check hash first for information
		Boolean inFocus = nodeHash.get(node);
		if(inFocus!=null) return inFocus;
		
		// Retrieve information manually
		Integer schemaID = SchemaTree.getSchema(node);
		if(!inFocus(schemaID)) return false;
		Focus focus = getFocus(schemaID);
		inFocus = focus==null || focus.contains(node);
		
		// Store information in hash before returning
		nodeHash.put(node,inFocus);
		return inFocus;
	}
	
	/** Adds a focused element to the specified side */
	public void addFocus(Integer schemaID, ElementPath elementPath)
	{
		if(elementPath!=null)
		{
			// Retrieve the focus associated with the specified schema
			Focus focus = getFocus(schemaID);
			if(focus==null)
				foci.add(focus = new Focus(schemaID, harmonyModel));
	
			// Adds the specified element to the focus
			focus.addFocus(elementPath);
			focusExists=true; nodeHash.clear();
		}
	}

	/** Removes a focused element from the specified side */
	public void removeFocus(Integer schemaID, ElementPath elementPath)
	{
		Focus focus = getFocus(schemaID);
		if(focus!=null)
		{
			focus.removeFocus(elementPath);
			focusExists=null; nodeHash.clear();
		}
	}

	/** Removes all foci from the specified side */
	public void removeAllFoci(Integer side)
	{
		for(Focus focus : getFoci()) focus.removeAllFoci();
		focusExists=false; nodeHash.clear();
	}
	
	/** Hides an element on the specified side */
	public void hideElement(Integer schemaID, Integer elementID)
	{
		// Retrieve the focus associated with the specified schema
		Focus focus = getFocus(schemaID);
		if(focus==null)
			foci.add(focus = new Focus(schemaID, harmonyModel));

		// Adds the specified element to the hidden elements
		focus.hideElement(elementID);
		nodeHash.clear();
	}

	/** Unhides an element on the specified side */
	public void unhideElement(Integer schemaID, Integer elementID)
	{
		Focus focus = getFocus(schemaID);
		if(focus!=null) focus.unhideElement(elementID);
		nodeHash.clear();
	}
}