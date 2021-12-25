package org.mitre.harmony.matchers.matchers.documentationMatcher;

import java.util.ArrayList;

import org.mitre.schemastore.model.SchemaElement;

import com.google.api.translate.Language;
import com.google.api.translate.Translate;

/** Class used to translate the list of schema elements */
public class ElementTranslator
{	
	/** Translate the list of schema elements */
	static ArrayList<SchemaElement> translate(ArrayList<SchemaElement> elements) throws Exception
	{
		// Stores the translated elements
		ArrayList<SchemaElement> translatedElements = new ArrayList<SchemaElement>();
		
		// Generate the list of words
		StringBuffer originalWords = new StringBuffer();
		for(SchemaElement element : elements)
		{
			originalWords.append(element.getName() + "\n|#|");
			originalWords.append(element.getDescription() + "\n|#|");
		}
		
		// Run the translator
		System.setProperty("http.proxyHost", "gatekeeper.org.mitre.org");
		System.setProperty("http.nonProxyHosts", "*.org.mitre.org|localhost");
		System.setProperty("http.proxyPort", "80");
		Translate.setHttpReferrer("www.openintegration.org");
		String translation = Translate.execute(originalWords.toString(), Language.AUTO_DETECT, Language.ENGLISH);
		
		// Store the results
		String translatedWords[] = translation.toString().split("\\s+\\|\\s?#\\s?\\|");
		for(int i=0; i<elements.size(); i++)
		{
			SchemaElement element = elements.get(i).copy();
			element.setName(translatedWords[i*2].trim());
			element.setDescription(translatedWords[i*2+1].trim());
			translatedElements.add(element);
		}
	
		// Returns the translated elements
		return translatedElements;
	}
}