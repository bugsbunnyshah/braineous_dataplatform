// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

import java.io.Serializable;

/**
 * Class for storing a data type
 * @author CWOLF
 */
public class DataType implements Serializable
{
	/** Stores the data type id */
	private Integer id;
	
	/** Stores the data type name */
	private String name;
	
	/** Stores the data type description */
	private String description;
	
	/** Constructs a default data type */
	public DataType() {}
	
	/** Constructs a data type */
	public DataType(Integer id, String name, String description)
		{ this.id=id; this.name=name; this.description=description; }
	
	// Handles all data type getters
	public Integer getId() { return id; }
	public String getName() { return name; }
	public String getDescription() { return description; }
	
	// Handles all data type setters
	public void setId(Integer id) { this.id = id; }
	public void setName(String name) { this.name = name; }
	public void setDescription(String description) { this.description = description; }
	
	/** String representation of the function implementation */
	public String toString()
		{ return name; }
}