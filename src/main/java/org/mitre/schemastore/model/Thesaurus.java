// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

/**
 * Class storing a thesaurus from the repository
 * @author CWOLF
 */
public class Thesaurus extends Schema
{
	/** Constructs a default thesaurus */
	public Thesaurus() {}
	
	/** Constructs a thesaurus */
	public Thesaurus(Integer id, String name, String description)
	{
		this.id = id;
		this.name = name;
		this.author = "";
		this.source = "";
		this.type = Thesaurus.class.toString();
		this.description = description;
		this.locked = false;
	}

	// Blocks specific schema getters
	public void setAuthor(String author) {}
	public void setSource(String source) {}
	public void setType(String type) {}
	public void setLocked(boolean locked) {}
}