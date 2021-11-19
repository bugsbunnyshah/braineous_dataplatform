package org.mitre.harmony.model;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Vector;

/** Class for managing a group of listeners */
public class ListenerGroup<Listener>
{
	/** Stores references to the various listener classes */
	private ReferenceQueue<Listener> queue = new ReferenceQueue<Listener>();  
	
	/** Stores the listeners */
	private Vector<WeakReference<Listener>> references = new Vector<WeakReference<Listener>>();

	/** Returns the list of listeners */
	public ArrayList<Listener> get()
	{
		// Eliminates listeners which are no longer used
		WeakReference wr = (WeakReference)queue.poll();
	    while(wr != null)
	    {
	    	references.removeElement(wr);
	    	wr = (WeakReference)queue.poll();
	    }

		// Return the listeners
	    ArrayList<Listener> listeners = new ArrayList<Listener>();
	    for(WeakReference<Listener> reference : references)
	    	listeners.add(reference.get());
	    return listeners;
	}
	
	/** Adds a listener */
	public void add(Listener listener)
		{ references.addElement(new WeakReference<Listener>(listener,queue)); }

	/** Removes a listener */
	public void remove(Listener listener)
		{ references.remove(listener); }
}
