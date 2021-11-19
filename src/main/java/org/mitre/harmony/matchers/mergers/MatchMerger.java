// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.matchers.mergers;

import org.mitre.harmony.matchers.ElementPair;
import org.mitre.harmony.matchers.MatchScores;
import org.mitre.harmony.matchers.MatchTypeMappings;
import org.mitre.harmony.matchers.MatcherScores;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.FilteredSchemaInfo;

/** Matcher Interface - A match merger merged together the results of multiple matchers */
public abstract class MatchMerger
{
	// Stores the match merger schema information
	protected FilteredSchemaInfo schema1, schema2;

	/** Stores the match merger type mapping information */
	private MatchTypeMappings typeMappings;
	
	/** Return the name of the match merger */
	abstract public String getName();

	/** Initializes the match merger */
	public void initialize(FilteredSchemaInfo schema1, FilteredSchemaInfo schema2)
		{ this.schema1 = schema1; this.schema2 = schema2; this.typeMappings = null; }
	
	/** Initializes the match merger */
	public void initialize(FilteredSchemaInfo schema1, FilteredSchemaInfo schema2, MatchTypeMappings typeMappings)
		{ this.schema1 = schema1; this.schema2 = schema2; this.typeMappings = typeMappings; initialize(); }
	
	/** Initializes the match merger */
	abstract protected void initialize();
	
	/** Feeds matcher scores into the merger */
	public void addMatcherScores(MatcherScores scores)
	{
		// Add all scores to merger if no type mapping is set
		if(typeMappings==null) { addMatcherScoresToMerger(scores); return; }
		
		// Otherwise, filter out all scores which are not part of the type mapping
		MatcherScores filteredScores = new MatcherScores(scores.getScoreCeiling());
		for(ElementPair elementPair : scores.getElementPairs())
		{
			// Get the elements being mapped together
			SchemaElement sourceElement = schema1.getElement(elementPair.getSourceElement());
			SchemaElement targetElement = schema2.getElement(elementPair.getTargetElement());

			if(!typeMappings.isMapped(sourceElement,targetElement)) continue;
			filteredScores.setScore(sourceElement.getId(), targetElement.getId(), scores.getScore(elementPair));
		}
		addMatcherScoresToMerger(filteredScores);
	}

	/** Feeds matcher scores into the merger */
	abstract protected void addMatcherScoresToMerger(MatcherScores scores);
	
	/** Retrieve match scores from the merger */
	abstract public MatchScores getMatchScores();	
}