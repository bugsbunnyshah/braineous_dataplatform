// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

/**
 * Class for storing a domain
 * @author CWOLF
 */
public class Domain extends SchemaElement
{
	/** Constructs a default domain */
	public Domain() {}
	
	/** Constructs the domain */
	public Domain(Integer id, String name, String description, Integer base)
		{ super(id,name,description,base); }
	
	/** Copies the domain */
	public Domain copy()
		{ return new Domain(getId(),getName(),getDescription(),getBase()); }
}
