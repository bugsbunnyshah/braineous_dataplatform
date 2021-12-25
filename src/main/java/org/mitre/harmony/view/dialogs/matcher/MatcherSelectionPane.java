package org.mitre.harmony.view.dialogs.matcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.mitre.harmony.matchers.MatcherManager;
import org.mitre.harmony.matchers.matchers.Matcher;
import org.mitre.harmony.view.dialogs.matcher.wizard.Wizard;
import org.mitre.harmony.view.dialogs.matcher.wizard.WizardPanel;

/** Constructs the matcher pane for the matcher wizard */
public class MatcherSelectionPane extends WizardPanel
{
	/** Class for storing matcher check box items */
	private class MatchersCheckBox extends JCheckBox 
	{
		/** Stores the matcher associated with this check box */
		private Matcher matcher;

		/** initializes the match matcher check box */
		private MatchersCheckBox(Matcher matcher)
		{
			this.matcher = matcher;
			setText(matcher.getName());
			setBackground(Color.white);
			setFocusable(false);
		}
	}
	
	/** Pane which displays all of the matcher check boxes */
	private JPanel checkboxPane = null;

	/** Check box indicating if advanced mode should be run */
	private JCheckBox advancedCheckbox = null;

	/** Constructs the matcher pane */
    public MatcherSelectionPane(Wizard wizard, ArrayList<Matcher> matchers)
    {
    	super(wizard);

    	// Create pane for storing all matchers
		checkboxPane = new JPanel();
		checkboxPane.setBackground(Color.white);
		checkboxPane.setLayout(new BoxLayout(checkboxPane,BoxLayout.Y_AXIS));

		// Populate the matchers into the check list
		for (Matcher matcher : MatcherManager.getVisibleMatchers())
		{
			MatchersCheckBox checkbox = new MatchersCheckBox(matcher);
			checkbox.setSelected(matchers.contains(matcher));
			checkboxPane.add(checkbox);
		}

    	// Create the advanced mode check box
    	advancedCheckbox = new JCheckBox("Select advanced matcher options");
    	advancedCheckbox.setFocusable(false);
    	advancedCheckbox.setBorder(new EmptyBorder(8,0,0,0));
		
		// Place list of matchers in center of pane
		setBorder(new EmptyBorder(20,20,20,20));
		setLayout(new BorderLayout());
		add(new JLabel("Please select which matchers to use:"),BorderLayout.NORTH);
		add(new JScrollPane(checkboxPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
		add(advancedCheckbox, BorderLayout.SOUTH);
    }

	/** Returns the list of matchers */
	public ArrayList<Matcher> getSelectedMatchers()
	{
		ArrayList<Matcher> selectedMatchers = new ArrayList<Matcher>();
		for(Component checkbox : checkboxPane.getComponents())
			if(((MatchersCheckBox)checkbox).isSelected())
				selectedMatchers.add(((MatchersCheckBox)checkbox).matcher);
		return selectedMatchers;
	}

	/** Indicates if running in advanced mode */
	public boolean inAdvancedMode()
		{ return advancedCheckbox.isSelected(); }
}