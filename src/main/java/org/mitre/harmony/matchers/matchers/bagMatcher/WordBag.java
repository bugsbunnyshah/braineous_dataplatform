/**
 * <p>
 * The WordBag class.
 * The WordBag is a bag of words created from a single node.  (Being a bag, 
 * it includes duplicates.)
 * </p>
 */

package org.mitre.harmony.matchers.matchers.bagMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.terms.AssociatedElement;
import org.mitre.schemastore.utils.Stemmer;
import org.mitre.schemastore.utils.Tokenizer;

/** Class for storing a word bag */
public class WordBag
{
	/** Stores a list of all stop words */
	private static HashSet<String> stopwords = null;
	
	/** Initializes the list of stop words */
	static
	{
		String[] stopwordArray =
			{"a", "i", "the", "of", "and", "that", "for", "by",
			 "as", "be", "or", "this", "then", "we",
			 "which", "with", "at", "from", "under",
			 "such", "there", "other", "if", "is",
			 "it", "can", "now", "an", "to", "but",
			 "upon", "where", "these", "when", "whether", "also",
			 "than", "after", "within", "before", "because",
			 "without", "however", "therefore", "between",
			 "those", "since", "into", "out", "some", "about",
			 "accordingly", "affecting", "affected", "again", "against",
			 "all", "almost", "already", "although", "always",
			 "among", "any", "anyone", "are", "away", "became",
			 "become", "becomes", "been", "being", "both", "briefly",
			 "came", "cannot", "could", "etc", "does", "done",
			 "during", "each", "either", "else", "ever", "every",
			 "following", "found", "further", "gave", "gets", "give",
			 "given", "giving", "gone", "got", "had", "hardly", "has",
			 "have", "having", "here", "how", "itself", "just", "keep",
			 "kept", "like", "made", "mainly", "make", "many", "might",
			 "more", "most", "mostly", "much", "must", "nearly", "necessarily",
			 "neither", "next", "none", "nor", "normally", "not",
			 "noted", "obtain", "obtained", "often", "only", "our",
			 "put", "owing", "particularly", "past", "perhaps", "please",
			 "possible", "possibly", "present", "probably", "prompt",
			 "promptly", "quickly", "quite", "rather", "readily",
			 "really", "their", "theirs", "them", "they",
			 "though", "through", "throughout", "said", "same",
			 "seem", "seen", "shall", "should", "so",
			 "sometime", "somewhat", "too", "toward", "unless",
			 "until", "use", "used", "usefully", "usefulness",
			 "using", "usually", "various", "very", "was",
			 "were", "what", "while", "who", "whose", "why",
			 "widely", "will", "would", "yet", "xsd","comment","string","",
			 "0","1","2","3","4","5","6","7","8","9","10","11", "12", "13", "14", "15", "100", 
			 "16", "17", "18", "19", "20","type"
			};
		stopwords = new HashSet<String>(Arrays.asList(stopwordArray));
	}

	/** Stores a list of all processed words */
	private Hashtable<String, Integer> wordMap = new Hashtable<String, Integer>();	
		
	/** Adds schema elements to the word bag */
	void addElement(SchemaElement element, boolean useName, boolean useDescription)
	{
		String text = "";
		if(useName) text += (element.getName() == null) ? "" : element.getName() + " ";
		if(useDescription) text += (element.getDescription() == null) ? "" : element.getDescription();
		addWords(Tokenizer.tokenize(text.trim()));
	}

	/** Adds associated elements to the word bag */
	void addAssociatedElement(AssociatedElement element, boolean useName, boolean useDescription)
	{
		String text = "";
		if(useName) text += (element.getName() == null) ? "" : element.getName() + " ";
		if(useDescription) text += (element.getDescription() == null) ? "" : element.getDescription();
		addWords(Tokenizer.tokenize(text.trim()));
	}
	
	/** Add words to the word bag */
	public void addWords(List<String> words)
	{
		// Don't proceed if no words were provided
		if(words == null) { return; }
		
		// Cycle through all words
		for(String word : words)
		{
			// Make word lower case
			word = word.toLowerCase();

			// Don't proceed if stop word
			if(stopwords.contains(word)) continue;

			// Stem the word
			word = Stemmer.stem(word);

			// Add word to word map
			Integer count = wordMap.get(word);
			if(count == null) count = 0;
			wordMap.put(word, count+1);
		}
	}	

	/** Removes a word from the word bag */
	public void removeWord(String word)
		{ wordMap.remove(word); }

	/** Returns the list of unique words found in the word bag */
	public ArrayList<String> getDistinctWords()
		{ return new ArrayList<String>(wordMap.keySet()); }

	/** Returns the list of words found in the word bag */
	public ArrayList<String> getWords()
	{
		ArrayList<String> words = new ArrayList<String>();
		for(String word : wordMap.keySet())
		{
			Integer count = wordMap.get(word);
			for(int i = 0; i < count; i++)
				words.add(word);
		}
		return words;
	}
	
	/** Returns a weight indicating the uniqueness of the specified word */
	public Integer getWordCount(String word)
	{
		Integer wordCount = wordMap.get(word);
		return (wordCount == null) ? 0 : wordCount;
	}
}