// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import org.mitre.schemastore.model.Alias;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

/** Class for tracking primary matches */
public class PrimaryMatches
{
	/** Private class for handling the comparison of schema objects */
	private class SchemaElementComparator implements Comparator<SchemaElement>
	{
		public int compare(SchemaElement so1, SchemaElement so2)
		{
			if(so1.getClass()==so2.getClass()) return so1.getName().compareTo(so2.getName());
			if(so1.getClass()==Entity.class) return -1; if(so2.getClass()==Entity.class) return 1;
			if(so1.getClass()==Domain.class) return -1; if(so2.getClass()==Domain.class) return 1;
			if(so1.getClass()==DomainValue.class) return -1; if(so2.getClass()==DomainValue.class) return 1;
			if(so1.getClass()==Relationship.class) return -1; if(so2.getClass()==Relationship.class) return 1;
			if(so1.getClass()==Subtype.class) return -1; if(so2.getClass()==Subtype.class) return 1;
			return 1;
		}
	}
	
	/** Object storing match information */
	private ArrayList<SchemaElement> matches = new ArrayList<SchemaElement>();
	
	/** Object storing primary match information */
	private HashMap<SchemaElement,ArrayList<SchemaElement>> primaryMatches = new HashMap<SchemaElement,ArrayList<SchemaElement>>();

	/** Indicates if subsumed matches are even possible */ @SuppressWarnings("rawtypes")
	static private boolean subsumedMatchesPossible(ArrayList<SchemaElement> matches)
	{
		HashSet<Class> types = new HashSet<Class>();
		for(SchemaElement match : matches)
			types.add(match.getClass());
		return types.size()>1;
	}
	
	/** Gets the primary matches for the specified element */
	private ArrayList<SchemaElement> getPrimaryMatches(SchemaElement match, HierarchicalSchemaInfo schemaInfo)
	{
		// Identify the list of primary matches
		ArrayList<SchemaElement> primaryMatches = new ArrayList<SchemaElement>();
		if(match instanceof Attribute)
		{
			Entity entity = schemaInfo.getEntity(match.getId());
			primaryMatches.addAll(getPrimaryMatches(entity,schemaInfo));
		}
		else if(match instanceof Domain)
		{
			for(Attribute attribute : schemaInfo.getAttributes(match.getId()))
				primaryMatches.addAll(getPrimaryMatches(attribute,schemaInfo));
		}
		else if(match instanceof DomainValue)
		{
			Domain domain = (Domain)schemaInfo.getElement(((DomainValue)match).getDomainID());
			primaryMatches.addAll(getPrimaryMatches(domain,schemaInfo));
		}
		else if(match instanceof Alias)
		{
			SchemaElement element = schemaInfo.getElement(((Alias)match).getElementID());
			primaryMatches.addAll(getPrimaryMatches(element,schemaInfo));
		}
		
		// If no primary matches found and a match, mark self as primary match
		if(primaryMatches.size()==0 && matches.contains(match))
			primaryMatches.add(match);

		// Returns the list of found primary matches
		return primaryMatches;
	}
	
	/** Adds a primary match to the primary match mapping */
	private void addPrimaryMatch(SchemaElement primaryMatch, SchemaElement match)
	{
		ArrayList<SchemaElement> matches = primaryMatches.get(primaryMatch);
		if(matches==null) primaryMatches.put(primaryMatch,matches = new ArrayList<SchemaElement>());
		if(!primaryMatch.equals(match)) matches.add(match);
	}

	/** PrimaryMatches constructor */
	PrimaryMatches(HierarchicalSchemaInfo schema, Collection<Match> matchesIn)
	{
		// Identify all schema object matches in the full list of matches
		for(Match match : matchesIn)
			if(match.getElement() instanceof SchemaElement)
				matches.add((SchemaElement)match.getElement());

		// If subsumed matches are possible, identify them
		if(subsumedMatchesPossible(matches))
		{
			for(SchemaElement match : matches)
				for(SchemaElement primaryMatch : getPrimaryMatches(match,schema))
					addPrimaryMatch(primaryMatch, match);
		}

		// Otherwise, mark all matches as primary matches
		else for(SchemaElement match : matches)
			addPrimaryMatch(match, match);
	}
	
	/** Returns the primary matches */
	public ArrayList<SchemaElement> getPrimaryMatches()
	{
		ArrayList<SchemaElement> primaryMatchList = new ArrayList<SchemaElement>(primaryMatches.keySet());
		Collections.sort(primaryMatchList,new SchemaElementComparator());
		return primaryMatchList;
	}
	
	/** Returns the subsumptions for the specified primary match */
	public ArrayList<SchemaElement> getSubsumedMatches(SchemaElement primaryMatch)
	{
		ArrayList<SchemaElement> subsumedMatches = primaryMatches.get(primaryMatch);
		if(subsumedMatches==null) subsumedMatches = new ArrayList<SchemaElement>();
		Collections.sort(subsumedMatches,new SchemaElementComparator());
		return subsumedMatches;
	}
}