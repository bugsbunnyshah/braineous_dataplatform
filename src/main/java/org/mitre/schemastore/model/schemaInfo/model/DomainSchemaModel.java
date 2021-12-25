// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.schemaInfo.model;

import java.util.ArrayList;

import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

/**
 * Class for displaying domain hierarchy
 */
public class DomainSchemaModel extends SchemaModel {

	/** Returns the schema model name */
	public String getName()
	{ 
		if (name == null) {
			name = "Domain";
		}
		return name;
	}

	/** Returns the root domain elements in this schema */
	public ArrayList<SchemaElement> getRootElements(HierarchicalSchemaInfo schemaInfo) {
		ArrayList<SchemaElement> domains = schemaInfo.getElements(Domain.class);
		ArrayList<SchemaElement> rootElements = new ArrayList<SchemaElement>();
		rootElements.addAll(domains);

		for (SchemaElement d : domains)
			for (Subtype subtype : schemaInfo.getSubTypes(d.getId()))
				rootElements.remove(schemaInfo.getElement(subtype.getChildID()));
		return rootElements;
	}

	/** Returns the parent elements of the specified element in this schema */
	public ArrayList<SchemaElement> getParentElements(HierarchicalSchemaInfo schemaInfo, Integer elementID) {
		ArrayList<SchemaElement> parentElements = new ArrayList<SchemaElement>();
		SchemaElement element = schemaInfo.getElement(elementID);

		// If not a domain value or domain, don't return anything
		if (!(element instanceof DomainValue) && !(element instanceof Domain)) return parentElements;

		// Find all parents of element from Subtype relationships
		for (Subtype s : schemaInfo.getSubTypes(elementID))
			if (s.getChildID().equals(elementID)) {
				SchemaElement parent = schemaInfo.getElement(s.getParentID());
				if (!parentElements.contains(parent)) parentElements.add(parent);
			}

		// If element is a domain value without a super type, then return its
		// domain as parent
		if (element instanceof DomainValue && parentElements.size() == 0) parentElements.add((Domain) schemaInfo.getElement(((DomainValue) element).getDomainID()));
		return parentElements;
	}

	/** Returns the children elements of the specified element in this schema */
	public ArrayList<SchemaElement> getChildElements(HierarchicalSchemaInfo schemaInfo, Integer elementID) {
		ArrayList<SchemaElement> childElements = new ArrayList<SchemaElement>();
		SchemaElement element = schemaInfo.getElement(elementID);

		// If not a domain value or domain, don't return anything
		if (!(element instanceof DomainValue) && !(element instanceof Domain)) return childElements;

		// Add children from Subtype relationships
		for (Subtype s : schemaInfo.getSubTypes(elementID))
			if (s.getParentID().equals(elementID)) {
				SchemaElement child = schemaInfo.getElement(s.getChildID());
				if (!childElements.contains(child)) childElements.add(child);
			}

		// If domain, add top level domain values as children
		if (element instanceof Domain /*&& childElements.size() == 0 */) {
			for (DomainValue domainvalue : schemaInfo.getDomainValuesForDomain(elementID)) {
				boolean isTopLevel = true;
				for (Subtype s : schemaInfo.getSubTypes(domainvalue.getId())) {
					if (s.getChildID().equals(domainvalue.getId())) {
						isTopLevel = false;
						break;
					}
				}
				if (isTopLevel) childElements.add(domainvalue);
			}
		}
		return childElements;
	}

	/** Returns the domains of the specified element in this schema */
	public Domain getDomainForElement(HierarchicalSchemaInfo schemaInfo, Integer elementID) {
		return null;
	}

	/** Returns the elements referenced by the specified domain */
	public ArrayList<SchemaElement> getElementsForDomain(HierarchicalSchemaInfo schemaInfo, Integer domainID) {
		return new ArrayList<SchemaElement>();
	}

	/**
	 * Returns the type name associated with the specified element (or NULL if
	 * element has no name)
	 */
	public SchemaElement getType(HierarchicalSchemaInfo schemaInfo, Integer elementID) {
		return null;
	}
}