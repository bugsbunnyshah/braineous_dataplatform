// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

import java.io.Serializable;

/**
 * Class for storing a function
 * @author CWOLF
 */
public class Function implements Serializable
{
	/** Stores the function id */
	private Integer id;
	
	/** Stores the function name */
	private String name;
	
	/** Stores the function description */
	private String description;
	
	/** Stores the function expression */
	private String expression;
	
	/** Stores the function category */
	private String category;
	
	/** Stores the input types */
	private Integer inputTypes[];
	
	/** Stores the output type */
	private Integer outputType;
	
	/** Constructs a default function */
	public Function() {}
	
	/** Constructs a function */
	public Function(Integer id, String name, String description, String expression, String category, Integer inputTypes[], Integer outputType)
		{ this.id=id; this.name=name; this.description=description; this.expression=expression; this.category=category; this.inputTypes=inputTypes; this.outputType=outputType; }
	
	// Handles all function getters
	public Integer getId() { return id; }
	public String getName() { return name; }
	public String getDescription() { return description; }
	public String getExpression() { return expression; }
	public String getCategory() { return category; }
	public Integer[] getInputTypes() { return inputTypes; }
	public Integer getOutputType() { return outputType; }
	
	// Handles all function setters
	public void setId(Integer id) { this.id = id; }
	public void setName(String name) { this.name = name; }
	public void setDescription(String description) { this.description = description; }
	public void setExpression(String expression) { this.expression = expression; }
	public void setCategory(String category) { this.category = category; }
	public void setInputTypes(Integer[] inputTypes) { this.inputTypes = inputTypes; }
	public void setOutputType(Integer outputType) { this.outputType = outputType; }

	/** Returns the hash code */
	public int hashCode()
		{ return id.hashCode(); }
	
	/** Indicates that two functions are equals */
	public boolean equals(Object object)
	{
		if(object instanceof Integer) return ((Integer)object).equals(id);
		if(object instanceof Function) return ((Function)object).id.equals(id);
		return false;
	}
	
	/** String representation of the function */
	public String toString()
		{ return name; }
}