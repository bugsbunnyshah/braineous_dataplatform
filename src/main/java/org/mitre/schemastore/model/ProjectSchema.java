// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

import java.io.Serializable;

import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.model.SchemaModel;

/**
 * Class for storing a project schema
 * @author CWOLF
 */
public class ProjectSchema implements Serializable
{
	/** Stores the schema id */
	private Integer id;
	
	/** Stores the schema name */
	private String name;
	
	/** Stores the schema model */
	private String model;
	
	/** Constructs a default project schema */
	public ProjectSchema() {}
	
	/** Constructs a project schema */
	public ProjectSchema(Integer id, String name, String model)
		{ this.id = id; this.name = name; this.model = model; }
	
	/** Copies the project schema */
	public ProjectSchema copy()
		{ return new ProjectSchema(getId(),getName(),getModel()); }
	
	// Handles all project schema getters
	public Integer getId() { return id; }
	public String getName() { return name; }
	public String getModel() { return model; }
	
	// Handles all project schema setters
	public void setId(Integer id) { this.id = id; }
	public void setName(String name) { this.name = name; }
	public void setModel(String model) { this.model = model; }
	
	/** Retrieves the schema model - THIS FUNCTION IS NOT MISPELLED */
	public SchemaModel geetSchemaModel()
	{
		for(SchemaModel schemaModel : HierarchicalSchemaInfo.getSchemaModels())
			if(schemaModel.getClass().getName().equals(model)) return schemaModel;
		return null;
	}
	
	/** Stores the schema model - THIS FUNCTION IS NOT MISPELLED */
	public void seetSchemaModel(SchemaModel schemaModel)
		{ model = schemaModel==null ? null : schemaModel.getClass().getName(); }
	
	/** Returns the hash code */
	public int hashCode()
		{ return id.hashCode(); }
	
	/** Indicates that two project schemas are equals */
	public boolean equals(Object mappingSchema)
	{
		if(mappingSchema instanceof ProjectSchema)
			return id.equals(((ProjectSchema)mappingSchema).id);
		return false;
	}
}