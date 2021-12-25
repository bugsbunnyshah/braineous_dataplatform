// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.model.selectedInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.mitre.harmony.controllers.MappingController;
import org.mitre.harmony.model.AbstractManager;
import org.mitre.harmony.model.HarmonyConsts;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.filters.FiltersListener;
import org.mitre.harmony.model.project.MappingListener;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.SchemaElement;

/**
 * Tracks selected info within Harmony
 * @author CWOLF
 */
public class SelectedInfoManager extends AbstractManager<SelectedInfoListener> implements MappingListener, FiltersListener
{
	// Constants for defining how to adjust the selected items
	protected static final int ADD = 0;
	protected static final int REMOVE = 1;
	protected static final int REPLACE = 2;
	
	/** Stores the displayed left element */
	private Integer displayedLeftElement = null;
	
	/** Stores the displayed right element */
	private Integer displayedRightElement = null;
	
	/** Stores the selected left elements */
	private HashSet<Integer> selectedLeftElements = new HashSet<Integer>();

	/** Stores the selected right elements */
	private HashSet<Integer> selectedRightElements = new HashSet<Integer>();

	/** Stores the selected mapping cells */
	protected HashSet<Integer> selectedMappingCells = new HashSet<Integer>();
	
	/** Returns the specified list of selected elements */
	protected HashSet<Integer> getSelectedElementSet(Integer role)
		{ return role==HarmonyConsts.LEFT ? selectedLeftElements : selectedRightElements; }
	
	/** Constructor used to initialize the selected info */
	public SelectedInfoManager(HarmonyModel harmonyModel)
		{ super(harmonyModel); }
	
	//--------------------- Returns the various selected pieces of info ---------------------
	
	/** Get the displayed element */
	public Integer getDisplayedElement(Integer side)
		{ return side==HarmonyConsts.LEFT ? displayedLeftElement : displayedRightElement; }
	
	/** Get the selected elements */
	public List<Integer> getSelectedElements(Integer side)
		{ return new ArrayList<Integer>(getSelectedElementSet(side)); }

	/** Get the selected mapping cells */
	public List<Integer> getSelectedMappingCells()
		{ return new ArrayList<Integer>(selectedMappingCells); }

	/** Indicates if the specified element is selected */
	public boolean isElementSelected(Integer element, Integer side)
		{ return getSelectedElementSet(side).contains(element); }
	
	/** Indicates if the specified link is selected */
	public boolean isMappingCellSelected(Integer mappingCellID)
		{ return selectedMappingCells.contains(mappingCellID); }
	
	// ------------ Handles the selecting of the various pieces of information -------------

	/** Updates the set as specified */
	protected boolean updateSet(HashSet<Integer> set, List<Integer> items, Integer mode)
	{
		// Check to make sure updates are possible
		boolean containsOne=false, containsAll=true;
		for(Integer item : items)
		{
			boolean containsElement = set.contains(item);
			containsOne |= containsElement; containsAll &= containsElement;
		}
		if(mode==ADD && containsAll) return false;
		if(mode==REMOVE && !containsOne) return false;
		if(mode==REPLACE && containsAll && set.size()==items.size()) return false;
			
		// Update the set
		switch(mode)
		{
			case ADD: for(Integer item : items) set.add(item); break;
			case REMOVE: for(Integer item : items) set.remove(item); break;
			case REPLACE: set.clear(); set.addAll(items); break;
		}		
		return true;
	}
	
	/** Sets the selected elements */
	private void setSelectedElements(List<Integer> elements, Integer side, Integer mode)
	{
		// Set the selected elements
		HashSet<Integer> selectedElements = getSelectedElementSet(side);
		if(!updateSet(selectedElements,elements,mode)) return;
		for(SelectedInfoListener listener : getListeners()) listener.selectedElementsModified(side);

		// Identify changes to the mapping cells required by the newly selected elements
		List<Integer> mappingCells = new ArrayList<Integer>();
		List<Integer> leftElements = getSelectedElements(HarmonyConsts.LEFT);
		List<Integer> rightElements = getSelectedElements(HarmonyConsts.RIGHT);
		for(Integer leftElement : leftElements)
			for(Integer rightElement : rightElements)
			{
				Integer mappingCell = getModel().getMappingManager().getMappingCellID(leftElement,rightElement);
				if(mappingCell!=null) mappingCells.add(mappingCell);
			}

		// Update the mapping cells
		if(updateSet(selectedMappingCells,mappingCells,REPLACE))
			for(SelectedInfoListener listener : getListeners()) listener.selectedMappingCellsModified();

		// Update the displayed elements
		updateDisplayedElements();
	}

