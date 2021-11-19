// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

/**
 * Class for storing an alias
 * @author CWOLF
 */
public class Alias extends SchemaElement
{
	/** Stores the element id */
	private Integer elementID;
	
	/** Constructs a default alias */
	public Alias() {}
	
	/** Constructs the alias */
	public Alias(Integer id, String name, Integer elementID, Integer base)
		{ super(id,name,"",base); this.elementID = elementID; }
	
	/** Copies the alias */
	public Alias copy()
		{ return new Alias(getId(),getName(),getElementID(),getBase()); }
	
	/** Retrieves the alias' element id */
	public Integer getElementID()
		{ return elementID; }
	
	/** Sets the alias' element id */
	public void setElementID(Integer elementID)
		{ this.elementID = elementID; }

	/** Returns the list of referenced IDs */
	public int[] getReferencedIDs()
		{ return new int[]{elementID}; }
}