// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

import java.io.Serializable;

/**
 * Class for storing a function implementation
 * @author CWOLF
 */
public class FunctionImp implements Serializable
{
	/** Stores the function id */
	private Integer functionID;
	
	/** Stores the implementation language */
	private String language;
	
	/** Stores the implementation dialect */
	private String dialect;
	
	/** Stores the function implementation */
	private String implementation;
	
	/** Constructs a default function implementation */
	public FunctionImp() {}
	
	/** Constructs a function implementation */
	public FunctionImp(Integer functionID, String language, String dialect, String implementation)
		{ this.functionID=functionID; this.language=language; this.dialect=dialect; this.implementation=implementation; }
	
	// Handles all function implementation getters
	public Integer getFunctionID() { return functionID; }
	public String getLanguage() { return language; }
	public String getDialect() { return dialect; }
	public String getImplementation() { return implementation; }
	
	// Handles all function implementation setters
	public void setFunctionID(Integer functionID) { this.functionID = functionID; }
	public void setLanguage(String language) { this.language = language; }
	public void setDialect(String dialect) { this.dialect = dialect; }
	public void setImplementation(String implementation) { this.implementation = implementation; }
	
	/** String representation of the function implementation */
	public String toString()
		{ return implementation; }
}