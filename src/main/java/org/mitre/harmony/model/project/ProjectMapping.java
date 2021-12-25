package org.mitre.harmony.model.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;

/**
 * Class for storing information about a mapping associated with the currently loaded project
 * @author CWOLF
 */
public class ProjectMapping extends Mapping
{
	/** Minimum mapping cell score */
	public static final double MIN_CONFIDENCE = 0;

	/** Maximum mapping cell score */
	public static final double MAX_CONFIDENCE = 1.0;
	
	/** Reference to the org.org.mitre.harmony model */
	private HarmonyModel harmonyModel = null;
	
	/** Indicates if the mapping is currently visible */
	private boolean isVisible = false;
	
	/** Stores the mapping cells */
	private HashMap<Integer,MappingCell> mappingCellHash = new HashMap<Integer,MappingCell>();

	/** Stores the mapping cells by key */
	private HashMap<String,Integer> mappingCellsByKey = new HashMap<String,Integer>();
	
	/** Stores the mapping cells by element reference */
	private HashMap<Integer,ArrayList<Integer>> mappingCellsByElement = new HashMap<Integer,ArrayList<Integer>>();
	
	/** Returns the key associated with the specified element IDs */
	private String getKey(Integer[] inputIDs, Integer outputID)
	{
		// Sort the list of elements
		List<Integer> sortedInputIDs = Arrays.asList(inputIDs);
		Collections.sort(sortedInputIDs);

		// Retrieve the mapping cell using the key
		StringBuffer key = new StringBuffer();
		for(Integer sortedInputID : sortedInputIDs)
			key.append(sortedInputID + " ");
		key.append(outputID);
		return key.toString();
	}
	
	/** Retrieve the mapping cells for the specified mapping cell IDs */
	private List<MappingCell> getMappingCellsByID(List<Integer> mappingCellIDs)
	{
		ArrayList<MappingCell> mappingCells = new ArrayList<MappingCell>();
		for(Integer mappingCellID : mappingCellIDs)
			mappingCells.add(mappingCellHash.get(mappingCellID));
		return mappingCells;
	}
	
	/** Constructor for the MappingInfo object*/
	public ProjectMapping(Mapping mapping, HarmonyModel harmonyModel)
	{
		super(mapping.getId(),mapping.getProjectId(),mapping.getSourceId(),mapping.getTargetId());
		this.harmonyModel = harmonyModel;
	}
	
	/** Indicates if the mapping is visible */
	public boolean isVisible()
		{ return isVisible; }
	
	/** Returns the mapping name */
	public String getName()
	{
		String schema1 = harmonyModel.getSchemaManager().getSchema(getSourceId()).getName();
		String schema2 = harmonyModel.getSchemaManager().getSchema(getTargetId()).getName();
		return "'" + schema1 + "' to '" + schema2 + "'";
	}
	
	/** Sets the mapping ID as well as the references to all of the stored mapping cells */
	public void setId(Integer id)
	{
		super.setId(id);
		for(MappingCell mappingCell : mappingCellHash.values())
			mappingCell.setMappingId(id);
	}
	
	/** Sets the visibility of the mapping */
	public void setVisibility(boolean visibility)
	{
		if(!isVisible==visibility && !harmonyModel.getMappingManager().areMappingsLocked())
		{
			isVisible = visibility;
			for(MappingListener listener : harmonyModel.getMappingManager().getListeners())
				listener.mappingVisibilityChanged(getId());
		}
	}
	
	/** Returns a list of all mapping cells */
	public ArrayList<MappingCell> getMappingCells()
		{ return new ArrayList<MappingCell>(mappingCellHash.values()); }

	/** Returns the requested mapping cell */
	public MappingCell getMappingCell(Integer mappingCellID)
	{
		MappingCell mappingCell = mappingCellHash.get(mappingCellID);
		return mappingCell==null ? null : mappingCell.copy();
	} 
	
	/** Returns the requested mapping cell ID */
	public Integer getMappingCellID(Integer inputID, Integer outputID)
		{ return mappingCellsByKey.get(getKey(new Integer[] {inputID},outputID)); }

	/** Returns the requested mapping cell ID */
	public Integer getMappingCellID(Integer[] inputIDs, Integer outputID)
		{ return mappingCellsByKey.get(getKey(inputIDs,outputID)); }	
	
	/** Returns a list of all mapping cells linked to the specified element */
	public ArrayList<Integer> getMappingCellsByElement(Integer elementID)
	{
		ArrayList<Integer> mappingCellIDs = mappingCellsByElement.get(elementID);
		if(mappingCellIDs==null) mappingCellIDs = new ArrayList<Integer>();
		return mappingCellIDs;
	}
	
	/** Returns if two objects are equal to one another */
	private boolean areEqual(Object object1, Object object2)
		{ return object1==null ? object2==null : object1.equals(object2); }
	
