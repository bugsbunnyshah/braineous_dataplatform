package org.mitre.schemastore.model.schemaInfo.model;

import java.util.ArrayList;

import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

public class OWLSchemaModel extends RelationalSchemaModel {

	/** Returns the schema model name */
	public String getName()
	{ 
		if (name == null) {
			name = "OWL";
		}
		return name;
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
		else if (element instanceof Relationship) {
			childElement = schemaInfo.getElement(((Relationship)element).getRightID());
		}
		
		if (childElement != null && 
				((childElement.getName() != null && childElement.getName().length() > 0) || childElement instanceof Domain))
			return childElement;

		return null;	
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
		if (element instanceof Attribute)
		{
			Attribute attr = (Attribute) element;
			childElements.addAll(schemaInfo.getDomainValuesForDomain(attr.getDomainID()));
		}
		if (element instanceof Containment) {
			Containment con = (Containment)element;
			SchemaElement e = schemaInfo.getElement(con.getChildID());
			if (e instanceof Domain){
				childElements.addAll(schemaInfo.getDomainValuesForDomain(e.getId()));
			}
		}
		return childElements;
	}
	
}
