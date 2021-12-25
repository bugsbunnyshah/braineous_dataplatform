package org.mitre.harmony.view.dialogs.importers;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/** Class for displaying a list of parameters */
class ParameterPane extends JPanel
{
	/** Constructs the parameter pane */
	ParameterPane()
	{
		setOpaque(false);
		setLayout(new GridBagLayout());		
	}
	
	/** Adds a parameter to the pane */
	void addParameter(String label, JComponent component, int offset)
	{
		// Position the parameter label
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = getComponentCount()/2;
		constraints.anchor = GridBagConstraints.FIRST_LINE_END;
		constraints.insets = new Insets(2+offset,0,1,0);
		add(new JLabel(label!=null ? label + ": " : ""),constraints);
			
		// Position the parameter component
		constraints.gridx = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(1,0,1,0);
		constraints.weightx = 1;
		if(component instanceof JTextArea)
			{ constraints.fill = GridBagConstraints.BOTH; constraints.weighty = 1; }
		add(component,constraints);
	}
	
	/** Adds a parameter to the pane */
	void addParameter(String label, JComponent component)
		{ addParameter(label,component,0); }
}