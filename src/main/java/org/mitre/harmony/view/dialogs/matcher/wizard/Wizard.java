package org.mitre.harmony.view.dialogs.matcher.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.mitre.harmony.matchers.MatchTypeMappings;
import org.mitre.harmony.matchers.MatcherManager;
import org.mitre.harmony.matchers.matchers.Matcher;
import org.mitre.harmony.matchers.mergers.MatchMerger;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.dialogs.matcher.MatchTypePane;
import org.mitre.harmony.view.dialogs.matcher.MatcherParametersPane;
import org.mitre.harmony.view.dialogs.matcher.MatcherSelectionPane;
import org.mitre.harmony.view.dialogs.matcher.MatchingStatusPanel;

/** Class defining the wizard */
public class Wizard extends JInternalFrame
{
    /** Stores a list of the various wizard panels */
    private ArrayList<WizardPanel> panels = new ArrayList<WizardPanel>();

    /** Store reference to the panel for showing the wizard panels */
    private JPanel cardPanel;
    
    /** Stores the wizard button pane */
    private WizardButtonPane buttonPane;
    
    /** Stores reference to the current panel */
    private Integer currentIndex = 0;

    /** Constructs the wizard */
    public Wizard(ArrayList<Matcher> matchers, MatchMerger merger, boolean custom, HarmonyModel harmonyModel)
    {
    	super("Run Schema Matchers");

    	// Create the list of wizard panels
    	panels.add(new MatcherSelectionPane(this, matchers));
    	for(Matcher matcher : MatcherManager.getVisibleMatchers())
    		if(matcher.getParameters().size()>0)
    			panels.add(new MatcherParametersPane(this, matcher));
    	panels.add(new MatchTypePane(this, harmonyModel));
    	panels.add(new MatchingStatusPanel(this, harmonyModel, merger));

    	// Generate the pane for displaying the wizard panels
        cardPanel = new JPanel();
        cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));       
        cardPanel.setLayout(new CardLayout());
    	for(WizardPanel panel : panels)
        	cardPanel.add(panel, panel.getID());
   
    	// Generate the wizard pane
    	JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());
        pane.add(cardPanel, BorderLayout.CENTER);
        pane.add(buttonPane = new WizardButtonPane(this), BorderLayout.SOUTH);

    	// Set the currently displayed panel
    	if(!custom) setCurrentPanel(panels.size()-1);
        
    	// Generate the wizard dialog
        setContentPane(pane);
        pack();
        setVisible(true);
    }
    
    /** Handles the closing of the wizard */
    public void close()
    	{ getPanel(currentIndex).aboutToHidePanel(); dispose(); }
    
    /** Returns the specified panel */
    public WizardPanel getPanel(Integer index)
		{ return index==null ? null : panels.get(index); }
    
    /** Returns the current index */
    public Integer getCurrentIndex()
    	{ return currentIndex; }
    
	/** Determine which index to move back to */
	public Integer getBackIndex()
	{
		// Return no pane if currently viewing first pane
		if(currentIndex==0) return null;
		
		// Otherwise, identify the next pane backward
		int backIndex = currentIndex-1;
		HashSet<Matcher> matchers = new HashSet<Matcher>(getSelectedMatchers());
		while(backIndex>=0)
		{
			WizardPanel panel = panels.get(backIndex);
			if(!(panel instanceof MatcherParametersPane)) return backIndex;
			if(inAdvancedMode())
				if(matchers.contains(((MatcherParametersPane)panel).getMatcher())) return backIndex;
			backIndex--;
		}
		return null;
	}

	/** Determine which index to move to next */
	public Integer getNextIndex()
	{
		int nextIndex = currentIndex+1;
		HashSet<Matcher> matchers = new HashSet<Matcher>(getSelectedMatchers());
		while(nextIndex<panels.size())
		{
			WizardPanel panel = panels.get(nextIndex);
			if(!(panel instanceof MatcherParametersPane)) return nextIndex;
			if(inAdvancedMode())
				if(matchers.contains(((MatcherParametersPane)panel).getMatcher())) return nextIndex;
			nextIndex++;
		}
		return null;
	}

	/** Switch current panel */
	public void setCurrentPanel(Integer newIndex)
	{
		// Informs old panel that it is about to be hidden
	    WizardPanel oldPanel = getPanel(currentIndex);
	    oldPanel.aboutToHidePanel();
	
	    // Informs the new panel that it is about to be displayed
	    WizardPanel newPanel = getPanel(currentIndex = newIndex);
	    buttonPane.updateButtons();
	    newPanel.aboutToDisplayPanel();
	
	    // Informs the new panel that it is being displayed
	    ((CardLayout)cardPanel.getLayout()).show(cardPanel, newPanel.getID());
	    newPanel.displayingPanel();
	}

    /** Indicates if in advanced mode */
    public boolean inAdvancedMode()
    {
    	for(WizardPanel panel : panels)
    		if(panel instanceof MatcherSelectionPane)
    			return ((MatcherSelectionPane)panel).inAdvancedMode();
    	return false;
    }

    /** Returns the selected matchers */
    public ArrayList<Matcher> getSelectedMatchers()
    {
    	for(WizardPanel panel : panels)
    		if(panel instanceof MatcherSelectionPane)
    			return ((MatcherSelectionPane)panel).getSelectedMatchers();
    	return new ArrayList<Matcher>();
    }

    /** Returns the selected match types */
    public MatchTypeMappings getSelectedMatchTypes()
    {
    	for(WizardPanel panel : panels)
    		if(panel instanceof MatchTypePane)
    			return ((MatchTypePane)panel).getMatchTypeMappings();
    	return null;
    }
}