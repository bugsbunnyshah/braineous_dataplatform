// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

import java.io.Serializable;

/**
 * Class for storing a mapping
 * @author CWOLF
 */
public class Mapping implements Serializable
{
	/** Stores the mapping id */
	private Integer id;

	/** Stores the project id */
	private Integer projectId;
	
	/** Stores the source schema */
	private Integer sourceId;

	/** Stores the target schema */
	private Integer targetId;

	/** Constructs a default mapping */
	public Mapping() {}

	/** Constructs a mapping */
	public Mapping(Integer id, Integer projectId, Integer sourceId, Integer targetId)
		{ this.id = id; this.projectId = projectId; this.sourceId = sourceId; this.targetId = targetId; }

	/** Copies the mapping */
	public Mapping copy()
		{ return new Mapping(getId(),getProjectId(),getSourceId(),getTargetId()); }

	// Handles all mapping getters
	public Integer getId() { return id; }
	public Integer getProjectId() { return projectId; }
	public Integer getSourceId() { return sourceId; }
	public Integer getTargetId() { return targetId; }

	// Handles all mapping setters
	public void setId(Integer id) { this.id = id; }
	public void setProjectId(Integer projectId) { this.projectId = projectId; }
	public void setSourceId(Integer sourceId) { this.sourceId = sourceId; }
	public void setTargetId(Integer targetId) { this.targetId = targetId; }

	/** Returns the hash code */
	public int hashCode()
		{ return id.hashCode(); }

	/** Indicates that two mappings are equals */
	public boolean equals(Object object)
	{
		if(object instanceof Integer) return ((Integer)object).equals(id);
		if(object instanceof Mapping) return ((Mapping)object).id.equals(id);
		return false;
	}

	/** String representation of the project */
	public String toString()
		{ return sourceId + " -> " + targetId; }
}