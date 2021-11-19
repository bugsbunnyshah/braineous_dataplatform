package org.mitre.harmony.matchers;

/** Class for storing a match score */
public class MatchScore
{
	/** Stores the source ID */
	private Integer sourceID;
	
	/** Stores the target ID */
	private Integer targetID;
	
	/** Stores the score */
	private Double score;
	
	/** Constructs the match score */
	public MatchScore(Integer sourceID, Integer targetID, Double score)
		{ this.sourceID = sourceID; this.targetID = targetID; this.score = score;	}
	
	/** Returns the source ID */
	public Integer getSourceID()
		{ return sourceID; }
	
	/** Returns the target ID */
	public Integer getTargetID()
		{ return targetID; }
	
	/** Returns the match score */
	public Double getScore()
		{ return score; }
}
