// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.model.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.project.MappingListener;
import org.mitre.harmony.model.project.ProjectMapping;
import org.mitre.schemastore.model.MappingCell;

/**
 * Keeps an updated listing of the maximum confidence associated with each element
 * @author CWOLF
 */
class ElementConfHashTable extends Hashtable<Integer,Double> implements MappingListener
{
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** Retrieves the element IDs associated with the specified mapping cell */
	private ArrayList<Integer> getElementIDs(MappingCell mappingCell)
	{
		ArrayList<Integer> elementIDs = new ArrayList<Integer>();
		elementIDs.addAll(Arrays.asList(mappingCell.getElementInputIDs()));
		elementIDs.add(mappingCell.getOutput());
		return elementIDs;
	}
	
	/** Constructor which initializes the hash table */
	ElementConfHashTable(HarmonyModel harmonyModel)
	{
		this.harmonyModel = harmonyModel;
		harmonyModel.getMappingManager().addListener(this);
	}
	
	/** Retrieves the specified element's confidence from the hash table */
	Double get(Integer elementID)
	{
		// Retrieves the cached node confidence
		Double confidence = super.get(elementID);
		if(confidence!=null) return confidence;
		
		// Calculates the node confidence
		confidence = Double.MIN_VALUE;
		for(ProjectMapping mapping : harmonyModel.getMappingManager().getMappings())
			for(Integer mappingCellID : mapping.getMappingCellsByElement(elementID))
			{
				Double mappingCellConf = mapping.getMappingCell(mappingCellID).getScore();
				if(mappingCellConf>confidence) confidence = mappingCellConf;
			}
		put(elementID,confidence);
		return confidence;
	}
	
	/** Updates confidences when a new mapping cell is added to the model */
	public void mappingCellsAdded(Integer mappingID, List<MappingCell> mappingCells)
	{
		// Cycle through all mapping cells
		for(MappingCell mappingCell : mappingCells)
		{
			// Retrieve mapping cell info
			ArrayList<Integer> elementIDs = getElementIDs(mappingCell);
			Double conf = mappingCell.getScore();
			
			// Updates the best element confidence score if needed
			for(Integer elementID : elementIDs)
			{
				Double elementConf = super.get(elementID);	
				if(elementConf!=null && conf>elementConf)
					{ put(elementID,conf); harmonyModel.getFilters().fireMaxConfidenceChanged(elementID); }
			}
		}
	}
	
	/** Updates confidences when a mapping cell is modified in the model */
	public void mappingCellsModified(Integer mappingID, List<MappingCell> oldMappingCells, List<MappingCell> newMappingCells)
	{
		// Cycle through all modified cells
		for(int i=0; i<oldMappingCells.size(); i++)
		{
			// Retrieve mapping cell info
			ArrayList<Integer> elementIDs = getElementIDs(newMappingCells.get(i));
			Double oldConf = oldMappingCells.get(i).getScore();
			Double newConf = newMappingCells.get(i).getScore();
			
			// Only update max confidence if the mapping cell's confidence changed
			if(oldConf!=newConf)
				for(Integer elementID : elementIDs)
				{
					Double elementConf = super.get(elementID);
					if(elementConf!=null)
					{
						boolean changed = false;
						if(newConf>elementConf) { put(elementID,newConf); changed = true; }
						else if(oldConf.equals(elementConf)) { remove(elementID); changed = true; }
						if(changed) harmonyModel.getFilters().fireMaxConfidenceChanged(elementID);
					}
				}
		}
	}
		
	/** Updates confidences when a mapping cell is removed from the model */
	public void mappingCellsRemoved(Integer mappingID, List<MappingCell> mappingCells)
	{
		// Cycle through mapping cells
		for(MappingCell mappingCell : mappingCells)
		{
			// Retrieve mapping cell info
			ArrayList<Integer> elementIDs = getElementIDs(mappingCell);
			Double conf = mappingCell.getScore();
	
			// Modify the first node's confidence score as needed
			for(Integer elementID : elementIDs)
			{
				Double elementConf = super.get(elementID);
				if(elementConf!=null && conf.equals(elementConf))
					{ remove(elementID); harmonyModel.getFilters().fireMaxConfidenceChanged(elementID); }
			}
		}
	}

	// Unused event listeners
	public void mappingAdded(Integer mappingID) {}
	public void mappingRemoved(Integer mappingID) {}
	public void mappingVisibilityChanged(Integer mappingID) {}
}