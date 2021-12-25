// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

import java.io.Serializable;

/**
 * Class for storing an annotation
 * @author CWOLF
 */
public class Annotation implements Serializable
{
	/** Stores the annotated element */
	private Integer elementID;

	/** Stores the annotated element group */
	private Integer groupID;
	
	/** Stores the annotated attribute name */
	private String attribute;
	
	/** Stores the element value */
	private String value;
	
	/** Constructs the default annotation */ public Annotation() {}	
	
	/** Constructs the annotation */
	public Annotation(Integer elementID, Integer groupID, String attribute, String value)
		{ this.elementID = elementID; this.groupID = (groupID==null || groupID==0) ? null : groupID; this.attribute = attribute; this.value = value; }

	/** Copies the annotation */
	public Annotation copy()
		{ return new Annotation(this.elementID, this.groupID, this.attribute, this.value); }
	
	// Handles all of the annotation getters
	public Integer getElementID() { return elementID; }
	public Integer getGroupID() { return groupID; }
	public String getAttribute() { return attribute; }
	public String getValue() { return value; }
	
	// Handles all of the annotation setters
	public void setElementID(Integer elementID) { this.elementID = elementID; }
	public void setGroupID(Integer groupID) { this.groupID = (groupID==null || groupID==0) ? null : groupID;  }
	public void setAttribute(String attribute) { this.attribute = attribute; }
	public void setValue(String value) { this.value = value; }
}