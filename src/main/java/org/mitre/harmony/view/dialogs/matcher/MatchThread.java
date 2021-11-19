package org.mitre.harmony.view.dialogs.matcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.mitre.harmony.matchers.MatchGenerator;
import org.mitre.harmony.matchers.MatchScore;
import org.mitre.harmony.matchers.MatchScores;
import org.mitre.harmony.matchers.MatchTypeMappings;
import org.mitre.harmony.matchers.MatcherManager;
import org.mitre.harmony.matchers.MatchGenerator.MatchGeneratorListener;
import org.mitre.harmony.matchers.matchers.Matcher;
import org.mitre.harmony.matchers.mergers.MatchMerger;
import org.mitre.harmony.model.HarmonyConsts;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.filters.FilterManager;
import org.mitre.harmony.model.filters.Focus;
import org.mitre.harmony.model.project.MappingManager;
import org.mitre.harmony.model.project.ProjectMapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.schemaInfo.FilteredSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

/** Constructs the match pane for the matcher wizard */
public class MatchThread extends Thread
{
	/** Stores the org.org.mitre.harmony model */
	private HarmonyModel harmonyModel;

	/** Stores the merger to be used in matching */
	private MatchMerger merger;

	/** Stores the matchers to be used in matching */
	private ArrayList<Matcher> matchers;

	/** Stores the type mappings to be used in matching */
	private MatchTypeMappings types;

	/** Used to halt the running of the matchers */
	private boolean stop = false;

	/** Stores the list of match listeners */
	private ArrayList<MatchListener> listeners = new ArrayList<MatchListener>();

	/** Class for monitoring match progress */
	private class ProgressThread extends Thread implements MatchGeneratorListener
	{
		/** Stores the total number of mappings being matched */
		private Integer currentMapping = 0, totalMappings = 0;

		/** Stores the current mapping being matched */
		private String leftSchema, rightSchema;

		/** Stores the current matcher being used */
		private Matcher matcher = null;

		/** Indicates that the thread should be stopped */
		private boolean stop = false;

		/** Sets the total number of mappings */
		public void setTotalMappings(Integer totalMappings)
		{
			this.currentMapping = -1;
			this.totalMappings = totalMappings;
		}

		/** Sets the current schema pair being matched */
		public void setCurrentSchemaPair(String leftSchema, String rightSchema)
		{
			this.leftSchema = leftSchema;
			this.rightSchema = rightSchema;
			currentMapping++;
			matcher = null;
		}

		/** Indicates the currently running matcher */
		public void matcherRun(Matcher matcher)
			{ this.matcher = matcher; }
		
		/** Runs the thread */
		public void run()
		{
			while(!stop)
			{
				// Update progress information
				if(matcher == null) matcher = matchers.get(0);

				// Update the schema progress
				Double matcherProgress = (100.0 * matcher.getPercentComplete());
				Double overallProgress = 0.0;

				try {
					Double matchersProgress = ((100 * matchers.indexOf(matcher) + matcherProgress) / matchers.size());
					overallProgress = new Double((100 * currentMapping + matchersProgress) / totalMappings);
				} catch(Exception e) {}

				String status = "Matching " + leftSchema + " to " + rightSchema + " with " + matcher.getName();
				for(MatchListener listener : listeners)
				{
					listener.updateMatcherProgress(matcherProgress, status);
					listener.updateOverallProgress(overallProgress);
				}

				// Wait a second before updating progress again
				try { Thread.sleep(1000); } catch(Exception e) {}
			}
		}

		/** Stops the progress thread */
		public void stopThread()
			{ stop = true; }
	}

	/** Constructs the match thread */
	MatchThread(HarmonyModel harmonyModel, MatchMerger merger, ArrayList<Matcher> matchers, MatchTypeMappings types)
	{
		this.harmonyModel = harmonyModel;
		this.merger = merger;
		this.matchers = matchers;
		this.types = types;
	}	

