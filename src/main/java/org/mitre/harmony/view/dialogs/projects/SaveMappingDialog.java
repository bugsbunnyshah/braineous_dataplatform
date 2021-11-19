// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs.projects;

import java.awt.BorderLayout;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mitre.harmony.controllers.ProjectController;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.dialogs.widgets.AbstractButtonPane;

/**
 * Class used for the saving the current project
 * @author CWOLF
 */
public class SaveMappingDialog extends JInternalFrame implements ListSelectionListener
{	
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** Stores the project pane */
	private ProjectPane projectPane = null;
	
	/** Stores the info pane */
	private InfoPane infoPane = null;

	/** Private class for defining the button pane */
	private class ButtonPane extends AbstractButtonPane
	{
		/** Constructs the button pane */
		public ButtonPane()
			{ super(new String[]{"Save", "Cancel"},1,2); }

		/** Handles selection of button */
		protected void buttonPressed(String label)
		{
			if(label.equals("Save"))
			{
				if(infoPane.validateInfo())
				{
					ProjectController.saveProject(harmonyModel,projectPane.getProject());
					dispose();
				}
			}
			else dispose();
		}
	}
	
	/** Generates the main pane of the dialog for saving projects */
	private JPanel getMainPane()
	{
		// Initialize the project pane
		projectPane = new ProjectPane(true,harmonyModel);
		projectPane.addListSelectionListener(this);

		//Initialize the info pane
		infoPane = new InfoPane(true, harmonyModel);
		infoPane.setInfo(projectPane.getProject());
		
		// Creates the main pane
		JPanel mainPane = new JPanel();
		mainPane.setBorder(new EmptyBorder(0,0,4,0));
		mainPane.setLayout(new BorderLayout());
		mainPane.add(projectPane,BorderLayout.WEST);
		mainPane.add(infoPane,BorderLayout.CENTER);
	    
		// Place list of roots in center of project pane
		JPanel pane = new JPanel();
		pane.setBorder(new EmptyBorder(15,15,10,15));
		pane.setLayout(new BorderLayout());
		pane.add(mainPane,BorderLayout.CENTER);
		pane.add(new ButtonPane(),BorderLayout.SOUTH);
		return pane;
	}

	/** Constructs the dialog for saving projects */
	public SaveMappingDialog(HarmonyModel harmonyModel)
	{
		super("Save Project As");
		this.harmonyModel = harmonyModel;
		setContentPane(getMainPane());
		pack();
		setVisible(true);
	}

	/** Handles a change to the selected project list */
	public void valueChanged(ListSelectionEvent e)
		{ infoPane.setInfo(projectPane.getProject()); }
}