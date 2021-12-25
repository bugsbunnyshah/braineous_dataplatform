// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.model.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.mitre.harmony.model.AbstractManager;
import org.mitre.harmony.model.HarmonyConsts;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.project.MappingListener;
import org.mitre.harmony.model.project.ProjectMapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

/**
 * Tracks link filters used in Harmony
 * @author CWOLF
 */
public class FilterManager extends AbstractManager<FiltersListener> implements MappingListener
{	
	// Constants for referencing the available filters
	static final public int USER_FILTER = 0;
	static final public int SYSTEM_FILTER = 1;
	static final public int HIERARCHY_FILTER = 2;
	static final public int BEST_FILTER = 3;
	
	// Stores the various filter settings
	private boolean filters[];
	
	// Stores all confidence filter settings
	private double minConfThreshold;
	private double maxConfThreshold;
	
	// Stores all depth filter settings
	private int minLeftDepth;
	private int maxLeftDepth;
	private int minRightDepth;
	private int maxRightDepth;

	/** Tracks element confidences when BEST is set */
	private ElementConfHashTable elementConfidences = null;
	
	/** Tracks items that are in focus */
	private FocusHashTable leftFocusHash = null;
	private FocusHashTable rightFocusHash = null;
	
	/** Constructor used to initializes the filters */
	public FilterManager(HarmonyModel harmonyModel)
	{
		super(harmonyModel);
		
		// Initialize the various filter settings
		filters = new boolean[4];
		filters[USER_FILTER]=true; filters[SYSTEM_FILTER]=true; filters[HIERARCHY_FILTER]=true; filters[BEST_FILTER]=false;
		minConfThreshold = ProjectMapping.MIN_CONFIDENCE;
		maxConfThreshold = ProjectMapping.MAX_CONFIDENCE;
		minLeftDepth = minRightDepth = 1;
		maxLeftDepth = maxRightDepth = Integer.MAX_VALUE;
		
		// Initialize the focus hash
		leftFocusHash = new FocusHashTable(harmonyModel);
		rightFocusHash = new FocusHashTable(harmonyModel);
	}
	
	//-----------------------------
	// Handles changes to a filter 
	//-----------------------------
	
	/** Sets the filter value */
	public void setFilter(Integer filter, boolean value)
	{
		// Only set filter if value has changed
		if(filters[filter]!=value)
		{
			filters[filter]=value;
			if(filter.equals(BEST_FILTER))
				elementConfidences = value ? new ElementConfHashTable(getModel()) : null;
			for(FiltersListener listener : getListeners()) listener.filterChanged(filter);
		}
	}
	
	/** Returns the current filter value */
	public boolean getFilter(Integer filter) { return filters[filter]; }

	//--------------------
	// Handles confidence
	//--------------------	
	
	/** Sets the confidence threshold */
	public void setConfidence(double minConfThresholdIn, double maxConfThresholdIn)
	{
		minConfThreshold = minConfThresholdIn;
		maxConfThreshold = maxConfThresholdIn;
		for(FiltersListener listener : getListeners()) listener.confidenceChanged();
	}

	/** Returns minimum confidence threshold */
	public double getMinConfThreshold() { return minConfThreshold; }
	
	/** Returns maximum confidence threshold */
	public double getMaxConfThreshold() { return maxConfThreshold; }
	
	//---------------
	// Handles depth
	//---------------
	
	/** Sets the depth; only links that originate from a node above this depth are to be displayed */
	public void setDepth(Integer side, int newMinDepth, int newMaxDepth)
	{
		if(side==HarmonyConsts.LEFT) { minLeftDepth = newMinDepth; maxLeftDepth = newMaxDepth; }
		else { minRightDepth = newMinDepth; maxRightDepth = newMaxDepth; }
		for(FiltersListener listener : getListeners()) listener.depthChanged(side);
	}
	
	/** Returns the minimum depth */
	public int getMinDepth(Integer side)
		{ return side==HarmonyConsts.LEFT ? minLeftDepth : minRightDepth; }
	