	/** Sets the selected mapping cells */
	private void setSelectedMappingCells(List<Integer> mappingCells, Integer mode)
	{
		// Set the selected mapping cells
		if(updateSet(selectedMappingCells,mappingCells,mode))
			for(SelectedInfoListener listener : getListeners()) listener.selectedMappingCellsModified();

		// Identify changes to the elements required by the newly selected mapping cells
		ArrayList<Integer> selectedLeftElements = new ArrayList<Integer>();
		ArrayList<Integer> selectedRightElements = new ArrayList<Integer>();		
		for(Integer mappingCellID : getSelectedMappingCells())
		{
			// Identify the elements for the mapping cell
			MappingCell mappingCell = getModel().getMappingManager().getMappingCell(mappingCellID);
			selectedLeftElements.addAll(Arrays.asList(mappingCell.getElementInputIDs()));
			selectedRightElements.add(mappingCell.getOutput());
		}

		// Update the elements
		Integer sides[] = { HarmonyConsts.LEFT, HarmonyConsts.RIGHT };
		for(Integer side : sides)
		{
			ArrayList<Integer> selectedElements = (side.equals(HarmonyConsts.LEFT)?selectedLeftElements:selectedRightElements);
			if(updateSet(getSelectedElementSet(side),selectedElements,REPLACE))
				for(SelectedInfoListener listener : getListeners())
					listener.selectedElementsModified(side);		
		}
			
		// Update the displayed elements
		updateDisplayedElements();
	}

	/** Updates the displayed elements */
	protected void updateDisplayedElements()
	{
		Integer leftElement = null, rightElement = null;

		// Check for easy case of only one item selected on a given side
		List<Integer> leftElements = getSelectedElements(HarmonyConsts.LEFT);
		List<Integer> rightElements = getSelectedElements(HarmonyConsts.RIGHT);
		if(leftElements.size()==1) leftElement = leftElements.get(0);
		if(rightElements.size()==1) rightElement = rightElements.get(0);

		// Otherwise, check for case where mapping cells are crossing
		else if(leftElements.size()==2 && rightElements.size()==2)
			if(leftElements.contains(rightElements.get(0)) && leftElements.contains(rightElements.get(1)))
				{ leftElement = leftElements.get(0); rightElement = leftElements.get(1); }

		// Update displayed elements if needed
		if(displayedLeftElement==null || !displayedLeftElement.equals(leftElement))
		{
			displayedLeftElement=leftElement; 
			for(SelectedInfoListener listener : getListeners())
				listener.displayedElementModified(HarmonyConsts.LEFT);
		}
		if(displayedRightElement==null || !displayedRightElement.equals(rightElement))
		{
			displayedRightElement=rightElement; 
			for(SelectedInfoListener listener : getListeners())
				listener.displayedElementModified(HarmonyConsts.RIGHT);
		}
	}
	
	/** Toggles the selected element */
	public void setElement(Integer element, Integer side, boolean append)
	{
		// Place the specified element into a list
		List<Integer> elements = Arrays.asList(new Integer[] {element});
		
		// Determines if the specified element should be treated as selected
		boolean selected = getSelectedElementSet(side).contains(element);
		if(!append) selected &= getSelectedElementSet(side).size()==1;
		
		// Handles selection of elements
		if(!append) setSelectedElements(selected ? new ArrayList<Integer>() : elements,side,REPLACE);
		else setSelectedElements(elements,side,selected?REMOVE:ADD);
	}
	
	/** Clears the selected elements */
	public void clearElements(Integer side)
		{ setSelectedElements(new ArrayList<Integer>(),side,REPLACE); }
	
