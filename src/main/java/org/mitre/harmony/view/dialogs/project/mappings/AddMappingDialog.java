// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs.project.mappings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.dialogs.widgets.AbstractButtonPane;
import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.Schema;

/**
 * Displays the dialog used for adding mappings
 * @author CWOLF
 */
class AddMappingDialog extends JInternalFrame implements ActionListener
{	
	/** Stores the list of schemas associated with the project */
	private ArrayList<Schema> schemas;
	
	/** Stores the list of mappings associated with the project */
	private ArrayList<Mapping> mappings;
	
	// Stores the source and target selection controls
	private JComboBox sourceSchema = new JComboBox();
	private JComboBox targetSchema = new JComboBox();	
	
	/** Private class for defining the button pane */
	private class ButtonPane extends AbstractButtonPane
	{
		/** Constructs the button pane */
		private ButtonPane()
			{ super(new String[]{"OK", "Cancel"},1,2); }

		/** Handles pressing of "OK" or "Cancel" button */
		protected void buttonPressed(String label)
		{
			// Clears out selection if canceled
			if(label.equals("Cancel"))
			{
				sourceSchema.setSelectedItem(null);
				targetSchema.setSelectedItem(null);
			}
			
			// Close down the "Add Mapping" pane
			dispose();
		}
	}
	
	/** Update the schema lists */
	private void updateSchemas(JComboBox schemaList)
	{		
		// Identify the selected item
		Schema selectedItem = null;
		if(schemaList.getSelectedItem()!=null)
			selectedItem = (Schema)schemaList.getSelectedItem();
		
		// Identify the item selected in the other list
		Integer otherID = null;
		JComboBox otherList = (schemaList==sourceSchema ? targetSchema : sourceSchema);
		if(otherList.getSelectedItem()!=null)
			otherID = ((Schema)otherList.getSelectedItem()).getId();
		
		// Construct the list of prohibited schemas
		HashSet<Integer> prohibitedSchemas = new HashSet<Integer>();
		if(otherID!=null) prohibitedSchemas.add(otherID);
		for(Mapping mapping : mappings)
		{
			if(schemaList==sourceSchema && mapping.getTargetId().equals(otherID))
				prohibitedSchemas.add(mapping.getSourceId());
			if(schemaList==targetSchema && mapping.getSourceId().equals(otherID))
				prohibitedSchemas.add(mapping.getTargetId());			
		}
		
		// Update schema list
		schemaList.removeAllItems();
		for(Schema schema : schemas)
			if(!prohibitedSchemas.contains(schema.getId()))
				schemaList.addItem(schema);
		
		// Reselects the currently selected item
		schemaList.setSelectedItem(selectedItem);
	}
	
	/** Generates the info pane */
	private JPanel getInfoPane()
	{
		// Initialize the source and target schema lists
		sourceSchema.setPreferredSize(new Dimension(120,20));
		targetSchema.setPreferredSize(new Dimension(120,20));
		updateSchemas(sourceSchema);
		updateSchemas(targetSchema);
		
		// Generates the source pane
		JPanel sourcePane = new JPanel();
		sourcePane.setLayout(new BoxLayout(sourcePane,BoxLayout.X_AXIS));
		sourcePane.add(new JLabel("Source: "));
		sourcePane.add(sourceSchema);
		
		// Generates the target pane
		JPanel targetPane = new JPanel();
		targetPane.setBorder(new EmptyBorder(4,4,0,0));
		targetPane.setLayout(new BoxLayout(targetPane,BoxLayout.X_AXIS));
		targetPane.add(new JLabel("Target: "));
		targetPane.add(targetSchema);
		
		// Generate the info pane
		JPanel pane = new JPanel();
		pane.setBorder(new EmptyBorder(10,10,0,10));
		pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));
		pane.add(sourcePane);
		pane.add(targetPane);

		// Add listeners to the schema lists
		sourceSchema.addActionListener(this);
		targetSchema.addActionListener(this);
		
		return pane;
	}
	
	/** Initializes the "Add Mapping" dialog */
	AddMappingDialog(HarmonyModel harmonyModel, ArrayList<Schema> schemas, ArrayList<Mapping> mappings)
	{
		super("Add Mapping");
		this.schemas = schemas;
		this.mappings = mappings;
		
		// Generate the main dialog panex
		JPanel pane = new JPanel();
		pane.setBorder(BorderFactory.createLineBorder(Color.black));
		pane.setLayout(new BorderLayout());
		pane.add(getInfoPane(),BorderLayout.CENTER);
		pane.add(new ButtonPane(),BorderLayout.SOUTH);
		
		// Initialize the dialog parameters
		setContentPane(pane);
		pack();
		setVisible(true);
	}
	
	/** Returns the generated mapping */
	Mapping getMapping()
	{
		// Retrieve the selected source and target
		Object source = sourceSchema.getSelectedItem();
		Object target = targetSchema.getSelectedItem();
		if(source==null || target==null) return null;
		
		// Return the mapping
		return new Mapping(null,null,((Schema)source).getId(),((Schema)target).getId());
	}

	/** Handles a change to one of the schema lists */
	public void actionPerformed(ActionEvent e)
		{ updateSchemas(e.getSource()==sourceSchema ? targetSchema : sourceSchema); }
}