	/** Returns the generated matcher name */
	private String getMatcherName()
	{
		// Handles the case where all matchers were used
		if(matchers.size() == MatcherManager.getMatchers().size())
			return merger.getName() + "(All Matchers)";

		// Handles the case where a single matcher was used
		if(matchers.size() == 1)
			return matchers.get(0).getName();

		// Handles the case where a subset of matchers was used
		String matcherName = merger.getName() + " (";
		for(Matcher matcher : matchers)
			matcherName += matcher.getName() + ", ";
		matcherName = matcherName.substring(0, matcherName.length() - 2) + ")";
		return matcherName;
	}

	/** Returns the list of filtered schema info for the specified side */
	private FilteredSchemaInfo getFilteredSchemaInfo(Integer schemaID, Integer side)
	{
		// Check to make sure that the schema is currently in focus
		FilterManager filters = harmonyModel.getFilters();
		if(!filters.inFocus(side, schemaID)) return null;

		// Create the filtered schema info object
		HierarchicalSchemaInfo schemaInfo = harmonyModel.getSchemaManager().getSchemaInfo(schemaID);
		FilteredSchemaInfo filteredSchemaInfo = new FilteredSchemaInfo(schemaInfo);

		// Set the filter roots
		Focus focus = filters.getFocus(side,schemaID);
		if(focus != null && !focus.contains(schemaID))
			filteredSchemaInfo.setFilteredRoots(focus.getFocusedIDs());

		// Filter by minimum and maximum depth
		filteredSchemaInfo.setMinDepth(filters.getMinDepth(side));
		filteredSchemaInfo.setMaxDepth(filters.getMaxDepth(side));

		// Hide all finished and unfocused elements
		ArrayList<Integer> finishedElements = harmonyModel.getPreferences().getFinishedElements(schemaID);
		if(focus!=null)
		{
			HashSet<Integer> elementIDs = focus.getElementIDs();
			for(Integer finishedElement : finishedElements)
				elementIDs.remove(finishedElement);
			filteredSchemaInfo.setVisibleElements(elementIDs);

		}
		else filteredSchemaInfo.setHiddenElements(finishedElements);
		
		// Return the filtered schema info object
		return filteredSchemaInfo;
	}

	/** Runs the matchers on the specified schemas */
	private void runMatch(Integer mappingID, FilteredSchemaInfo sourceSchema, FilteredSchemaInfo targetSchema, String matcherName, ProgressThread progressThread)
	{
		// If matched elements should be ignored, hide all user matched elements
		if(harmonyModel.getPreferences().getIgnoreMatchedElements())
		{
			// Use copies of the filtered schema info
			sourceSchema = sourceSchema.copy();
			targetSchema = targetSchema.copy();
			
			// Retrieve the current mapping
			Integer sourceID = sourceSchema.getSchema().getId();
			Integer targetID = targetSchema.getSchema().getId();
			ProjectMapping mapping = harmonyModel.getMappingManager().getMapping(sourceID, targetID);

			// Identify all user matched elements
			HashSet<Integer> sourceElements = new HashSet<Integer>(sourceSchema.getHiddenElements());
			HashSet<Integer> targetElements = new HashSet<Integer>(targetSchema.getHiddenElements());
			for(MappingCell mappingCell : mapping.getMappingCells())
				if(mappingCell.isValidated())
				{
					sourceElements.addAll(Arrays.asList(mappingCell.getElementInputIDs()));
					targetElements.add(mappingCell.getOutput());
				}

			// Set the hidden elements
			sourceSchema.setHiddenElements(sourceElements);
			targetSchema.setHiddenElements(targetElements);
		}
		
		// Generate the match scores for the left and right roots
		MatchGenerator generator = new MatchGenerator(matchers, merger, types);
		generator.addListener(progressThread);
		MatchScores matchScores = generator.getScores(sourceSchema, targetSchema);

		// Store the generated mapping cells
		ArrayList<MappingCell> mappingCells = new ArrayList<MappingCell>();
		MappingManager manager = harmonyModel.getMappingManager();
		for(MatchScore matchScore : matchScores.getScores())
		{	
			// Don't proceed if process has been stopped
			if(stop) return;

			// Don't store mapping cells which were already validated
			Integer mappingCellID = manager.getMappingCellID(matchScore.getSourceID(), matchScore.getTargetID());
			if (mappingCellID != null && manager.getMappingCell(mappingCellID).isValidated()) continue;

			// Get the mapping cell properties
			Integer id = manager.getMappingCellID(matchScore.getSourceID(), matchScore.getTargetID());
			Integer input = matchScore.getSourceID();
			Integer output = matchScore.getTargetID();
			Double score = matchScore.getScore();
			Date date = Calendar.getInstance().getTime();

			// Generate the mapping cell
			mappingCells.add(MappingCell.createProposedMappingCell(id, mappingID, input, output, score, matcherName, date, null));
		}
		manager.getMapping(mappingID).setMappingCells(mappingCells);
	}