	/** Returns the maximum depth */
	public int getMaxDepth(Integer side)
		{ return side==HarmonyConsts.LEFT ? maxLeftDepth : maxRightDepth; }
	
	//---------------
	// Handles focus
	//---------------	
	
	/** Returns the specified focus hash */
	private FocusHashTable getFocusHash(Integer side)
		{ return side==HarmonyConsts.LEFT ? leftFocusHash : rightFocusHash; }
	
	/** Adds a focused element to the specified side */
	public void addFocus(Integer side, Integer schemaID, ElementPath elementPath)
	{ 
		getFocusHash(side).addFocus(schemaID, elementPath);
		for(FiltersListener listener : getListeners()) listener.focusChanged(side);
	}

	/** Removes a focused element from the specified side */
	public void removeFocus(Integer side, Integer schemaID, ElementPath elementPath)
	{
		getFocusHash(side).removeFocus(schemaID, elementPath);
		for(FiltersListener listener : getListeners()) listener.focusChanged(side);
	}

	/** Removes all foci from the specified side */
	public void removeAllFoci(Integer side)
	{
		getFocusHash(side).removeAllFoci(side);
		for(FiltersListener listener : getListeners()) listener.focusChanged(side);		
	}
	
	/** Hides an element on the specified side */
	public void hideElement(Integer side, Integer schemaID, Integer elementID)
	{
		getFocusHash(side).hideElement(schemaID, elementID);
		for(FiltersListener listener : getListeners()) listener.focusChanged(side);
	}

	/** Unhides an element on the specified side */
	public void unhideElement(Integer side, Integer schemaID, Integer elementID)
	{
		getFocusHash(side).unhideElement(schemaID, elementID);
		for(FiltersListener listener : getListeners()) listener.focusChanged(side);
	}
	
	/** Return the list of foci on the specified side */
	public ArrayList<Focus> getFoci(Integer side)
		{ return new ArrayList<Focus>(getFocusHash(side).getFoci()); }
	
	/** Returns the focus associated with the specified schema */
	public Focus getFocus(Integer side, Integer schemaID)
		{ return getFocusHash(side).getFocus(schemaID); }
	
	/** Indicates if the specified schema is in focus */
	public boolean inFocus(Integer side, Integer schemaID)
		{ return getFocusHash(side).inFocus(schemaID); }
	
	/** Indicates if the specified element is in focus */
	public boolean inFocus(Integer side, Integer schemaID, Integer elementID)
		{ return getFocusHash(side).inFocus(schemaID, elementID); }
	
	/** Indicates if the specified node is in focus */
	public boolean inFocus(Integer side, DefaultMutableTreeNode node)
		{ return getFocusHash(side).inFocus(node); }
	
	/** Identifies all elements in focus on the specified side */
	public HashSet<Integer> getFocusedElementIDs(Integer side)
	{
		HashSet<Integer> focusedElements = new HashSet<Integer>();
		FocusHashTable focusHash = getFocusHash(side);
		for(Integer schemaID : getModel().getProjectManager().getSchemaIDs(side))
			if(focusHash.inFocus(schemaID))
			{
				Focus focus = focusHash.getFocus(schemaID);
				if(focus==null)
				{
					HierarchicalSchemaInfo schema = getModel().getSchemaManager().getSchemaInfo(schemaID);
					for(SchemaElement element : schema.getElements(null))
						focusedElements.add(element.getId());
				}
				else focusedElements.addAll(focus.getElementIDs());
			}
		return focusedElements;
	}
	
