// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.mappingPane;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.tree.DefaultMutableTreeNode;

import org.mitre.harmony.model.HarmonyConsts;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.filters.FilterManager;
import org.mitre.harmony.view.schemaTree.SchemaTree;
import org.mitre.schemastore.model.MappingCell;

/**
 * Stores the info on mapping cell's lines
 * @author CWOLF
 */
class MappingCellLines
{
	// Defines line colors used in displaying the mapping cells
	private static Color BLACK = Color.black;
	private static Color BLUE = Color.blue;
	private static Color GREEN = new Color(0.0f,0.8f,0.0f);
	
	/** Stores the mapping pane to which these lines are associated */
	private MappingPane mappingPane;
	
	/** Stores the mapping cell ID */
	private Integer mappingCellID;
	
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** Indicates if the mapping cell is hidden from display */
	private Boolean hidden;

	/** List of lines associated with the mapping cell */
	private ArrayList<MappingCellLine> lines;

	/** Private class for storing a node mapping */
	private class NodeMapping
	{
		// Stores the pair of tree nodes
		private ArrayList<DefaultMutableTreeNode> inputNodes;
		private DefaultMutableTreeNode outputNode;
		
		/** Constructs a Node Mapping */
		private NodeMapping(ArrayList<DefaultMutableTreeNode> inputNodes, DefaultMutableTreeNode outputNode)
			{ this.inputNodes=inputNodes; this.outputNode=outputNode; }
	}
	
	/** Returns the derivations for the various input node lists */
	private ArrayList<ArrayList<DefaultMutableTreeNode>> getDerivations(ArrayList<DefaultMutableTreeNode> derivation, ArrayList<ArrayList<DefaultMutableTreeNode>> inputNodesList)
	{
		ArrayList<ArrayList<DefaultMutableTreeNode>> derivations = new ArrayList<ArrayList<DefaultMutableTreeNode>>();
		if(derivation.size()<inputNodesList.size())
		{
			for(DefaultMutableTreeNode node : inputNodesList.get(derivation.size()))
			{
				derivation.add(node);
				derivations.addAll(getDerivations(derivation,inputNodesList));
				derivation.remove(derivation.size()-1);
			}
		}
		else derivations.add(new ArrayList<DefaultMutableTreeNode>(derivation));
		return derivations;
	}
	
	/** Calculates the score for the pair of nodes */
	private Double getScore(Integer direction, DefaultMutableTreeNode node1, DefaultMutableTreeNode node2)
	{
		Integer element1ID = SchemaTree.getElement(direction.equals(HarmonyConsts.LEFT) ? node1 : node2);
		Integer element2ID = SchemaTree.getElement(direction.equals(HarmonyConsts.LEFT) ? node2 : node1);
		Integer mappingID = harmonyModel.getMappingManager().getMappingCellID(element1ID, element2ID);
		return mappingID==null ? 0.0 : harmonyModel.getMappingManager().getMappingCell(mappingID).getScore();
	}
	
	/** Generate the list of node mappings */
	private ArrayList<NodeMapping> getNodeMappings(MappingCell mappingCell)
	{
		ArrayList<NodeMapping> nodeMappings = new ArrayList<NodeMapping>();
		
		// Retrieve the left and right schema trees
		SchemaTreeImp leftTree = mappingPane.getTree(HarmonyConsts.LEFT);
		SchemaTreeImp rightTree = mappingPane.getTree(HarmonyConsts.RIGHT);

		// Convert mapping cell IDs into nodes
		ArrayList<ArrayList<DefaultMutableTreeNode>> inputNodesList = new ArrayList<ArrayList<DefaultMutableTreeNode>>();
		for(Integer inputID : mappingCell.getElementInputIDs())
			inputNodesList.add(leftTree.getSchemaElementNodes(inputID));
		ArrayList<DefaultMutableTreeNode> outputNodes = rightTree.getSchemaElementNodes(mappingCell.getOutput());
		
		// Generate all node mappings
		for(ArrayList<DefaultMutableTreeNode> inputNodes : getDerivations(new ArrayList<DefaultMutableTreeNode>(),inputNodesList))
			for(DefaultMutableTreeNode outputNode : outputNodes)
				nodeMappings.add(new NodeMapping(inputNodes,outputNode));
	
		// Only proceed if hierarchical filtering is on and more than one node pair exists
		if(harmonyModel.getFilters().getFilter(FilterManager.HIERARCHY_FILTER) && nodeMappings.size()>=2)
			if(inputNodesList.size()==1)
			{
				// Eliminate all node pairs where there exists a better hierarchical match
				PAIR1_LOOP: for(int loc1=0; loc1<nodeMappings.size(); loc1++)
					PAIR2_LOOP: for(int loc2=loc1+1; loc2<nodeMappings.size(); loc2++)
					{
						// Define the two tree nodes being compared
						NodeMapping pair1 = nodeMappings.get(loc1);
						NodeMapping pair2 = nodeMappings.get(loc2);
						
						// Identify the focal node and matched nodes
						Integer direction = null;
						DefaultMutableTreeNode focalNode=null, match1=null, match2=null;
						if(pair1.inputNodes.get(0).equals(pair2.inputNodes.get(0)))
						{
							direction=HarmonyConsts.LEFT; focalNode=pair1.inputNodes.get(0);
							match1=pair1.outputNode; match2=pair2.outputNode;
						}
						else if(pair1.outputNode.equals(pair2.outputNode))
						{
							direction=HarmonyConsts.RIGHT; focalNode=pair1.outputNode;
							match1=pair1.inputNodes.get(0); match2=pair2.inputNodes.get(0);
						}
						if(focalNode==null) continue;
		
						// Shift the matched nodes to the first unique parent node
						while(SchemaTree.getElement(match1).equals(SchemaTree.getElement(match2)))
						{
							match1 = (DefaultMutableTreeNode)match1.getParent();
							match2 = (DefaultMutableTreeNode)match2.getParent();
							if(SchemaTree.getElement(match1)==null || SchemaTree.getElement(match2)==null) break;
						}
						
						// Identify the better matched node based on context
						while(focalNode.getParent()!=null)
						{
							focalNode = (DefaultMutableTreeNode)focalNode.getParent();
							Double pair1Score = getScore(direction,focalNode,match1);
							Double pair2Score = getScore(direction,focalNode,match2);
							if(pair1Score>pair2Score+0.1) { nodeMappings.remove(loc2--); continue PAIR2_LOOP; }
							if(pair2Score>pair1Score+0.1) { nodeMappings.remove(loc1--); continue PAIR1_LOOP; }
						}
					}
			}
		
		return nodeMappings;
	}
	