	/** Runs the thread */
	public void run()
	{
		// Generate the matcher name
		String matcherName = getMatcherName();

		// Retrieve the visible mappings
		ArrayList<ProjectMapping> mappings = new ArrayList<ProjectMapping>();
		FilterManager filters = harmonyModel.getFilters();
		for(ProjectMapping mapping : harmonyModel.getMappingManager().getMappings())
			if(mapping.isVisible() && filters.inFocus(HarmonyConsts.LEFT, mapping.getSourceId()) && filters.inFocus(HarmonyConsts.RIGHT, mapping.getTargetId()))
				mappings.add(mapping);

		// Gather up the schema info
		HashMap<Integer,FilteredSchemaInfo> schemaInfoList = new HashMap<Integer,FilteredSchemaInfo>();
		for(ProjectMapping mapping : mappings)
		{
			Integer sourceID = mapping.getSourceId(), targetID = mapping.getTargetId();
			if(!schemaInfoList.containsKey(sourceID))
				schemaInfoList.put(sourceID, getFilteredSchemaInfo(sourceID,HarmonyConsts.LEFT));
			if(!schemaInfoList.containsKey(targetID))
				schemaInfoList.put(targetID, getFilteredSchemaInfo(targetID,HarmonyConsts.RIGHT));
		}

		// Create a thread for monitoring the progress of the match process
		ProgressThread progressThread = new ProgressThread();
		progressThread.start();
		progressThread.setTotalMappings(mappings.size());

		// Matches all left schemas to all right schemas
		for(ProjectMapping mapping : mappings)
		{
			// Don't proceed if the matching has been stopped
			if(stop) { break; }

			// Get schema info
			FilteredSchemaInfo sourceSchema = schemaInfoList.get(mapping.getSourceId());
			FilteredSchemaInfo targetSchema = schemaInfoList.get(mapping.getTargetId());
			
			// Inform the progress thread of the current pair of schemas being matched
			String leftName = sourceSchema.getSchema().getName();
			String rightName = targetSchema.getSchema().getName();
			progressThread.setCurrentSchemaPair(leftName,rightName);

			// Run the matcher of the current pair of schemas
			try {
				runMatch(mapping.getId(), sourceSchema, targetSchema, matcherName, progressThread);
			}
			catch (Exception e) { System.out.println("(E) MatchThread.run - " + e.getMessage()); }
		}

		// Stops the progress thread
		progressThread.stopThread();

		// Inform listeners when the matching is completed
		for (MatchListener listener : listeners)
			listener.matchCompleted();
	}
	
	/** Stops the matching process */
	public void stopThread()
		{ stop = true; }

	/** Adds a match listener */
	public void addListener(MatchListener listener)
		{ listeners.add(listener); }
}