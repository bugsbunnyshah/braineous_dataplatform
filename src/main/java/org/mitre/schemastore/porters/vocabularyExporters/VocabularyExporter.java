package org.mitre.schemastore.porters.vocabularyExporters;

import java.io.File;
import java.io.IOException;

import org.mitre.schemastore.model.terms.VocabularyTerms;
import org.mitre.schemastore.porters.Exporter;

/** Abstract Schema Exporter class */
public abstract class VocabularyExporter extends Exporter
{
	/** Exports the specified vocabulary to the specified file */
	abstract public void exportVocabulary(VocabularyTerms vocabulary, File file) throws IOException;
}
