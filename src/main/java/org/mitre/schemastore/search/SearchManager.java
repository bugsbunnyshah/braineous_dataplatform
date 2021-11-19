// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.schemastore.search;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.mitre.schemastore.client.SchemaStoreClient;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

/**
 * Manages searches run within Harmony
 */
public class SearchManager
{	
	/** Indicates if the keyword is contained within the specified string */
	static private boolean matches(String text, String keyword)
		{ return text.matches("(?i).*" + keyword.replaceAll("\\*",".*") + ".*"); }
	
	/** Searches for the specified repository */
	static public HashMap<Integer, RepositorySearchResult> search(String keywordString, ArrayList<Integer> tagIDs, SchemaStoreClient client) throws RemoteException
	{
		HashMap<Integer,RepositorySearchResult> results = new HashMap<Integer,RepositorySearchResult>();
		
		// Generate the keywords
		if(keywordString.equals("")) return results;
		ArrayList<String> keywords = new ArrayList<String>(Arrays.asList(keywordString.split("\\s+")));
		
		// Generate the list of schemas associated with the specified tags
		ArrayList<Integer> taggedSchemas = new ArrayList<Integer>();
		if(tagIDs!=null)
			for(Integer tagID : tagIDs)
				taggedSchemas.addAll(client.getTagSchemas(tagID));

		// Search for all keyword matches
		ArrayList<ArrayList<Match>> keywordMatches = new ArrayList<ArrayList<Match>>();
		for(String keyword : keywords)
		{
			// Generate the array of matches for this keyword
			ArrayList<Match> matches = new ArrayList<Match>();
			keywordMatches.add(matches);
			
			// Retrieve all matching schemas
			for(Schema schema : client.getSchemas())
				if(taggedSchemas.size()==0 || taggedSchemas.contains(schema.getId()))
					if(matches(schema.getName(),keyword) || matches(schema.getDescription(),keyword))
						matches.add(new Match(schema));
			
			// Retrieve all matching schema elements
			for(SchemaElement element : client.getSchemaElementsForKeyword(keyword.toString(), tagIDs))
				matches.add(new Match(element));
		}
				
		// Then identify the matched schemas (those that match all keywords)
		HashSet<Integer> matchedSchemas = null;
		for(ArrayList<Match> matches : keywordMatches)
		{
			// Identify the schemas matching a single keyword
			HashSet<Integer> keywordSchemas = new HashSet<Integer>();
			for(Match match : matches)
				keywordSchemas.add(match.getSchema());
			
			// Merge these schemas back into the list of all matched schemas
			if(matchedSchemas==null) matchedSchemas = keywordSchemas;
			else for(Integer matchedSchema : new ArrayList<Integer>(matchedSchemas))
				if(!keywordSchemas.contains(matchedSchema))
					matchedSchemas.remove(matchedSchema);
		}
			
		// Finally construct the search results
		for(Integer schemaID : matchedSchemas)
		{
			// Retrieve the list of schema matches
			ArrayList<Match> schemaMatches = new ArrayList<Match>();
			for(ArrayList<Match> matches : keywordMatches)
				for(Match match : matches)
					if(match.getSchema().equals(schemaID))
						schemaMatches.add(match);

			// Store the generated search result
			HierarchicalSchemaInfo schema = new HierarchicalSchemaInfo(client.getSchemaInfo(schemaID));
			results.put(schemaID, new RepositorySearchResult(schema,schemaMatches));				
		}

		return results;
	}

	/** Search the specified schema for elements that match the keyword */
	public static HashMap<Integer, SchemaSearchResult> search(String keyword, HierarchicalSchemaInfo schema)
	{		
		HashMap<Integer,SchemaSearchResult> results = new HashMap<Integer,SchemaSearchResult>();
		
		// Generate the keyword
		if(keyword.equals("")) return results;
		keyword = ".*" + keyword + ".*";
		if(keyword.toLowerCase().equals(keyword)) keyword = "(?i)" + keyword;
		
		// Determine what elements match search criteria
		for(SchemaElement element : schema.getHierarchicalElements())
		{				
			// Check to see if element name or description matches search criteria
			boolean nameMatched = element.getName().matches(keyword);
			boolean descriptionMatched = element.getDescription().matches(keyword);

			// Stores a new search result if needed
			if(nameMatched || descriptionMatched)
				results.put(element.getId(), new SchemaSearchResult(nameMatched,descriptionMatched));
		}

		return results;
	}

	/** Search the specified schema for elements with a given ID */
	public static HashMap<Integer,SchemaSearchResult> search(Integer id, HierarchicalSchemaInfo schema)
	{		
		HashMap<Integer,SchemaSearchResult> results = new HashMap<Integer,SchemaSearchResult>();
				
		// Determine what elements match search criteria
		for(SchemaElement element : schema.getHierarchicalElements())
		{				
			// Check to see if element name or description matches search criteria
			boolean matched = element.getId().equals(id);

			// Stores a new search result if needed
			if(matched)
				results.put(element.getId(), new SchemaSearchResult(true,true));
		}

		return results;
	}


}