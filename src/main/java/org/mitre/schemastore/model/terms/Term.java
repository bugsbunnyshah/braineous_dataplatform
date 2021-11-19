// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.terms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Class for storing a vocabulary term
 * @author CWOLF
 */
public class Term implements Serializable
{
	/** Stores the term ID */
	private Integer id;
	
	/** Stores the term name */
	private String name;
	
	/** Stores the term description */
	private String description;
	
	/** Stores the list of associated elements */
	private AssociatedElement[] elements = new AssociatedElement[0];

	/** Constructs the default term */ public Term() {}
	
	/** Constructs the term */
	public Term(Integer id, String name, String description)
		{ this.id = id; this.name = name; this.description = description; }
	
	/** Constructs the term */
	public Term(Integer id, String name, String description, AssociatedElement[] elements)
		{ this.id = id; this.name = name; this.description = description; this.elements = elements; }

	/** Copies the term */
	public Term copy()
	{
		ArrayList<AssociatedElement> elementArray = new ArrayList<AssociatedElement>();
		for(AssociatedElement element : elements)
			elementArray.add(element.copy());		
		return new Term(id, name, description, elementArray.toArray(new AssociatedElement[0]));
	}

	// Handles all of the term getters
	public Integer getId() { return id; }
	public String getName() { return name; }
	public String getDescription() { return description; }
	public AssociatedElement[] getElements() { return elements; }

	// Handles all of the term setters
	public void setId(Integer id) { this.id = id; }
	public void setName(String name) { this.name = name; }
	public void setDescription(String description) { this.description = description; }
	public void setElements(AssociatedElement[] elements) { this.elements = elements; }

	/** Declares a hash code for the specified term */
	public int hashCode()
		{ return getId().hashCode(); }

	/** Indicates that two terms are equal */
	public boolean equals(Object object)
	{
		if(object instanceof Term) return ((Term)object).getId().equals(id);
		return false;
	}	
	
	/** Gets the associated elements for the specified schema */
	public AssociatedElement[] getAssociatedElements(int schemaID)
	{
		ArrayList<AssociatedElement> Aelements = new ArrayList<AssociatedElement>();
		for(AssociatedElement element : elements)
			if(element.getSchemaID().equals(schemaID)) Aelements.add(element);
		AssociatedElement[] retval = new AssociatedElement[Aelements.size()];
		return Aelements.toArray(retval);
	}
	
	/** Adds an associated element to the term */
	public boolean addAssociatedElement(AssociatedElement newElement)
	{
		// Don't add associated element if already exists
		for(int i = 0; i < this.elements.length; i++)
			if(this.elements[i].getElementID().equals(newElement.getElementID()))
				if(this.elements[i].getSchemaID().equals(newElement.getSchemaID())) return false;

		// Adds the element to the array
		ArrayList<AssociatedElement> elements = new ArrayList<AssociatedElement>(Arrays.asList(this.elements));
		elements.add(newElement);	
		this.elements = elements.toArray(new AssociatedElement[0]);
		return true;
	}
	
	/** Removes the associated element from the term */
	public void removeAssociatedElement(AssociatedElement oldElement)
	{
		ArrayList<AssociatedElement> elements = new ArrayList<AssociatedElement>(Arrays.asList(this.elements));
		for(AssociatedElement element : elements)
			if(element.getSchemaID().equals(oldElement.getSchemaID()) && element.getElementID().equals(oldElement.getElementID()))
				{ elements.remove(element); break; }
		this.elements = elements.toArray(new AssociatedElement[0]);		
	}

	/** Removes the associated element from the term */
	public void removeAssociatedElement(Integer id, Integer schemaID)
	{
		ArrayList<AssociatedElement> elements = new ArrayList<AssociatedElement>(Arrays.asList(this.elements));
		for(int i = 0; i < this.elements.length; i++)
			if(this.elements[i].getSchemaID().equals(schemaID) && this.elements[i].getElementID().equals(id))
				{ elements.remove(i); break; }
		this.elements = elements.toArray(new AssociatedElement[0]);		
	}
}
