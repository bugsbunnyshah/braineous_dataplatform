// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.harmony.view.dialogs.importers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.mitre.harmony.controllers.ProjectController;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.SchemaStoreManager;
import org.mitre.harmony.model.project.ProjectMapping;
import org.mitre.harmony.view.dialogs.importers.URIParameter.URIListener;
import org.mitre.harmony.view.dialogs.widgets.AbstractButtonPane;
import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.Project;
import org.mitre.schemastore.model.ProjectSchema;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.porters.Importer;
import org.mitre.schemastore.porters.PorterType;
import org.mitre.schemastore.porters.mappingImporters.MappingImporter;

/** Class for displaying the mapping importer dialog */
public class ImportMappingDialog extends JInternalFrame implements ActionListener, URIListener
{		
	/** Stores the org.org.mitre.harmony model */
	private HarmonyModel harmonyModel;
	
	// Stores the panes associated with this dialog
	private JComboBox importerList = null;
	private URIParameter uriField = null;	
	private SchemaSelector sourceSelector = new SchemaSelector();
	private SchemaSelector targetSelector = new SchemaSelector();
	private JCheckBox showAllSchemas = null;
	private ButtonPane buttons = new ButtonPane();
	
	/** Private class for managing a schema selector */
	private class SchemaSelector extends JPanel
	{		
		/** Stores the schema combo box */
		private JComboBox comboBox = new JComboBox();
	
		/** Stores the reference schema ID */
		private Integer referenceID = null;
		
		/** Stores the reference schema */
		private JLabel referenceLabel = new JLabel();
		
		/** Constructs the schema selector */
		public SchemaSelector()
		{
			// Initialize the pane components
			JLabel label = new JLabel("Schema: ");
			label.setFont(new Font("Default",Font.BOLD,getFont().getSize()-2));
			comboBox.setBackground(Color.white);
			comboBox.setFocusable(false);
			referenceLabel.setFont(new Font("Default",Font.BOLD,getFont().getSize()-2));
			setSchema(null);
			
			// Generate the reference pane
			JPanel referencePane = new JPanel();
			referencePane.setLayout(new BorderLayout());
			referencePane.add(label,BorderLayout.WEST);
			referencePane.add(referenceLabel,BorderLayout.CENTER);

			// Generate the selector pane
			setBorder(new EmptyBorder(3,0,0,0));
			setLayout(new BorderLayout());
			add(comboBox,BorderLayout.NORTH);
			add(referencePane,BorderLayout.SOUTH);
		}
		
		/** Sets the selected schema */
		private void updateSelectedSchema()
		{
			comboBox.setSelectedIndex(-1);
			for(int i=0; i<comboBox.getItemCount(); i++)
				if(((Schema)comboBox.getItemAt(i)).getId().equals(referenceID))
					comboBox.setSelectedIndex(i);			
		}
		
		/** Updates the selector schemas */
		private void updateSchemas(ArrayList<Schema> schemas)
		{
			comboBox.removeAllItems();
			for(Schema schema : schemas) comboBox.addItem(schema);
			updateSelectedSchema();
		}
		
		/** Sets the selected schema */
		private void setSchema(ProjectSchema schema)
		{
			referenceID = schema==null ? null : schema.getId();
			referenceLabel.setText(schema==null ? "<None>" : schema.getName());
			updateSelectedSchema();
		}
		
		/** Gets the selected schema */
		private Schema getSchema()
			{ return (Schema)comboBox.getSelectedItem(); }
	}
	
	/** Private class for defining the button pane */
	private class ButtonPane extends AbstractButtonPane
	{
		/** Constructs the button pane */
		public ButtonPane()
			{ super(new String[]{"OK", "Cancel"},1,2); }

