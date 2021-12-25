package org.mitre.harmony.view.dialogs.project.schemas;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/** Displays a schema model row (model selection box and schema) */
class SchemaModelRow extends JPanel
{	
	/** Creates a pane to center the provided component */
	private JComponent getCenteredPane(JComponent component)
	{
		if(component instanceof JLabel)
			((JLabel)component).setHorizontalAlignment(SwingConstants.CENTER);
		return component;
	}

	/** Constructs the schema row */
	SchemaModelRow(JComponent modelSelection, JComponent schema)
	{
		// Initializes the model selection
		modelSelection.setBorder(new EmptyBorder(0,0,0,6));
		modelSelection.setPreferredSize(new Dimension(120,20));
		
		// Create the row pane
		setOpaque(false);
		setLayout(new BorderLayout());
		add(getCenteredPane(modelSelection),BorderLayout.WEST);
		add(schema,BorderLayout.CENTER);
	}
}