// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs.matcher;

/**
 * Interface used by all match listeners
 * @author CWOLF
 */
public interface MatchListener
{
	/** Indicates the current progress of the matchers */
	public void updateMatcherProgress(Double percentComplete, String status);

	/** Indicates the current progress of the matching of schema pairs */
	public void updateOverallProgress(Double percentComplete);
	
	/** Indicates that the matching is complete */
	public void matchCompleted();
}
