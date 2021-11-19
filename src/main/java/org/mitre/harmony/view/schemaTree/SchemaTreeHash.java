// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.schemaTree;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Tracks duplicate nodes in schema tree 
 * @author CWOLF
 */
class SchemaTreeHash implements SchemaTreeListener
{	
	/** Tracks all locations of each schema node */
	private Hashtable<Integer, ArrayList<DefaultMutableTreeNode>> nodeLocs;

	/** Builds node hash */
	private void buildNodeHash(SchemaTree tree)
	{
		// Clear out hash table information
		nodeLocs.clear();
		
		// Traverse through all nodes in tree
		Enumeration subNodes = tree.root.depthFirstEnumeration();
		while(subNodes.hasMoreElements())
		{
			// Place node in correct hashtable array
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)subNodes.nextElement();
			if (!(node.getUserObject() instanceof Integer)) continue;
			Integer schemaNode = (Integer)node.getUserObject();
			ArrayList<DefaultMutableTreeNode> nodes = nodeLocs.get(schemaNode);
			if(nodes==null) {
				nodes = new ArrayList<DefaultMutableTreeNode>();
				nodeLocs.put(schemaNode,nodes);
			}
			nodes.add(node);
		}
	}
	
	/** Initializes schema tree hash */
	SchemaTreeHash(SchemaTree tree)
	{ 
		nodeLocs = new Hashtable<Integer, ArrayList<DefaultMutableTreeNode>>();
		
		buildNodeHash(tree);
		tree.addSchemaTreeListener(this);
	}
	
	/** Retrieves list of tree locations associated with schema node */
	ArrayList<DefaultMutableTreeNode> get(Integer node)
	{
		ArrayList<DefaultMutableTreeNode> list = nodeLocs.get(node);
		return list==null ? new ArrayList<DefaultMutableTreeNode>() : nodeLocs.get(node);
	}

	/** Rebuild node hash whenever the schema tree structure is modified */
	public void schemaStructureModified(SchemaTree tree)
		{ buildNodeHash(tree); }
	
	/** Unused listener action */		
	public void schemaDisplayModified(SchemaTree tree) {}
}