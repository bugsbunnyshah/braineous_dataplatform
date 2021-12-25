package org.mitre.harmony.view.dialogs.matcher.wizard;

import javax.swing.JPanel;

/** Abstract class for storing a panel for the wizard */
abstract public class WizardPanel extends JPanel
{    
	/** Stores the wizard associated with this panel */
	private Wizard wizard;
	
    /** Construct the wizard panel */
    public WizardPanel(Wizard wizard)
    	{ this.wizard = wizard; }
    
    /** Returns the ID associated with this pane */
    public String getID()
    	{ return getClass().getSimpleName(); }
    
    /** Returns the wizard associated with this pane */
    public final Wizard getWizard()
    	{ return wizard; }   
 
    // Allows panel to perform actions before, during, and after being displayed
    public void aboutToDisplayPanel() {}
    public void displayingPanel() {}
    public void aboutToHidePanel() {}
}