	/** Toggles the selected mapping cells */
	public void setMappingCells(List<Integer> mappingCells, boolean append)
	{
		// Determine if the specified mapping cells should be treated as selected
		boolean selected = true;		
		for(Integer mappingCell : mappingCells)
			if(!selectedMappingCells.contains(mappingCell)) { selected=false; break; }
		if(!append) selected &= mappingCells.size()==selectedMappingCells.size();
		
		// Handles the case where the specified mapping cells should replace the old selected mapping cells
		if(!append) setSelectedMappingCells(selected ? new ArrayList<Integer>() : mappingCells, REPLACE);
		else setSelectedMappingCells(mappingCells,selected?REMOVE:ADD);
	}
	
	//------------ Updates the selected information based on the occurrence of events ------------
	
	/** Unselect elements that are out of focus */
	public void focusChanged(Integer side)
	{
		// Identify all of the elements that are no longer visible
		ArrayList<Integer> removedElements = new ArrayList<Integer>();
		for(Integer elementID : getSelectedElements(side))
			if(!getModel().getFilters().inFocus(side, null, elementID))
				removedElements.add(elementID);
		
		// Remove the eliminated selected elements
		if(removedElements.size()>0)
			setSelectedElements(removedElements,side,REMOVE);
	}
	
	/** Unselect elements that are out of depth */
	public void depthChanged(Integer sideIn)
	{
		Integer sides[] = { HarmonyConsts.LEFT, HarmonyConsts.RIGHT };
		for(Integer side : sides)
		{
			// Get the minimum and maximum depth for the specified side
			int minDepth = getModel().getFilters().getMinDepth(side);
			int maxDepth = getModel().getFilters().getMaxDepth(side);
			
			// Identify all of the elements that are no longer within the specified depths
			ArrayList<Integer> removedElements = new ArrayList<Integer>();			
			for(Integer elementID : getSelectedElements(side))
			{
				boolean visible = false;
				for(Integer schemaID : getModel().getProjectManager().getSchemaIDs(side))
					for(Integer depth : getModel().getSchemaManager().getDepths(schemaID, elementID))
						if(depth>=minDepth && depth<=maxDepth)
							{ visible=true; break; }
				if(!visible) removedElements.add(elementID);
			}
			
			// Remove the eliminated selected elements
			if(removedElements.size()>0)
				setSelectedElements(removedElements,side,REMOVE);
		}
	}
	
	/** Unselect schemas and elements associated with the deleted schema */
	public void mappingRemoved(Integer mappingID)
	{
		// Check for eliminated selected elements in both sides
		Integer sides[] = { HarmonyConsts.LEFT, HarmonyConsts.RIGHT };
		for(Integer side : sides)
		{
			// Gets the list of all schema elements which still exist
			HashSet<SchemaElement> schemaElements = getModel().getProjectManager().getSchemaElements(side);
			
			// Find all of the eliminated selected elements
			ArrayList<Integer> removedElements = new ArrayList<Integer>();
			for(Integer selectedElement : getSelectedElements(side))
				if(!schemaElements.contains(selectedElement))
					removedElements.add(selectedElement);
	
			// Remove the eliminated selected elements
			if(removedElements.size()>0)
				setSelectedElements(removedElements,side,REMOVE);
		}
	}
		
	/** Unselect mapping cells that have been removed */
	public void mappingCellsRemoved(Integer mappingID, List<MappingCell> mappingCells)
		{ setSelectedMappingCells(MappingController.getMappingCellIDs(mappingCells),REMOVE); }

	// Unused action events
	public void mappingAdded(Integer mappingID) {}
	public void mappingVisibilityChanged(Integer mappingID) {}
	public void mappingCellsAdded(Integer mappingID, List<MappingCell> mappingCells) {}
	public void mappingCellsModified(Integer mappingID, List<MappingCell> oldMappingCells, List<MappingCell> newMappingCells) {}
	public void filterChanged(Integer filter) {}
	public void confidenceChanged() {}
	public void maxConfidenceChanged(Integer schemaObjectID) {}
}
