// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.model;

import java.util.ArrayList;

/**
 * Class defining an abstract manager
 * @author CWOLF
 */
public abstract class AbstractManager<Listener>
{	
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** Stores manager listeners */
	protected ListenerGroup<Listener> listeners = new ListenerGroup<Listener>();

	/** Constructor used to monitor changes that might affect the selected info */
	public AbstractManager(HarmonyModel harmonyModel)
		{ this.harmonyModel = harmonyModel; }

	/** Returns the Harmony model */
	protected HarmonyModel getModel()
		{ return harmonyModel; }
	
	/** Adds a listener */
	public void addListener(Listener listener) { listeners.add(listener); }

	/** Removes a listener */
	public void removeListener(Listener listener) { listeners.remove(listener); }

	/** Returns the list of listeners */
	public ArrayList<Listener> getListeners()
		{ return listeners.get(); }
}