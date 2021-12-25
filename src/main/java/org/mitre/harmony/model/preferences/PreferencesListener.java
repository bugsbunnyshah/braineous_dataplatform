// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.model.preferences;

import java.util.HashSet;

/**
 * Interface used by all Harmony preference listeners
 * @author CWOLF
 */
public interface PreferencesListener
{
	/** Indicates change to displayed view */
	public void displayedViewChanged();

	/** Indicates change to schema type display setting */
	public void showSchemaTypesChanged();

	/** Indicates change to alphabetize setting */
	public void alphabetizedChanged();
	
	/**Indicates change to cardinality display setting */
	public void showCardinalityChanged();
	
	/** Indicates the marking of an element as finished */
	public void elementsMarkedAsFinished(Integer schemaID, HashSet<Integer> elementIDs);
	
	/** Indicates the marking of an element as unfinished */
	public void elementsMarkedAsUnfinished(Integer schemaID, HashSet<Integer> elementIDs);
	
}
