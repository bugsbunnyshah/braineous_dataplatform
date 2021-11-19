package org.mitre.schemastore.model.mappingInfo;

import java.io.Serializable;
import java.util.ArrayList;

import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;

public class MappingInfoExt implements Serializable {

	/** Stores the mapping */
	private Mapping mapping;

	/** Stores the mapping cells */
	private AssociatedElementHash associatedElementHash;

	/** Constructs the mapping info */
	public MappingInfoExt(Mapping mapping, ArrayList<MappingCell> mappingCells) {
		this.mapping = mapping;
		associatedElementHash = new AssociatedElementHash(mappingCells);
	}

	/** Returns the mapping */
	public Mapping getMapping() {
		return mapping;
	}

	/** Returns the mapping ID */
	public Integer getId() {
		return mapping.getId();
	}

	/** Returns the mapping source ID */
	public Integer getSourceID() {
		return mapping.getSourceId();
	}

	/** Returns the mapping target ID */
	public Integer getTargetID() {
		return mapping.getTargetId();
	}

	/** Returns the mapping cells */
	public AssociatedElementHash getMappingCells() {
		return associatedElementHash;
	}
	
	/** Copy the mapping info */
	public MappingInfoExt copy()
	{
		ArrayList<MappingCell> mappingCells = new ArrayList<MappingCell>();
		for(MappingCell mappingCell : associatedElementHash.get())
			mappingCells.add(mappingCell);
		return new MappingInfoExt(mapping.copy(),mappingCells);
	}
}
