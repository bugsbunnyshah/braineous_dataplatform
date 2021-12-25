package org.mitre.harmony.view.dialogs.project.schemas;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.model.SchemaModel;

/** Displays a schema item with the model setting */
class SchemaModelItem extends JPanel
{
	// Constants to define which side the schema should be displayed on
	static final int NONE = 0;
	static final int LEFT = 1;
	static final int RIGHT = 2;
	
	/** Stores the schema for this schema item */
	private Schema schema;

	// Stores the model selection for the specific schema
	private JComboBox modelSelection = new JComboBox();
	
	/** Constructs the schema model item */
	SchemaModelItem(Schema schema, HarmonyModel harmonyModel)
	{
		this.schema = schema;
		
		// Initialize the model selection box
		modelSelection.setOpaque(false);
		modelSelection.setPreferredSize(new Dimension(80,20));
		modelSelection.addItem("<Default>");
		HierarchicalSchemaInfo schemaInfo = harmonyModel.getSchemaManager().getSchemaInfo(schema.getId());
		for(SchemaModel model : HierarchicalSchemaInfo.getSchemaModels()) {
			//if (schemaInfo.shouldExpandAll(model))
			//	modelSelection.addItem(model);
		}

		// Set the selected model
		SchemaModel selectedModel = harmonyModel.getProjectManager().getSchemaModel(schema.getId());
		//if(selectedModel!=null && schemaInfo.shouldExpandAll(selectedModel))
		//	modelSelection.setSelectedItem(selectedModel);
		
		// Constructs the schema item
		setBorder(new EmptyBorder(3,0,3,0));
		setMaximumSize(new Dimension(10000,26));
		setOpaque(false);
		setFocusable(false);
		setLayout(new BorderLayout());
		add(new SchemaModelRow(modelSelection,new JLabel(schema.getName())));
	}
	
	/** Handles the enabling of components in this dialog */
	public void setEnabled(boolean enabled)
		{ modelSelection.setEnabled(enabled); }
	
	/** Returns the schema associated with this item */
	Schema getSchema() { return schema; }
	
	/** Returns the schema model associated with this item */
	SchemaModel getModel()
	{
		Object model = modelSelection.getSelectedItem();
		return (model instanceof SchemaModel) ? (SchemaModel)model : null;
	}
}