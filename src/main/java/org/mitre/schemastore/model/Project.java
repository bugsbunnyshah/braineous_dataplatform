// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.mitre.schemastore.model.schemaInfo.model.SchemaModel;

/**
 * Class for storing a project
 * @author CWOLF
 */
public class Project implements Serializable
{
	/** Stores the project id */
	private Integer id;

	/** Stores the project name */
	private String name;

	/** Stores the project description */
	private String description;

	/** Stores the project author */
	private String author;

	/** Stores the project schemas */
	private ProjectSchema[] schemas;

	/** Constructs a default project */	public Project() {}

	/** Constructs a project */
	public Project(Integer id, String name, String description, String author, ProjectSchema[] schemas)
		{ this.id = id; this.name = name; this.description = description; this.author = author; this.schemas = schemas; }

	/** Copies the project */
	public Project copy()
		{ return new Project(getId(),getName(),getDescription(),getAuthor(),getSchemas()); }

	// Handles all project getters
	public Integer getId() { return id; }
	public String getName() { return name; }
	public String getDescription() { return description; }
	public String getAuthor() { return author; }
	public ProjectSchema[] getSchemas() { return schemas; }

	// Handles all project setters
	public void setId(Integer id) { this.id = id; }
	public void setName(String name) { this.name = name; }
	public void setDescription(String description) { this.description = description; }
	public void setAuthor(String author) { this.author = author; }
	public void setSchemas(ProjectSchema[] schemas)
		{ this.schemas = schemas; }
	
	/** Returns the list of project schema IDs */
	public Integer[] getSchemaIDs()
	{
		ArrayList<Integer> schemaIDs = new ArrayList<Integer>();
		if(schemas!=null)
			for(ProjectSchema schema : schemas)
				schemaIDs.add(schema.getId());
		return schemaIDs.toArray(new Integer[0]);
	}
	
	/** Retrieves the schema model for the specified schema */
	public SchemaModel getSchemaModel(Integer schemaID)
	{
		for(ProjectSchema schema : schemas)
			if(schema.getId().equals(schemaID))
				return schema.geetSchemaModel();
		return null;
	}
	
	/** Returns the hash code */
	public int hashCode()
		{ return id.hashCode(); }

	/** Indicates that two projects are equals */
	public boolean equals(Object object)
	{
		if(object instanceof Integer) return ((Integer)object).equals(id);
		if(object instanceof Project) return ((Project)object).id.equals(id);
		return false;
	}

	/** String representation of the project */
	public String toString()
		{ return name; }
}