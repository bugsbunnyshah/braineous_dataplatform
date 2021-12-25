// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.harmony.view.dialogs.importers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.SchemaStoreManager;
import org.mitre.harmony.view.dialogs.widgets.AbstractButtonPane;
import org.mitre.schemastore.porters.Importer;
import org.mitre.schemastore.porters.PorterType;

/**
 * Abstract dialog for importing
 * @author CWOLF
 */
abstract public class AbstractImportDialog extends JInternalFrame implements ActionListener
{		
	/** Stores the org.org.mitre.harmony model */
	protected HarmonyModel harmonyModel;
	
	/** Flag indicating if import was successful */
	private boolean successful = false;
	
	// Stores the panes associated with this dialog
	protected JComboBox selectionList = null;
	protected JTextField nameField = new JTextField();
	protected JTextField authorField = new JTextField();
	protected JTextArea descriptionField = new JTextArea();
	protected URIParameter uriField;
	protected ButtonPane buttonPane;
	
	/** Private class for defining the button pane */
	private class ButtonPane extends AbstractButtonPane
	{
		/** Constructs the button pane */
		public ButtonPane()
			{ super(new String[]{"OK", "Cancel"},1,2); }

		/** Handles selection of button */
		protected void buttonPressed(String label)
		{
			if(!label.equals("Cancel"))
			{
				// Retrieve the information from the various fields
				String name = nameField.getText();
				String author = authorField.getText();
				String description = descriptionField.getText();
				URI uri = uriField.getURI();
				
				// Determine if the name is unique
				if(getUsedNames().contains(name))
				{
					nameField.setBackground(Color.yellow);
					JOptionPane.showMessageDialog(AbstractImportDialog.this,"The provided name is already used!","Duplicated Name",JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// Update highlighting
				nameField.setBackground(name.length()>0 ? Color.white : Color.yellow);
				authorField.setBackground(author.length()>0 ? Color.white : Color.yellow);
				if(uriField.isEnabled()) uriField.setBackground(uri!=null ? Color.white : Color.yellow);
				
				// If completed, run importer
				if(name.length()>0 && author.length()>0 && (!uriField.isEnabled() || uri!=null))
				{
					try { importItem(name, author, description, uri); successful=true; dispose(); }
					catch(Exception e2) { JOptionPane.showMessageDialog(null,e2.getMessage(),"Import Error",JOptionPane.ERROR_MESSAGE); }
				}
				else JOptionPane.showMessageDialog(AbstractImportDialog.this,"All fields must be completed before import!","Missing Fields",JOptionPane.ERROR_MESSAGE);
			}
			else dispose();
		}
	}
	
	/** Constructs the selection pane */
	private JPanel getSelectionPane()
	{
		// Initializes the label
		JLabel selectionLabel = new JLabel("Importer: ");
		selectionLabel.setVerticalAlignment(SwingConstants.CENTER);
		
		// Generate the selection list
		ArrayList<Importer> importers = SchemaStoreManager.getPorters(getImporterType());
		selectionList = new JComboBox(new Vector<Importer>(importers));
		selectionList.setBackground(Color.white);
		selectionList.setFocusable(false);
		selectionList.addActionListener(this);
		
		// Generate the importer list pane
		JPanel importerPane = new JPanel();
		importerPane.setOpaque(false);
		importerPane.setLayout(new BoxLayout(importerPane,BoxLayout.X_AXIS));
		importerPane.add(selectionLabel);
		importerPane.add(selectionList);
		
		// Generate the selection pane
		JPanel pane = new JPanel();
		pane.setBorder(new EmptyBorder(5,0,5,0));
		pane.setOpaque(false);
		pane.setLayout(new BorderLayout());
		pane.add(importerPane,BorderLayout.WEST);
		return pane;
	}
	
	/** Constructs the information pane */
	private JPanel getInformationPane()
	{
		// Initialize the name
		nameField.setBorder(new LineBorder(Color.gray));
		
		// Initialize the author
		authorField.setBorder(new LineBorder(Color.gray));
		//authorField.setText(harmonyModel.getUserName());
		
		// Initialize the description
		descriptionField.setBorder(new LineBorder(Color.gray));
		descriptionField.setRows(5);
		descriptionField.setLineWrap(true);
		descriptionField.setWrapStyleWord(true);
		descriptionField.setPreferredSize(new Dimension(300,descriptionField.getHeight()));
		
		// Initialize the uri
		uriField = new URIParameter(harmonyModel);
		
		// Generates the information pane
		ParameterPane pane = new ParameterPane();
		pane.setBorder(new CompoundBorder(new LineBorder(Color.gray),new EmptyBorder(6,6,6,6)));
		pane.addParameter("Name", nameField);
		pane.addParameter("Author", authorField);
		pane.addParameter("Description", descriptionField);
		pane.addParameter("Location", uriField);
		return pane;
	}
	
	/** Constructs the importer dialog */
	public AbstractImportDialog(HarmonyModel harmonyModel)
	{
		super();
		this.harmonyModel = harmonyModel;
		
		// Initialize the main pane
		JPanel mainPane = new JPanel();
		mainPane.setBorder(new EmptyBorder(5,10,5,10));
		mainPane.setLayout(new BorderLayout());
		mainPane.add(getSelectionPane(),BorderLayout.NORTH);
		mainPane.add(getInformationPane(),BorderLayout.CENTER);
		mainPane.add(buttonPane = new ButtonPane(), BorderLayout.SOUTH);
		
		// Initialize the selected importer
		selectionList.setSelectedIndex(0);
		
		// Initialize the dialog pane
		setTitle(getDialogTitle());
		setContentPane(mainPane);
		pack();
	}
	
	/** Sets the URI for this dialog */
	public void setURI(URI uri, String displayName)
		{ uriField.setURI(uri, displayName); }
	
	/** Handles the enabling of elements in this dialog */
	public void setEnabled(boolean enabled)
	{
		selectionList.setEnabled(enabled);
		nameField.setEnabled(enabled);
		authorField.setEnabled(enabled);
		descriptionField.setEnabled(enabled);
		uriField.setEnabled(enabled);
		buttonPane.setEnabled(enabled);
	}
	
	/** Handles changes to the selected importer */
	public void actionPerformed(ActionEvent e)
		{ uriField.setImporter(getImporter()); }
	
	/** Returns the type of importer being run */
	abstract protected PorterType getImporterType();
	
	/** Returns the list of used names */
	abstract protected ArrayList<String> getUsedNames();
	
	/** Imports the currently specified item */
	abstract protected void importItem(String name, String author, String description, URI uri) throws Exception;
	
	/** Returns the dialog title */
	private String getDialogTitle()
	{
		if(getImporterType()==PorterType.SCHEMA_IMPORTERS) return "Import Schema";
		if(getImporterType()==PorterType.PROJECT_IMPORTERS) return "Import Project";
		if(getImporterType()==PorterType.MAPPING_IMPORTERS) return "Import Mapping";
		return null;
	}
	
	/** Returns the currently selected importer */
	public Importer getImporter()
		{ return (Importer)selectionList.getSelectedItem(); }
	
	/** Indicates if the import was successful */
	public boolean isSuccessful()
		{ return successful; }
}