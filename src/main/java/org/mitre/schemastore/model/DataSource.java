// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

import java.io.Serializable;

/**
 * Class for storing a data source
 * @author CWOLF
 */
public class DataSource implements Serializable
{
	/** Stores the data source id */
	private Integer id;
	
	/** Stores the data source name */
	private String name;
	
	/** Stores the data source URL */
	private String url;
	
	/** Stores the data source schema ID */
	private Integer schemaID;
	
	/** Stores the data source element ID */
	private Integer elementID;

	/** Constructs a default data source */
	public DataSource() {} 
	
	/** Constructs a data source object */
	public DataSource(Integer id, String name, String url, Integer schemaID, Integer elementID)
		{ this.id = id; this.name = name; this.url = url; this.schemaID = schemaID; this.elementID = elementID; }
	
	// Handles all data source getters
	public Integer getId() { return id; }
	public String getName() { return name; }
	public String getUrl() { return url; }
	public Integer getSchemaID() { return schemaID; }
	public Integer getElementID() { return elementID; }

	// Handles all data source setters
	public void setId(Integer id) { this.id = id; }
	public void setUrl(String url) { this.url = url; }
	public void setName(String name) { this.name = name; }
	public void setSchemaID(Integer schemaID) { this.schemaID = schemaID; }
	public void setElementID(Integer elementID) { this.elementID = elementID; }

	/** Returns the hash code */
	public int hashCode()
		{ return id.hashCode(); }
	
	/** Indicates that two data sources are equals */
	public boolean equals(Object object)
	{
		if(object instanceof Integer) return ((Integer)object).equals(id);
		if(object instanceof DataSource) return ((DataSource)object).id.equals(id);
		return false;
	}
	
	/** String representation of the data source */
	public String toString()
		{ return name; }
}
