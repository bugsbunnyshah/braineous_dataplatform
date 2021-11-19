package org.mitre.harmony.view.dialogs.project.mappings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.dialogs.widgets.Link;
import org.mitre.schemastore.model.Mapping;

/** Schema selection item class */
class MappingSelectionItem extends JPanel implements ActionListener
{
	/** Stores the mapping associated with this check box */
	private Mapping mapping;
	
	/** Stores the mapping label */
	private String label;
	
	/** Stores the check box associated with the mapping */
	private JCheckBox checkbox;
	
	/** Stores the delete link associated with the mapping */
	private Link deleteMapping = null;
	
	/** Constructs the mapping check box */
	MappingSelectionItem(HarmonyModel harmonyModel, Mapping mapping, boolean selected)
	{
		this.mapping = mapping;
		
		// Generate the mapping label
		String sourceName = harmonyModel.getSchemaManager().getSchema(mapping.getSourceId()).getName();
		String targetName = harmonyModel.getSchemaManager().getSchema(mapping.getTargetId()).getName();
		label = "'" + sourceName + "' to '" + targetName + "'";
		
		// Initialize the check box
		checkbox = new JCheckBox(label);
		checkbox.setOpaque(false);
		checkbox.setEnabled(!harmonyModel.getMappingManager().areMappingsLocked());
		checkbox.setFocusable(false);
		checkbox.setSelected(selected);
		checkbox.addActionListener(this);
	
		// Initialize the delete link
		//if(harmonyModel.getInstantiationType()!=InstantiationType.EMBEDDED)
		//	deleteMapping = new Link("Delete",this);
		
		// Constructs the check box pane
		JPanel checkboxPane = new JPanel();
		checkboxPane.setOpaque(false);
		checkboxPane.setLayout(new BoxLayout(checkboxPane,BoxLayout.X_AXIS));
		checkboxPane.add(checkbox);
		if(deleteMapping!=null) checkboxPane.add(deleteMapping);
		
		// Constructs the schema check box
		setOpaque(false);
		setLayout(new BorderLayout());
		add(checkboxPane,BorderLayout.WEST);
	}
	
	/** Returns the mapping associated with this check box */
	Mapping getMapping()
		{ return mapping; }
	
	/** Indicates if the item is selected */
	boolean isSelected()
		{ return checkbox.isSelected(); }
	
	/** Enables the item */
	public void setEnabled(boolean enable)
		{ checkbox.setEnabled(enable); }
	
	/** Handles actions occurring to the mapping selection item */
	public void actionPerformed(ActionEvent e)
	{
		// Get the mapping selection pane
		Component component = getParent();
		while(!(component instanceof MappingSelectionPane))
			component = component.getParent();
		MappingSelectionPane pane = (MappingSelectionPane)component;
		
		// Lock down check boxes which can't be displayed simultaneously
		if(e.getSource()==checkbox)
			pane.updateEnabledCheckboxes();
		
		// Handles deletion of a mapping
		else if(e.getSource()==deleteMapping)
			pane.deleteMappingItem(this);
	}

	/** Returns the string representation of this item */
	public String toString()
		{ return label; }
}