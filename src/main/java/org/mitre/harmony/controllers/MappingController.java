package org.mitre.harmony.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.project.ProjectMapping;
import org.mitre.schemastore.model.MappingCell;

/** Class for various mapping controls */
public class MappingController
{
	// Constants defining mapping cell settings
	static public final Integer ALL = 0;
	static public final Integer SYSTEM = 1;
	static public final Integer USER = 2;
	static public final Integer FOCUSED = 3;
	static public final Integer UNFOCUSED = 4;
	static public final Integer VISIBLE = 5;
	static public final Integer HIDDEN = 6;
	
	/** Translate the list of mapping cells to mapping cell IDs */
	static public ArrayList<Integer> getMappingCellIDs(List<MappingCell> mappingCells)
	{
		ArrayList<Integer> mappingCellIDs = new ArrayList<Integer>();
		for(MappingCell mappingCell : mappingCells) mappingCellIDs.add(mappingCell.getId());
		return mappingCellIDs;
	}
	
	/** Retrieves the specified mapping cells */
	static public HashSet<MappingCell> getMappingCells(HarmonyModel harmonyModel, Integer type, Integer focus, Integer visibility)
	{
		// Get the list of mapping cells
		HashSet<MappingCell> mappingCells = new HashSet<MappingCell>();
		for(MappingCell mappingCell : harmonyModel.getMappingManager().getMappingCells())
		{
			boolean validated = mappingCell.isValidated();
			if(type==ALL || (type==SYSTEM && !validated) || (type== USER && validated))
				mappingCells.add(mappingCell);
		}
		
		// Filter out mapping cells based on filter settings
		if(focus!=ALL)
		{
			boolean useFocused = focus==FOCUSED;
			HashSet<MappingCell> focusedMappingCells = harmonyModel.getFilters().getFocusedMappingCells();
			for(MappingCell mappingCell : new ArrayList<MappingCell>(mappingCells))
			{
				boolean isFocused = focusedMappingCells.contains(mappingCell);
				if(useFocused != isFocused) mappingCells.remove(mappingCell);
			}					
		}

		// Filter out mapping cells based on visibility
		if(visibility!=ALL)
		{
			boolean useVisible = visibility== VISIBLE;
			for(MappingCell mappingCell : new ArrayList<MappingCell>(mappingCells))
			{
				boolean isVisible = harmonyModel.getFilters().isVisibleMappingCell(mappingCell.getId());
				if(useVisible != isVisible) mappingCells.remove(mappingCell);
			}
		}
		
		return mappingCells;
	}
	
	/** Selects the specified mapping cells */
	static public void selectMappingCells(HarmonyModel harmonyModel, Integer type, Integer focus, Integer visibility)
	{		
		ArrayList<MappingCell> mappingCells = new ArrayList<MappingCell>(getMappingCells(harmonyModel, type, focus, visibility));
		ArrayList<Integer> mappingCellIDs = MappingController.getMappingCellIDs(mappingCells);
		harmonyModel.getSelectedInfo().setMappingCells(mappingCellIDs, false);
	}
		
	/** Deletes the specified mapping cells */
	static public void deleteMappingCells(HarmonyModel harmonyModel, Integer type, Integer focus, Integer visibility)
	{
		ArrayList<MappingCell> mappingCells = new ArrayList<MappingCell>(getMappingCells(harmonyModel, type, focus, visibility));		
		harmonyModel.getMappingManager().deleteMappingCells(mappingCells);
	}
	
	/** Mark visible mapping cells associated with the specified node as finished */
	static public void markAsFinished(HarmonyModel harmonyModel, Integer elementID)
	{
		// Cycle through all mappings in the project
		for(ProjectMapping mapping : harmonyModel.getMappingManager().getMappings())
		{
			// Identify all computer generated links
			HashSet<MappingCell> mappingCellsToDelete = new HashSet<MappingCell>();
			for(Integer mappingCellID : mapping.getMappingCellsByElement(elementID))
			{
				MappingCell mappingCell = mapping.getMappingCell(mappingCellID);
				if(!mappingCell.isValidated()) mappingCellsToDelete.add(mappingCell);
			}		
			
			// Delete all Mark all visible links as user selected and all others as rejected
			mapping.deleteMappingCells(new ArrayList<MappingCell>(mappingCellsToDelete));
		}
	}
}
