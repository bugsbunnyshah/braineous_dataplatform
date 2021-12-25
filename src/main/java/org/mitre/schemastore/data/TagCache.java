// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data;

import java.util.ArrayList;
import java.util.HashMap;

import org.mitre.schemastore.data.database.TagDataCalls;
import org.mitre.schemastore.data.database.TagDataCalls.SchemaTag;
import org.mitre.schemastore.model.Tag;

/** Class for managing the tags in the schema repository */
public class TagCache extends DataCache
{
	/** Stores reference to the tag data calls */
	private TagDataCalls dataCalls = null;
	
	/** Stores the validation number */
	private Integer validationNumber = 0;
	
	/** Stores a mapping of schema tags */
	private HashMap<Integer,ArrayList<Integer>> schemaTags = new HashMap<Integer,ArrayList<Integer>>();
	
	/** Stores a mapping of tag schemas */
	private HashMap<Integer,ArrayList<Integer>> tagSchemas = new HashMap<Integer,ArrayList<Integer>>();
	
	/** Constructs the tags cache */
	TagCache(DataManager manager, TagDataCalls dataCalls)
		{ super(manager); this.dataCalls=dataCalls; }
	
	/** Refreshes the schema tags */
	private void recacheAsNeeded()
	{
		// Check to see if the schema tags have changed any
		Integer newValidationNumber = dataCalls.getSchemaTagValidationNumber();
		if(!newValidationNumber.equals(validationNumber))
		{
			validationNumber = newValidationNumber;
			
			// Clears the cached schema tags
			schemaTags.clear();
			tagSchemas.clear();
			
			// Caches the schema tags
			for(SchemaTag schemaTag : dataCalls.getSchemaTags())
			{
				// Place in schema tag hash
				ArrayList<Integer> schemaTagArray = schemaTags.get(schemaTag.getSchemaID());
				if(schemaTagArray==null) schemaTags.put(schemaTag.getSchemaID(),schemaTagArray = new ArrayList<Integer>());
				schemaTagArray.add(schemaTag.getTagID());
				
				// Place in tag schema hash
				ArrayList<Integer> tagSchemaArray = tagSchemas.get(schemaTag.getTagID());
				if(tagSchemaArray==null) tagSchemas.put(schemaTag.getTagID(),tagSchemaArray = new ArrayList<Integer>());
				tagSchemaArray.add(schemaTag.getSchemaID());
			}
		}
	}
	
	/** Returns a listing of all tags */
	public ArrayList<Tag> getTags()
		{ return dataCalls.getTags(); }

	/** Returns the specified tag */
	public Tag getTag(Integer tagID)
		{ return dataCalls.getTag(tagID); }
	
	/** Returns the listing of sub-categories for the specified tag */
	public ArrayList<Tag> getSubcategories(Integer tagID)
		{ return dataCalls.getSubcategories(tagID); }
	
	/** Adds the specified tag */
	public Integer addTag(Tag tag)
		{ return dataCalls.addTag(tag); }
	
	/** Updates the specified tag */
	public boolean updateTag(Tag tag)
		{ return dataCalls.updateTag(tag); }
	
	/** Removes the specified tag (and all sub-categories) */
	public boolean deleteTag(Integer tagID)
	{
		for(Tag subcategory : getSubcategories(tagID))
			if(!deleteTag(subcategory.getId())) return false;
		return dataCalls.deleteTag(tagID);
	}
	
	/** Returns the list of tag schemas */
	public ArrayList<Integer> getTagSchemas(Integer tagID)
	{
		recacheAsNeeded();
		if(tagSchemas.get(tagID)!=null)
			return tagSchemas.get(tagID);
		return new ArrayList<Integer>();
	}
	
	/** Returns the list of schema tags */
	public ArrayList<Integer> getSchemaTags(Integer schemaID)
	{
		recacheAsNeeded();
		if(schemaTags.get(schemaID)!=null)
			return schemaTags.get(schemaID);
		return new ArrayList<Integer>();
	}
	
	/** Adds a tag to the specified schema */
	public Boolean addTagToSchema(Integer schemaID, Integer tagID)
		{ return dataCalls.addTagToSchema(schemaID, tagID); }

	/** Removes a tag from the specified schema */
	public Boolean removeTagFromSchema(Integer schemaID, Integer tagID)
		{ return dataCalls.removeTagFromSchema(schemaID, tagID); }
}
