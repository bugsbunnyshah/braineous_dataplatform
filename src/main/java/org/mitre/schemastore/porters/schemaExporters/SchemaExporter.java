// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.porters.schemaExporters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.porters.Exporter;

/** Abstract Schema Exporter class */
public abstract class SchemaExporter extends Exporter
{
	/** Exports the specified schema to the specified file */
	abstract public void exportSchema(Schema schema, ArrayList<SchemaElement> schemaElements, File file) throws IOException;
}
