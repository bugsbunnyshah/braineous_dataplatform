package org.mitre.harmony.view.dialogs.project.schemas;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.schemastore.model.Schema;

/** Schema selection item class */
class SchemaSelectionItem extends JPanel implements ActionListener
{
	/** Stores the schema associated with this check box */
	private Schema schema;
	
	/** Stores the check box associated with the schema */
	private JCheckBox checkbox;
	
	/** Constructs the schema check box */
	SchemaSelectionItem(Schema schema, HarmonyModel harmonyModel)
	{
		this.schema = schema;
		
		// Initialize the check box
		checkbox = new JCheckBox(schema.getName());
		checkbox.setOpaque(false);
		checkbox.setFocusable(false);
		//checkbox.setEnabled(harmonyModel.getInstantiationType()!=InstantiationType.EMBEDDED);
		checkbox.addActionListener(this);
		
		// Constructs the check box pane
		JPanel checkboxPane = new JPanel();
		checkboxPane.setOpaque(false);
		checkboxPane.setLayout(new BoxLayout(checkboxPane,BoxLayout.X_AXIS));
		checkboxPane.add(checkbox);
		
		// Constructs the schema check box
		setOpaque(false);
		setLayout(new BorderLayout());
		add(checkboxPane,BorderLayout.WEST);
	}
	
	/** Handles the enabling of components in this dialog */
	public void setEnabled(boolean enabled)
		{ checkbox.setEnabled(enabled); }
	
	/** Returns the schema associated with this check box */
	Schema getSchema()
		{ return schema; }
	
	/** Indicates if the schema is selected */
	boolean isSelected()
		{ return checkbox.isSelected(); }
	
	/** Sets the selection of the schema check box */
	void setSelected(boolean selected)
		{ checkbox.setSelected(selected); }
	
	/** Handles selection of a schema */
	public void actionPerformed(ActionEvent e)
	{
		// Get the schema pane
		Component component = getParent();
		while(!(component instanceof SchemaPane))
			component = component.getParent();
		SchemaPane pane = (SchemaPane)component;
		
		// Inform the schema dialog that a schema has been selected/unselected
		if(checkbox.isSelected())
			pane.selectSchema(schema.getId());
		else pane.unselectSchema(schema.getId());
	}
}