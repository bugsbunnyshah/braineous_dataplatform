// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.model.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mitre.harmony.model.AbstractManager;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;

/**
 * Class used to manage the current project
 * @author CWOLF
 */
public class MappingManager extends AbstractManager<MappingListener>
{	
	/** Stores the mappings associated with the project */
	private ArrayList<ProjectMapping> mappings = new ArrayList<ProjectMapping>();
	
	/** Store lock to prevent changing of mappings */
	private boolean mappingLock = false;
	
	/** Stores the last mapping cell id handed out */
	protected Integer maxID = 0;
	
	/** Constructs the project manager */
	public MappingManager(HarmonyModel harmonyModel)
		{ super(harmonyModel); }

	/** Returns if the mappings are currently locked */
	public boolean areMappingsLocked()
		{ return mappingLock; }
	
	/** Sets the mapping lock */
	public void setMappingLock(boolean mappingLock)
		{ this.mappingLock = mappingLock; }
	
	/** Returns the list of mappings */
	public ArrayList<ProjectMapping> getMappings()
		{ return new ArrayList<ProjectMapping>(mappings); }
	
	/** Returns the specified mapping */
	public ProjectMapping getMapping(Integer mappingID)
	{
		for(ProjectMapping mapping : mappings)
			if(mapping.getId().equals(mappingID)) return mapping;
		return null;
	}
	
	/** Returns the mapping for the specified source and target schema IDs */
	public ProjectMapping getMapping(Integer sourceID, Integer targetID)
	{
		for(ProjectMapping mapping : mappings)
			if(mapping.getSourceId().equals(sourceID) && mapping.getTargetId().equals(targetID)) return mapping;
		return null;
	}
	
	/** Returns the list of all mapping cells */
	public ArrayList<MappingCell> getMappingCells()
	{
		ArrayList<MappingCell> mappingCells = new ArrayList<MappingCell>();
		for(ProjectMapping mapping : mappings)
			mappingCells.addAll(mapping.getMappingCells());
		return mappingCells;
	}
	
	/** Deletes the specified mapping cells by ID */
	public ArrayList<MappingCell> getMappingCellsByID(List<Integer> mappingCellIDs)
	{
		ArrayList<MappingCell> mappingCells = new ArrayList<MappingCell>();
		for(Integer mappingCellID : mappingCellIDs)
			mappingCells.add(getMappingCell(mappingCellID));
		return mappingCells;
	}
	
	/** Returns the list of mapping cells for the specified element */
	public ArrayList<Integer> getMappingCellsByElement(Integer elementID)
	{
		ArrayList<Integer> mappingCells = new ArrayList<Integer>();
		for(ProjectMapping mapping : mappings)
			mappingCells.addAll(mapping.getMappingCellsByElement(elementID));
		return mappingCells;
	}	
	
	/** Returns the specified mapping cell */
	public MappingCell getMappingCell(Integer mappingCellID)
	{
		for(ProjectMapping mapping : mappings)
		{
			MappingCell mappingCell = mapping.getMappingCell(mappingCellID);
			if(mappingCell!=null) return mappingCell;
		}
		return null;
	}	

	/** Returns the requested mapping cell ID */
	public Integer getMappingCellID(Integer element1ID, Integer element2ID)
	{
		for(ProjectMapping mapping : mappings)
		{
			Integer mappingCellID = mapping.getMappingCellID(element1ID,element2ID);
			if(mappingCellID!=null) return mappingCellID;
		}
		return null;
	}
	
	/** Adds the specified mapping */
	public ProjectMapping addMapping(Mapping mapping)
	{
		// Adds the mapping
		if(mapping.getId()==null) mapping.setId(maxID++);
		ProjectMapping projectMapping = new ProjectMapping(mapping,getModel());
		mappings.add(projectMapping);

		// Inform listeners that the mapping was added
		for(MappingListener listener : getListeners())
			listener.mappingAdded(mapping.getId());
		
		// Returns the project mapping
		return projectMapping;
	}

	
	/** Removes the specified mapping */
	public void removeMapping(ProjectMapping mapping)
	{
		// Remove the mapping
		mapping.deleteMappingCells(mapping.getMappingCells());
		mappings.remove(mapping);
		
		// Inform listeners that the mapping was removed
		for(MappingListener listener : getListeners())
			listener.mappingRemoved(mapping.getId());
	}
	
	/** Separate out mapping cells by mapping */
	private HashMap<Integer,ArrayList<MappingCell>> getMappingCellMap(List<MappingCell> mappingCells)
	{
		HashMap<Integer,ArrayList<MappingCell>> mappingCellMap = new HashMap<Integer,ArrayList<MappingCell>>();
		for(MappingCell mappingCell : mappingCells)
		{
			ArrayList<MappingCell> mappingCellList = mappingCellMap.get(mappingCell.getMappingId());
			if(mappingCellList==null) mappingCellMap.put(mappingCell.getMappingId(),mappingCellList=new ArrayList<MappingCell>());
			mappingCellList.add(mappingCell);
		}
		return mappingCellMap;
	}
	
	/** Sets the specified mapping cells */
	public void setMappingCells(List<MappingCell> mappingCells)
	{
		HashMap<Integer,ArrayList<MappingCell>> mappingCellMap = getMappingCellMap(mappingCells);
		for(Integer mappingID : mappingCellMap.keySet())
			getMapping(mappingID).setMappingCells(mappingCellMap.get(mappingID));
	}
	
	/** Validates the specified mapping cells */
	public void validateMappingCells(List<MappingCell> mappingCells)
	{
		HashMap<Integer,ArrayList<MappingCell>> mappingCellMap = getMappingCellMap(mappingCells);
		for(Integer mappingID : mappingCellMap.keySet())
			getMapping(mappingID).validateMappingCells(mappingCellMap.get(mappingID));
	}
	
	/** Deletes the specified mapping cells */
	public void deleteMappingCells(List<MappingCell> mappingCells)
	{
		HashMap<Integer,ArrayList<MappingCell>> mappingCellMap = getMappingCellMap(mappingCells);
		for(Integer mappingID : mappingCellMap.keySet())
			getMapping(mappingID).deleteMappingCells(mappingCellMap.get(mappingID));
	}
	
	/** Removes all mappings */
	public void removeAllMappings()
		{ for(ProjectMapping mapping : new ArrayList<ProjectMapping>(mappings)) removeMapping(mapping); }
	
	/** Returns the listeners associated with this manager */
	public ArrayList<MappingListener> getListeners()
		{ return super.getListeners(); }
}