	/** Retrieves the focused mapping cells */
	public HashSet<MappingCell> getFocusedMappingCells()
	{
		// Retrieve the visible source and target nodes
		HashSet<Integer> leftIDs = new HashSet<Integer>(getModel().getFilters().getFocusedElementIDs(HarmonyConsts.LEFT));
		HashSet<Integer> rightIDs = new HashSet<Integer>(getModel().getFilters().getFocusedElementIDs(HarmonyConsts.RIGHT));	    	
		
		// Create list of all mapping cells in focus
		HashSet<MappingCell> mappingCells = new HashSet<MappingCell>();
		for(Integer leftID : (leftIDs.size()<rightIDs.size() ? leftIDs : rightIDs))
		{
			ArrayList<Integer> mappingCellIDs = getModel().getMappingManager().getMappingCellsByElement(leftID);
			MAPPING_CELL_LOOP: for(Integer mappingCellID : mappingCellIDs)
			{
				// Make sure that all elements referenced by the mapping cell do exist
				MappingCell mappingCell = getModel().getMappingManager().getMappingCell(mappingCellID);
				for(Integer inputID : mappingCell.getElementInputIDs())
					if(!leftIDs.contains(inputID)) continue MAPPING_CELL_LOOP;
				if(!rightIDs.contains(mappingCell.getOutput())) continue;
				mappingCells.add(mappingCell);
			}
		}
		return mappingCells;
	}
	
	//--------------------
	// Handles visibility
	//--------------------

	/** Determines if the specified node is visible or not */
	public boolean isVisibleNode(Integer side, DefaultMutableTreeNode node)
	{
		// Check that the element is within focus
		if(!getFocusHash(side).inFocus(node)) return false;
		
		// Check that the element is within depth
		int depth = node.getPath().length-2;
		return depth >= getMinDepth(side) && depth <= getMaxDepth(side);
	}
	
	/** Determines if the specified mapping cell is visible or not */
	public boolean isVisibleMappingCell(Integer mappingCellID)
	{
		// Retrieve the mapping cell info
		MappingCell mappingCell = getModel().getMappingManager().getMappingCell(mappingCellID);
		
		// Check if link is within confidence thresholds
		double confidence = mappingCell.getScore();
		if(confidence < minConfThreshold || confidence > maxConfThreshold) return false;
		
		// If BEST is asserted, check to ensure that link is best link for either left or right
		if(getFilter(BEST_FILTER))
		{
			// Gather up all elements associated with the mapping cell
			ArrayList<Integer> elementIDs = new ArrayList<Integer>();
			elementIDs.add(mappingCell.getOutput());
			elementIDs.addAll(Arrays.asList(mappingCell.getElementInputIDs()));

			// Determine if considered a "best" mapping cell
			boolean best = false;
			for(Integer elementID : elementIDs)
				if(confidence == elementConfidences.get(elementID)) { best=true; break; }
			if(!best) return false;
		}

		// Check that link matches current filters for USER and SYSTEM links		
		boolean validated = mappingCell.isValidated();
		if(validated && !getFilter(USER_FILTER)) return false;
		if(!validated && !getFilter(SYSTEM_FILTER)) return false;

		// Indicates that the mapping cell is visible
		return true;
	}
	
	/** Remove focus filters to schemas that are no longer being viewed */
	public void mappingRemoved(Integer mappingID)
	{
		// Remove foci from left and right side based on modification to selected schema
		Integer sides[] = { HarmonyConsts.LEFT, HarmonyConsts.RIGHT };
		for(Integer side : sides)
		{
			HashSet<Integer> schemas = getModel().getProjectManager().getSchemaIDs(side);
			ArrayList<Focus> foci = getFocusHash(side).getFoci();
			for(Focus focus : new ArrayList<Focus>(foci))
				if(!schemas.contains(focus.getSchemaID()))
				{
					foci.remove(focus);
					for(FiltersListener listener : getListeners()) listener.focusChanged(side);
				}
		}
	}
	
	// Unused event listeners
	public void mappingAdded(Integer mappingID) {}
	public void mappingVisibilityChanged(Integer mappingID) {}
	public void mappingCellsAdded(Integer mappingID, List<MappingCell> mappingCells) {}
	public void mappingCellsModified(Integer mappingID, List<MappingCell> oldMappingCells, List<MappingCell> newMappingCells) {}
	public void mappingCellsRemoved(Integer mappingID, List<MappingCell> mappingCells) {}

	/** Inform listeners that the max confidence has changed */
	void fireMaxConfidenceChanged(Integer schemaObjectID)
		{ for(FiltersListener listener : getListeners()) listener.maxConfidenceChanged(schemaObjectID); }
}