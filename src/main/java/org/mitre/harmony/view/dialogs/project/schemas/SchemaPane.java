// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs.project.schemas;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.schemastore.model.ProjectSchema;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.schemaInfo.model.SchemaModel;

/**
 * Displays the dialog for configuring the current project
 * @author CWOLF
 */
public class SchemaPane extends JPanel
{
	/** Reference to the Harmony model */
	private HarmonyModel harmonyModel;
	
	// Stores the various panes used in this pane
	private SchemaSelectionPane schemaSelectionPane = null;
	private SchemaModelPane schemaModelPane = null;
	
	/** Initializes the properties dialog */
	public SchemaPane(HarmonyModel harmonyModel)
	{
		this.harmonyModel = harmonyModel;
		
		// Initializes the selection and display panes
		schemaSelectionPane = new SchemaSelectionPane(harmonyModel);
		schemaModelPane = new SchemaModelPane(harmonyModel);
		
		// Populate panes with schema information
		for(Integer schemaID : harmonyModel.getProjectManager().getSchemaIDs())
			selectSchema(schemaID);

		// Initializes the selection pane
		schemaSelectionPane.setPreferredSize(new Dimension(200,0));
		
		// Constructs the schema pane
		setBorder(new CompoundBorder(new TitledBorder("Schemas"),new EmptyBorder(5,5,5,5)));
		setLayout(new BorderLayout());
		add(schemaSelectionPane,BorderLayout.WEST);
		add(schemaModelPane,BorderLayout.CENTER);
   	}
	
	/** Handles the enabling of components in this dialog */
	public void setEnabled(boolean enabled)
	{
		schemaSelectionPane.setEnabled(enabled);
		schemaModelPane.setEnabled(enabled);
	}
	
	/** Returns the selected schemas */
	public ArrayList<Schema> getSchemas()
		{ return schemaSelectionPane.getSelectedSchemas(); }
	
	/** Select a schema */
	public void selectSchema(Integer schemaID)
	{
		schemaSelectionPane.schemaSelected(schemaID, true);
		schemaModelPane.selectSchema(schemaID);
	}
	
	/** Unselect a schema */
	public void unselectSchema(Integer schemaID)
	{
		schemaSelectionPane.schemaSelected(schemaID, false);
		schemaModelPane.unselectSchema(schemaID);
	}
	
	/** Saves the project schemas */
	public void save()
	{
		ArrayList<ProjectSchema> schemas = new ArrayList<ProjectSchema>();
		for(Schema schema : getSchemas())
		{
			SchemaModel schemaModel = schemaModelPane.getModel(schema.getId());
			String modelName = schemaModel==null ? null : schemaModel.getClass().getName();
			schemas.add(new ProjectSchema(schema.getId(),schema.getName(),modelName));
		}
		harmonyModel.getProjectManager().setSchemas(schemas);
	}
}
