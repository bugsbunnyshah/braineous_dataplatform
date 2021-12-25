// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

import java.io.Serializable;

/**
 * Class for storing a mapping cell input
 * @author CWOLF
 */
public class MappingCellInput implements Serializable
{
	/** Stores an element ID */
	private Integer elementID = null;

	/** Stores a constant */
	private String constant = null;

	/** Constructs the default mapping cell input */ public MappingCellInput() {}	
	
	/** Constructs the mapping cell input for an element ID */
	public MappingCellInput(Integer elementID)
		{ this.elementID = elementID; }

	/** Constructs the mapping cell input for a constant */
	public MappingCellInput(String constant)
		{ this.constant = constant; }

	// Handles all of the mapping cell input getters
	public Integer getElementID() { return elementID; }
	public String getConstant() { return constant; }
	
	// Handles all of the mapping cell setters
	public void setElementID(Integer elementID) { this.elementID = elementID; }
	public void setConstant(String constant) { this.constant = constant; }

	/** Indicates if the input is a constant */
	public boolean isConstant()
		{ return constant!=null; }
	
	/** Display the string representation of the mapping cell input */
	public String toString()
		{ return isConstant() ? "\"" + constant + "\"" : elementID.toString(); }

	/** Retrieves the mapping cell input from the string */
	static public MappingCellInput parse(String value)
	{ 
    	if(value.matches("\".*\"")) return new MappingCellInput(value.substring(1,value.length()-2));
    	else try {return new MappingCellInput(Integer.parseInt(value)); }
    	catch(Exception e) { return null; }
	}
}