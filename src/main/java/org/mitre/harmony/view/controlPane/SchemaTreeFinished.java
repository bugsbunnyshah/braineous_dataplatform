// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.controlPane;

import java.util.HashSet;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.preferences.PreferencesListener;
import org.mitre.harmony.view.schemaTree.SchemaTree;
import org.mitre.harmony.view.schemaTree.SchemaTreeListener;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

/**
 * Pane used to display the finished ratio of the schema tree
 */
public class SchemaTreeFinished extends JPanel implements PreferencesListener, SchemaTreeListener
{
	/** Stores the tree to which this pane is associated */
	private SchemaTree tree = null;
	
	/** Stores the Harmony Model */
	private HarmonyModel harmonyModel;
	
	// Variables to keep track of finished versus total elements
	private int finished = 0;
	private int total = 0;

	/** Stores the finished count label */
	private JLabel label = new JLabel("XX/XX"); 
	
	/** This method synchronizes the UI with the schema tree */
	private void refresh()
		{ label.setText(finished + "/" + total); }

	/** Creates a new progress widget tied to the indicated tree */
	public SchemaTreeFinished(SchemaTree tree, HarmonyModel harmonyModel)
	{
		this.tree = tree;
		this.harmonyModel = harmonyModel;
		add(new JLabel("Finished:"));
		add(label);
		schemaStructureModified(tree);
		harmonyModel.getPreferences().addListener(this);
		tree.addSchemaTreeListener(this);
		refresh();
	}

	/** Whenever the schema tree structure is modified, the UI must be refreshed */
	public void schemaStructureModified(SchemaTree tree)
	{
		// Reset the finished and total counts
		finished=0;
		total=0;
		
		// Count up all displayed elements in each schema
		for(Integer schemaID : tree.getSchemas())
		{
			HierarchicalSchemaInfo schemaInfo = harmonyModel.getSchemaManager().getSchemaInfo(schemaID);
			total += schemaInfo.getHierarchicalElements().size();
		}

		refresh();
	}
	
	/** Increase the finished count */
	public void elementsMarkedAsFinished(Integer schemaID, HashSet<Integer> elementIDs)
		{ if(tree.getSchemas().contains(schemaID)) { finished+=elementIDs.size(); refresh(); } }

	/** Decrease the finished count */
	public void elementsMarkedAsUnfinished(Integer schemaID, HashSet<Integer> elementIDs)
		{ if(tree.getSchemas().contains(schemaID)) { finished-=elementIDs.size(); refresh(); } }
		
	// Unused event listeners
	public void schemaDisplayModified(SchemaTree tree) {}
	public void alphabetizedChanged() {}
	public void displayedViewChanged() {}
	public void showSchemaTypesChanged() {}
	public void showCardinalityChanged() {}
}