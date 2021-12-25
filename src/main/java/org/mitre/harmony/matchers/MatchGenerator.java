// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.matchers;

import java.util.ArrayList;
import java.util.EventListener;

import org.mitre.harmony.matchers.matchers.Matcher;
import org.mitre.harmony.matchers.mergers.MatchMerger;
import org.mitre.schemastore.model.schemaInfo.FilteredSchemaInfo;

/**
 * Generates the match scores
 * @author CWOLF
 */
public class MatchGenerator
{	
	/** Match Generator Listener */
	public interface MatchGeneratorListener extends EventListener
	{
		/** Indicates the matcher being run */
		public void matcherRun(Matcher matcher);
	}
	
	/** Stores the matchers */
	private ArrayList<Matcher> matchers;
	
	/** Stores the merger */
	private MatchMerger merger;
	
	/** Stores the mapping for the type of schema elements to be matched */
	private MatchTypeMappings typeMappings;
	
	/** Stores the list of match generation listeners */
	private ArrayList<MatchGeneratorListener> listeners = new ArrayList<MatchGeneratorListener>();
	
	/** Constructs the match generator */
	public MatchGenerator(ArrayList<Matcher> matchers, MatchMerger merger)
		{ this(matchers, merger, null); }
	
	/** Constructs the match generator */
	public MatchGenerator(ArrayList<Matcher> matchers, MatchMerger merger, MatchTypeMappings typeMappings)
		{ this.matchers = matchers; this.merger = merger; this.typeMappings = typeMappings; }
	
	/** Adds a listener to the match generator */
	public void addListener(MatchGeneratorListener listener)
		{ listeners.add(listener); }
	
	/** Run the matchers to calculate match scores */
	public MatchScores getScores(FilteredSchemaInfo schema1, FilteredSchemaInfo schema2)
	{
		merger.initialize(schema1, schema2, typeMappings);
		for(Matcher matcher : matchers)
		{
			// Make sure that the matcher can run
			if(matcher.needsClient() && MatcherManager.getClient()==null)
				System.out.println("The matcher \"" + matcher.getName() + "\" cannot run without referencing the repository");

			// Inform listeners that the matcher is being run
			for(MatchGeneratorListener listener : listeners)
				listener.matcherRun(matcher);
			
			// Run the matcher
			matcher.initialize(schema1, schema2, typeMappings);
			merger.addMatcherScores(matcher.match());
		}
		return merger.getMatchScores();
	}
}