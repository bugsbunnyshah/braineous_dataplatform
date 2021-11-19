// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.model.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.mitre.harmony.model.AbstractManager;
import org.mitre.harmony.model.ConfigManager;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.project.ProjectListener;

/**
 * Tracks preferences used in Harmony
 * @author CWOLF
 */
public class PreferencesManager extends AbstractManager<PreferencesListener> implements ProjectListener
{	
	/** Stores if the various schema elements have been marked as finished */
	private HashMap<Integer,HashSet<Integer>> finishedElementMap = new HashMap<Integer,HashSet<Integer>>();
	
	/** Constructor used to monitor changes that might affect the selected info */
	public PreferencesManager(HarmonyModel harmonyModel)
	{
		super(harmonyModel);
		ArrayList<Integer> schemaIDs = harmonyModel.getProjectManager().getSchemaIDs();
		
		// Only set finished element preferences for schemas that exist in the mapping
		HashMap<Integer,String> map = ConfigManager.getHashMap("preferences.finished");
		for(Integer schemaID : map.keySet())
			if(schemaIDs.contains(schemaID))
			{
				// Format the element list
				String list = map.get(schemaID);
				list = list.substring(1,list.length()-1);

				// Set the listed elements to the finished state
				HashSet<Integer> elementIDs = new HashSet<Integer>();
				for(String element : list.split(","))
					try { elementIDs.add(Integer.valueOf(element.trim())); } catch(Exception e) {}
				setFinished(schemaID, elementIDs, true);
			}
	}

	/** Remove the finished elements associated with a removed schema */
	public void schemaRemoved(Integer schemaID)
	{
		// Remove all "finished element" preferences associated with the removed schema
		HashSet<Integer> finishedElements = finishedElementMap.get(schemaID);
		if(finishedElements!=null)
			setFinished(schemaID, finishedElements, false);
	}
	
	// ------------- Preference for if schema types should be shown ------------

	/** Returns the preference for if schema types should be displayed */
	public boolean getShowSchemaTypes()
		{ try { return Boolean.parseBoolean(ConfigManager.getParm("preferences.showSchemaTypes")); } catch(Exception e) {} return false; }
	
	/** Set preference to show schema types */
	public void setShowSchemaTypes(boolean newShowSchemaTypes)
	{
		// Only set preference if changed from original
		if(newShowSchemaTypes!=getShowSchemaTypes())
		{
			ConfigManager.setParm("preferences.showSchemaTypes",Boolean.toString(newShowSchemaTypes));
			for(PreferencesListener listener : getListeners())
				listener.showSchemaTypesChanged();
		}
	}
	/** Returns the preference for if cardinality should be displayed */
	public boolean getShowCardinality()
		{ try { return Boolean.parseBoolean(ConfigManager.getParm("preferences.showCardinality")); } catch(Exception e) {} return false; }
	
	/** Set preference to show schema types */
	public void setShowCardinality(boolean newShowCardinality)
	{
		// Only set preference if changed from original
		if(newShowCardinality!=getShowCardinality())
		{
			ConfigManager.setParm("preferences.showCardinality",Boolean.toString(newShowCardinality));
			for(PreferencesListener listener : getListeners())
				listener.showCardinalityChanged();
		}
	}
	
	// ------------- Preference for if schema nodes should be alphabetized ------------

	/** Returns the preference for if schema nodes should be alphabetized */
	public boolean getAlphabetized()
		{ try { return Boolean.parseBoolean(ConfigManager.getParm("preferences.alphabetize")); } catch(Exception e) {} return false; }
	
	/** Set preference to alphabetize */
	public void setAlphabetized(boolean newAlphabetized)
	{
		// Only set preference if changed from original
		if(newAlphabetized!=getAlphabetized())
		{
			ConfigManager.setParm("preferences.alphabetize",Boolean.toString(newAlphabetized));
			for(PreferencesListener listener : getListeners())
				listener.alphabetizedChanged();
		}
	}	
	
