package org.mitre.harmony.view.dialogs.project.schemas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.dialogs.widgets.UnderlinedLabel;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.schemaInfo.model.SchemaModel;

/** Class for handling the setting of the schema model */
public class SchemaModelPane extends JPanel
{	
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** List of schemas with model settings */
	private JPanel schemaList = null;
	
	/** Constructs the schema model pane */
	SchemaModelPane(HarmonyModel harmonyModel)
	{
		this.harmonyModel = harmonyModel;
		
		// Create the header
		JLabel col1Label = new UnderlinedLabel("Schema Model");
		JLabel col2Label = new UnderlinedLabel("Schema");
		JPanel headerPane = new SchemaModelRow(col1Label, col2Label);

		// Create the schema item list
		schemaList = new JPanel();
		schemaList.setBackground(Color.white);
		schemaList.setLayout(new BoxLayout(schemaList,BoxLayout.Y_AXIS));
		
		// Create the schema scroll pane
		JScrollPane schemaScrollPane = new JScrollPane(schemaList,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		schemaScrollPane.setBorder(null);
		schemaScrollPane.setPreferredSize(new Dimension(250, 200));
		
		// Create the schema pane
		setBackground(Color.white);
		setBorder(new CompoundBorder(new LineBorder(Color.gray),new EmptyBorder(1,4,1,4)));
		setLayout(new BorderLayout());
		add(headerPane,BorderLayout.NORTH);
		add(schemaScrollPane,BorderLayout.CENTER);
	}
	
	/** Handles the enabling of components in this dialog */
	public void setEnabled(boolean enabled)
		{ for(Component item : schemaList.getComponents()) item.setEnabled(enabled); }
	
	/** Adds a schema to the pane */
	void selectSchema(Integer schemaID)
	{
		Schema schema = harmonyModel.getSchemaManager().getSchema(schemaID);
		int loc = 0;
		for(loc=0; loc<schemaList.getComponentCount(); loc++)
		{
			SchemaModelItem item = (SchemaModelItem)schemaList.getComponent(loc);
			if(item.getSchema().getName().toLowerCase().compareTo(schema.getName().toLowerCase())>0)
				break;
		}
		schemaList.add(new SchemaModelItem(schema, harmonyModel),loc);
		revalidate(); repaint();
	}

	/** Removes a schema from the pane */
	void unselectSchema(Integer schemaID)
	{
		for(int i=0; i<schemaList.getComponentCount(); i++)
		{
			SchemaModelItem item = (SchemaModelItem)schemaList.getComponent(i);
			if(item.getSchema().getId().equals(schemaID))
				{ schemaList.remove(i); break; }
		}
		revalidate(); repaint();
	}
	
	/** Returns the selected model for the specified schema */
	SchemaModel getModel(Integer schemaID)
	{
		for(int i=0; i<schemaList.getComponentCount(); i++)
		{
			SchemaModelItem item = (SchemaModelItem)schemaList.getComponent(i);
			if(item.getSchema().getId().equals(schemaID))
				return item.getModel();
		}
		return null;
	}
}