// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.schemaInfo.model;

import java.util.*;

import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

/**
 *  Class for displaying relationship hierarchy
 */
public class GSIPSchemaModel extends SchemaModel
{
	private HashMap<String, ArrayList<DomainValue>> cachedDomainValues = new HashMap<String, ArrayList<DomainValue>>();

	/** Returns the schema model name */
	public String getName()
	{ 
		if (name == null) {
			name = "GSIP";
		}
		return name;
	}
	/** Returns the root elements in this schema */
	public ArrayList<SchemaElement> getRootElements(HierarchicalSchemaInfo schemaInfo)
	{

		ArrayList<SchemaElement> result = orderEntitiesByName(schemaInfo.getElements(Entity.class));
		for (SchemaElement schemaElement : schemaInfo.getElements(Subtype.class))
		{
			Subtype subtype = (Subtype)schemaElement;
			result.remove(schemaInfo.getElement(subtype.getChildID()));
		}
		for (SchemaElement schemaElement : schemaInfo.getElements(Containment.class))
		{
			Containment containment = (Containment)schemaElement;
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
		
		if(element instanceof Relationship) {
			parentElements.add(schemaInfo.getEntity(((Relationship) element).getLeftID()));
		}
		if (element instanceof DomainValue) {
			DomainValue domainValue = (DomainValue) element;
			Integer domainID = domainValue.getDomainID();
			for (SchemaElement attribute : schemaInfo.getElements(Attribute.class)){
				Integer attributeID = ((Attribute) attribute).getDomainID();
				if (attributeID.intValue() ==domainID.intValue()) {
					parentElements.add(attribute);
				}
			} 
		}
		
		
		// If an entity, return its super-types as parents.
		if(element instanceof Entity) {
			for (Subtype subtype : schemaInfo.getSubTypes(element.getId())) {
				Integer parentID = subtype.getParentID();
				if (!elementID.equals(parentID)) {
					parentElements.add(schemaInfo.getElement(parentID));
				}
			}
			
			// Retrieve parents via containments.
			for (Containment containment : schemaInfo.getContainments(element.getId()))
				if(elementID.equals(containment.getChildID()) && !containment.getName().equals(""))
					parentElements.add(schemaInfo.getElement(containment.getParentID()));

		}

		return parentElements;
	}
	
	/** Returns the children elements of the specified element in this schema */
	public ArrayList<SchemaElement> getChildElements(HierarchicalSchemaInfo schemaInfo, Integer elementID)
	{
		ArrayList<SchemaElement> childElements = new ArrayList<SchemaElement>();

		// Produce the list of children elements (only entities have children elements)
		SchemaElement element = schemaInfo.getElement(elementID);
	
		if(element instanceof Entity)
		{
			// Retrieve entity attributes		
			for(Attribute value : orderAttributesByName(schemaInfo.getAttributes(elementID))) {
				childElements.add(value);
			}
			// Retrieve subtypes as children
			for (Subtype subtype : schemaInfo.getSubTypes(element.getId()))
			{
				Integer childID = subtype.getChildID();
				if (!elementID.equals(childID))
					childElements.add(schemaInfo.getElement(childID));
			}

			// Retrieve relationships		
			String rIDs = ":";
			for(Relationship value : schemaInfo.getRelationships(elementID)) {				
				if (value.getLeftID().equals(elementID))  {
					int match=0;
					for (Containment containment : schemaInfo.getContainments(elementID))  {
						if (containment.getChildID().equals(value.getId())) { match++; }
					}
					if (match==0 && !rIDs.contains(":"+value.getId()+":")) { 
						childElements.add(value); 
						rIDs+= value.getId()+":";
					}					
				}				
			}
			// Retrieve children via containments.
			for (Containment containment : schemaInfo.getContainments(element.getId()))  {				
				// might not need second predicate, copied from RelationalSchemaModel
				if(elementID.equals(containment.getParentID()) && !containment.getName().equals("")) {
					if (schemaInfo.getElement(containment.getChildID()) instanceof Entity) {
						childElements.add(schemaInfo.getElement(containment.getChildID()));
					}
					else { childElements.add(containment); }  //just show containment icon.
				}
			}
		}
		else if (element instanceof Attribute) {
			// Retrieve entity attributes		
			Attribute att = (Attribute) schemaInfo.getElement(elementID);
			if (cachedDomainValues.get(att.getDomainID().toString())==null) {
				cachedDomainValues.put(att.getDomainID().toString(), orderDomainValuesByName(schemaInfo.getDomainValuesForElement(elementID)));
			}			
			for(DomainValue domainValue : cachedDomainValues.get(att.getDomainID().toString())) {
				childElements.add(domainValue);
			}		
		}
		else if (element instanceof Containment) {
			Containment c = (Containment) element;
			SchemaElement child = schemaInfo.getElement(c.getChildID());
			if (child instanceof Relationship) { 
				Relationship r = (Relationship) child;
				Entity e = (Entity) schemaInfo.getElement(r.getRightID());
				r.setName(e.getName());
			}
		}
		return childElements;
	}
	
	/** Returns the domains of the specified element in this schema */
	public Domain getDomainForElement(HierarchicalSchemaInfo schemaInfo, Integer elementID)
	{
		SchemaElement element = schemaInfo.getElement(elementID);
		
		// Find attribute domain values
		if(element instanceof Attribute)
			return (Domain)schemaInfo.getElement(((Attribute)element).getDomainID());
	
		return null;
	}

	/** Returns the elements referenced by the specified domain */
	public ArrayList<SchemaElement> getElementsForDomain(HierarchicalSchemaInfo schemaInfo, Integer domainID)
	{
		ArrayList<SchemaElement> domainElements = new ArrayList<SchemaElement>();

		// Find all attributes associated with the domain
		for(Attribute attribute : schemaInfo.getAttributes(domainID))
			domainElements.add(attribute);

		
		return domainElements;
	}
	
	/** Returns the domain values associated with the specified element in this schema */
	@SuppressWarnings("unchecked")
	public ArrayList<DomainValue> orderDomainValuesByName(ArrayList<DomainValue> domainValues)
	{
		Collections.sort(domainValues, byNameComparator);
		return domainValues;
	}
	
	/** Retrieves the attributes for the specified schema element */
	@SuppressWarnings("unchecked")
	private ArrayList<Attribute> orderAttributesByName(ArrayList<Attribute> attributes)
	{
		Collections.sort(attributes, byNameComparator);
		return attributes;
	}
	
	/** Retrieves the attributes for the specified schema element */
	@SuppressWarnings("unchecked")
	private ArrayList<SchemaElement> orderEntitiesByName(ArrayList<SchemaElement> entities)
	{
		Collections.sort(entities, byNameComparator);
		return entities;
	}
	
    public Comparator byNameComparator = new Comparator() {
    	public int compare(Object o1, Object o2) {
            SchemaElement item1 = (SchemaElement) o1;
            SchemaElement item2 = (SchemaElement) o2;
                
            if (item1 == null && item2 == null) { return 0; }
            else if (item2 == null) { return 1; }
            else if (item1 == null) { return -1; }
                
            String name1 = item1.getName();
            String name2 = item2.getName();
                
            if (name1 == null) { name1 = ""; }
            if (name2 == null) { name2 = ""; }
            return name1.toUpperCase().compareTo(name2.toUpperCase());
    	}
    };
	
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
	
	public String getTypeString(HierarchicalSchemaInfo schemaInfo, Integer elementID)
	{
		SchemaElement type = getType(schemaInfo,elementID);
		if (schemaInfo.getElement(elementID) instanceof Relationship) { 
			Relationship r = (Relationship) schemaInfo.getElement(elementID);
			String rDescription = r.getDescription().replaceFirst("\\(", "");
			rDescription = rDescription.split(" :")[0];  System.out.println(rDescription);
			return rDescription; 
		}
		return type==null ? null : type.getName();
	}
}