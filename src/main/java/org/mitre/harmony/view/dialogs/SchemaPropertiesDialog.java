// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.schemastore.model.Schema;

/**
 * Displays the dialog showing schema properties
 */
public class SchemaPropertiesDialog extends JInternalFrame
{
	/** Displays the label */
	private JPanel displayLabel(String name)
	{
		// Create the label
		JPanel label = new JPanel();
		label.setLayout(new BorderLayout());
		label.add(new JLabel(name + ": "),BorderLayout.EAST);
	
		// Create the label pane
		JPanel labelPane = new JPanel();
		labelPane.setLayout(new BorderLayout());
		labelPane.add(label,BorderLayout.NORTH);
		return labelPane;
	}
	
	/** Displays a field with the provided information */
	private JComponent displayField(String value, int rows)
	{
		// Create the field
		JComponent field = null;
		if(rows==1) { field = new JTextField(value); ((JTextField)field).setCaretPosition(0); }
		else field = new JTextArea(value,3,0);
		
		// Configure the field
		field.setBorder(null);
		field.setOpaque(false);
		field.setFont(new Font("Arial",Font.PLAIN,12));
		field.setPreferredSize(new Dimension(250,12));
		if(field instanceof JTextField) ((JTextField)field).setEditable(false);
		if(field instanceof JTextArea) ((JTextArea)field).setEditable(false);
		
		// Create the field pane
		JPanel fieldPane = new JPanel();
		fieldPane.setBorder(new CompoundBorder(new LineBorder(Color.gray), new EmptyBorder(2,2,2,2)));
		fieldPane.setBackground(Color.white);
		fieldPane.setLayout(new BorderLayout());
		fieldPane.add(field, BorderLayout.CENTER);
		return fieldPane;
	}
		
	/** Displays an item with the specified name and value */
	private JPanel displayItem(String name, String value, int rows)
	{
		JPanel pane = new JPanel();
		pane.setOpaque(false);
		pane.setBorder(new EmptyBorder(2,2,2,2));
		pane.setLayout(new BorderLayout());
		pane.add(displayLabel(name),BorderLayout.CENTER);
		pane.add(displayField(value,rows),BorderLayout.EAST);
		return pane;
	}
	
	/** Initializes the link dialog */
	public SchemaPropertiesDialog(Integer schemaID, HarmonyModel harmonyModel)
	{
		super("Properties for Schema "+harmonyModel.getSchemaManager().getSchema(schemaID).getName());
		Schema schema = harmonyModel.getSchemaManager().getSchema(schemaID);
		
		// Set up the main dialog pane
		JPanel pane = new JPanel();
		pane.setBorder(new CompoundBorder(new EmptyBorder(3,3,3,3),BorderFactory.createTitledBorder("Properties")));
		pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));
		pane.add(displayItem("Name",schema.getName(),1));
		pane.add(displayItem("Author",schema.getAuthor(),1));
		pane.add(displayItem("Source",schema.getSource(),1));
		pane.add(displayItem("Type",schema.getType(),1));
		pane.add(displayItem("Description",schema.getDescription(),3));
		
		// Initialize the dialog parameters
	   	setClosable(true);
	   	setContentPane(pane);
 		pack();
		setVisible(true);
	}
}