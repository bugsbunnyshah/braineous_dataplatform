// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.mitre.harmony.matchers.MatcherManager;
import org.mitre.harmony.matchers.matchers.Matcher;
import org.mitre.harmony.matchers.mergers.MatchMerger;
import org.mitre.harmony.matchers.mergers.VoteMerger;
import org.mitre.harmony.model.HarmonyConsts;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.project.ProjectManager;

/** Menu to display all available matchers */
public class MatcherMenu extends AbstractMenu
{
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;

	/** Initializes the matcher menu to display all installed matchers */
	public MatcherMenu(HarmonyModel harmonyModel)
	{
		super("Matchers");
		this.harmonyModel = harmonyModel;
		setMnemonic(KeyEvent.VK_M);

		// Place all matchers into menu
		for(MatchMerger merger : MatcherManager.getMergers())
			add(new MergerMenuItem(merger));

		// Place all matchers into menu
		for(Matcher matcher : MatcherManager.getVisibleMatchers())
			add(new MatcherMenuItem(matcher));

		// Add a menu checkbox to specify if user matched elements should be ignored in matching
		addSeparator();
		add(createCheckboxItem("Ignore Validated Elements", harmonyModel.getPreferences().getIgnoreMatchedElements(), new IgnoreMatchedElementsAction()));
	}

	/** Class for handling matcher menu items */
	private class MatcherMenuItem extends JMenuItem implements ActionListener
	{
		// Stores the matcher associated with this menu item
		private Matcher matcher;

		/** Initializes the matcher menu item */
		MatcherMenuItem(Matcher matcher)
		{
			this.matcher = matcher;
			setText(matcher.getName());
			addActionListener(this);
		}

		/** Handles the selection of this matcher */
		public void actionPerformed(ActionEvent e)
		{
			if(!checkSchemasExist()) return;

			ArrayList<Matcher> matchers = new ArrayList<Matcher>();
			matchers.add(matcher);
			launchMatchWizard(matchers, new VoteMerger(), false);
		}
	}

	/** Class for handling merger menu */
	private class MergerMenuItem extends JMenu implements ActionListener
	{
		// Stores the matcher associated with this menu
		private MatchMerger merger;

		// Stores the menu items which may be selected
		JMenuItem fullMatch = new JMenuItem("Run All Matchers");
		JMenuItem customMatch = new JMenuItem("Run Custom Matchers...");

		/** Initializes the merger menu */
		MergerMenuItem(MatchMerger merger)
		{
			this.merger = merger;
			setText(merger.getName());

			// Creates a full merger menu item
			fullMatch.addActionListener(this);
			add(fullMatch);

			// Creates a custom match menu item
			customMatch.addActionListener(this);
			add(customMatch);
		}

		/** Handles the selection of this merger */
		public void actionPerformed(ActionEvent e)
		{
			if(!checkSchemasExist()) { return; }

			// Run the matcher (through the matcher wizard)
			if(e.getSource() == fullMatch)
				launchMatchWizard(MatcherManager.getDefaultMatchers(), merger, false);
			else if (e.getSource() == customMatch)
				launchMatchWizard(MatcherManager.getDefaultMatchers(), merger, true);
		}
	}

	/** Make sure that schema actually exist to be merged */
	private boolean checkSchemasExist()
	{
		// Check to see how many schemas exist on each side of the project
		ProjectManager projectManager = harmonyModel.getProjectManager();
		Integer leftSchemas = projectManager.getSchemaElements(HarmonyConsts.LEFT).size();
		Integer rightSchemas = projectManager.getSchemaElements(HarmonyConsts.RIGHT).size();

		// No need to proceed if no schemas exist on a specific side
		if(leftSchemas.equals(0) || rightSchemas.equals(0))
		{
			JOptionPane.showInternalMessageDialog(getParent(), "No schemas are currently open to match.", "Matching Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}

	/** Launches the matcher wizard */
	private void launchMatchWizard(ArrayList<Matcher> matchers, MatchMerger merger, boolean custom)
		{
			//harmonyModel.getDialogManager().openDialog(new Wizard(matchers, merger, custom, harmonyModel));
		}

	/** Action for ignoring the matched elements */
	private class IgnoreMatchedElementsAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			boolean isSelected = ((JCheckBoxMenuItem)(e.getSource())).isSelected();			
			harmonyModel.getPreferences().setIgnoredMatchedElements(isSelected);
		}
	}
}