		/** Get the mapping cells from the selected importer */
		private ArrayList<MappingCell> getMappingCells(Integer sourceID, Integer targetID, URI uri) throws Exception
		{
			// Get mapping cells
			/*MappingImporter importer = (MappingImporter)getImporter();
			importer.initialize(uri);
			importer.setSchemas(sourceID, targetID);
			ArrayList<MappingCell> mappingCells = importer.getMappingCells();
			
			// Display a dialog with any ignored mapping cells
			ArrayList<MappingCellPaths> paths = importer.getUnidentifiedMappingCellPaths();
			if(paths.size()>0)
				//harmonyModel.getDialogManager().openDialog(new UnidentifiedMappingCellsDialog(paths));

			// Return the mapping cells
			return mappingCells;*/
			return null;
		}
		
		/** Handles selection of button */
		protected void buttonPressed(String label)
		{
			if(label.equals("OK"))
			{
				try {
					// Gather up URI of the file being imported
					URI uri = uriField.getURI();

					// Gather up schema alignment settings
					Schema source = sourceSelector.getSchema();
					Schema target = targetSelector.getSchema();
					
					// Check to see if mapping already exists in project
					for(ProjectMapping mapping : harmonyModel.getMappingManager().getMappings())
						if(mapping.getSourceId().equals(source.getId()) && mapping.getTargetId().equals(target.getId()))
						{
							if(mapping.getMappingCells().size()>0)
								throw new Exception("Mapping already exists in project");
							else harmonyModel.getMappingManager().removeMapping(mapping);
						}
							
					// Retrieve the mapping cells from the importer
					ArrayList<MappingCell> mappingCells = null;
					/*if(harmonyModel.getInstantiationType()!=InstantiationType.WEBAPP)
						mappingCells = getMappingCells(source.getId(), target.getId(), uri);
					else mappingCells = SchemaStoreManager.getImportedMappingCells(getImporter(), source.getId(), target.getId(), uri);*/
					for(MappingCell mappingCell : mappingCells) mappingCell.setId(null);

					// Add schemas to the project
					Project project = harmonyModel.getProjectManager().getProject();
					HashSet<ProjectSchema> schemas = new HashSet<ProjectSchema>();
					if(project.getSchemas()!=null) schemas.addAll(Arrays.asList(project.getSchemas()));
					for(Schema schema : new Schema[]{source,target})
						schemas.add(new ProjectSchema(schema.getId(),schema.getName(),null));
					harmonyModel.getProjectManager().setSchemas(new ArrayList<ProjectSchema>(schemas));
					
					// Store the mapping
					Mapping mapping = new Mapping(null,project.getId(),source.getId(),target.getId());
					ProjectMapping projectMapping = harmonyModel.getMappingManager().addMapping(mapping);
					projectMapping.setMappingCells(mappingCells);
					
					// Display the mapping before shutting down
					ProjectController.selectMappings(harmonyModel);
					dispose();
				}
				catch(Exception e)
					{ JOptionPane.showMessageDialog(this,e.getMessage(),"Import Error",JOptionPane.ERROR_MESSAGE); }
			}
			else dispose();
		}
	}
	
	/** Constructs the selection pane */
	private JPanel getSelectionPane()
	{
		// Initializes the label
		JLabel selectionLabel = new JLabel("Importers: ");
		selectionLabel.setVerticalAlignment(SwingConstants.CENTER);
		
		// Generate the selection list
		ArrayList<Importer> importers = SchemaStoreManager.getPorters(PorterType.MAPPING_IMPORTERS);
		importerList = new JComboBox(new Vector<Importer>(importers));
		importerList.setBackground(Color.white);
		importerList.setFocusable(false);
		importerList.setSelectedIndex(0);
		importerList.addActionListener(this);
		
		// Generate the importer list pane
		JPanel importerPane = new JPanel();
		importerPane.setOpaque(false);
		importerPane.setLayout(new BoxLayout(importerPane,BoxLayout.X_AXIS));
		importerPane.add(selectionLabel);
		importerPane.add(importerList);
		
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
		// Initialize the uri
		uriField = new URIParameter(harmonyModel);
		uriField.setImporter(getImporter());
		uriField.setBorder(new EmptyBorder(0,0,2,0));
		uriField.addListener(this);
		
		// Initialize the schema selectors
		updateSchemas(false);
		
		// Initialize the checkbox for showing all schemas
		showAllSchemas = new JCheckBox("Allow selection of non-project schemas");
		showAllSchemas.setFocusable(false);
		showAllSchemas.addActionListener(this);
		
		// Generates the information pane
		ParameterPane pane = new ParameterPane();
		pane.setBorder(new CompoundBorder(new LineBorder(Color.gray),new EmptyBorder(6,6,6,6)));
		pane.addParameter("File / URI", uriField);
		pane.addParameter("Source",sourceSelector,6);
		pane.addParameter("Target",targetSelector,6);
		pane.addParameter(null, showAllSchemas);
		return pane;
	}

