// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.dialogs.widgets.AbstractButtonPane;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.model.SchemaModel;

/**
 * Displays the dialog which allows mapping cells to be accepted/rejected
 * @author CWOLF
 */
public class SchemaModelDialog extends JInternalFrame
{
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** Stores the schema ID upon which this dialog pertains */
	private Integer schemaID;
	
	/** Stores the list of available models */
	private JList modelList = null;
	
	/** Private class for defining the button pane */
	private class ButtonPane extends AbstractButtonPane
	{
		/** Constructs the button pane */
		public ButtonPane()
			{ super(new String[]{"OK", "Cancel"},1,2); }

		/** Handles selection of button */
		protected void buttonPressed(String label)
		{
			if(label.equals("OK"))
			{
				SchemaModel model = (SchemaModel)modelList.getSelectedValue();
				harmonyModel.getProjectManager().setSchemaModel(schemaID, model);
			}
			dispose();
		}
	}
		
	/** Initializes the mapping cell dialog */
	public SchemaModelDialog(Integer schemaID, HarmonyModel harmonyModel)
	{
		super("Select Schema Model");
		
		// Initialize the selected links
		this.schemaID = schemaID;
		this.harmonyModel = harmonyModel;
		SchemaModel model = harmonyModel.getSchemaManager().getSchemaInfo(schemaID).getModel();
		HierarchicalSchemaInfo schemaInfo = harmonyModel.getSchemaManager().getSchemaInfo(schemaID);
		/*if (!schemaInfo.shouldExpandAll()){
			schemaInfo.setModel(null);
		}*/
		ArrayList<SchemaModel> possibleModels = HierarchicalSchemaInfo.getSchemaModels();
		Vector<SchemaModel> acceptableModels = new Vector<SchemaModel>();
		for (SchemaModel testModel: possibleModels) {
			/*if (schemaInfo.shouldExpandAll(testModel)) {
				acceptableModels.add(testModel);
			}*/
		}
		// Initializes the mapping list
		modelList = new JList(acceptableModels);
		modelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		for(int i=0; i<modelList.getModel().getSize(); i++)
			if(modelList.getModel().getElementAt(i).getClass().equals(model.getClass()))
				modelList.setSelectedIndex(i);
		
		// Generate the model list pane
		JPanel modelPane = new JPanel();
		modelPane.setBorder(new EmptyBorder(10,10,0,10));
		modelPane.setLayout(new BorderLayout());
		modelPane.add(new JScrollPane(modelList,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
		
		// Generate the main dialog pane
		JPanel pane = new JPanel();
		pane.setBorder(BorderFactory.createLineBorder(Color.black));
		pane.setLayout(new BorderLayout());
		pane.add(modelPane,BorderLayout.CENTER);
		pane.add(new ButtonPane(),BorderLayout.SOUTH);
		
		// Initialize the dialog parameters
		setContentPane(pane);
		setSize(200,250);
		setVisible(true);
	}
}