	// ------------- Preference for if matched elements should be ignored ------------

	/** Returns the preference for if matched elements should be ignored */
	public boolean getIgnoreMatchedElements()
		{ try { return Boolean.parseBoolean(ConfigManager.getParm("preferences.ignoreMatchedElements")); } catch(Exception e) {} return false; }
	
	/** Set preference to ignore matched elements */
	public void setIgnoredMatchedElements(boolean newIgnoreMatchedElements)
	{
		// Only set preference if changed from original
		if(newIgnoreMatchedElements!=getIgnoreMatchedElements())
			ConfigManager.setParm("preferences.ignoreMatchedElements",Boolean.toString(newIgnoreMatchedElements));
	}	
	
	// ------------- Preferences for storing the import and export directories -------------
	
	/** Returns the import directory */
	public File getImportDir()
		{ try { return new File(ConfigManager.getParm("preferences.importDir")); } catch(Exception e) {} return new File("."); }

	/** Returns the export directory */
	public File getExportDir()
		{ try { return new File(ConfigManager.getParm("preferences.exportDir")); } catch(Exception e) {} return new File("."); }

	/** Set current directory used for importing projects */
	public void setImportDir(File importDir)
	{
		// Only set preference if changed from original
		if(!importDir.equals(getImportDir()))
			ConfigManager.setParm("preferences.importDir",importDir.getPath());
	}
	
	/** Set current directory used for exporting projects */
	public void setExportDir(File exportDir)
	{
		// Only set preference if changed from original
		if(!exportDir.equals(getExportDir()))
			ConfigManager.setParm("preferences.exportDir",exportDir.getPath());
	}

	// ------------- Preference for tracking which schema elements are marked as finished -------------
	
	/** Returns if the specified schema element is marked as finished */
	public boolean isFinished(Integer schemaID, Integer elementID)
	{
		HashSet<Integer> finishedElements = finishedElementMap.get(schemaID);
		return finishedElements!=null && finishedElements.contains(elementID);
	}
	
	/** Returns the finished elements for the specified schema */
	public ArrayList<Integer> getFinishedElements(Integer schemaID)
	{
		HashSet<Integer> finishedElements = finishedElementMap.get(schemaID);
		return finishedElements==null ? new ArrayList<Integer>() : new ArrayList<Integer>(finishedElements);
	}
	
	/** Sets the flag indicating if analysis of the schema element is finished */
	public void setFinished(Integer schemaID, HashSet<Integer> elementIDs, boolean finished)
	{
		// Retrieve the list of finished elements from the map
		HashSet<Integer> finishedElements = finishedElementMap.get(schemaID);
		if(finishedElements==null) finishedElementMap.put(schemaID, finishedElements = new HashSet<Integer>());

		// Eliminate all elements which aren't really changing state
		for(Integer elementID : new ArrayList<Integer>(elementIDs))
			if(finishedElements.contains(elementID)==finished)
				elementIDs.remove(elementID);
		
		// Update the list of finished items
		if(finished) finishedElements.addAll(elementIDs);
		else finishedElements.removeAll(elementIDs);
		ConfigManager.setParm("preferences.finished",finishedElementMap.toString());
		
		// Inform listeners to the changes made to preferences
		for(PreferencesListener listener : getListeners())
		{
			if(finished) listener.elementsMarkedAsFinished(schemaID, elementIDs);
			else listener.elementsMarkedAsUnfinished(schemaID, elementIDs);
		}
	}
	
	/** Unmarks all finished elements */
	public void unmarkAllFinished()
	{
		for(Integer schemaID : finishedElementMap.keySet())
			setFinished(schemaID,finishedElementMap.get(schemaID),false);
	}

	// Unused listener events
	public void projectModified() {}
	public void schemaAdded(Integer schemaID) {}
	public void schemaModelModified(Integer schemaID) {}
}