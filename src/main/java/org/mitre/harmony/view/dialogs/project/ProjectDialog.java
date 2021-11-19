// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs.project;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.dialogs.project.mappings.MappingPane;
import org.mitre.harmony.view.dialogs.project.schemas.SchemaPane;
import org.mitre.harmony.view.dialogs.widgets.AbstractButtonPane;

/**
 * Displays the project dialog
 * @author CWOLF
 */
public class ProjectDialog extends JInternalFrame
{
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	// Stores the various panes used in this pane
	private SchemaPane schemaPane = null;
	private MappingPane mappingPane = null;
	private ButtonPane buttonPane = null;
	
	/** Private class for defining the button pane */
	private class ButtonPane extends AbstractButtonPane
	{
		/** Constructs the button pane */
		public ButtonPane()
			{ super(new String[]{"OK", "Cancel"},1,2); }

		/** Handles selection of button */
		protected void buttonPressed(String label)
		{
			if(label.equals("OK"))
			{
				if(mappingPane.getMappings().size()==0)
				{
					int reply = JOptionPane.showConfirmDialog(null,"No mappings have been defined.  Proceed?","Project Failure",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
					if(reply==JOptionPane.NO_OPTION) return;
				}
				save();
			}
			dispose();
		}
	}
	
	/** Initializes the project dialog */
	public ProjectDialog(HarmonyModel harmonyModel)
	{
		super("Project Configuration");
		this.harmonyModel = harmonyModel;
		
		// Constructs the tabbed panes
		JPanel infoPane = new JPanel();
		infoPane.setLayout(new GridLayout(2,1));
		infoPane.add(schemaPane = new SchemaPane(harmonyModel));
		infoPane.add(mappingPane = new MappingPane(this));
		
		// Constructs the content pane 
		JPanel pane = new JPanel();
		pane.setBorder(new EmptyBorder(10,10,0,10));
		pane.setLayout(new BorderLayout());
		pane.add(infoPane,BorderLayout.CENTER);
		pane.add(buttonPane = new ButtonPane(),BorderLayout.SOUTH);
		
		// Set up loader dialog layout and contents
		setContentPane(pane);
		pack();
		setSize(550,getHeight());
		setVisible(true);
   	}
	
	/** Handles the enabling of components in this dialog */
	public void setEnabled(boolean enabled)
	{
		schemaPane.setEnabled(enabled);
		mappingPane.setEnabled(enabled);
		buttonPane.setEnabled(enabled);
	}
	
	/** Returns the org.org.mitre.harmony model */
	public HarmonyModel getHarmonyModel()
		{ return harmonyModel; }
	
	/** Returns the schema pane */
	public SchemaPane getSchemaPane()
		{ return schemaPane; }

	/** Returns the mapping pane */
	public MappingPane getMappingPane()
		{ return mappingPane; }
	
	/** Saves the project */
	void save()
		{ schemaPane.save(); mappingPane.save(); }
}
