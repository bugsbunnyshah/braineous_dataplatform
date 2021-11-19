// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.schemaInfo.model;

import java.util.ArrayList;
import java.util.Arrays;

import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

/**
 *  Class for displaying XML hierarchy
 */
public class XMLSchemaModel extends SchemaModel
{
	/** Returns the schema model name */
	public String getName()
	{ 
		if (name == null) {
			name = "XML";
		}
		return name;
	}
	
	/** Returns the root elements in this schema */
	public ArrayList<SchemaElement> getRootElements(HierarchicalSchemaInfo schemaInfo)
	{
		ArrayList<SchemaElement> rootElements = new ArrayList<SchemaElement>();

		// Find all containments whose roots are null 
		// and were not created by splitting 
		for(SchemaElement element : schemaInfo.getElements(Containment.class))
			if(((Containment)element).getParentID()==null){
				
				Containment cont = (Containment)element;
				boolean isSplit = false;
				
				for (SchemaElement se : schemaInfo.getElements(Containment.class)){
					Containment cont2 = (Containment)se;
					if (!cont.getId().equals(cont2.getId())  
							&& cont.getName().equals(cont2.getName()) 
							&& cont.getDescription().equals(cont2.getDescription())
							&& cont.getChildID().equals(cont2.getChildID())
							&& cont2.getParentID() != null
							&& !cont.getBase().equals(cont2.getBase()))
					{
						isSplit = true;
					}
				}
				if (isSplit == false)
					rootElements.add(element);
				
			}
		return rootElements;
	}
	
	
	/** Returns the parent elements of the specified element in this schema */
	public ArrayList<SchemaElement> getParentElements(HierarchicalSchemaInfo schemaInfo, Integer elementID)
	{
		ArrayList<SchemaElement> parentElements = new ArrayList<SchemaElement>();
		
		// Identify the parent ID for which containments need to be found
		SchemaElement element = schemaInfo.getElement(elementID);
		Integer parentID = null;
		if (element instanceof Containment) parentID = ((Containment)element).getParentID();
		if (element instanceof Attribute) parentID = ((Attribute)element).getEntityID();

		// Expand the parent to include all of its children.
		// Let A be a super-class of B with attributes x and y, respectively.
		// Let C be a containment that links P1 to A.
		// Let D be a containment that links P2 to B.
		// The children of C is x.
		// The children of D are x and y.
		// Therefore, the parents of x are C and D.
		// The parents of y is C.
		if (parentID != null) {
			// For A, descendants = {A,B}.  For B, descendants = {B}.
			ArrayList<Integer> descendants = getSubTypes(schemaInfo, parentID);
			for (Integer descendantID : descendants) {
				// Get all containments that reference A or B.
				for (Containment containment : schemaInfo.getContainments(descendantID)) {
					// If the containment references A or B, add it to the result.
					if (containment.getChildID().equals(descendantID)) {
						parentElements.add(containment);
					}
				}
			}
		}
			
		return parentElements;
	}
	
	/** Returns the children elements of the specified element in this schema */
	public ArrayList<SchemaElement> getChildElements(HierarchicalSchemaInfo schemaInfo, Integer elementID)
	{
		ArrayList<SchemaElement> childElements = new ArrayList<SchemaElement>();
		
		// Find all containments one level lower on the schema
		SchemaElement element = schemaInfo.getElement(elementID);
		if(element instanceof Containment)
		{
			Integer childID = ((Containment)element).getChildID();

			if (!(schemaInfo.getElement(childID) instanceof Domain)){
			
			
				// Build list of all IDs for super-type entities
				ArrayList<Integer> superTypeIDs = getSuperTypes(schemaInfo, childID);
					
				// Retrieves all containments whose parent is the child ID
				for (Integer id : superTypeIDs)
					for(Containment containment : schemaInfo.getContainments(id))
						if(id.equals(containment.getParentID()))
							childElements.add(containment);
	
				// Retrieves all attributes whose element is the child ID
				for (Integer id : superTypeIDs)
					for(Attribute attribute : schemaInfo.getAttributes(id))
						childElements.add(attribute);
			}
		}
			
		return childElements;
	}
	
	/** Identify the super types of the specified element */
	private ArrayList<Integer> getSuperTypes(HierarchicalSchemaInfo schemaInfo, Integer childID)
	{
		ArrayList<Integer> elementIDs = new ArrayList<Integer>(Arrays.asList(new Integer[]{childID}));
		for(int i=0; i<elementIDs.size(); i++)
		{
			Integer parentID = elementIDs.get(i);
			for(Subtype subtype : schemaInfo.getSubTypes(parentID))
				if(!elementIDs.contains(subtype.getParentID()))
					elementIDs.add(subtype.getParentID());
		}
		return elementIDs;
	}

	/** Identify the sub types of the specified element */
	private ArrayList<Integer> getSubTypes(HierarchicalSchemaInfo schemaInfo, Integer parentID)
	{
		ArrayList<Integer> elementIDs = new ArrayList<Integer>(Arrays.asList(new Integer[]{parentID}));
		for(int i=0; i<elementIDs.size(); i++)
		{
			Integer childID = elementIDs.get(i);
			for(Subtype subtype : schemaInfo.getSubTypes(childID))
				if(!elementIDs.contains(subtype.getChildID()))
					elementIDs.add(subtype.getChildID());
		}
		return elementIDs;
	}

	/** Returns the domains of the specified element in this schema */
	public Domain getDomainForElement(HierarchicalSchemaInfo schemaInfo, Integer elementID)
	{
		// Find the domain attached to this containment
		SchemaElement element = schemaInfo.getElement(elementID);
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
		
		// Find all containments that reference this domain
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