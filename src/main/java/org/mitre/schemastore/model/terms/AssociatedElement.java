// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.terms;

import java.io.Serializable;

/**
 * Class for storing an element associated with a vocabulary term
 * @author CWOLF
 */
public class AssociatedElement implements Serializable
{
	/** Stores the schema ID */
	private Integer schemaID;
	
	/** Stores the schema element */
	private Integer elementID;

	/** Stores the element name */
	private String name;

	/** Stores the element description */
	private String description;
	
	/** Constructs the default associated element */ public AssociatedElement() {}	
	
	/** Constructs the associated element */
	public AssociatedElement(Integer schemaID, Integer elementID, String name, String description)
		{ this.schemaID = schemaID; this.elementID = elementID; this.name = name; this.description = description; }

	/** Copies the associated element */
	public AssociatedElement copy()
		{ return new AssociatedElement(this.schemaID, this.elementID, this.name, this.description); }
	
	// Handles all of the associated element getters
	public Integer getSchemaID() { return schemaID; }
	public Integer getElementID() { return elementID; }
	public String getName() { return name; }
	public String getDescription() { return description; }
	
	// Handles all of the associated element setters
	public void setSchemaID(Integer schemaID) { this.schemaID = schemaID; }
	public void setElementID(Integer elementID) { this.elementID = elementID; }
	public void setName(String name) { this.name = name; }
	public void setDescription(String description) { this.description = description; }

	/** Declares a hash code for the specified associated element */
	public int hashCode()
		{ return getSchemaID().hashCode() + getElementID().hashCode(); }

	/** Indicates that two associated elements are equal */
	public boolean equals(Object object)
	{
		if(object instanceof AssociatedElement)
		{
			AssociatedElement element = (AssociatedElement)object;
			return element.getSchemaID().equals(schemaID) && element.getElementID().equals(elementID);
		}
		return false;
	}	
}