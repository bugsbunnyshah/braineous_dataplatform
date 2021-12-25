// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.schemaInfo.model;

import java.util.ArrayList;

import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

/**
 * Class for displaying inheritance and containment in one hierarchy.
 * 
 * @author PMORK
 */
public class MixedSchemaModel extends SchemaModel {

	/** Returns the schema model name */
	public String getName()
	{ 
		if (name == null) {
			name = "Mixed";
		}
		return name;
	}

	/** Returns the root elements in this schema */
	public ArrayList<SchemaElement> getRootElements(HierarchicalSchemaInfo schemaInfo) {
		ArrayList<SchemaElement> rootElements = schemaInfo.getElements(Entity.class);

		// Remove all entities with a super-class.
		for (SchemaElement schemaElement : schemaInfo.getElements(Subtype.class)) {
			Subtype subtype = (Subtype) schemaElement;
			rootElements.remove(schemaInfo.getElement(subtype.getChildID()));
		}

		// Remove all entities within a container.
		for (SchemaElement schemaElement : schemaInfo.getElements(Containment.class)) {
			Containment containment = (Containment) schemaElement;
			// Leave in any entity whose parent doesn't exist.
			if (containment.getParentID() != null) rootElements.remove(schemaInfo.getElement(containment.getChildID()));
		}

		return rootElements;
	}

	/** Returns the parent elements of the specified element in this schema */
	public ArrayList<SchemaElement> getParentElements(HierarchicalSchemaInfo schemaInfo, Integer elementID) {
		// This view should only deal with entities.
		SchemaElement element = schemaInfo.getElement(elementID);
		if (!(element instanceof Entity)) return null;

		ArrayList<SchemaElement> parentElements = new ArrayList<SchemaElement>();

		// Add all of the super-classes to the result.
		for (Subtype subtype : schemaInfo.getSubTypes(element.getId())) {
			Integer parentID = subtype.getParentID();
			if (!elementID.equals(parentID)) {
				parentElements.add(schemaInfo.getElement(parentID));
			}
		}

		// Add all of the parent containers to the result.
		for (Containment containment : schemaInfo.getContainments(element.getId())) {
			Integer parentID = containment.getParentID();
			// TODO: Is the null test necessary?
			if (parentID != null && !elementID.equals(parentID)) {
				parentElements.add(schemaInfo.getElement(parentID));
			}
		}

		return parentElements;
	}

	/** Returns the children elements of the specified element in this schema */
	public ArrayList<SchemaElement> getChildElements(HierarchicalSchemaInfo schemaInfo, Integer elementID) {
		// This view should only deal with entities.
		SchemaElement element = schemaInfo.getElement(elementID);
		ArrayList<SchemaElement> childElements = new ArrayList<SchemaElement>();

		if (!(element instanceof Entity)) if (element instanceof Attribute) childElements.add(element);
		else return null;

		// Retrieve subtypes as children
		for (Subtype subtype : schemaInfo.getSubTypes(element.getId())) {
			Integer childID = subtype.getChildID();
			if (!elementID.equals(childID)) childElements.add(schemaInfo.getElement(childID));
		}

		// Retrieve all entities referenced as children using containments.
		for (Containment containment : schemaInfo.getContainments(element.getId())) {
			Integer childID = containment.getChildID();
			if (!elementID.equals(childID)) childElements.add(schemaInfo.getElement(childID));
		}

		return childElements;
	}

	/** Returns null because this model only deals with entities */
	public Domain getDomainForElement(HierarchicalSchemaInfo schemaInfo, Integer elementID) {
		return null;
	}

	/** Returns null because this model only deals with entities */
	public ArrayList<SchemaElement> getElementsForDomain(HierarchicalSchemaInfo schemaInfo, Integer domainID) {
		return null;
	}

	/** Returns null because this model only deals with entities */
	public SchemaElement getType(HierarchicalSchemaInfo schemaInfo, Integer elementID) {
		return null;
	}

	public enum Relationship {
		NEITHER, INHERITANCE, AGGREGATION, BOTH
	}

	/** Returns the relationship between a particular pair of schema elements. */
	public static Relationship getRelationshipBetween(HierarchicalSchemaInfo schemaInfo, Integer parentID, Integer childID) {
		boolean inh = inheritanceExists(schemaInfo, parentID, childID);
		boolean agg = aggregationExists(schemaInfo, parentID, childID);
		if (inh && agg) return Relationship.BOTH;
		if (inh) return Relationship.INHERITANCE;
		if (agg) return Relationship.AGGREGATION;
		return Relationship.NEITHER;
	}

	/**
	 * Returns true if there exists a subtype relationship between the parent
	 * and child.
	 */
	static private boolean inheritanceExists(HierarchicalSchemaInfo schemaInfo, Integer parentID, Integer childID) {
		for (Subtype subtype : schemaInfo.getSubTypes(parentID)) {
			if (subtype.getParentID().equals(parentID) && subtype.getChildID().equals(childID)) return true;
		}
		return false;
	}

	/**
	 * Returns true if there exists a subtype relationship between the parent
	 * and child.
	 */
	static private boolean aggregationExists(HierarchicalSchemaInfo schemaInfo, Integer parentID, Integer childID) {
		for (Containment containment : schemaInfo.getContainments(parentID)) {
			if (parentID.equals( containment.getParentID() ) && childID.equals(containment.getChildID() ))
				return true;
		}
		return false;
	}
}