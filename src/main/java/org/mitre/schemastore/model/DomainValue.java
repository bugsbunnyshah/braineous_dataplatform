// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

/**
 * Class for storing a domain value
 * @author CWOLF
 */
public class DomainValue extends SchemaElement
{
	/** Stores the domain value's domain id */
	private Integer domainID;
	
	/** Constructs a default domain value */
	public DomainValue() {}
	
	/** Constructs the domain value */
	public DomainValue(Integer id, String name, String description, Integer domainID, Integer base)
		{ super(id,name,description,base); this.domainID=domainID; }
	
	/** Copies the domain value */
	public DomainValue copy()
		{ return new DomainValue(getId(),getName(),getDescription(),getDomainID(),getBase()); }
	
	/** Retrieves the domain value's domain id */
	public Integer getDomainID()
		{ return domainID; }
	
	/** Sets the domain value's domain id */
	public void setDomainID(Integer domainID)
		{ this.domainID = domainID; }

	/** Returns the list of referenced IDs */
	public int[] getReferencedIDs()
		{ return new int[]{domainID}; }
}
