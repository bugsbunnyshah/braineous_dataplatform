// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

/**
 * Class for storing a relationship
 * @author CWOLF
 */
public class Relationship extends SchemaElement
{
	/** Stores the relationship's left id */
	private Integer leftID;
	
	/** Stores the relationship's left min value */
	private Integer leftMin;
	
	/** Stores the relationship's left max value */
	private Integer leftMax;
	
	/** Stores the relationship's right id */
	private Integer rightID;
	
	/** Stores the relationship's right min value */
	private Integer rightMin;
	
	/** Stores the relationship's right max value */
	private Integer rightMax;	

	/** Constructs a default relationship */
	public Relationship() {}
	
	/** Constructs the relationship */
	public Relationship(Integer id, String name, String description, Integer leftID, Integer leftMin, Integer leftMax, Integer rightID, Integer rightMin, Integer rightMax, Integer base)
		{ super(id,name,description,base); this.leftID=leftID; this.leftMin=leftMin; this.leftMax=leftMax; this.rightID=rightID; this.rightMin=rightMin; this.rightMax=rightMax; }
	
	/** Copies the relationship */
	public Relationship copy()
		{ return new Relationship(getId(),getName(),getDescription(),getLeftID(),getLeftMin(),getLeftMax(),getRightID(),getRightMin(),getRightMax(),getBase()); }
	
	// Handles all relationship getters
	public Integer getLeftID() { return leftID; }
	public Integer getLeftMin() { return leftMin; }
	public Integer getLeftMax() { return leftMax; }
	public Integer getRightID() { return rightID; }
	public Integer getRightMin() { return rightMin; }
	public Integer getRightMax() { return rightMax; }

	// Handles all relationship setters
	public void setLeftID(Integer leftID) { this.leftID = leftID; }
	public void setLeftMax(Integer leftMax) { this.leftMax = leftMax; }
	public void setLeftMin(Integer leftMin) { this.leftMin = leftMin; }
	public void setRightID(Integer rightID) { this.rightID = rightID; }
	public void setRightMax(Integer rightMax) { this.rightMax = rightMax; }
	public void setRightMin(Integer rightMin) { this.rightMin = rightMin; }

	/** Returns the list of referenced IDs */
	public int[] getReferencedIDs()
		{ return new int[]{leftID,rightID}; }
}