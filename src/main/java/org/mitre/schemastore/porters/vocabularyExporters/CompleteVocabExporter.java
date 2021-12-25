package org.mitre.schemastore.porters.vocabularyExporters;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;

import org.mitre.schemastore.model.terms.AssociatedElement;
import org.mitre.schemastore.model.terms.Term;
import org.mitre.schemastore.model.terms.VocabularyTerms;

public class CompleteVocabExporter extends VocabularyExporter
{
	/** Returns the name of the exporter */ @Override
	public String getName()
		{ return "Complete Vocabulary Exporter"; }
	
	/** Returns the description of the exporter */ @Override
	public String getDescription()
		{ return "Export the full vocabulary terms and their corresponding terms"; }

	/** Returns the file type for this exporter */ @Override
	public String getFileType()
		{ return ".csv"; }

	/** Exports the vocabulary */ @Override
	public void exportVocabulary(VocabularyTerms terms, File file) throws IOException
	{
		// Display the column names
		PrintStream os = new PrintStream(file);
		os.print("Vocabulary|");
		for (Integer sid : terms.getSchemaIDs())
			os.print(client.getSchema(sid).getName() + "|");
		os.print("\n");

		// Sort terms based on number of matches
		Term[] sortedTerms = terms.getTerms();
		class TermComparator implements Comparator<Term>
		{
			public int compare(Term term1, Term term2)
			{
				if(term1.getElements().length > term2.getElements().length) return -1; 
				else if(term1.getElements().length < term2.getElements().length) return 1; 
				return term1.getName().compareTo(term2.getName()); 
			}			
		}
		Arrays.sort(sortedTerms, new TermComparator());
		
		// Output the terms
		for(Term sortedTerm : sortedTerms)
		{
			os.print(sortedTerm.getName() + "|");
			for(Integer schemaID : terms.getSchemaIDs())
			{
				String value = "";
				for(AssociatedElement element : sortedTerm.getAssociatedElements(schemaID))
					value += element.getName() + ",";
				os.print((value.length()==0 ? " " : value.substring(0,value.length()-1)) + "|");
			}
			os.print("\n");
		}
		os.flush();
		os.close();
	}
}