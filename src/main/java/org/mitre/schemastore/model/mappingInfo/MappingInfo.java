// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.mappingInfo;

import java.io.Serializable;
import java.util.ArrayList;

import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;

/**
 * Class for storing the mapping info
 */
public class MappingInfo implements Serializable
{
	/** Stores the mapping */
	private Mapping mapping;

	/** Stores the mapping cells */
	private MappingCellHash mappingCellHash;
	
	/** Constructs the mapping info */
	public MappingInfo(Mapping mapping, ArrayList<MappingCell> mappingCells)
	{
		this.mapping = mapping;
		mappingCellHash = new MappingCellHash(mappingCells);
	}		

	/** Copy the mapping info */
	public MappingInfo copy()
	{
		ArrayList<MappingCell> mappingCells = new ArrayList<MappingCell>();
		for(MappingCell mappingCell : mappingCellHash.get())
			mappingCells.add(mappingCell);
		return new MappingInfo(mapping.copy(),mappingCells);
	}

	/** Returns the mapping */
	public Mapping getMapping()
		{ return mapping; }

	/** Returns the mapping ID */
	public Integer getId()
		{ return mapping.getId(); }
	
	/** Returns the mapping source ID */
	public Integer getSourceID()
		{ return mapping.getSourceId(); }
	
	/** Returns the mapping target ID */
	public Integer getTargetID()
		{ return mapping.getTargetId(); }

	/** Returns the mapping cells */
	public MappingCellHash getMappingCells()
		{ return mappingCellHash; }
}