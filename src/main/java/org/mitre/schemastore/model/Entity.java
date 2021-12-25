// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

/**
 * Class for storing an entity
 * @author CWOLF
 */
public class Entity extends SchemaElement
{
	/** Constructs a default entity */
	public Entity() {}
	
	/** Constructs the entity */
	public Entity(Integer id, String name, String description, Integer base)
		{ super(id,name,description,base); }
	
	/** Copies the entity */
	public Entity copy()
		{ return new Entity(getId(),getName(),getDescription(),getBase()); }
}