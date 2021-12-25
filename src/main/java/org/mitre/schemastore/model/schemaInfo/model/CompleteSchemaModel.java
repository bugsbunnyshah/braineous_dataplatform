// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.schemaInfo.model;

import java.util.ArrayList;

import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

/**
 *  Class for displaying relationship hierarchy
 */
public class CompleteSchemaModel extends SchemaModel
{
	/** Returns the schema model name */
	public String getName()
	{ 
		if (name == null) {
			name = "Complete";
		}
		return name;
	}
	
	/** Returns the root elements in this schema */
	public ArrayList<SchemaElement> getRootElements(HierarchicalSchemaInfo schemaInfo)
	{
		ArrayList<SchemaElement> rootElements = schemaInfo.getElements(Entity.class);

		// Identify all top level entities
		for (SchemaElement schemaElement : schemaInfo.getElements(Subtype.class))
		{
			Subtype subtype = (Subtype)schemaElement;
			rootElements.remove(schemaInfo.getElement(subtype.getChildID()));
		}
		
		// Identify all domains
		for(SchemaElement element : schemaInfo.getElements(Domain.class))
			rootElements.add(element);
		
		return rootElements;
	}
	
	/** Returns the parent elements of the specified element in this schema */
	public ArrayList<SchemaElement> getParentElements(HierarchicalSchemaInfo schemaInfo, Integer elementID)
	{
		ArrayList<SchemaElement> parentElements = new ArrayList<SchemaElement>();
		
		// If attribute, return entity as parent
		SchemaElement element = schemaInfo.getElement(elementID);
		if(element instanceof Attribute)
			parentElements.add(schemaInfo.getEntity(elementID));
		
		// If an entity, return its super-types as parents.
		if(element instanceof Entity) {
			for (Subtype subtype : schemaInfo.getSubTypes(element.getId())) {
				Integer parentID = subtype.getParentID();
				if (!elementID.equals(parentID)) {
					parentElements.add(schemaInfo.getElement(parentID));
				}
			}
		}
		
		// If domain value, return its domain
		if(element instanceof DomainValue)
			parentElements.add(schemaInfo.getElement(((DomainValue)element).getDomainID()));
		
		// If a containment, return the containing element as a parent.
		if(element instanceof Containment)
		{
			Containment containment = (Containment)element;
			Integer parentID = containment.getParentID();
			parentElements.add(schemaInfo.getElement(parentID));
		}

		// If element is relationship, return the elements associated with the relationship
		if (element instanceof Relationship)
		{
			parentElements.add(schemaInfo.getElement(((Relationship)element).getLeftID()));
			parentElements.add(schemaInfo.getElement(((Relationship)element).getRightID()));
		}
		
		return parentElements;
	}
	
	/** Returns the children elements of the specified element in this schema */
	public ArrayList<SchemaElement> getChildElements(HierarchicalSchemaInfo schemaInfo, Integer elementID)
	{
		ArrayList<SchemaElement> childElements = new ArrayList<SchemaElement>();
		
		// Produce the list of children elements for entities
		SchemaElement element = schemaInfo.getElement(elementID);
		if(element instanceof Entity)
		{
			// Retrieve entity attributes		
			for(Attribute value : schemaInfo.getAttributes(elementID))
				childElements.add(value);
	
			// Retrieve containments as children.
			for (Containment containment : schemaInfo.getContainments(element.getId()))
				if(elementID.equals(containment.getParentID()) && !containment.getName().equals(""))
					childElements.add(containment);
			
			// Retrieve relationships as children
			for (Relationship rel : schemaInfo.getRelationships(elementID))
				childElements.add(rel);
			
			// Retrieve subtypes as children
			for (Subtype subtype : schemaInfo.getSubTypes(element.getId()))
			{
				Integer childID = subtype.getChildID();
				if (!elementID.equals(childID))
					childElements.add(schemaInfo.getElement(childID));
			}
		}

		// Produce the list of children elements for domains
		if(element instanceof Domain)
			childElements.addAll(schemaInfo.getDomainValuesForDomain(element.getId()));
		
		return childElements;
	}
	
	/** Returns the domains of the specified element in this schema */
	public Domain getDomainForElement(HierarchicalSchemaInfo schemaInfo, Integer elementID)
	{
		SchemaElement element = schemaInfo.getElement(elementID);
		
		// Find attribute domain values
		if(element instanceof Attribute)
			return (Domain)schemaInfo.getElement(((Attribute)element).getDomainID());

		// Find containment domain values
		if(element instanceof Containment)
		{
			Integer containmentID = ((Containment)element).getChildID();
			SchemaElement childElement = schemaInfo.getElement(containmentID);
			if(childElement instanceof Domain)
				return (Domain)childElement;
		}
		
		return null;
	}	
	
	/** Returns the elements referenced by the specified domain */
	public ArrayList<SchemaElement> getElementsForDomain(HierarchicalSchemaInfo schemaInfo, Integer domainID)
	{
		ArrayList<SchemaElement> domainElements = new ArrayList<SchemaElement>();

		// Find all attributes associated with the domain
		for(Attribute attribute : schemaInfo.getAttributes(domainID))
			domainElements.add(attribute);

		// Find all containments associated with the domain
		for(Containment containment : schemaInfo.getContainments(domainID))
			if(containment.getChildID().equals(domainID))
				domainElements.add(containment);
		
		return domainElements;
	}

	/** Returns the type name associated with the specified element (or NULL if element has no name) */
	public SchemaElement getType(HierarchicalSchemaInfo schemaInfo, Integer elementID)
	{
		SchemaElement element = schemaInfo.getElement(elementID);
		SchemaElement childElement = null;
		
		if(element instanceof Containment)
			childElement = schemaInfo.getElement(((Containment)element).getChildID());
				
		else if (element instanceof Attribute)
			childElement = schemaInfo.getElement(((Attribute)element).getDomainID());
		
		if (childElement != null && childElement.getName() != null && childElement.getName().length() > 0)
			return childElement;

		return null;	
	}
}