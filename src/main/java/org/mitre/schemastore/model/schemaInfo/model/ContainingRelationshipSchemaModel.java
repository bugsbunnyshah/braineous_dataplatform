package org.mitre.schemastore.model.schemaInfo.model;

import java.util.ArrayList;
import java.util.Arrays;

import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

public class ContainingRelationshipSchemaModel extends OWLSchemaModel {

	/** Returns the schema model name */
	public String getName()
	{ 
		if (name == null) {
			name = "Containing Relationship";
		}
		return name;
	}
		
	
	/** Returns the root elements in this schema */
	public ArrayList<SchemaElement> getRootElements(HierarchicalSchemaInfo schemaInfo)
	{
		ArrayList<SchemaElement> result = schemaInfo.getElements(Entity.class);
		for (SchemaElement schemaElement : schemaInfo.getElements(Subtype.class))
		{
			Subtype subtype = (Subtype)schemaElement;
			result.remove(schemaInfo.getElement(subtype.getChildID()));
		}
		for (SchemaElement element : schemaInfo.getElements(Relationship.class))
		{
			Relationship rel = (Relationship) element;
			result.remove(schemaInfo.getElement(rel.getRightID()));
		}
		for (SchemaElement element : schemaInfo.getElements(Containment.class)){
			Containment containment = (Containment) element;
			if (containment.getParentID()== null) {
				result.add(containment);
			}
			result.remove(schemaInfo.getElement(containment.getChildID()));
		}
		return result;
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
			for (Relationship rel : schemaInfo.getRelationships(elementID)){
				if (rel.getRightID().equals(elementID)) {
					parentElements.add(rel);
				}
			}
		}
		
		// If a containment, return the containing element as a parent.
		if(element instanceof Containment)
		{
			Containment containment = (Containment)element;
			Integer parentID = containment.getParentID();
			parentElements.add(schemaInfo.getElement(parentID));
		}

		// If element is relationship, return 
		if (element instanceof Relationship)
		{
			parentElements.add(schemaInfo.getElement(((Relationship)element).getLeftID()));
		}
		
		
		return parentElements;
	}

	/** Returns the children elements of the specified element in this schema */
	public ArrayList<SchemaElement> getChildElements(HierarchicalSchemaInfo schemaInfo, Integer elementID)
	{
		ArrayList<SchemaElement> childElements = new ArrayList<SchemaElement>();
		
		// Produce the list of children elements (entities, relationships and containments have children elements)
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
				if (rel.getLeftID()!= null &&  elementID != null && rel.getLeftID().equals(elementID))
					childElements.add(rel);
			
			// Retrieve subtypes as children
			for (Subtype subtype : schemaInfo.getSubTypes(element.getId()))
			{
				Integer childID = subtype.getChildID();
				if (!elementID.equals(childID))
					childElements.add(schemaInfo.getElement(childID));
			}
		}
		
		if (element instanceof Relationship) {
			childElements.add(schemaInfo.getElement(((Relationship)element).getRightID()));
		}
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
	
	
}