	/** Sets the mapping cell */
	public void setMappingCells(List<MappingCell> mappingCells)
	{
		// Sort out new and already created mapping cells
		ArrayList<MappingCell> newMappingCells = new ArrayList<MappingCell>();
		ArrayList<MappingCell> existingMappingCells = new ArrayList<MappingCell>();
		for(MappingCell mappingCell : mappingCells) {
			if (mappingCellHash.containsKey(mappingCell.getId())) {
				existingMappingCells.add(mappingCell);
			} else {
				newMappingCells.add(mappingCell);
			}
		}
		
		// Handles the addition of new mapping cells
		if(newMappingCells.size()>0)
		{
			for(MappingCell mappingCell : newMappingCells)
			{
				// Get information about the mapping cell to be stored
				Integer mappingCellID = ++harmonyModel.getMappingManager().maxID;
				Integer inputIDs[] = mappingCell.getElementInputIDs();
				Integer outputID = mappingCell.getOutput();
				mappingCell.setId(mappingCellID);
				if(mappingCell.getDate()==null)
					mappingCell.setModificationDate(Calendar.getInstance().getTime());
	
				// Store the mapping cell info in the hash and by key
				mappingCellHash.put(mappingCellID, mappingCell);
				mappingCellsByKey.put(getKey(inputIDs,outputID),mappingCellID);
				
				// Stores the mapping cell info be element reference
				ArrayList<Integer> elementIDs = new ArrayList<Integer>(Arrays.asList(inputIDs));
				elementIDs.add(outputID);
				for(Integer elementID : elementIDs)
				{
					ArrayList<Integer> mappingCellIDs = mappingCellsByElement.get(elementID);
					if(mappingCellIDs==null) mappingCellsByElement.put(elementID, mappingCellIDs = new ArrayList<Integer>());
					mappingCellIDs.add(mappingCellID);
				}
			}
			
			// Informs listeners of the added mapping cells
			for(MappingListener listener : harmonyModel.getMappingManager().getListeners())
				listener.mappingCellsAdded(getId(), newMappingCells);
		}
		
		// Handles the modification of existing mapping cells
		if(existingMappingCells.size()>0)
		{
			ArrayList<MappingCell> oldMappingCells = new ArrayList<MappingCell>();
			for(MappingCell mappingCell : existingMappingCells)
			{
				// Update the mapping cell
				MappingCell oldMappingCell = mappingCellHash.get(mappingCell.getId());
				oldMappingCells.add(oldMappingCell);
				mappingCellHash.put(mappingCell.getId(), mappingCell);
				
				// Determine if the mapping cell has been modified
				boolean modified = !areEqual(mappingCell.getScore(),oldMappingCell.getScore()) ||
								   !areEqual(mappingCell.getFunctionID(),oldMappingCell.getFunctionID()) ||
								   !areEqual(mappingCell.getNotes(),oldMappingCell.getNotes());
				
				// Set the modification date if the score has been modified
				if(modified) mappingCell.setModificationDate(Calendar.getInstance().getTime());
				else mappingCell.setModificationDate(oldMappingCell.getModificationDate());
			}
			
			// Informs listeners of the modified mapping cells
			for(MappingListener listener : harmonyModel.getMappingManager().getListeners())
				listener.mappingCellsModified(getId(), oldMappingCells, existingMappingCells);
		}
	}
	
	/** Validates the specified mapping cells */
	public void validateMappingCells(List<MappingCell> mappingCells)
	{
		ArrayList<MappingCell> validatedMappingCells = new ArrayList<MappingCell>();
		for(MappingCell mappingCell : new ArrayList<MappingCell>(mappingCells))
		{
			// Check to make sure mapping cell isn't already validated
			if(mappingCell.isValidated()) continue;
			
			// Retrieve the various mapping cell fields
			Integer id = mappingCell.getId();
			Integer mappingID = mappingCell.getMappingId();
			Integer inputID = mappingCell.getElementInputIDs()[0];
			Integer outputID = mappingCell.getOutput();
			Date date = Calendar.getInstance().getTime();		
			String notes = mappingCell.getNotes();
	
			// Generate the validated mapping cell
			MappingCell validatedMappingCell = MappingCell.createIdentityMappingCell(id, mappingID, inputID, outputID, "author", date, notes);
			validatedMappingCells.add(validatedMappingCell);
			mappingCellHash.put(validatedMappingCell.getId(), validatedMappingCell);
		}
		
		// Informs listeners of the modified mapping cells
		for(MappingListener listener : harmonyModel.getMappingManager().getListeners())
			listener.mappingCellsModified(getId(), mappingCells, validatedMappingCells);
	}
	
	/** Validates the specified mapping cells by ID */
	public void validateMappingCellsByID(List<Integer> mappingCellIDs)
		{ validateMappingCells(getMappingCellsByID(mappingCellIDs)); }
	
	/** Deletes the specified mapping cells */
	public void deleteMappingCells(List<MappingCell> mappingCells)
	{
		for(MappingCell mappingCell : mappingCells)
		{
			// Remove the mapping cell from the hash and key caches
			mappingCellHash.remove(mappingCell.getId());
			mappingCellsByKey.remove(getKey(mappingCell.getElementInputIDs(),mappingCell.getOutput()));

			// Remove the mapping cell from the element reference
			ArrayList<Integer> elementIDs = new ArrayList<Integer>(Arrays.asList(mappingCell.getElementInputIDs()));
			elementIDs.add(mappingCell.getOutput());
			for(Integer elementID : elementIDs)
			{
				ArrayList<Integer> mappingCellIDs = mappingCellsByElement.get(elementID);
				if(mappingCellIDs.size()>1) mappingCellIDs.remove(mappingCell.getId());
				else mappingCellsByElement.remove(elementID);
			}
		}
		
		// Informs listeners of the removed mapping cells
		for(MappingListener listener : harmonyModel.getMappingManager().getListeners())
			listener.mappingCellsRemoved(getId(), mappingCells);
	}
	
	/** Deletes the specified mapping cells by ID */
	public void deleteMappingCellsByID(List<Integer> mappingCellIDs)
		{ deleteMappingCells(getMappingCellsByID(mappingCellIDs)); }

	/** Returns the string representation of this project mapping */
	public String toString()
		{ return getName(); }
}