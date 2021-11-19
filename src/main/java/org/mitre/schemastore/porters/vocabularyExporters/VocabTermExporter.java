/**
 * 
 */
package org.mitre.schemastore.porters.vocabularyExporters;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.mitre.schemastore.model.terms.Term;
import org.mitre.schemastore.model.terms.VocabularyTerms;

/**
 * @author HAOLI
 * 
 */
public class VocabTermExporter extends VocabularyExporter {

	@Override
	public void exportVocabulary( VocabularyTerms vocabulary, File file)
			throws IOException {
		PrintStream os = new PrintStream(file);
		for ( Term term : vocabulary.getTerms() ) 
			os.println( term.getName() + ", " + term.getDescription() ); 
		os.flush();
		os.close();
	}

	@Override
	public String getFileType() {
		return ".csv";
	}

	@Override
	public String getDescription() {
		return "Get a listing of vocabulary terms and respective descriptions.";
	}

	@Override
	public String getName() {
		return "Vocab List Exporter";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
