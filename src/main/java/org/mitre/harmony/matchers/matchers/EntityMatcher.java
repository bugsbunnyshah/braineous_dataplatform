// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.matchers.matchers;

import java.util.ArrayList;
import java.util.HashMap;

import org.mitre.harmony.matchers.MatcherScores;
import org.mitre.harmony.matchers.matchers.bagMatcher.BagMatcher;
import org.mitre.harmony.matchers.matchers.bagMatcher.WordBag;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.model.schemaInfo.FilteredSchemaInfo;

/** Entity Matcher Class */
public class EntityMatcher extends BagMatcher
{
	/** Defines the entity map class */
	protected class EntityMap extends HashMap<SchemaElement,ArrayList<SchemaElement>> {};
	
	/** Returns the name of the matcher */
	public String getName()
		{ return "Entity Matcher"; }
	
	/** Generates scores for the specified elements */ @Override
	public MatcherScores generateScores()
	{
		EntityMap sourceEntities = getEntities(schema1);
		EntityMap targetEntities = getEntities(schema2);		
		return match(sourceEntities, targetEntities);
	}
	
	/** Generates scores for the specified source and target entities */
	protected MatcherScores match(EntityMap sourceEntities, EntityMap targetEntities)
	{
		// Generate the word bags for each entity
		HashMap<Integer,WordBag> wordBags = new HashMap<Integer,WordBag>();
		for(EntityMap entities : new EntityMap[]{sourceEntities,targetEntities})
			for(SchemaElement entity : entities.keySet())
			{
				WordBag wordBag = generateWordBag(entity);
				for(SchemaElement element : entities.get(entity))
					addElementToWordBag(wordBag, element);
				wordBags.put(entity.getId(), wordBag);
			}
				
		// Generate the scores
		return computeScores(new ArrayList<SchemaElement>(sourceEntities.keySet()), new ArrayList<SchemaElement>(targetEntities.keySet()), wordBags);
	}
	
	/** Returns the entities for the specified schema */
	protected EntityMap getEntities(FilteredSchemaInfo schema)
	{
		EntityMap entityMap = new EntityMap();
		for(SchemaElement entity : schema.getElements(Entity.class))
		{
			// Retrieve the elements associated with the entity
			ArrayList<SchemaElement> elements = new ArrayList<SchemaElement>();
			for(SchemaElement element : schema.getReferencingElements(entity.getId()))
			{
				if(element instanceof Containment && ((Containment)element).getChildID().equals(entity.getId())) { continue; }
				if(element instanceof Subtype && ((Subtype)element).getChildID().equals(entity.getId())) { continue; }
				elements.add(element);
			}

			// Add the elements to the entity map if falls within filter
			boolean visible = false;
			visible |= schema.isVisible(entity.getId());
			for(SchemaElement element : elements)
				if(schema.isVisible(element.getId()))
					{ visible = true; break; }
			if(visible) entityMap.put(entity,elements);
		}
		return entityMap;
	}
}