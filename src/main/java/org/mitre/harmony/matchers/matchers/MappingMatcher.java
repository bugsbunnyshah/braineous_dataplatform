// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.matchers.matchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.mitre.harmony.matchers.MatcherManager;
import org.mitre.harmony.matchers.MatcherScores;
import org.mitre.harmony.matchers.matchers.bagMatcher.BagMatcher;
import org.mitre.harmony.matchers.matchers.bagMatcher.WordBag;
import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.Project;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.SchemaInfo;

/** Mapping Matcher Class */
public class MappingMatcher extends BagMatcher
{
	/** Stores the word bag used for this matcher */
	private HashMap<Integer, WordBag> wordBags = new HashMap<Integer, WordBag>();
	
	/** Returns the name of the matcher */
	public String getName()
		{ return "Mapping Matcher"; }

	/** Indicates that the matcher needs a repository client */
	public boolean needsClient() { return true; }
	
	/** Generates match scores for the specified elements */ @Override
	public MatcherScores generateScores()
	{
		// Only proceed if the client is set (otherwise, the mappings can't be accessed)
		if(MatcherManager.getClient()==null) return new MatcherScores(SCORE_CEILING);
		
		// Create word bags for the source elements
		ArrayList<SchemaElement> sourceElements = schema1.getFilteredElements();
		for(SchemaElement sourceElement : sourceElements)
			wordBags.put(sourceElement.getId(), generateWordBag(sourceElement));
		
		// Create word bags for the target elements
		ArrayList<SchemaElement> targetElements = schema2.getFilteredElements();
		for(SchemaElement targetElement : targetElements)
			wordBags.put(targetElement.getId(), generateWordBag(targetElement));

		// Identify associated mappings
		ArrayList<Mapping> associatedMappings = new ArrayList<Mapping>();
		associatedMappings = getAssociatedMappings(schema1.getSchema().getId());
		associatedMappings = getAssociatedMappings(schema2.getSchema().getId());
		
		// Generate scores (only if associated mappings exist)
		if(associatedMappings.size() > 0)
		{
			// Add terms from associated mappings
			for(Mapping associatedMapping : associatedMappings)
				addAssociatedTerms(associatedMapping);
			
			// Generate the match scores
			return computeScores(sourceElements, targetElements, wordBags);
		}
		return new MatcherScores(SCORE_CEILING);
	}

	/** Returns the associated mappings */
	private ArrayList<Mapping> getAssociatedMappings(Integer schemaID)
	{
		ArrayList<Mapping> mappings = new ArrayList<Mapping>();
		try {
			for(Project project : MatcherManager.getClient().getProjects())
				for(Mapping mapping : MatcherManager.getClient().getMappings(project.getId()))
					if(mapping.getSourceId().equals(schemaID) || mapping.getTargetId().equals(schemaID))
						mappings.add(mapping);
		} catch(Exception e) {}
		return mappings;
	}
	
	/** Adds associated terms for the associated mapping */
	private void addAssociatedTerms(Mapping mapping)
	{
		try {
			// Identify the associated schema
			boolean isSource = !mapping.getSourceId().equals(schema1) && !mapping.getSourceId().equals(schema2);
			SchemaInfo schema = MatcherManager.getClient().getSchemaInfo(isSource ? mapping.getSourceId() : mapping.getTargetId());

			// Find associated elements
			for(MappingCell mappingCell : MatcherManager.getClient().getMappingCells(mapping.getId()))
				if(mappingCell.isValidated())
				{
					// Get reference IDs
					ArrayList<Integer> referenceIDs = new ArrayList<Integer>();
					if(isSource) referenceIDs.add(mappingCell.getOutput());
					else referenceIDs.addAll(Arrays.asList(mappingCell.getElementInputIDs()));
					
					// Get the word bags
					ArrayList<WordBag> bags = new ArrayList<WordBag>();
					for(Integer referenceID : referenceIDs)
					{
						WordBag bag = wordBags.get(referenceID);
						if(bag!=null) bags.add(bag);
					}
					if(bags.size() == 0) continue;
	
					// Get associated IDs
					ArrayList<Integer> associatedIDs = new ArrayList<Integer>();
					if(isSource) associatedIDs.addAll(Arrays.asList(mappingCell.getElementInputIDs()));
					else associatedIDs.add(mappingCell.getOutput());
					
					// Add in associated terms
					for(Integer associatedID : associatedIDs)
					{
						SchemaElement element = schema.getElement(associatedID);
						for(WordBag bag : bags) addElementToWordBag(bag, element);
					}
				}
		} catch(Exception e) {}
	}
}