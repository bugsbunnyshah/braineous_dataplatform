// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.search;

import org.mitre.schemastore.model.Alias;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;

/** Class for storing a search result match */
public class Match
{
	// Declares the various match types
	static public final Integer SCHEMA = 0;
	static public final Integer ELEMENT = 1;
	static public final Integer ALIAS = 2;
	
	/** Stores the matched element */
	private Object element;
		
	/** Constructs the match */
	public Match(Object element)
		{ this.element = element; }

	/** Returns the match element ID */
	public Integer getElementID()
		{ return element instanceof Schema ? ((Schema)element).getId() : ((SchemaElement)element).getId(); }
	
	/** Returns the match element */
	public Object getElement()
		{ return element; }
	
	/** Returns the match type */
	public Integer getType()
		{ return element instanceof Schema ? SCHEMA : element instanceof Alias ? ALIAS : ELEMENT; }
	
	/** Returns the base schema of this match */
	public Integer getSchema()
	{
		if(element instanceof Schema)
			return ((Schema)element).getId();
		return ((SchemaElement)element).getBase();
	}
}