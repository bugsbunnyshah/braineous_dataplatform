// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.schemastore.search;

/**
 * Holds a search result
 */
public class SchemaSearchResult
{
	/** Stores if the element name matched */
	private boolean nameMatched;
	
	/** Stores if the element description matched */
	private boolean descriptionMatched;

	/** Constructor used to initialize the search result */
	public SchemaSearchResult(boolean nameMatched, boolean descriptionMatched)
		{ this.nameMatched = nameMatched; this.descriptionMatched = descriptionMatched; }

	/** Returns if the name matched */
	public boolean nameMatched()
		{ return nameMatched; }
	
	/** Returns if the description matched */
	public boolean descriptionMatched()
		{ return descriptionMatched; }
}
