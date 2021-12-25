// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.matchers.matchers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import org.mitre.harmony.matchers.MatcherScore;
import org.mitre.harmony.matchers.MatcherScores;
import org.mitre.harmony.matchers.matchers.bagMatcher.BagMatcher;
import org.mitre.harmony.matchers.matchers.bagMatcher.WordBag;
import org.mitre.schemastore.model.SchemaElement;

/**
 * WordNet Matcher 
 * Author: MDMORSE
 * Date:   Jan 22, 2009
 * A thesaurus matcher that uses the wordnet dictionary
 */
public class WordNetMatcher extends BagMatcher
{
	/** Returns the name of the matcher */
	public String getName()
		{ return "WordNet Matcher"; }
	
	/** Generates scores for the specified elements */ @Override
	public MatcherScores generateScores()
	{
		// Create word bags for the source and target elements
		ArrayList<SchemaElement> sourceElements = schema1.getFilteredElements();
		ArrayList<SchemaElement> targetElements = schema2.getFilteredElements();

		// Sets the completed and total comparisons
		completedComparisons = 0;
		totalComparisons = sourceElements.size() * targetElements.size();

		// Get the thesaurus and acronym dictionaries
		URL thesaurusFile = getClass().getResource("wordnet.txt");
		HashMap<String, HashSet<Integer>> thesaurus = getThesaurus(thesaurusFile);
		
		// Generate the match scores
		MatcherScores scores = new MatcherScores(SCORE_CEILING);
		for(SchemaElement sourceElement : sourceElements)
			for(SchemaElement targetElement : targetElements)
			{
				if(isAllowableMatch(sourceElement, targetElement))
					if(scores.getScore(sourceElement.getId(), targetElement.getId()) == null)
					{
						MatcherScore score = findMatcherScore(sourceElement,targetElement,thesaurus);
						if(score != null)
							scores.setScore(sourceElement.getId(), targetElement.getId(), score);
					}
				completedComparisons++;
			}
		return scores;
	}
	
	/** Get the specified dictionary from file */
	private static HashMap<String, HashSet<Integer>> getThesaurus(URL dictionaryFile)
	{
		HashMap<String,HashSet<Integer>> thesaurus = new HashMap<String,HashSet<Integer>>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(dictionaryFile.openStream()));
			String line;
			while((line = br.readLine()) != null)
			{
				StringTokenizer scan = new StringTokenizer(line);

				// Collect the word and senset
				String word = scan.nextToken();
				HashSet<Integer> synList = new HashSet<Integer>();
				while (scan.hasMoreTokens())
					synList.add(new Integer(scan.nextToken()));
				
				// Add entry to the dictionary
				thesaurus.put(word,synList);
	        } 
		}
		catch (java.io.IOException e) { System.out.println("(E) WordNetMatcher:getThesaurus - " + e.getMessage()); }
		return thesaurus;
	}
	
	/** Evaluate the score between the source and target element using the wordnet thesaurus */
	private MatcherScore findMatcherScore(SchemaElement source, SchemaElement target, HashMap<String,HashSet<Integer>> thesaurus)
	{
		int matches = 0;

		// Compile sets of words contained in both source and target elements
		WordBag sourceWordBag = generateWordBag(source);
		WordBag targetWordBag = generateWordBag(target);

		// Get text in both.
		ArrayList<String> sourceWords = sourceWordBag.getWords();
		ArrayList<String> targetWords = targetWordBag.getWords();

		// Swap word sets to make sure smaller word set is used in outer loop
		if(sourceWords.size()>targetWords.size())
		{
			ArrayList<String> tempWords = sourceWords;
			sourceWords = targetWords;
			targetWords = tempWords;
		}
		
		// Cycle through each source word
		for(String sourceWord : sourceWords)
		{
			// Retrieve the senset for the  source word
			HashSet<Integer> sourceSenset = thesaurus.get(sourceWord);
			if(sourceSenset == null) continue;
			
			// Cycle through each target word
			for(String targetWord : targetWords)
			{
				// Retrieve the senset for the target word
				HashSet<Integer> targetSenset = thesaurus.get(targetWord);
				if(targetSenset == null) continue;

				// Check for match
				boolean match = false;
				for(Integer sourceSen : sourceSenset)
					if(targetSenset.contains(sourceSen))
						{ match = true; break; }

				// If match found, mark as such and continue
				if(match) matches++;
			}
		}
		
		// Return the matcher score
		if (matches > 0)
			return new MatcherScore(1.0*matches, 1.0*sourceWords.size());
		return null;
	}
}
