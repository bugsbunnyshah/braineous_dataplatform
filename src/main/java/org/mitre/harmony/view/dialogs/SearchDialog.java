// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.mitre.harmony.controllers.FocusController;
import org.mitre.harmony.model.HarmonyConsts;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.dialogs.widgets.AbstractButtonPane;
import org.mitre.harmony.view.dialogs.widgets.OptionPane;

/**
 * Displays the search dialog for search for keywords in schemas
 * @author CWOLF
 */
public class SearchDialog extends JInternalFrame implements KeyListener
{
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** Stores the search field */
	private JTextField searchField = new JTextField();
	
	/** Stores the search side */
	private OptionPane searchSide = null;
	
	/** Private class for defining the button pane */
	private class ButtonPane extends AbstractButtonPane
	{
		/** Constructs the button pane */
		private ButtonPane()
			{ super(new String[]{"Set Focus", "Search", "Add Focus", "Clear Search", "Remove Foci", "Close"},3,2); }

		/** Handles selection of button */
		protected void buttonPressed(String label)
		{		
			// Close down the search pane if "Close" selected
			if(label.equals("Close")) { dispose(); return; }

			// Perform operations on specified sides
			for(Integer side : getSelectedSides())
			{			
				// Run a search if "Search" selected
				if(label.equals("Search") || label.equals("Set Focus") || label.equals("Add Focus"))
				{
					// Run search on specified query
					harmonyModel.getSearchManager().runQuery(side, searchField.getText());
	
					// Set focus areas on search results
					if(label.equals("Set Focus") || label.equals("Add Focus"))
					{
						boolean append = label.equals("Add Focus");
						FocusController.setFocusOnSearchResults(harmonyModel, side, append);
					}
				}
		
				// Clear the search terms if "Clear Search" selected
				else if(label.equals("Clear Search"))
					harmonyModel.getSearchManager().runQuery(side, "");
			
				// Remove the foci if "Remove Foci" selected
				else if(label.equals("Remove Foci"))
					harmonyModel.getFilters().removeAllFoci(side);
			}
		}
	}
		
	/** Generates the info pane */
	private JPanel getInfoPane()
	{
		// Generate the search label
		JLabel searchLabel = new JLabel("Search: ");
		searchLabel.setBorder(new EmptyBorder(0,0,15,0));
		
		// Generate the regular expression label
		JLabel regExpLabel = new JLabel("(regular expressions permitted)");
		regExpLabel.setFont(new Font("Default",Font.PLAIN,9));
		
		// Add a key listener to the search field
		searchField.addKeyListener(this);
		
		// Generates the search field pane
		JPanel searchFieldPane = new JPanel();
		searchFieldPane.setLayout(new BorderLayout());
		searchFieldPane.add(searchField,BorderLayout.NORTH);
		searchFieldPane.add(regExpLabel,BorderLayout.SOUTH);
		
		// Generates the search pane
		JPanel searchPane = new JPanel();
		searchPane.setLayout(new BoxLayout(searchPane,BoxLayout.X_AXIS));
		searchPane.add(searchLabel);
		searchPane.add(searchFieldPane);
		
		// Generates the side selection options
		searchSide = new OptionPane("Side",new String[]{"Left","Right","Both"});
		searchSide.setSelectedButton("Both");
		
		// Generate the info pane
		JPanel pane = new JPanel();
		pane.setBorder(new EmptyBorder(10,10,0,10));
		pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));
		pane.add(searchPane);
		pane.add(searchSide);
		return pane;
	}
	
	/** Initializes the search dialog */
	public SearchDialog(HarmonyModel harmonyModel)
	{
		super("Search");
		this.harmonyModel = harmonyModel;
		
		// Generate the main dialog pane
		JPanel pane = new JPanel();
		pane.setBorder(BorderFactory.createLineBorder(Color.black));
		pane.setLayout(new BorderLayout());
		pane.add(getInfoPane(),BorderLayout.CENTER);
		pane.add(new ButtonPane(),BorderLayout.SOUTH);
		
		// Initialize the dialog parameters
    	setClosable(true);
		setContentPane(pane);
		pack();
		setVisible(true);
	}
	
	/** Returns the selected sides */
	private ArrayList<Integer> getSelectedSides()
	{
		ArrayList<Integer> sides = new ArrayList<Integer>();
		String side = searchSide.getSelectedButton();
		if(!side.equals("Right")) sides.add(HarmonyConsts.LEFT);
		if(!side.equals("Left")) sides.add(HarmonyConsts.RIGHT);
		return sides;
	}
	
	/** Update matches every time a new search keyword is entered */
	public void keyTyped(KeyEvent e)
	{
		if(e.getKeyChar() == KeyEvent.VK_ENTER)
			for(Integer side : getSelectedSides())
				harmonyModel.getSearchManager().runQuery(side, searchField.getText());
	}
	
	/** Unused listener actions */
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
}