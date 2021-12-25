// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.mappingInfo;

import java.util.ArrayList;
import java.util.HashMap;

import org.mitre.schemastore.model.MappingCell;

/**
 * Class for storing mapping cells in a hash
 */
public class MappingCellHash
{	
	/** Stores the mapping cells */
	private HashMap<String,MappingCell> mappingCellHash = new HashMap<String,MappingCell>();

	/** Constructs the hash map key */
	private String getKey(Integer[] inputIDs, Integer outputID)
	{
		StringBuffer key = new StringBuffer();
		for(Integer input : inputIDs)
			key.append(input+"_");
		key.append(outputID);
		return key.toString();
	}

	/** Constructs the hash map key */
	private String getKey(MappingCell mappingCell)
		{ return getKey(mappingCell.getElementInputIDs(),mappingCell.getOutput()); }
	
	/** Constructs the mapping cell hash */
	public MappingCellHash(ArrayList<MappingCell> mappingCells)
	{
		for(MappingCell mappingCell : mappingCells)
			mappingCellHash.put(getKey(mappingCell),mappingCell);
	}		
	
	/** Sets the specified mapping cell */
	public void set(MappingCell mappingCell)
		{ mappingCellHash.put(getKey(mappingCell),mappingCell); }
	
	/** Deletes the specified mapping cell */
	public void delete(MappingCell mappingCell)
		{ mappingCellHash.remove(getKey(mappingCell)); }
	
	/** Returns all of the mapping cells */
	public ArrayList<MappingCell> get()
		{ return new ArrayList<MappingCell>(mappingCellHash.values()); }
	
	/** Returns the specified mapping cell */
	public MappingCell get(Integer inputID, Integer outputID)
		{ return mappingCellHash.get(getKey(new Integer[]{inputID},outputID)); }

	/** Returns the specified mapping cell */
	public MappingCell get(Integer inputIDs[], Integer outputID)
		{ return mappingCellHash.get(getKey(inputIDs,outputID)); }

	/** Indicates if the mapping contains the specified mapping cell */
	public boolean contains(Integer inputID, Integer outputID)
		{ return get(inputID,outputID)!=null; }

	/** Indicates if the mapping contains the specified mapping cell */
	public boolean contains(Integer inputIDs[], Integer outputID)
		{ return get(inputIDs,outputID)!=null; }
}