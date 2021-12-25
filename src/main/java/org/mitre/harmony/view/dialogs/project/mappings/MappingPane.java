// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs.project.mappings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.dialogs.project.ProjectDialog;
import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.Schema;

/**
 * Displays the mapping pane for defining a project
 * @author CWOLF
 */
public class MappingPane extends JPanel implements ActionListener, InternalFrameListener
{	
	/** Stores the Project dialog */
	private ProjectDialog projectDialog;
	
	// Stores the various panes used in this pane
	private MappingSelectionPane mappingSelectionPane = null;
	private JButton button = null;
	
	/** Initializes the Mapping pane */
	public MappingPane(ProjectDialog projectDialog)
	{
		this.projectDialog = projectDialog;
		
		// Initializes the mapping selection and display panes
		mappingSelectionPane = new MappingSelectionPane(projectDialog.getHarmonyModel());

		// Initializes the selection pane
		mappingSelectionPane.setPreferredSize(new Dimension(300,0));
		
		// Create the import schema button
		button = new JButton("Add Mapping");
		button.setFocusable(false);
		//button.setEnabled(projectDialog.getHarmonyModel().getInstantiationType()!=InstantiationType.EMBEDDED);
		button.addActionListener(this);
		
		// Constructs the mapping pane
		setBorder(new CompoundBorder(new TitledBorder("Mappings (select to display)"),new EmptyBorder(5,5,5,5)));
		setLayout(new BorderLayout());
		add(mappingSelectionPane,BorderLayout.CENTER);
		add(button,BorderLayout.SOUTH);
   	}
	
	/** Handles the enabling of components in this dialog */
	public void setEnabled(boolean enabled)
		{ button.setEnabled(enabled); }
	
	/** Returns the declared mappings */
	public ArrayList<Mapping> getMappings()
		{ return mappingSelectionPane.getMappings(); }
	
	/** Handles the pressing of the "Add Mapping" button */
	public void actionPerformed(ActionEvent e)
	{
		// Get the ProjectDialog pane
		Component component = getParent();
		while(!(component instanceof ProjectDialog))
			component = component.getParent();
		
		// Get the current schemas and mappings
		ArrayList<Schema> schemas = projectDialog.getSchemaPane().getSchemas();
		ArrayList<Mapping> mappings = mappingSelectionPane.getMappings();

		// Run the dialog to add a mapping
		HarmonyModel harmonyModel = projectDialog.getHarmonyModel();
		AddMappingDialog dialog = new AddMappingDialog(harmonyModel, schemas, mappings);
		//harmonyModel.getDialogManager().openDialog(dialog);
		dialog.addInternalFrameListener(this);
	}
	
	/** Saves the project mappings */
	public void save()
		{ mappingSelectionPane.save(); }
	
	/** Updates the schema list when the schema dialog is closed */
	public void internalFrameClosed(InternalFrameEvent e)
	{
		Mapping mapping = ((AddMappingDialog)e.getInternalFrame()).getMapping();
		if(mapping!=null) mappingSelectionPane.addMapping(mapping);
	}

	// Unused event listeners
	public void internalFrameOpened(InternalFrameEvent e) {}
	public void internalFrameClosing(InternalFrameEvent e) {}
	public void internalFrameIconified(InternalFrameEvent e) {}
	public void internalFrameDeiconified(InternalFrameEvent e) {}
	public void internalFrameActivated(InternalFrameEvent e) {}
	public void internalFrameDeactivated(InternalFrameEvent e) {}
}