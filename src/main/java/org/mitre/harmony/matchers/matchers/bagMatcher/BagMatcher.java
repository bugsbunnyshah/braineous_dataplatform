// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.matchers.matchers.bagMatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mitre.harmony.matchers.MatcherScore;
import org.mitre.harmony.matchers.MatcherScores;
import org.mitre.harmony.matchers.matchers.Matcher;
import org.mitre.harmony.matchers.parameters.MatcherCheckboxParameter;
import org.mitre.harmony.matchers.parameters.MatcherParameter;
import org.mitre.schemastore.model.SchemaElement;

/** Bag Matcher Class */
abstract public class BagMatcher extends Matcher
{
	/** Constant defining the score ceiling */
	public final static double SCORE_CEILING = 10;

	// Stores the matcher parameters
	private MatcherCheckboxParameter name = new MatcherCheckboxParameter(NAME,true);
	private MatcherCheckboxParameter description = new MatcherCheckboxParameter(DESCRIPTION,true);
	
	/** Returns the list of parameters associated with the bag matcher */
	public ArrayList<MatcherParameter> getMatcherParameters()
	{
		ArrayList<MatcherParameter> parameters = new ArrayList<MatcherParameter>();
		parameters.add(name);
		parameters.add(description);
		return parameters;
	}
	
	/** Generates the scores for the specified elements */
	abstract public MatcherScores generateScores();
	
	/** Generates scores for the specified elements */ @Override
	final public MatcherScores match()
	{
		// Don't proceed if neither "name" nor "description" option selected
		if(!name.isSelected() && !description.isSelected())
			return new MatcherScores(100.0);

		// Generate the match scoresx
		return generateScores();
	}
	
	/** Generate a word bag for the specified element */
	public WordBag generateWordBag(SchemaElement element)
	{
		WordBag wordBag = new WordBag();
		addElementToWordBag(wordBag, element);
		return wordBag;
	}
	
	/** Adds an element to the word bag */
	public void addElementToWordBag(WordBag wordBag, SchemaElement element)
	{
		boolean useName = name.isSelected();
		boolean useDescription = description.isSelected();
		wordBag.addElement(element, useName, useDescription);
	}
	
	/** Generates the word weights */
	protected HashMap<String,Double> getWordWeights(List<SchemaElement> sourceElements, List<SchemaElement> targetElements, HashMap<Integer,WordBag> wordBags)
	{
		HashMap<String, Integer> corpus = new HashMap<String, Integer>();
		Integer totalWordCount = 0;
		
		// Cycle through all source and target elements
		ArrayList<SchemaElement> elements = new ArrayList<SchemaElement>();
		elements.addAll(sourceElements); elements.addAll(targetElements);
		for(SchemaElement element : elements)
		{
			// Extract the word bag for the element
			WordBag wordBag = wordBags.get(element.getId());
			totalWordCount += wordBag.getWords().size();
			
			// Store all distinct words in the element to the corpus
			for(String word : wordBag.getDistinctWords())
			{
				// Increment the word count in the corpus
				Integer count = corpus.get(word);
				if(count == null) { count = 0; }
				corpus.put(word, count+1);
			}
		}

		// Calculate the document weight and inflation constant
		double documentWeight = Math.log(elements.size());
		double inflationConstant = 10.0 * elements.size() / totalWordCount;
		
		// Calculate out the word weights
		HashMap<String, Double> wordWeights = new HashMap<String, Double>();
		for(String word : corpus.keySet())
		{
			double wordWeight = Math.log(corpus.get(word));
			wordWeights.put(word, inflationConstant*(documentWeight-wordWeight)/documentWeight);
		}
		return wordWeights;
	}

	/** Compute the matcher score */
	protected static MatcherScore computeScore(WordBag sourceBag, WordBag targetBag, HashMap<String,Double> wordWeights)
	{
		// Calculate the source score
		Double sourceScore = 0.0;
		for(String word : sourceBag.getDistinctWords())
			sourceScore += sourceBag.getWordCount(word) * wordWeights.get(word);

		// Calculate the target score
		Double targetScore = 0.0;
		for(String word : targetBag.getDistinctWords())
			targetScore += targetBag.getWordCount(word) * wordWeights.get(word);
		
		// Calculate the intersection score
		Double intersectionScore = 0.0;
		for(String word : sourceBag.getDistinctWords())
		{
			Integer count = Math.min(sourceBag.getWordCount(word), targetBag.getWordCount(word));
			intersectionScore += count * wordWeights.get(word);
		}

		// Return the matcher score
		if(intersectionScore == 0) { return null; }
		return new MatcherScore(intersectionScore, Math.min(sourceScore, targetScore));
	}

	/** Compute the matcher scores */
	protected MatcherScores computeScores(List<SchemaElement> sourceElements, List<SchemaElement> targetElements, HashMap<Integer,WordBag> wordBags)
	{
		// Sets the completed and total comparisons
		completedComparisons = 0;
		totalComparisons = sourceElements.size() * targetElements.size();
		
		// Get the word weights for all words existent in the source and target schemas
		HashMap<String,Double> wordWeights = getWordWeights(sourceElements, targetElements, wordBags);
		
		// Generate the scores
		MatcherScores scores = new MatcherScores(SCORE_CEILING);
		for(SchemaElement sourceElement : sourceElements)
			for(SchemaElement targetElement : targetElements)
			{
				if(isAllowableMatch(sourceElement, targetElement))
					if(scores.getScore(sourceElement.getId(), targetElement.getId()) == null)
					{
						WordBag sourceBag = wordBags.get(sourceElement.getId());
						WordBag targetBag = wordBags.get(targetElement.getId());
						MatcherScore score = computeScore(sourceBag, targetBag, wordWeights);
						if(score != null) { scores.setScore(sourceElement.getId(), targetElement.getId(), score); }
					}
				completedComparisons++;
			}
		return scores;
	}
}