	/** Generate all of the lines which make up the mapping cell */
	private void generateLines()
	{
		lines = new ArrayList<MappingCellLine>();
	
		// Retrieve the left and right schema trees
		MappingCell mappingCell = harmonyModel.getMappingManager().getMappingCell(mappingCellID);
		SchemaTreeImp leftTree = mappingPane.getTree(HarmonyConsts.LEFT);
		SchemaTreeImp rightTree = mappingPane.getTree(HarmonyConsts.RIGHT);
		
		// Cycle through all combination of source and target nodes
		MAPPING_CELL_LOOP: for(NodeMapping nodeMapping : getNodeMappings(mappingCell))
		{
			// Only create lines if they are within the specified depths
			for(DefaultMutableTreeNode inputNode : nodeMapping.inputNodes)
				if(!harmonyModel.getFilters().isVisibleNode(HarmonyConsts.LEFT,inputNode)) continue MAPPING_CELL_LOOP;
			if(!harmonyModel.getFilters().isVisibleNode(HarmonyConsts.RIGHT,nodeMapping.outputNode)) continue;
			
			// Only create lines if they are visible on the screen (saves processing time)
			boolean visible = false;
			Integer outputRow = rightTree.getNodeRow(nodeMapping.outputNode);
			for(DefaultMutableTreeNode inputNode : nodeMapping.inputNodes)
			{
				Integer inputRow = leftTree.getNodeRow(inputNode);
				visible |= (inputRow>=leftTree.firstVisibleRow || outputRow>=rightTree.firstVisibleRow) &&
				           (inputRow<=leftTree.lastVisibleRow || outputRow<=rightTree.lastVisibleRow);
				if(visible) break;
			}
			if(!visible) continue;
			
			// Add line linking the left and right nodes
			lines.add(new MappingCellLine(mappingPane,nodeMapping.inputNodes,nodeMapping.outputNode,mappingCell.isIdentityFunction()));
		}
	}
	
	/** Initializes the mapping cell lines */
	MappingCellLines(MappingPane mappingPane, Integer mappingCellID, HarmonyModel harmonyModel)
	{
		this.mappingPane = mappingPane;
		this.mappingCellID = mappingCellID;
		this.harmonyModel = harmonyModel;
	}
	
	/** Returns the mapping cell ID */
	Integer getMappingCellID()
		{ return mappingCellID; }
	
	/** Returns the list of lines associated with this mapping cell */
	synchronized ArrayList<MappingCellLine> getLines()
	{
		if(!getHidden() && lines==null) generateLines();
		return lines==null ? new ArrayList<MappingCellLine>() : new ArrayList<MappingCellLine>(lines);
	}
	
	/** Returns indication of if the mapping cell's lines are currently hidden */
	synchronized boolean getHidden()
	{
		if(hidden==null)
		{
			hidden = !harmonyModel.getFilters().isVisibleMappingCell(mappingCellID) && !harmonyModel.getSelectedInfo().isMappingCellSelected(mappingCellID);
			if(hidden) lines = null;
		}
		return hidden;
	}

	/** Returns the color associated with this mapping cell */
	Color getColor()
	{
		MappingCell mappingCell = harmonyModel.getMappingManager().getMappingCell(mappingCellID);
		double conf = mappingCell.getScore();
    	if(harmonyModel.getSelectedInfo().isMappingCellSelected(mappingCellID)) return BLUE;
    	else if(mappingCell.isValidated()) return BLACK;
    	else if(conf>=1) return GREEN;
    	else if(conf<0.5) return new Color(0.8f,0.8f-0.4f*(0.5f-(float)conf),0.0f);
	    else return new Color(0.8f-0.8f*((float)conf-0.5f),0.8f,0.0f);
	}
	
	/** Calculates all of the lines associated with this mapping cell */
	void updateLines()
		{ lines = null; }
	
	/** Unset hidden variable to force recalculation */
	void updateHidden()
		{ hidden = null; }
}