// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.schemastore.data;


/**
 * Class defining an abstract data cache
 * @author CWOLF
 */
public abstract class DataCache
{	
	/** Stores the data manager */
	private DataManager dataManager;

	/** GraphData Cache Constructor */
	public DataCache(DataManager dataManager)
		{ this.dataManager = dataManager; }

	/** Returns the data manager */
	protected DataManager getManager()
		{ return dataManager; }
}