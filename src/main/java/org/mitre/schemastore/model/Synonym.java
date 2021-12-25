// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

/**
 * Class for storing a synonym
 * @author HAOLI
 */
public class Synonym extends SchemaElement
{ 
	/** Stores the associated element id */
	private Integer elementID;

	/** Constructs a default synonym */
	public Synonym() {}
	
	/** Constructs the synonym */
	public Synonym(Integer id, String name, String description, Integer elementID, Integer base)
		{ super(id,name,description,base); this.elementID = elementID; }

	/** Copies the synonym */
	public Synonym copy()
		{ return new Synonym(getId(),getName(),getDescription(),getElementID(),getBase()); }
	
	/** Retrieves the synonym's element id */
	public Integer getElementID()
		{ return elementID; }	
	
	/** Sets the synonym's element id */
	public void setElementID(Integer elementID)
		{ this.elementID = elementID; }

	/** Returns the list of referenced IDs */
	public int[] getReferencedIDs()
		{ return new int[]{elementID}; }
}