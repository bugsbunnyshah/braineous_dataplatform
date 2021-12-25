// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.model.project;

import java.util.List;

import org.mitre.schemastore.model.MappingCell;

/**
 * Interface used by all Harmony project listeners
 * @author CWOLF
 */
public interface MappingListener
{
	/** Indicates that a mapping has been added to the project */
	public void mappingAdded(Integer mappingID);
	
	/** Indicates that a mapping has been removed from the project */
	public void mappingRemoved(Integer mappingID);
	
	/** Indicates that a mapping's visibility has changed */
	public void mappingVisibilityChanged(Integer mappingID);
	
	/** Indicates that mapping cells have been added */
	public void mappingCellsAdded(Integer mappingID, List<MappingCell> mappingCells);

	/** Indicates that mapping cells have been modified */
	public void mappingCellsModified(Integer mappingID, List<MappingCell> oldMappingCells, List<MappingCell> newMappingCells);
	
	/** Indicates that mapping cells have been removed */
	public void mappingCellsRemoved(Integer mappingID, List<MappingCell> mappingCells);
}
