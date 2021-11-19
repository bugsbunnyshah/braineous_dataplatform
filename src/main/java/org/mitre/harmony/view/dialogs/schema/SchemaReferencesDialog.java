// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs.schema;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.dialogs.widgets.TitledPane;
import org.mitre.schemastore.model.Project;
import org.mitre.schemastore.model.Schema;

/**
 * Displays the schema references dialog
 * @author CWOLF
 */
public class SchemaReferencesDialog extends JInternalFrame
{
	/** Comparator used to alphabetize schemas */
	private class ObjectComparator implements Comparator<Object>
	{
		public int compare(Object schema1, Object schema2)
			{ return schema1.toString().compareTo(schema2.toString()); }	
	}
	
	/** Generate the specified list */
	private TitledPane getTitledList(String title, ArrayList<? extends Object> objects)
	{
		// Generates the list
		JList list = new JList();
		Collections.sort(objects,new ObjectComparator());
		list.setListData(new Vector<Object>(objects));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// Create a titled scroll pane to hold the list of objects
		JScrollPane schemaScrollPane = new JScrollPane(list,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		schemaScrollPane.setPreferredSize(new Dimension(130,200));
		TitledPane pane = new TitledPane(title,schemaScrollPane);
		pane.setBorder(new EmptyBorder(5,5,5,5));
		return pane;
	}
	
	/** Initializes the schema references dialog */
	public SchemaReferencesDialog(HarmonyModel harmonyModel, Schema schema)
	{
		super("Schema Usage Info");
		
		// Identify the dependent schemas
		ArrayList<Schema> schemas = new ArrayList<Schema>();
		for(Integer schemaID : harmonyModel.getSchemaManager().getDescendentSchemas(schema.getId()))
			schemas.add(harmonyModel.getSchemaManager().getSchema(schemaID));
		
		// Identify the dependent projects
		ArrayList<Project> projects = new ArrayList<Project>();
		for(Project project : harmonyModel.getProjectManager().getProjects())
			if(Arrays.asList(project.getSchemaIDs()).contains(schema.getId()))
				projects.add(project);		

		// Also, check if current project references the project
		if(harmonyModel.getProjectManager().getSchemaIDs().contains(schema.getId()))
		{
			Project project = harmonyModel.getProjectManager().getProject().copy();
			project.setName("<Current Project>");
			projects.add(project);
		}
		
		// Constructs the content pane 
		JPanel pane = new JPanel();
		pane.setBorder(new EmptyBorder(5,5,5,5));
		pane.setLayout(new GridLayout(1,2));
		pane.add(getTitledList("Schemas",schemas));
		pane.add(getTitledList("Projects",projects));
		
		// Set up loader dialog layout and contents
		setClosable(true);
		setContentPane(pane);
		pack();
		setVisible(true);
   	}
}