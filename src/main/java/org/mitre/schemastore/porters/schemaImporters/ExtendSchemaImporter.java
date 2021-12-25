// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.porters.schemaImporters;

import java.util.ArrayList;

import org.mitre.schemastore.porters.URIType;

/** Importer for extending schemas within the repository */
public class ExtendSchemaImporter extends SchemaImporter
{
	/** Returns the importer name */
	public String getName()
		{ return "Extend Schema"; }
	
	/** Returns the importer description */
	public String getDescription()
		{ return "This method allows the extending of a schema in the repository"; }
	
	/** Returns the importer URI type */
	public URIType getURIType()
		{ return URIType.SCHEMA; }
	
	/** Returns the list of schemas which this schema extends */
	protected ArrayList<Integer> generateExtendedSchemaIDs()
	{
		ArrayList<Integer> extendedSchemaIDs = new ArrayList<Integer>();
		extendedSchemaIDs.add(Integer.parseInt(uri.toString()));
		return extendedSchemaIDs;
	}
}