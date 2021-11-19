// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.search;

import java.util.ArrayList;

import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

/** Class for storing a schema's search results */
public class RepositorySearchResult
{
	/** Stores the search result schema ID */
	private Integer schemaID;
	
	/** Stores the primary matches */
	private PrimaryMatches primaryMatches;
	
	/** Constructs the search result */
	RepositorySearchResult(HierarchicalSchemaInfo schema, ArrayList<Match> matches)
	{
		this.schemaID = schema.getSchema().getId();
		primaryMatches = new PrimaryMatches(schema, matches);
	}
	
	/** Returns the search result schema ID */
	public Integer getSchemaID()
		{ return schemaID; }
	
	/** Returns the primary match object */
	public PrimaryMatches getPrimaryMatches()
		{ return primaryMatches; }
}
