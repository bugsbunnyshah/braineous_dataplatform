// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.schemaInfo.model;

import java.util.ArrayList;

import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

/**
 * Interface for representing the schema model
 */
abstract public class SchemaModel
{
	String name = null;
	
	public void setName(String name) {
		this.name = name;
	}
	/** Returns the name of the schema model */
	public String getName(){
		return name;
	}
	
	/** Returns the root elements in this schema */
	abstract public ArrayList<SchemaElement> getRootElements(HierarchicalSchemaInfo schemaInfo);
	
	/** Returns the parent elements of the specified element in this schema */
	abstract public ArrayList<SchemaElement> getParentElements(HierarchicalSchemaInfo schemaInfo, Integer elementID);
	
	/** Returns the children elements of the specified element in this schema */
	abstract public ArrayList<SchemaElement> getChildElements(HierarchicalSchemaInfo schemaInfo, Integer elementID);
	
	/** Returns the domain of the specified element in this schema */
	abstract public Domain getDomainForElement(HierarchicalSchemaInfo schemaInfo, Integer elementID);
	
	/** Returns the elements referenced by the specified domain */
	abstract public ArrayList<SchemaElement> getElementsForDomain(HierarchicalSchemaInfo schemaInfo, Integer domainID);

	/** Returns the type name associated with the specified element (or NULL if element has no name) */
	abstract public SchemaElement getType(HierarchicalSchemaInfo schemaInfo, Integer elementID);
	
	/** Returns the type display (allows overriding of the type) */
	public String getTypeString(HierarchicalSchemaInfo schemaInfo, Integer elementID)
	{
		SchemaElement type = getType(schemaInfo,elementID);
		return type==null ? null : schemaInfo.getDisplayName(elementID);
	}
	
	/** Returns the name of the schema model */
	public String toString() { return getName(); }
}