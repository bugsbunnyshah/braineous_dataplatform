// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.mappingPane;

import java.awt.Component;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import javax.swing.tree.DefaultMutableTreeNode;

import org.mitre.harmony.model.HarmonyConsts;

/**
 * Stores the info on a single line associated with a schema tree
 * @author CWOLF
 */
class MappingCellLine extends ArrayList<Line2D.Float>
{
	/** Indicates if the nodes are currently hidden under the parent node */
	private boolean visible;

	/** Returns the position of the specified node */
	private int getPosition(SchemaTreeImp tree, DefaultMutableTreeNode node)
		{ return (int)tree.getBufferedRowBounds(tree.getNodeRow(node)).getCenterY(); }
	
	/** Initialize the line */
	MappingCellLine(MappingPane mappingPane, ArrayList<DefaultMutableTreeNode> inputNodes, DefaultMutableTreeNode outputNode, boolean isIdentityFunction)
	{		
		// Retrieve the source and target schema trees
		SchemaTreeImp sourceTree = mappingPane.getTree(HarmonyConsts.LEFT);
		SchemaTreeImp targetTree = mappingPane.getTree(HarmonyConsts.RIGHT);
		
		// Determine if the mapping cell line is visible or hidden behind parent nodes
		visible = targetTree.isVisible(outputNode);
		for(DefaultMutableTreeNode sNode : inputNodes)
			visible &= sourceTree.isVisible(sNode);

		// Gets the location of the various sides of the function pane
		Component functionPane = mappingPane.getFunctionPane();
		int minX = functionPane.getX();
		int midX = functionPane.getX() + functionPane.getWidth()/2;
		int maxX = functionPane.getX() + functionPane.getWidth();
		
		// Handles mapping cell lines that have no functions
		if(isIdentityFunction)
		{
			// Define the mapping cell line
			Point sourcePt = new Point(minX, getPosition(sourceTree,inputNodes.get(0)));
			Point targetPt = new Point(maxX, getPosition(targetTree,outputNode));
			add(new Line2D.Float(sourcePt,targetPt));
		}
		
		// Handles mapping cell lines with functions
		else
		{
			// Calculate out the center point
			double midY = 0;
			for(DefaultMutableTreeNode inputNode : inputNodes)
				midY +=getPosition(sourceTree, inputNode);
			midY = (midY/inputNodes.size() + getPosition(targetTree, outputNode))/2;
			Point centerPt = new Point(midX, (int)midY);

			// Generate the mapping cell lines
			for(DefaultMutableTreeNode inputNode : inputNodes)
				add(new Line2D.Float(new Point(minX, getPosition(sourceTree, inputNode)), centerPt));
			add(new Line2D.Float(centerPt, new Point(maxX, getPosition(targetTree, outputNode))));
		}
	}
	
	/** Returns if the mapping cell line is visible */
	boolean isVisible() { return visible; }

	/** Indicates if two mapping cell lines are equal */
	public boolean equals(MappingCellLine line)
		{ return toString().equals(line.toString()); }
	
	/** Returns the hash code for the mapping cell line */ @Override
	public int hashCode()
		{ return toString().hashCode(); }
}