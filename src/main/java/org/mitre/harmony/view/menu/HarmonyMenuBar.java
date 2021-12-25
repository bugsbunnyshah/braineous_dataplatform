// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;

import org.mitre.harmony.model.HarmonyConsts;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.dialogs.SelectionDialog;

/**
 * Displays all menu bar choices in Harmony
 * @author CWOLF
 */
public class HarmonyMenuBar extends JMenuBar
{	
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;	

	/** Drop-down menu found under edit menu bar heading */
	private class EditMenu extends AbstractMenu
	{
		/** Initializes the edit drop-down menu */
		private EditMenu()
		{
			// Gives the drop-down menu the title of "Edit"
			super("Edit");
		    setMnemonic(KeyEvent.VK_E);
			
			// Add edit drop-down items to edit drop-down menu
			add(createMenuItem("Select Links...", KeyEvent.VK_S, new SelectLinksAction()));
			add(createMenuItem("Remove Links...", KeyEvent.VK_R, new RemoveLinksAction()));
		}
	    
		/** Action for selecting links */
		private class SelectLinksAction extends AbstractAction
		{
			public void actionPerformed(ActionEvent e)
			{ 
				SelectionDialog dialog = new SelectionDialog(harmonyModel,SelectionDialog.SELECT);
				//harmonyModel.getDialogManager().openDialog(dialog);
			}
		}
	    
		/** Action for removing links */
		private class RemoveLinksAction extends AbstractAction
		{
			public void actionPerformed(ActionEvent e)
			{
				SelectionDialog dialog = new SelectionDialog(harmonyModel,SelectionDialog.DELETE);
				//harmonyModel.getDialogManager().openDialog(dialog);
			}
		}
	}

	/** Drop-down menu found under view menu bar heading */
	private class ViewMenu extends AbstractMenu
	{
		/** Initializes the view drop-down menu */
		private ViewMenu()
		{
			super("View");
			setMnemonic(KeyEvent.VK_V);
	
			// Add view items to view drop-down menu
		    add(createCheckboxItem("Alphabetize", harmonyModel.getPreferences().getAlphabetized(), new AlphabetizeAction()));
		    add(createCheckboxItem("Show Types", harmonyModel.getPreferences().getShowSchemaTypes(), new ShowTypesAction()));
		    add(createCheckboxItem("Show Cardinality", harmonyModel.getPreferences().getShowCardinality(), new ShowCardinalityAction()));
		}
	    
		/** Action for alphabetizing sibling elements */
		private class AlphabetizeAction extends AbstractAction
		{
			public void actionPerformed(ActionEvent e)
			{
				boolean isSelected = ((JCheckBoxMenuItem)(e.getSource())).isSelected();
				harmonyModel.getPreferences().setAlphabetized(isSelected);
			}
		}
		 /** Action for showing cardinality */
		/** Action for alphabetizing sibling elements */
		private class ShowCardinalityAction extends AbstractAction
		{
			public void actionPerformed(ActionEvent e)
			{
				boolean isSelected = ((JCheckBoxMenuItem)(e.getSource())).isSelected();
				harmonyModel.getPreferences().setShowCardinality(isSelected);
			}
		}

		/** Action for showing types */
		private class ShowTypesAction extends AbstractAction
		{
			public void actionPerformed(ActionEvent e)
			{
				boolean isSelected = ((JCheckBoxMenuItem)(e.getSource())).isSelected();			
				harmonyModel.getPreferences().setShowSchemaTypes(isSelected);
			}
		}
	}

	/** Drop-down menu found under search menu bar heading */
	private class SearchMenu extends AbstractMenu implements ActionListener
	{
		private JRadioButtonMenuItem highlightFocusArea;	// Option to highlight focus area
		private JRadioButtonMenuItem highlightAll;			// Option to highlight all
		
		/** Initializes the search drop-down menu */
		private SearchMenu()
		{
			super("Search");
			setMnemonic(KeyEvent.VK_V);

			// Initialize view drop-down items
			highlightFocusArea = new JRadioButtonMenuItem("Highlight Focus Area",true);
			highlightAll = new JRadioButtonMenuItem("Highlight All");
			
			// Groups the radio buttons together
			ButtonGroup group = new ButtonGroup();
			group.add(highlightFocusArea);
			group.add(highlightAll);
			highlightAll.setSelected(true);
			
			// Attach action listeners to view drop-down items
			highlightFocusArea.addActionListener(this);
			highlightAll.addActionListener(this);

			// Add view drop-down items to view drop-down menu
			add(createMenuItem("Clear Results", KeyEvent.VK_C, new ClearResultsAction()));
			add(createMenuItem("Search...", KeyEvent.VK_S, new SearchAction()));
			addSeparator();
		    add(highlightFocusArea);
		    add(highlightAll);
		}
		
		/** Handles the selection of a highlight area */
	    public void actionPerformed(ActionEvent e)
	    {
	    	Object source = e.getSource();
	    	if(source==highlightFocusArea || source==highlightAll)
	    		harmonyModel.getSearchManager().setHighlightAll(source==highlightAll);
	    }
		
		/** Action for clearing the search results */
		private class ClearResultsAction extends AbstractAction
		{
			public void actionPerformed(ActionEvent e)
			{
	    		harmonyModel.getSearchManager().runQuery(HarmonyConsts.LEFT, "");
	    		harmonyModel.getSearchManager().runQuery(HarmonyConsts.RIGHT, "");
			}
		}
		
		/** Action for displaying the "Search" dialog */
		private class SearchAction extends AbstractAction
		{
			public void actionPerformed(ActionEvent e)
				{
					//harmonyModel.getDialogManager().openDialog(new SearchDialog(harmonyModel));
				}
		}
	}
	
	/** Drop-down menu found under help menu bar heading */
	private class HelpMenu extends AbstractMenu
	{
		/** Initializes the help drop-down menu */
		private HelpMenu()
		{
			// Gives the drop-down menu the title of "Help"
			super("Help");
		    setMnemonic(KeyEvent.VK_H);
			
			// Add help drop-down items to help drop-down menu
		    add(createMenuItem("About Harmony", KeyEvent.VK_A, new AboutAction()));
		    add(createMenuItem("Getting Started", KeyEvent.VK_G, new GettingStartedAction()));
		}
		
		/** Action for displaying the "About" dialog */
		private class AboutAction extends AbstractAction
		{
			public void actionPerformed(ActionEvent e)
				{
					//harmonyModel.getDialogManager().openDialog(new AboutDialog(harmonyModel));
				}
		}

		/** Action for displaying the "Getting Started" dialog */
		private class GettingStartedAction extends AbstractAction
		{
			public void actionPerformed(ActionEvent e)
				{
					//harmonyModel.getDialogManager().openDialog(new GettingStartedDialog(harmonyModel));
				}
		}
	}
	
	/** Initializes the Harmony menu bar */
	public HarmonyMenuBar(HarmonyModel harmonyModel)
	{
		this.harmonyModel = harmonyModel;
	    add(new ProjectMenu(harmonyModel));	
	    add(new EditMenu());
	    add(new SearchMenu());
	    add(new MatcherMenu(harmonyModel));
	    add(new ViewMenu());
	    add(new HelpMenu());
	}
}
