// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.schemaTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

/** Used to generate all nodes within the schema tree */
class SchemaTreeGenerator
{	
	/** Stores reference to the org.org.mitre.harmony model */
	private HarmonyModel harmonyModel = null;
	
	/** Stores the schema tree being generated */
	private SchemaTree tree = null;
		
	/** Returns the sorted schema elements */
	private ArrayList<SchemaElement> sortElements(ArrayList<SchemaElement> schemaElements)
	{
		/** Handles the sorting of the element list */
		class ElementComparator implements Comparator<SchemaElement> 
		{
			public int compare(SchemaElement element1, SchemaElement element2)
				{ 
				// Special case - if Attributes are being compared to non-attributes, list attributes first.
				if (element1 instanceof Attribute && !(element2 instanceof Attribute)) { return -1; }
				else if( element2 instanceof Attribute && !(element1 instanceof Attribute)) { return 1; }
				else if (element1 instanceof Containment && !(element2 instanceof Containment)) { return -1; }
				else if( element2 instanceof Containment && !(element1 instanceof Containment)) { return 1; }
				else if (element1 instanceof Relationship && !(element2 instanceof Relationship)) { return -1; }
				else if( element2 instanceof Relationship && !(element1 instanceof Relationship)) { return 1; }
				return element1.getName().toLowerCase().compareTo(element2.getName().toLowerCase()); 				
				}
		}
		
		// Sorts the elements if needed
		if(harmonyModel.getPreferences().getAlphabetized()) Collections.sort(schemaElements, new ElementComparator());
		return schemaElements;
	}

	/** Initializes schema tree when no schemas are present */
	private void initialize()
	{
		// Initialize tree by only indicating the no schemas are available
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("<No Schemas>");
		((DefaultTreeModel)tree.getModel()).insertNodeInto(node,tree.root,0);
        tree.scrollPathToVisible(new TreePath(node.getPath()));
	}
	
	/** Adds a schema element */
	private DefaultMutableTreeNode addNode(HierarchicalSchemaInfo schemaInfo, Integer elementID, HashSet<Integer> pathIDs)
	{
		// Create element node
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(elementID);
		pathIDs.add(elementID);

		// Create child nodes
		for(SchemaElement childElement : sortElements(schemaInfo.getChildElements(elementID,pathIDs)))
		{
			DefaultMutableTreeNode childNode = addNode(schemaInfo, childElement.getId(), pathIDs);
			if(childNode!=null) node.add(childNode);				
		}
		
		// Return element node
		pathIDs.remove(elementID);
		return node;
	}
	
	/** Constructs the Schema Tree Generator */
	SchemaTreeGenerator(SchemaTree tree, HarmonyModel harmonyModel)
		{ this.tree = tree; this.harmonyModel = harmonyModel; initialize(); }
	
	/** Adds a schema to the schema tree */
	void addSchema(Integer schemaID)
	{
		// Get the schema associated with the schema ID
		Schema schema = harmonyModel.getSchemaManager().getSchema(schemaID);
		
		// If first schema, eliminate "no schema" placeholder
		if(((DefaultMutableTreeNode)tree.root.getChildAt(0)).getUserObject() instanceof String)
			((DefaultTreeModel)tree.getModel()).removeNodeFromParent((DefaultMutableTreeNode)tree.root.getChildAt(0));
		
		// Determine placement for new schema
		int loc=0;
		while(loc<tree.root.getChildCount())
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.root.getChildAt(loc);
			if(((Schema)node.getUserObject()).getName().compareTo(schema.getName())>=0) break;
			loc++;
		}
		
		// Install tree branch for specific schema
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(schema);
		((DefaultTreeModel)tree.getModel()).insertNodeInto(node,tree.root,loc);
		tree.expandPath(new TreePath(((DefaultMutableTreeNode)node.getParent()).getPath()));

		// Add root elements to the schema		
		HierarchicalSchemaInfo schemaInfo = harmonyModel.getSchemaManager().getSchemaInfo(schemaID);
		for(SchemaElement element : sortElements(schemaInfo.getRootElements()))
		{				
			DefaultMutableTreeNode entityNode = addNode(schemaInfo, element.getId(), new HashSet<Integer>());
			node.add(entityNode);
			tree.expandPath(new TreePath(((DefaultMutableTreeNode)entityNode.getParent()).getPath()));
		}
		
		// Indicate the schema tree has changed
		for(SchemaTreeListener listener : tree.getSchemaTreeListeners())
		{
			listener.schemaStructureModified(tree);
			listener.schemaDisplayModified(tree);
		}	

		// Ensure that the added nodes are properly displayed
		((DefaultTreeModel)tree.getModel()).nodeStructureChanged(node);	
	}
	
	/** Removes a schema from the schema tree */
	void removeSchema(Integer schemaID)
	{
		// Remove the schema from the schema tree
		DefaultMutableTreeNode node = tree.getSchemaNode(schemaID);
		if(node!=null) ((DefaultTreeModel)tree.getModel()).removeNodeFromParent(node);
		
		// Insert placeholder if "no schemas"
		if(tree.root.getChildCount()==0)
			initialize();
		
		// Indicate that schema tree has changed
		for(SchemaTreeListener listener : tree.getSchemaTreeListeners())
		{
			listener.schemaStructureModified(tree);
			listener.schemaDisplayModified(tree);
		}
	}
	
	/** Regenerate the specified schema */
	void regenerateSchema(Integer schemaID)
		{ removeSchema(schemaID); addSchema(schemaID); }
}
