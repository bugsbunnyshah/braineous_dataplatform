// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.matchers.matchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.mitre.harmony.matchers.ElementPair;
import org.mitre.harmony.matchers.MatcherScore;
import org.mitre.harmony.matchers.MatcherScores;
import org.mitre.harmony.matchers.matchers.bagMatcher.WordBag;
import org.mitre.schemastore.model.SchemaElement;

/** Quick Documentation Matcher Class */
public class QuickMatcher extends EntityMatcher
{
	/** Returns the name of the matcher */
	public String getName()
		{ return "Quick Matcher"; }
	
	/** Generates scores for the specified elements */ @Override
	public MatcherScores generateScores()
	{		
		// Retrieve the source and target entities
		EntityMap sourceEntities = getEntities(schema1);
		EntityMap targetEntities = getEntities(schema2);
	
		// Identify best matches between entities
		MatcherScores entityScores = match(sourceEntities, targetEntities);
		HashSet<ElementPair> bestMatches = getBestMatches(entityScores);

		// Sets the completed and total comparisons
		completedComparisons = 0;
		totalComparisons = bestMatches.size();

		// Generate the word bags
		HashMap<Integer,WordBag> wordBags = new HashMap<Integer,WordBag>();
		ArrayList<ElementPair> pairs = new ArrayList<ElementPair>();
		for(ElementPair bestMatch : bestMatches)
			for(SchemaElement source : sourceEntities.get(schema1.getElement(bestMatch.getSourceElement())))
				if(schema1.isVisible(source.getId()))
					for(SchemaElement target : targetEntities.get(schema2.getElement(bestMatch.getTargetElement())))
						if(schema2.isVisible(target.getId()))
						{
							// Get word bags for source and target
							WordBag sourceBag = wordBags.get(source.getId());
							if(sourceBag == null) { wordBags.put(source.getId(), sourceBag = generateWordBag(source)); }
							WordBag targetBag = wordBags.get(target.getId());
							if(targetBag == null) { wordBags.put(target.getId(), targetBag = generateWordBag(target)); }

							// Stores the element pair
							pairs.add(new ElementPair(source.getId(), target.getId()));
						}

		// Calculate the word weights
		HashSet<SchemaElement> sourceElements = new HashSet<SchemaElement>();
		HashSet<SchemaElement> targetElements = new HashSet<SchemaElement>();
		for (ElementPair pair : pairs)
		{
			sourceElements.add(schema1.getElement(pair.getSourceElement()));
			targetElements.add(schema2.getElement(pair.getTargetElement()));
		}
		HashMap<String,Double> wordWeights = getWordWeights(new ArrayList<SchemaElement>(sourceElements), new ArrayList<SchemaElement>(targetElements), wordBags);
		
		// Generate element scores
		MatcherScores scores = new MatcherScores(SCORE_CEILING);
		for (ElementPair pair : pairs)
		{
			// Get the source and target elements
			SchemaElement sourceElement = schema1.getElement(pair.getSourceElement());
			SchemaElement targetElement = schema2.getElement(pair.getTargetElement());
			if(scores.getScore(sourceElement.getId(), targetElement.getId()) == null)
			{
				WordBag sourceBag = wordBags.get(sourceElement.getId());
				WordBag targetBag = wordBags.get(targetElement.getId());
				MatcherScore score = computeScore(sourceBag, targetBag, wordWeights);
				if(score != null) { scores.setScore(sourceElement.getId(), targetElement.getId(), score); }
			}
			completedComparisons++;
		}		
		
		// Transfer over entity scores
		for (ElementPair elementPair : entityScores.getElementPairs())
			if (schema1.isVisible(elementPair.getSourceElement()) && schema2.isVisible(elementPair.getTargetElement()))
				scores.setScore(elementPair.getSourceElement(), elementPair.getTargetElement(), entityScores.getScore(elementPair));
		
		// Return the generated scores
		return scores;
	}

	/** Returns a list of the best matches */
	private HashSet<ElementPair> getBestMatches(MatcherScores scores)
	{
		// Scan through matcher scores to identify best matches
		HashMap<Integer,Double> bestScores = new HashMap<Integer,Double>();
		HashMap<Integer,ArrayList<ElementPair>> bestPairs = new HashMap<Integer,ArrayList<ElementPair>>();
		for (ElementPair elementPair : scores.getElementPairs())
		{
			// Calculate rough match score (not as accurate as through merger)
			MatcherScore matcherScore = scores.getScore(elementPair);
			Double score = matcherScore.getPositiveEvidence()/matcherScore.getTotalEvidence();
			
			// Determine if the element pair is best match
			for (Integer elementID : new Integer[]{elementPair.getSourceElement(),elementPair.getTargetElement()})
			{
				Double elementScore = bestScores.get(elementID);
				if (elementScore == null || score >= elementScore)
				{
					// Updates the best score
					bestScores.put(elementID, score);
					
					// Updates the best element pairs
					ArrayList<ElementPair> elementPairs = bestPairs.get(elementID);
					if (!score.equals(elementScore))
						bestPairs.put(elementID, elementPairs = new ArrayList<ElementPair>());
					elementPairs.add(elementPair);
				}
			}
		}
		
		// Return the best matches
		HashSet<ElementPair> bestMatches = new HashSet<ElementPair>();
		for (ArrayList<ElementPair> elementPairs : bestPairs.values())
			bestMatches.addAll(elementPairs);
		return bestMatches;
	}
}