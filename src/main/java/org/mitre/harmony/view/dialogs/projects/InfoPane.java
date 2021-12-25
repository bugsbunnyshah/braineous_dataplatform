package org.mitre.harmony.view.dialogs.projects;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.dialogs.widgets.TitledPane;
import org.mitre.schemastore.model.Project;
import org.mitre.schemastore.model.ProjectSchema;

/** Private class for managing the info pane */
class InfoPane extends JPanel implements CaretListener
{	
	/** Declares the default font */
	static private Font defaultFont = new Font(null,Font.PLAIN,12);
	
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	// Stores the various info fields
	private JTextField nameField = new JTextField();
	private JTextField authorField = new JTextField();
	private JTextArea descriptionField = new JTextArea();
	private JPanel schemaPane = new JPanel();
	
	/** Stores the currently displayed project */
	private Project project = null;
	
	/** Constructs the info pane */
	InfoPane(boolean saveMode, HarmonyModel harmonyModel)
	{
		this.harmonyModel = harmonyModel;
		
		// Initialize the name field
		nameField.setBackground(Color.white);
		nameField.setBorder(new LineBorder(Color.gray));
		nameField.setEditable(saveMode);
		nameField.addCaretListener(this);
		
		// Initialize the author field
		authorField.setBackground(Color.white);
		authorField.setBorder(new LineBorder(Color.gray));
		authorField.setEditable(saveMode);
		authorField.addCaretListener(this);
		
		// Initialize the description field
		descriptionField.setBackground(Color.white);
		descriptionField.setBorder(new LineBorder(Color.gray));
		descriptionField.setEditable(saveMode);
		descriptionField.setLineWrap(true);
		descriptionField.setWrapStyleWord(true);
		descriptionField.addCaretListener(this);
		
		// Initializes the schema pane
		schemaPane.setBackground(Color.white);
		schemaPane.setLayout(new BoxLayout(schemaPane,BoxLayout.Y_AXIS));
		
		// Create the top info pane
		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane,BoxLayout.Y_AXIS));
		topPane.add(new TitledPane("Name",nameField));
		topPane.add(new TitledPane("Author",authorField));
		
		// Creates the center info pane
		JPanel centerPane = new JPanel();
		centerPane.setLayout(new GridLayout(2,1));
		centerPane.add(new TitledPane("Description",descriptionField));
		centerPane.add(new TitledPane("Schemas",new JScrollPane(schemaPane,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)));		
		
		// Create the info pane
		setBorder(new EmptyBorder(0,6,0,0));
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(250,250));
		add(topPane, BorderLayout.NORTH);
		add(centerPane, BorderLayout.CENTER);
	}
	
	/** Sets the info pane */
	void setInfo(Project project)
	{
		this.project = null;
		
		// Initialize the various fields
		nameField.setBackground(Color.white);
		authorField.setBackground(Color.white);
		descriptionField.setBackground(Color.white);
		schemaPane.removeAll();
		
		// Make sure that a project is selected
		if(project!=null)
		{
			// Place information into the various fields
			nameField.setText(project.getName());
			authorField.setText(project.getAuthor());
			descriptionField.setText(project.getDescription());
	
			// Display the selected schema information
			if(project.getSchemas()!=null)
				for(ProjectSchema schema : project.getSchemas())
				{
					JLabel label = new JLabel(harmonyModel.getSchemaManager().getSchema(schema.getId()).getName());
					label.setFont(defaultFont);
					schemaPane.add(label);
				}
		}

		// Refresh the schema pane to display the new schemas
		schemaPane.revalidate();
		schemaPane.repaint();
		
		// Set the current project being displayed
		this.project = project;
	}

	/** Validates the general info */
	boolean validateInfo()
	{
		// Checks for a valid name (no duplication of project names allowed)
		boolean validName = nameField.getText().length()>0;		
		String name = nameField.getText();
		for(Project project : harmonyModel.getProjectManager().getProjects())
			validName |= project.getName().equals(name);
		
		// Checks for a valid author
		boolean validAuthor = authorField.getText().length()>0;

		// Check for valid schemas
		boolean validSchemas = schemaPane.getComponents().length>=2;
		
		// Update highlighting
		nameField.setBackground(validName ? Color.white : Color.yellow);
		authorField.setBackground(validAuthor ? Color.white : Color.yellow);
		schemaPane.setBackground(validSchemas ? Color.white : Color.yellow);
		
		// Indicates if the general info is completely provided
		return validName && validAuthor && validSchemas;
	}

	/** Update the project when the data fields are modified */
	public void caretUpdate(CaretEvent e)
	{
		if(project!=null)
		{
			project.setName(nameField.getText());
			project.setAuthor(authorField.getText());
			project.setDescription(descriptionField.getText());
		}
	}
}