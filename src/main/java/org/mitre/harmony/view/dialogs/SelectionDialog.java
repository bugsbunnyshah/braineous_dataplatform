// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.mitre.harmony.controllers.MappingController;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.dialogs.widgets.AbstractButtonPane;
import org.mitre.harmony.view.dialogs.widgets.OptionPane;

/**
 * Displays the search dialog for search for keywords in schemas
 * @author CWOLF
 */
public class SelectionDialog extends JInternalFrame
{
	// Constants for defining the selection dialog mode
	static public final Integer SELECT = 0;
	static public final Integer DELETE = 1;
	
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** Stores the dialog mode */
	private Integer mode;
	
	// Stores the filters
	private OptionPane typeFilter=null, focusFilter=null, visibilityFilter=null;
	
	/** Private class for defining the button pane */
	private class ButtonPane extends AbstractButtonPane
	{
		/** Constructs the button pane */
		private ButtonPane()
			{ super(new String[]{"OK","Cancel"},1,2); }

		/** Handles selection of button */
		protected void buttonPressed(String label)
		{
			if(label.equals("OK"))
			{
				// Retrieve the mapping cell type setting
				String type = typeFilter.getSelectedButton();
				Integer typeSetting = type.equals("All") ? MappingController.ALL : type.equals("System") ? MappingController.SYSTEM : MappingController.USER;
				
				// Retrieve the mapping cell focus setting
				String focus = focusFilter.getSelectedButton();
				Integer focusSetting = focus.equals("All") ? MappingController.ALL : focus.equals("Focused") ? MappingController.FOCUSED : MappingController.UNFOCUSED;
				
				// Retrieve the mapping cell visibility setting
				String visibility = visibilityFilter.getSelectedButton();
				Integer visibilitySetting = visibility.equals("All") ? MappingController.ALL : visibility.equals("Visible") ? MappingController.VISIBLE : MappingController.HIDDEN;
				
				// Select or delete mapping cells
				if(mode.equals(SELECT))
					MappingController.selectMappingCells(harmonyModel, typeSetting, focusSetting, visibilitySetting);
				else MappingController.deleteMappingCells(harmonyModel, typeSetting, focusSetting, visibilitySetting);
			}
			dispose();
		}
	}
	
	/** Initializes the search dialog */
	public SelectionDialog(HarmonyModel harmonyModel, Integer mode)
	{
		super((mode==SELECT ? "Select" : "Remove") + " Links");
		this.harmonyModel = harmonyModel;
		this.mode = mode;
		
		// Initialize the type filter
		typeFilter = new OptionPane("Type",new String[]{"All","User","System"});
		typeFilter.setBorder(new EmptyBorder(0,20,0,0));
		typeFilter.setSelectedButton("All");
		
		// Initialize the focus filter
		focusFilter = new OptionPane("Focus",new String[]{"All","Focused","Unfocused"});
		focusFilter.setBorder(new EmptyBorder(0,13,0,0));
		focusFilter.setSelectedButton(mode==SELECT ? "Focused" : "All");
		focusFilter.setEnabled(mode==DELETE);

		// Initialize the visibility filter
		visibilityFilter = new OptionPane("Visibility",new String[]{"All","Visible","Hidden"});		
		visibilityFilter.setSelectedButton(mode==SELECT ? "Visible" : "All");
		visibilityFilter.setEnabled(mode==DELETE);
		
		// Create the info pane
		JPanel infoPane = new JPanel();
		infoPane.setBorder(new CompoundBorder(new EmptyBorder(5,5,0,5),new CompoundBorder(new LineBorder(Color.gray),new EmptyBorder(5,5,5,5))));
		infoPane.setLayout(new GridLayout(3,1));
		infoPane.add(typeFilter);
		infoPane.add(focusFilter);
		infoPane.add(visibilityFilter);
		
		// Generate the main dialog pane
		JPanel pane = new JPanel();
		pane.setBorder(BorderFactory.createLineBorder(Color.black));
		pane.setLayout(new BorderLayout());
		pane.add(infoPane,BorderLayout.CENTER);
		pane.add(new ButtonPane(),BorderLayout.SOUTH);
		
		// Initialize the dialog parameters
		setContentPane(pane);
		setSize(200,250);
		pack();
		setVisible(true);
	}
}