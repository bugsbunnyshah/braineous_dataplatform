// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

import java.io.Serializable;

/**
 * Class storing a single schema from the repository
 * @author CWOLF
 */
public class Schema implements Serializable
{	
	/** Stores the schema id */
	protected Integer id;
	
	/** Stores the schema name */
	protected String name;
	
	/** Stores the schema author */
	protected String author;
	
	/** Stores the schema source */
	protected String source;

	/** Stores the schema type */
	protected String type;
	
	/** Stores the schema description */
	protected String description;	
	
	/** Indicates if the schema is locked */
	protected boolean locked;
	
	/** Constructs a default schema */
	public Schema() {}
	
	/** Constructs a schema */
	public Schema(Integer id, String name, String author, String source, String type, String description, boolean locked)
		{ this.id = id; this.name = name; this.author = author;  this.source = source; this.type = type; this.description = description; this.locked = locked; }
	
	/** Copies the schema */
	public Schema copy()
		{ return new Schema(getId(),getName(),getAuthor(),getSource(),getType(),getDescription(),getLocked()); }
	
	// Handles all schema getters
	public Integer getId() { return id; }
	public String getName() { return name; }
	public String getAuthor() { return author; }
	public String getSource() { return source; }
	public String getType() { return type; }
	public String getDescription() { return description; }
	public boolean getLocked() { return locked; }

	// Handles all schema setters
	public void setId(Integer id) { this.id = id; }
	public void setName(String name) { this.name = name; }
	public void setAuthor(String author) { this.author = author; }
	public void setSource(String source) { this.source = source; }
	public void setType(String type) { this.type = type; }
	public void setDescription(String description) { this.description = description; }
	public void setLocked(boolean locked) { this.locked = locked; }

	/** Returns the hash code */
	public int hashCode()
		{ return id.hashCode(); }
	
	/** Indicates that two schemas are equals */
	public boolean equals(Object object)
	{
		if(object instanceof Integer) return ((Integer)object).equals(id);
		if(object instanceof Schema) return ((Schema)object).id.equals(id);
		return false;
	}
	
	/** String representation of the schema */
	public String toString()
		{ return name; }
}