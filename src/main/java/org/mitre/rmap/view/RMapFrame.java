// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.
package org.mitre.rmap.view;

import org.mitre.harmony.model.HarmonyConsts;
import org.mitre.harmony.view.harmonyPane.TitledPane;
import org.mitre.harmony.view.mappingPane.MappingPane;
import org.mitre.rmap.generator.Dependency;
import org.mitre.rmap.generator.SQLGenerator;
import org.mitre.rmap.generator.SQLGeneratorException;
import org.mitre.rmap.model.DependencyTableModel;
import org.mitre.rmap.model.RMapHarmonyModel;
import org.mitre.schemastore.client.SchemaStoreClient;
import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.ProjectSchema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class RMapFrame extends JInternalFrame {
	/** Stores the Harmony model */
	private RMapHarmonyModel harmonyModel;

	/** stores the schema mapping object */
	private MappingPane schemaMappingPane;

	/** the different view panes */
	private JPanel mainPane;			// the whole thing
	private JPanel cardPane;			// stores the two mappingPane/generatedPane, which share the same space
	private JPanel mappingPane;			// displays the mapping display between two schemas
	private JPanel generatedPane;		// displays the generated SQL
	private JPanel dependencyPane;		// displays the list of dependencies and the "Generate SQL" button
	
	/** stores the text panel so we can add to it later */
	private JTextArea textArea;

	/** Dependencies to be stored */
	private ArrayList<Dependency> dependenciesOrdered;

	/** Dependency Table GraphData */
	private Object[][] dependencyTableData;
	private Integer dependencyDisplayedIndex = null;

	/** For the card layout */
	// each string is the label for the card that we want to show
	private final static String MAPPING_PANE = "MAPPING_PANE";
	private final static String GENERATED_PANE = "GENERATED_PANE";

	/** Returns the view pane
	 *  the view pane contains the three column mapping dimensions */
	private JPanel getMappingPane() {
		// Clear out the action map
		getActionMap().clear();
		
		// create the mapping pane and store it in the class variable
		schemaMappingPane = new MappingPane(this, harmonyModel);

		// Generate the new view
		return new TitledPane(null, (JComponent)schemaMappingPane);
	}
	
	private JPanel getGeneratedPane() {
		// create a new panel that contains the generated SQL
		JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());

		// put the generated SQL into a text pane
        textArea = new JTextArea();
		textArea.setEditable(true);
		textArea.setLineWrap(false);

		// change the font
		Font font = new Font("Courier", Font.PLAIN, 10);
		textArea.setFont(font);

		// create a scrollbar for the text box and add it to the panel
		JScrollPane generatedTextScrollPane = new JScrollPane(textArea);
		pane.add(generatedTextScrollPane, BorderLayout.CENTER);

		// add a button to save the SQL
		JButton saveSQLButton = new JButton("Save Generated SQL");
		saveSQLButton.addActionListener(new SaveSQLButtonListener(saveSQLButton));
		saveSQLButton.setEnabled(true);

        // add a button to copy the text
        JButton copySQLButton = new JButton("Copy Generated SQL");
        copySQLButton.addActionListener(new CopySQLButtonListener(copySQLButton));
        copySQLButton.setEnabled(true);

		// add a button to close the window
        JButton generateSQLButton = new JButton("Close Generated SQL");
        generateSQLButton.addActionListener(new CloseSQLButtonListener(generateSQLButton));
        generateSQLButton.setEnabled(true);

        // create a panel with padding to put the button in and add it to the panel
        JPanel SQLButtonPane = new JPanel();
        SQLButtonPane.add(saveSQLButton, JPanel.LEFT_ALIGNMENT);
        SQLButtonPane.add(copySQLButton, JPanel.CENTER_ALIGNMENT);
        SQLButtonPane.add(generateSQLButton, JPanel.RIGHT_ALIGNMENT);
        pane.add(SQLButtonPane, BorderLayout.SOUTH);

        // put a title on this pane
		return new TitledPane("Generated SQL", pane);
	}

	private JPanel getDependencyPane() {
        // THIS PANEL BASICALLY DEFINES THE ENTIRE WIDTH OF THE RIGHT COLUMN (in this case, it is 225)
		Integer sidePaneWidth = 225;
		Integer sidePaneDependencyHeight = 300;
        Dimension sidePaneSize = new Dimension(sidePaneWidth, 40);

		// build the list of dependencies
        dependencyTableData = new Object[dependenciesOrdered.size()][2];
        for (int i = 0; i < dependenciesOrdered.size(); i++) {
            dependencyTableData[i][0] = new Boolean(false);
            dependencyTableData[i][1] = new String(
                    i +
                    " -- [" + 
                            dependenciesOrdered.get(i).getSourceLogicalRelation().getMappingSchemaEntitySet().get(0).getName() +
                    "][" + 
                            dependenciesOrdered.get(i).getTargetLogicalRelation().getMappingSchemaEntitySet().get(0).getName() +
                    "]"
            );
        }
        
		// build the dependency table
        DependencyTableModel dependencyTableModel = new DependencyTableModel(dependencyTableData);
        JTable dependencyTable = new JTable(dependencyTableModel);

        // add a selection listener (defined below) to handle selecting or checking things
        SelectionListener selListener = new SelectionListener(dependencyTable);
        dependencyTable.getSelectionModel().addListSelectionListener(selListener);
        dependencyTable.getColumnModel().getSelectionModel().addListSelectionListener(selListener);
        dependencyTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);	// only allow selecting one thing at a time
        dependencyTable.getColumnModel().getColumn(0).setMaxWidth(20);	// specify the checkbox column width to be very small

        // create a scrolling pane for the list of dependencies
        JScrollPane dependencyTableScrollPane = new JScrollPane(dependencyTable);

        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());
		pane.add(dependencyTableScrollPane, BorderLayout.CENTER);

		// initialize the button that generates SQL
        JButton generateSQLButton = new JButton("Generate SQL");
        generateSQLButton.addActionListener(new GenerateSQLButtonListener(generateSQLButton));
        generateSQLButton.setEnabled(true);

        // create a panel with padding to put the button in
        JPanel generateSQLButtonPane = new JPanel();
        generateSQLButtonPane.add(generateSQLButton, JPanel.CENTER_ALIGNMENT);

        // add the button to the bottom
        pane.add(generateSQLButtonPane, BorderLayout.SOUTH);
        
        // specify a width for the side pane
		pane.setPreferredSize(sidePaneSize);
		pane.setMaximumSize(sidePaneSize);

		TitledPane titledPane = new TitledPane("Dependencies", pane);
        titledPane.setPreferredSize(new Dimension(sidePaneWidth, sidePaneDependencyHeight));
        return titledPane;
	}

	/** Generates the main pane
	 *  the main pane contains in it both the view pane (see above)
	 *  and the dependencies pane, which is generated in this method */
	private JPanel getMainPane() {
		// generate the schema mapping display and the middle pane that shows it
		mappingPane = getMappingPane();
		dependencyPane = getDependencyPane();
		generatedPane = getGeneratedPane();
		
		// generate the card layout for the mapping/generated panes
		cardPane = new JPanel(new CardLayout());
		cardPane.add(mappingPane, MAPPING_PANE);
		cardPane.add(generatedPane, GENERATED_PANE);

		// generate the main pane of RMap
		mainPane = new JPanel();
		mainPane.setLayout(new BorderLayout());
		mainPane.add(cardPane, BorderLayout.CENTER);
		mainPane.add(dependencyPane, BorderLayout.EAST);
		return mainPane;
	}

	private class SelectionListener implements ListSelectionListener {
        private JTable table;

        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        public SelectionListener(JTable table) {
        	this.table = table;
        }

        public void valueChanged(ListSelectionEvent e) {
        	if (e.getValueIsAdjusting()) {
        		// The mouse button has not yet been released
                return;
        	}

        	// save current work for visible dependency
        	Dependency dependencyDisplayed = null;
        	if (dependencyDisplayedIndex != null) {
        		ArrayList<MappingCell> mappingCells = harmonyModel.getMappingManager().getMappingCells();
        		ArrayList<MappingCell> updatedCells = new ArrayList<MappingCell>();
        		dependencyDisplayed = dependenciesOrdered.get(dependencyDisplayedIndex);
        		for (MappingCell cell : mappingCells) {
        			if (cell.getScore() > -1.0) {
        				MappingCell cellCopy = cell.copy();
        				updatedCells.add(cellCopy);
        			}
        		}

        		dependencyDisplayed.updateCorrespondences(updatedCells);
        		harmonyModel.getSchemaManager().setMappingCells(dependencyDisplayed, updatedCells);
        	}

    		// clicked on the dependency, so show it on the schema list
        	if (dependenciesOrdered != null) {
    			dependencyDisplayedIndex = table.getSelectedRow(); // save the value of the currently displayed dependency
        		dependencyDisplayed = dependenciesOrdered.get(dependencyDisplayedIndex);
    			Integer displayedMappingID = harmonyModel.getSchemaManager().getMappingID(dependencyDisplayed);

    			if (displayedMappingID != null) {
    				// get the new schemas
    				ArrayList<ProjectSchema> projectSchemas = new ArrayList<ProjectSchema>();
    				ProjectSchema sourceProjectSchema = harmonyModel.getSchemaManager().getSourceSchema(displayedMappingID);
    				ProjectSchema targetProjectSchema = harmonyModel.getSchemaManager().getTargetSchema(displayedMappingID);
    				projectSchemas.add(sourceProjectSchema);
    				projectSchemas.add(targetProjectSchema);

    				// retrieve old/new mapping and mapping cell information
    				Mapping newMapping = harmonyModel.getSchemaManager().getMapping(displayedMappingID);
    				ArrayList<MappingCell> newMappingCells = harmonyModel.getSchemaManager().getMappingCells(displayedMappingID);

    				// delete the old mapping cell information
    				harmonyModel.getMappingManager().removeAllMappings();

    				// set the new schema and mapping information
    				harmonyModel.getProjectManager().setSchemas(projectSchemas);
    				harmonyModel.getMappingManager().addMapping(newMapping);
    				harmonyModel.getMappingManager().setMappingCells(newMappingCells);

    				// show the mappings and expand the tree to display everything (including all fields in each schema)
    				harmonyModel.getMappingManager().getMapping(displayedMappingID).setVisibility(true);
    				
    				schemaMappingPane.getTree(HarmonyConsts.LEFT).expandNode(schemaMappingPane.getTree(HarmonyConsts.LEFT).root);
    				schemaMappingPane.getTree(HarmonyConsts.RIGHT).expandNode(schemaMappingPane.getTree(HarmonyConsts.RIGHT).root);

    				harmonyModel.getProjectManager().setModified(false);
    			}
        	}
        }
	} // end SelectionListener class

	private class CopySQLButtonListener implements ActionListener {
		public CopySQLButtonListener(JButton button) {
			button.setEnabled(true);
		}

		public void actionPerformed(ActionEvent e) {
			textArea.selectAll();
			textArea.copy();
		}
	} // end CopySQLButtonListener

	private class SaveSQLButtonListener implements ActionListener {
		public SaveSQLButtonListener(JButton button) {
			button.setEnabled(true);
		}

		public void actionPerformed(ActionEvent e) {
			// show a dialog to save the SQL
			JFileChooser fc = new JFileChooser(harmonyModel.getPreferences().getExportDir());
			fc.setDialogType(JFileChooser.SAVE_DIALOG);
			if (fc.showDialog(mappingPane, "Save") == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				harmonyModel.getPreferences().setImportDir(fc.getSelectedFile().getParentFile());

				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(file.getAbsoluteFile().toString()));
					out.write(textArea.getText());
					out.close();
				} catch (IOException ioe) {
					System.err.println("[E] Failed to save file: " + ioe.getMessage());
				}
			}
		}
	} // end SaveSQLButtonListener

    private class GenerateSQLButtonListener implements ActionListener {
        public GenerateSQLButtonListener(JButton button) {
            button.setEnabled(true);
        }

        public void actionPerformed(ActionEvent e) {
        	Dependency dependenciesDisplayed = null;
        	if (dependencyDisplayedIndex != null){
        		ArrayList<MappingCell> mappingCells = harmonyModel.getMappingManager().getMappingCells();
        		ArrayList<MappingCell> updatedCells = new ArrayList<MappingCell>();
        		dependenciesDisplayed = dependenciesOrdered.get(dependencyDisplayedIndex);
        		for (MappingCell cell : mappingCells){
        			if (cell.getScore() > -1.0) {
        				MappingCell cellCopy = cell.copy();
        				updatedCells.add(cellCopy);
        			}
        		}
        		dependenciesDisplayed.updateCorrespondences(updatedCells);
        		harmonyModel.getSchemaManager().setMappingCells(dependenciesDisplayed, updatedCells);
        	}

        	// store what has been checked
        	ArrayList<Integer> selectedIndices = new ArrayList<Integer>();
        	for (int i = 0; i < dependencyTableData.length; i++) {
        		if ((Boolean)dependencyTableData[i][0]) { selectedIndices.add(i); }
        	}

        	// if no depencies between elements are in this relation, show a warning
    		ArrayList<Dependency> selectedDependencies = new ArrayList<Dependency>();
    		for (Integer index : selectedIndices) {
    			Dependency selected = dependenciesOrdered.get(index);
    			selectedDependencies.add(selected);
    		}

        	// if no dependencies has been checked show a warning
        	if (selectedDependencies.size() == 0) {
        		JOptionPane.showMessageDialog(mainPane, "No dependencies are selected. Cannot generate SQL.", "Error", JOptionPane.ERROR_MESSAGE);
        		return;
        	}
        	
        	// create the generator which has all of our SQL stuff in it
    		SQLGenerator generator = new SQLGenerator();

        	// if there is a problem with the dependencies, show a warning
        	try {
        		generator.checkForErrors(selectedDependencies);
        	} catch (SQLGeneratorException exp) {
        		JOptionPane.showMessageDialog(mainPane, exp.getMessage() + " Cannot generate SQL.", "Error", JOptionPane.ERROR_MESSAGE);
        		return;
        	}

    		// ask what kind of output they would like to generate
    		Object[] exportChoices = generator.getExportTypes().toArray();
    		String selectTargetDB = (String) JOptionPane.showInputDialog(
				mappingPane,					// what frame to make ourselves modal to
				"Choose a SQL dialect:",		// the question in the box
				"Generate SQL",					// the title bar
				JOptionPane.QUESTION_MESSAGE,	// the type of dialog
				null,							// a custom icon
				exportChoices,  				// an string object array of choices
				exportChoices[0]				// the default choice
			);

    		if (selectTargetDB != null && selectTargetDB.length() > 0) {
    			// if there is an error while generating the SQL, show it
    			try {
    				// this call actually generates the SQL and puts it into a string array
    				ArrayList<String> generatedSQL = generator.generate(selectedDependencies, selectTargetDB);

	        		// collect the generated SQL into a normal string
	        		StringBuffer generatedString = new StringBuffer();
	        		for (String i : generatedSQL) {
	        			if (i == null) { continue; }
	        			generatedString.append(i).append("\n");
	        		}

	        		// set the text into the generated pane's text box
	        		textArea.setText(generatedString.toString());
	        		textArea.setCaretPosition(0);

        			// change cards to the generated SQL
        			CardLayout c = (CardLayout)(cardPane.getLayout());
        			c.show(cardPane, GENERATED_PANE);
    			} catch (SQLGeneratorException exp) {
            		JOptionPane.showMessageDialog(mainPane, exp.getMessage() + " Cannot generate SQL.", "Error", JOptionPane.ERROR_MESSAGE);
            		return;
    			}
        	}
        }
    } // end class GenerateSQLButtonListener
    
    private class CloseSQLButtonListener implements ActionListener {
        public CloseSQLButtonListener(JButton button) {
            button.setEnabled(true);
        }

        public void actionPerformed(ActionEvent e) {
    		// clear the text
    		textArea.setText("");
    		textArea.setCaretPosition(0);

    		// change cards to the mapping pane again
    		CardLayout c = (CardLayout)(cardPane.getLayout());
    		c.show(cardPane, MAPPING_PANE);
        }
    }

	public RMapFrame(RMapHarmonyModel harmonyModel, SchemaStoreClient client, Integer elementID) {
		super();
		this.harmonyModel = harmonyModel;

		// Place title on application
		String mappingName = harmonyModel.getProjectManager().getProject().getName();
		setTitle("RMap SQL Generator" + (mappingName != null ? " - " + harmonyModel.getProjectManager().getProject().getName() : ""));

		// Generate dependencies
		dependenciesOrdered = Dependency.generate(client, elementID);

		// initialize the schema list
		harmonyModel.getSchemaManager().initSchemas(dependenciesOrdered);

		// build a layout for the display
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		this.setLayout(layout);

		// Set dialog pane settings
		((javax.swing.plaf.basic.BasicInternalFrameUI) getUI()).setNorthPane(null);
		setBorder(new EmptyBorder(0, 0, 0, 0));
		try { setMaximum(true); } catch (Exception e) {}
		setContentPane(getMainPane());
        setVisible(true);
	}
}
