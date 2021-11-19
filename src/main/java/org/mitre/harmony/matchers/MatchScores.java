package org.mitre.harmony.matchers;

import java.util.ArrayList;
import java.util.HashMap;

import org.mitre.schemastore.model.SchemaElement;

/** Class for storing match scores */
public class MatchScores
{	
	/** Hash map storing the match scores */
	private HashMap<ElementPair,Double> scores = new HashMap<ElementPair,Double>();
	
	/** Sets a match score */
	public void setScore(Integer element1ID, Integer element2ID, Double score)
		{ if(score!=null) scores.put(new ElementPair(element1ID,element2ID),score); }

	/** Sets a match score */
	public void setScore(SchemaElement element1, SchemaElement element2, Double score)
		{ setScore(element1.getId(), element2.getId(), score); }
	
	/** Gets a match score */
	public Double getScore(Integer element1ID, Integer element2ID)
		{ return scores.get(new ElementPair(element1ID,element2ID)); }
	
	/** Gets a match score */
	public Double getScore(SchemaElement element1, SchemaElement element2)
		{ return getScore(element1.getId(),element2.getId()); }
	
	/** Get match scores */
	public ArrayList<MatchScore> getScores()
	{
		ArrayList<MatchScore> matchScores = new ArrayList<MatchScore>();
		for(ElementPair pair : scores.keySet())
			matchScores.add(new MatchScore(pair.getSourceElement(),pair.getTargetElement(),scores.get(pair)));
		return matchScores;
	}
}
