// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

/**
 * Class for storing a containment relationship
 * @author CWOLF
 */
public class Containment extends SchemaElement
{
	/** Stores the containment's parent id */
	private Integer parentID;
	
	/** Stores the containment's child id */
	private Integer childID;

	/** Stores the containment's min value */
	private Integer min;
	
	/** Stores the containment's max value */
	private Integer max;
	
	/** Constructs a default containment relationship */
	public Containment() {}

	/** Constructs the containment relationship */
	public Containment(Integer id, String name, String description, Integer parentID, Integer childID, Integer min, Integer max, Integer base)
		{ super(id,name,description,base); this.parentID=parentID; this.childID=childID; this.min=min; this.max=max; }

	/** Copies the containment relationship */
	public Containment copy()
		{ return new Containment(getId(),getName(),getDescription(),getParentID(),getChildID(),getMin(),getMax(),getBase()); }
	
	// Handles all containment getters
	public Integer getParentID() { return parentID==null || parentID.equals(0) ? null : parentID; }
	public Integer getChildID() { return childID; }
	public Integer getMin() { return min; }
	public Integer getMax() { return max; }
	
	// Handles all containment setters
	public void setParentID(Integer parentID) { this.parentID = parentID==null || parentID==0 ? null : parentID; }
	public void setChildID(Integer childID) { this.childID = childID; }	
	public void setMin(Integer min) { this.min = min; }
	public void setMax(Integer max) { this.max = max; }

	/** Returns the list of referenced IDs */
	public int[] getReferencedIDs()
	{
		if(parentID!=null) return new int[]{parentID,childID};
		return new int[]{childID};
	}
}