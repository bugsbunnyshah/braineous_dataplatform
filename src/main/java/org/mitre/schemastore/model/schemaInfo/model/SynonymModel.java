// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.schemaInfo.model;

import java.util.ArrayList;

import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Synonym;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

/**
 * Class for displaying synonyms
 */
public class SynonymModel extends SchemaModel
{
	/** Returns the schema model name */
	public String getName()
	{ 
		if (name == null) {
			name = "Synonym";
		}
		return name;
	}

	/** Returns the list of elements which contain synonyms */
	public ArrayList<SchemaElement> getRootElements(HierarchicalSchemaInfo schemaInfo)
		{ return schemaInfo.getElementsContainingSynonyms(); }

	/** Returns the parent elements of the specified element in this schema */
	public ArrayList<SchemaElement> getParentElements(HierarchicalSchemaInfo schemaInfo, Integer elementID)
	{
		ArrayList<SchemaElement> parentElements = new ArrayList<SchemaElement>();
		SchemaElement element = schemaInfo.getElement(elementID);
		if((element instanceof Synonym))
			parentElements.add(schemaInfo.getElement(((Synonym) element).getElementID()));
		return parentElements;
	}

	/** Returns the children elements of the specified element in this schema */
	public ArrayList<SchemaElement> getChildElements(HierarchicalSchemaInfo schemaInfo, Integer elementID)
	{
		ArrayList<SchemaElement> childElements = new ArrayList<SchemaElement>();
		childElements.addAll(schemaInfo.getSynonyms(elementID));
		return childElements;
	}

	/** Returns the domains of the specified element in this schema */
	public Domain getDomainForElement(HierarchicalSchemaInfo schemaInfo, Integer elementID)
		{ return null; }

	/** Returns the elements referenced by the specified domain */
	public ArrayList<SchemaElement> getElementsForDomain(HierarchicalSchemaInfo schemaInfo, Integer domainID)
		{ return new ArrayList<SchemaElement>(); }

	/** Returns the type name associated with the specified element (or NULL if element has no name) */
	public SchemaElement getType(HierarchicalSchemaInfo schemaInfo, Integer elementID)
		{ return null; }
}