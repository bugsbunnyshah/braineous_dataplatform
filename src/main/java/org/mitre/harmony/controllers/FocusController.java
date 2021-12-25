package org.mitre.harmony.controllers;

import java.util.ArrayList;
import java.util.HashMap;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.filters.ElementPath;
import org.mitre.harmony.model.filters.Focus;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;
import org.mitre.schemastore.search.SchemaSearchResult;


/** Class for various focus controls */
public class FocusController
{
	/** Sets the focus on the current search results */
	static public void setFocusOnSearchResults(HarmonyModel harmonyModel, Integer side, boolean append)
	{
		// Get the already set focused paths
		HashMap<Integer,ArrayList<ElementPath>> paths = new HashMap<Integer,ArrayList<ElementPath>>();
		if(append)
			for(Focus focus : harmonyModel.getFilters().getFoci(side))
				paths.put(focus.getSchemaID(), new ArrayList<ElementPath>(focus.getFocusedPaths()));
		
		// Construct new focused paths based on matches
		HashMap<Integer,SchemaSearchResult> matches = harmonyModel.getSearchManager().getMatches(side);
		for(Integer schemaID : harmonyModel.getProjectManager().getSchemaIDs(side))
		{
			// Retrieve the schema paths
			ArrayList<ElementPath> schemaPaths = paths.get(schemaID);
			if(schemaPaths==null) paths.put(schemaID,schemaPaths = new ArrayList<ElementPath>());
			
			// Identify all matches in a schema
			HierarchicalSchemaInfo schemaInfo = harmonyModel.getSchemaManager().getSchemaInfo(schemaID);
			for(Integer match : matches.keySet())
			{
				// Identify all locations in the schema where the element exists
				PATH_LOOP: for(ArrayList<SchemaElement> matchElements : schemaInfo.getPaths(match))
				{
					ElementPath matchPath = ElementPath.getElementPath(matchElements);
					for(ElementPath schemaPath : schemaPaths)
						if(matchPath.contains(schemaPath)) continue PATH_LOOP;
					schemaPaths.add(matchPath);
				}
			}
		}

		// Update the foci with the newly generated foci
		harmonyModel.getFilters().removeAllFoci(side);
		for(Integer schemaID : paths.keySet())
			for(ElementPath path : paths.get(schemaID))
				harmonyModel.getFilters().addFocus(side, schemaID, path);
	}
}