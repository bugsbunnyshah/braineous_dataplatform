package org.mitre.harmony.model.filters;

import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.tree.DefaultMutableTreeNode;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.schemaTree.SchemaTree;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

/** Class for storing the focus */
public class Focus
{	
	/** Stores the schema info being focused on */
	private HierarchicalSchemaInfo schemaInfo;
	
	/** Stores the list of focused IDs */
	private ArrayList<ElementPath> focusPaths = new ArrayList<ElementPath>();
	
	/** Stores the list of hidden IDs */
	private ArrayList<Integer> hiddenIDs = new ArrayList<Integer>();
	
	/** Stores the list of all elements in focus */
	private HashSet<Integer> elementsInFocus;
	
	/** Stores the list of all hidden elements */
	private HashSet<Integer> hiddenElements;
	
	/** Returns the list of descendant IDs for the specified element */
	private HashSet<Integer> getDescendantIDs(Integer elementID, HashSet<Integer> descendantIDs)
	{
		if(!hiddenIDs.contains(elementID))
			for(SchemaElement childElement : schemaInfo.getChildElements(elementID))
				if(!descendantIDs.contains(childElement.getId()))
				{
					descendantIDs.add(childElement.getId());
					descendantIDs.addAll(getDescendantIDs(childElement.getId(),descendantIDs));
				}
		return descendantIDs;
	}
	
	/** Constructs the focus object */
	public Focus(Integer schemaID, HarmonyModel harmonyModel)
		{ schemaInfo = harmonyModel.getSchemaManager().getSchemaInfo(schemaID); }
	
	/** Returns the schema associated with this focus */
	public Integer getSchemaID() { return schemaInfo.getSchema().getId(); }
	
	/** Returns the focus paths */
	public ArrayList<ElementPath> getFocusedPaths() { return focusPaths; }

	/** Returns the focus IDs */
	public ArrayList<Integer> getFocusedIDs()
	{
		ArrayList<Integer> focusIDs = new ArrayList<Integer>();
		for(ElementPath focusPath : getFocusedPaths())
			focusIDs.add(focusPath.getElementID());
		return focusIDs;
	}
	
	/** Returns the hidden IDs */
	public ArrayList<Integer> getHiddenIDs() { return hiddenIDs; }
	
	/** Adds a focus */
	void addFocus(ElementPath elementPath)
	{
		// Removes focus elements which are sub-items of the new focus 
		if(elementPath.size()>0)
		{
			for(ElementPath focusPath : new ArrayList<ElementPath>(focusPaths))
				if(elementPath.contains(focusPath) || focusPath.contains(elementPath))
					focusPaths.remove(focusPath);
		}
		else focusPaths.clear();
			
		// Adds the focus element
		focusPaths.add(elementPath);
		elementsInFocus=null;
		hiddenElements=null;
	}
	
	/** Removes a focus */
	void removeFocus(ElementPath elementPath)
		{ focusPaths.remove(elementPath); elementsInFocus=null; hiddenElements=null; }
	
	/** Removes all foci */
	void removeAllFoci()
		{ focusPaths.clear(); elementsInFocus=null; hiddenElements=null; }
	
	/** Hides the specified element */
	void hideElement(Integer elementID)
	{
		// Get descendants of the specified element
		ArrayList<Integer> descendantIDs = new ArrayList<Integer>();
		for(SchemaElement descendant : schemaInfo.getDescendantElements(elementID))
			descendantIDs.add(descendant.getId());
		
		// Remove focus elements which are superseded by the hidden element
		for(ElementPath focusPath : new ArrayList<ElementPath>(focusPaths))
			if(descendantIDs.contains(focusPath.getElementID()))
				focusPaths.remove(focusPath);		
		
		// Adds the hidden element
		hiddenIDs.add(elementID);
		elementsInFocus=null;
		hiddenElements=null;
	}
	
	/** Unhides the specified element */
	void unhideElement(Integer elementID)
		{ hiddenIDs.remove(elementID); elementsInFocus=null; hiddenElements=null; }

	/** Returns the list of elements in focus */
	public HashSet<Integer> getElementIDs()
	{
		if(elementsInFocus==null)
		{
			elementsInFocus = new HashSet<Integer>();
			
			// Identify the root IDs
			ArrayList<Integer> rootIDs = new ArrayList<Integer>();
			for(ElementPath focusPath : focusPaths)
				if(focusPath.size()>0)
					rootIDs.add(focusPath.getElementID());

			// Handle the case where no root IDs are declared
			if(rootIDs.size()==0)
			{
				elementsInFocus.add(schemaInfo.getSchema().getId());
				for(SchemaElement element : schemaInfo.getRootElements())
					rootIDs.add(element.getId());
			}
				
			// Create the list of all elements in focus
			for(Integer elementID : rootIDs)
			{
				elementsInFocus.add(elementID);
				elementsInFocus.addAll(getDescendantIDs(elementID, new HashSet<Integer>()));
			}
		}
		return elementsInFocus;
	}
	
	/** Returns the list of hidden elements */
	public HashSet<Integer> getHiddenElements()
	{
		if(hiddenElements==null)
		{
			hiddenElements = new HashSet<Integer>();
			for(Integer hiddenID : hiddenIDs)
				for(SchemaElement descendant : schemaInfo.getDescendantElements(hiddenID))
					if(!contains(descendant.getId()))
						hiddenElements.add(descendant.getId());
		}
		return hiddenElements;
	}
	
	/** Indicates if the specified element is within focus */
	public boolean contains(Integer elementID)
		{ return getElementIDs().contains(elementID); }

	/** Indicates if the specified node is within focus */
	public boolean contains(DefaultMutableTreeNode node)
	{
		// First verify that the schema is being focused on
		Integer schemaID = SchemaTree.getSchema(node);
		if(!getSchemaID().equals(schemaID)) return false;
		
		// Check to make sure the node is focused on
		Object element = node.getUserObject();
		if(element instanceof Schema) return contains(((Schema)element).getId());
		if(element instanceof Integer && contains((Integer)element))
		{
			// Make sure that the node is on the selected path
			if(focusPaths.size()==0) return true;
			ElementPath elementPath = SchemaTree.getElementPath(node);
			for(ElementPath focusPath : focusPaths)
				if(elementPath.contains(focusPath)) return true;
		}
		return false;
	}
}