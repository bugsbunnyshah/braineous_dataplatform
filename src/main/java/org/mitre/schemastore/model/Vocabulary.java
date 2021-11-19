// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

/**
 * Class storing a vocabulary from the repository
 * @author CWOLF
 */
public class Vocabulary extends Schema
{
	/** Constructs a default vocabulary */
	public Vocabulary() {}
	
	/** Constructs a vocabulary */
	public Vocabulary(Integer id, String name)
	{
		this.id = id;
		this.name = name;
		this.author = ""; 
		this.source = "";
		this.type = Vocabulary.class.toString();
		this.description = "";
		this.locked = false;
	}

	// Blocks specific schema getters
	public void setAuthor(String author) {}
	public void setSource(String source) {}
	public void setType(String type) {}
	public void setDescription(String description) {}
	public void setLocked(boolean locked) {}
}