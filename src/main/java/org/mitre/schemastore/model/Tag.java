// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

import java.io.Serializable;

/**
 * Class for storing a schema tag
 * @author CWOLF
 */
public class Tag implements Serializable
{
	/** Stores the tag id */
	private Integer id;
	
	/** Stores the tag name */
	private String name;
	
	/** Stores the parent category for this tag */
	private Integer parentID;
	
	/** Constructs a default tag */
	public Tag() {}
	
	/** Constructs a tag */
	public Tag(Integer id, String name, Integer parentID)
		{ this.id = id; this.name = name; this.parentID = (parentID==null || parentID==0) ? null : parentID; }
	
	// Handles all tag getters
	public Integer getId() { return id; }
	public String getName() { return name; }
	public Integer getParentId() { return parentID; }
	
	// Handles all tag setters
	public void setId(Integer id) { this.id = id; }
	public void setName(String name) { this.name = name; }
	public void setParentId(Integer parentID) { this.parentID = (parentID==null || parentID==0) ? null : parentID; }

	/** Returns the hash code */
	public int hashCode()
		{ return id.hashCode(); }
	
	/** Indicates that two tags are equals */
	public boolean equals(Object object)
	{
		if(object instanceof Integer) return ((Integer)object).equals(id);
		if(object instanceof Tag) return ((Tag)object).id.equals(id);
		return false;
	}
	
	/** String representation of the tag */
	public String toString()
		{ return name; }
}