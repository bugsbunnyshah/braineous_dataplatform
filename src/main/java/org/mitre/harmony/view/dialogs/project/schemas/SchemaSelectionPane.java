package org.mitre.harmony.view.dialogs.project.schemas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.dialogs.project.ProjectDialog;
import org.mitre.harmony.view.dialogs.schema.SchemaDialog;
import org.mitre.schemastore.model.Schema;

/** Class for allowing the selection of schemas */
class SchemaSelectionPane extends JPanel implements ActionListener, InternalFrameListener
{
	/** Comparator used to alphabetize schemas */
	private class SchemaComparator implements Comparator<Schema>
	{
		public int compare(Schema schema1, Schema schema2)
			{ return schema1.getName().toLowerCase().compareTo(schema2.getName().toLowerCase()); }	
	}
	
	/** Stores the org.org.mitre.harmony model */
	private HarmonyModel harmonyModel;
	
	/** Set of check boxes containing the schema selection */
	private JPanel schemaList = null;

	/** Stores the "Manage Schema" button */
	private JButton button = new JButton("Manage Schemas");
	
	/** Create the schema list */
	private void updateSchemaList()
	{
		// Get a list of currently selected schemas
		ArrayList<Schema> selectedSchemas = getSelectedSchemas();
		
		// Get the list available schemas
		ArrayList<Schema> schemas = harmonyModel.getSchemaManager().getSchemas();
		Collections.sort(schemas, new SchemaComparator());
		
		// Generate the list of schemas
		schemaList.removeAll();
		for(Schema schema : schemas)
		{
			SchemaSelectionItem item = new SchemaSelectionItem(schema,harmonyModel);
			if(selectedSchemas.contains(schema)) item.setSelected(true);
			schemaList.add(item);
		}
		revalidate(); repaint();
	}
	
	/** Constructs the Schema List pane */
	SchemaSelectionPane(HarmonyModel harmonyModel)
	{			
		this.harmonyModel = harmonyModel;
		
		// Create the schema list
		schemaList = new JPanel();
		schemaList.setOpaque(false);
		schemaList.setLayout(new BoxLayout(schemaList,BoxLayout.Y_AXIS));
		updateSchemaList();

		// Force schema list to not spread out
		JPanel schemaListPane = new JPanel();
		schemaListPane.setBackground(Color.white);
		schemaListPane.setLayout(new BorderLayout());
		schemaListPane.add(schemaList,BorderLayout.NORTH);
		
		// Create a scroll pane to hold the list of schemas
		JScrollPane schemaScrollPane = new JScrollPane(schemaListPane,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		schemaScrollPane.setPreferredSize(new Dimension(250, 200));

		// Creates the schema selection pane
		setBorder(new EmptyBorder(0,0,0,5));
		setLayout(new BorderLayout());
		add(schemaScrollPane,BorderLayout.CENTER);
		/*if(harmonyModel.getInstantiationType()!=InstantiationType.EMBEDDED)
		{
			// Create the import schema button
			button.setFocusable(false);
			button.addActionListener(this);

			// Create a button pane
			JPanel pane = new JPanel();
			pane.setLayout(new BorderLayout());
			pane.setBorder(new EmptyBorder(5,0,0,0));
			pane.add(button,BorderLayout.CENTER);
			add(pane,BorderLayout.SOUTH);
		}*/
	}
	
	/** Handles the enabling of components in this dialog */
	public void setEnabled(boolean enabled)
	{
		for(Component item : schemaList.getComponents()) item.setEnabled(enabled);
		button.setEnabled(enabled);
	}
	
	/** Returns the list of selected schemas */
	ArrayList<Schema> getSelectedSchemas()
	{
		ArrayList<Schema> schemas = new ArrayList<Schema>();
		for(Component component : schemaList.getComponents())
		{
			SchemaSelectionItem item = (SchemaSelectionItem)component;
			if(item.isSelected()) schemas.add(item.getSchema());
		}
		return schemas;
	}
	
	/** Returns the list of selected schema IDs */
	private ArrayList<Integer> getSelectedSchemaIDs()
	{
		ArrayList<Integer> schemaIDs = new ArrayList<Integer>();
		for(Schema schema : getSelectedSchemas())
			schemaIDs.add(schema.getId());
		return schemaIDs;
	}
	
	/** Set the specified schema selection */
	void schemaSelected(Integer schemaID, boolean selected)
	{
		for(int i=0; i<schemaList.getComponentCount(); i++)
		{
			SchemaSelectionItem item = (SchemaSelectionItem)schemaList.getComponent(i);
			if(item.getSchema().getId().equals(schemaID))
				{ item.setSelected(selected); break; }
		}
	}	
	
	/** Handles the import of a schema */
	public void actionPerformed(ActionEvent e)
	{
		// Get the ProjectDialog pane
		Component component = getParent();
		while(!(component instanceof ProjectDialog))
			component = component.getParent();
		
		// Display the schema dialog
		SchemaDialog dialog = new SchemaDialog(harmonyModel, getSelectedSchemaIDs());
		//harmonyModel.getDialogManager().openDialog(dialog);
		dialog.addInternalFrameListener(this);
	}
	
	/** Updates the schema list when the schema dialog is closed */
	public void internalFrameClosed(InternalFrameEvent e)
		{ updateSchemaList(); }

	// Unused event listeners
	public void internalFrameOpened(InternalFrameEvent e) {}
	public void internalFrameClosing(InternalFrameEvent e) {}
	public void internalFrameIconified(InternalFrameEvent e) {}
	public void internalFrameDeiconified(InternalFrameEvent e) {}
	public void internalFrameActivated(InternalFrameEvent e) {}
	public void internalFrameDeactivated(InternalFrameEvent e) {}
}