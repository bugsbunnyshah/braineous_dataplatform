package org.mitre.schemastore.model.mappingInfo;

import java.util.ArrayList;
import java.util.HashMap;

import org.mitre.schemastore.model.MappingCell;

/**
 * Reverse index mapping cells by elementID whether the element is a source or a target. 
 * @author HAOLI
 *
 */
public class AssociatedElementHash {
	/** Stores the mapping cells */
	private HashMap<Integer, ArrayList<MappingCell>> idCellHash = new HashMap<Integer, ArrayList<MappingCell>>();

	public AssociatedElementHash(ArrayList<MappingCell> mappingCells) {
		for (MappingCell mappingCell : mappingCells)
			set(mappingCell);
	}

	/** Sets the specified mapping cell */
	public void set(MappingCell mappingCell) {
		Integer output = mappingCell.getOutput();
		if (!idCellHash.containsKey(output))
			idCellHash.put(output, new ArrayList<MappingCell>()); 
		if (!get(output).contains(mappingCell))
			get(output).add(mappingCell); 

		for (Integer input : mappingCell.getElementInputIDs()) {
			if ( !idCellHash.containsKey(input))
				idCellHash.put(input, new ArrayList<MappingCell>()); 

			if (!get(input).contains(mappingCell))
				get(input).add(mappingCell); 
		}
	}
	
	/** Returns all of the mapping cells */
	public ArrayList<MappingCell> get()
	{
		ArrayList<MappingCell> allCells = new ArrayList<MappingCell>();
		for ( ArrayList<MappingCell> oneList : idCellHash.values() ) {
			for ( MappingCell cell : oneList)
				if ( !allCells.contains(cell))
					allCells.add(cell); 
		}
		return allCells;
	}

	/**
	 * Returns a set of mappings cells related to the element ID regardless of
	 * whether it be an input or an output
	 **/
	public ArrayList<MappingCell> get(Integer elementID) {
		if (idCellHash.containsKey(elementID))
			return idCellHash.get(elementID);
		else
			return new ArrayList<MappingCell>(0);
	}
	
	/**
	 * Returns a list of mappingCells by input element ID with minimum and/or
	 * maximum scores specified
	 * 
	 * @param inputID
	 *            schema element ID
	 * @param minScore
	 *            minimum mapping cell score
	 * @param maxScore
	 *            maximum mapping cell score
	 * @return
	 */
	public ArrayList<MappingCell> get(Integer inputID, Double minScore, Double maxScore) {
		ArrayList<MappingCell> result = new ArrayList<MappingCell>();
		for (MappingCell cell : get(inputID)) {
			if (minScore != null && maxScore != null && cell.getScore().doubleValue() >= minScore.doubleValue() && cell.getScore().doubleValue() <= maxScore.doubleValue())
				result.add(cell);
			else if (minScore != null && cell.getScore().doubleValue() >= minScore.doubleValue())
				result.add(cell);
			else if (maxScore != null && cell.getScore().doubleValue() <= maxScore.doubleValue())
				result.add(cell);
		}
		return result;
	}

	/** Deletes the specified mapping cell */
	public void delete(MappingCell mappingCell) {
		for (Integer input : mappingCell.getElementInputIDs())
			if (idCellHash.containsKey(input))
				idCellHash.get(input).remove(mappingCell);

		Integer output = mappingCell.getOutput();
		if (idCellHash.containsKey(output))
			idCellHash.get(output).remove(mappingCell);
	}

	/** Returns the specified mapping cell with input and output IDs */
	public MappingCell get(Integer inputID, Integer outputID) {
		if (idCellHash.containsKey(inputID)) {
			for (MappingCell cell : idCellHash.get(inputID))
				if (cell.getOutput().equals(outputID))
					return cell;
		}
		return null;
	}

	/** Indicates if the mapping contains the specified mapping cell */
	public boolean contains(Integer inputID, Integer outputID) {
		return contains(new Integer[] { inputID }, outputID);
	}

	/** Indicates if the mapping contains the specified mapping cell */
	public boolean contains(Integer inputIDs[], Integer outputID) {
		for (Integer input : inputIDs)
			if (get(input, outputID) != null)
				return true;
		return false;
	}

}