	/** Constructs the mapping importer dialog */
	public ImportMappingDialog(HarmonyModel harmonyModel)
	{
		super("Import Mapping");
		this.harmonyModel = harmonyModel;
		
		// Initialize the main pane
		JPanel pane = new JPanel();
		pane.setBorder(new EmptyBorder(5,10,5,10));
		pane.setLayout(new BorderLayout());
		pane.add(getSelectionPane(),BorderLayout.NORTH);
		pane.add(getInformationPane(),BorderLayout.CENTER);
		pane.add(buttons, BorderLayout.SOUTH);
		
		// Initialize the dialog pane
		setContentPane(pane);
		pack();
		setVisible(true);
	}

	/** Sets the URI for this dialog */
	public void setURI(URI uri, String displayName)
		{ uriField.setURI(uri, displayName); }
	
	/** Updates the displayed schemas */
	void updateSchemas(boolean showAll)
	{
		// Identify schemas to place in the schema lists
		ArrayList<Schema> schemas = harmonyModel.getSchemaManager().getSchemas();
		if(!showAll)
		{
			ArrayList<Integer> projectSchemaIDs =  harmonyModel.getProjectManager().getSchemaIDs();
			for(Schema schema : new ArrayList<Schema>(schemas))
				if(!projectSchemaIDs.contains(schema.getId()))
					schemas.remove(schema);
		}
		
		// Refresh the schema selectors
		sourceSelector.updateSchemas(schemas);
		targetSelector.updateSchemas(schemas);
	}
	
	/** Returns the currently selected importer */
	private Importer getImporter()
		{ return (Importer)importerList.getSelectedItem(); }
	
	/** Handles various actions performed on this dialog */
	public void actionPerformed(ActionEvent e)
	{
		// Handles changes to the selected importer
		if(e.getSource().equals(importerList))
			uriField.setImporter(getImporter());
		
		// Handles the checking/unchecking of the "Show All Schemas" checkbox
		if(e.getSource().equals(showAllSchemas))
			updateSchemas(showAllSchemas.isSelected());
	}

	/** Retrieves the suggested schemas */
	private ArrayList<ProjectSchema> getSuggestedSchemas() throws Exception
	{
		// Initialize the importer
		MappingImporter importer = (MappingImporter)getImporter();
		importer.initialize(uriField.getURI());

		// Get the suggested schemas
		ArrayList<ProjectSchema> schemas = new ArrayList<ProjectSchema>();
		schemas.add(importer.getSourceSchema());
		schemas.add(importer.getTargetSchema());
		return schemas;
	}
	
	/** Handles changes to the specified file to import */
	public void uriModified()
	{
		// Retrieve the suggested schemas
		ArrayList<ProjectSchema> schemas = new ArrayList<ProjectSchema>();
		/*if(harmonyModel.getInstantiationType()!=InstantiationType.WEBAPP)
			try { schemas = getSuggestedSchemas(); } catch(Exception e) {}
		else schemas = SchemaStoreManager.getSuggestedSchemas(getImporter(), uriField.getURI());*/
		
		// Update the labeling on the source and schema panes
		sourceSelector.setSchema(schemas.size()>0 ? schemas.get(0) : null);
		targetSelector.setSchema(schemas.size()>1 ? schemas.get(1) : null);
	}
}