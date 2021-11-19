// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.schemaTree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.tree.DefaultMutableTreeNode;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.filters.FilterManager;
import org.mitre.harmony.model.filters.Focus;
import org.mitre.harmony.view.dialogs.SchemaPropertiesDialog;
import org.mitre.harmony.view.dialogs.SchemaStatisticsDialog;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

/**
 * Constructs a popup menu to appear when schema nodes are right clicked
 * @author CWOLF
 */
class SchemaTreeNodeMenu extends JPopupMenu implements ActionListener
{
	// Objects associated with the schema tree node menu
	private SchemaTree tree;				// Tree associated with selected node
	private DefaultMutableTreeNode node;	// Node which was right-clicked

	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	// Menu items associated with the schema tree node menu
	private JMenuItem expand = new JMenuItem("Expand All");
	private JMenuItem collapse = new JMenuItem("Collapse All");
	private JMenuItem setFocus = new JMenuItem("Set Focus");
	private JMenuItem clearFocus = new JMenuItem("Clear Focus");
	private JMenuItem clearAllFoci = new JMenuItem("Clear All Foci");
	private JMenuItem hideChildElement = new JMenuItem("Hide Child Elements");
	private JMenuItem unhideChildElement = new JMenuItem("Unhide Child Elements");
	private JMenuItem markUnfinished = new JMenuItem("Mark as Unfinished");
	private JMenuItem markFinished = new JMenuItem("Mark as Finished");
	private JMenuItem statistics = new JMenuItem("View Statistics");
	private JMenuItem properties = new JMenuItem("Properties");
	
	/** Initialize the popup menu when a tree node is selected */
	SchemaTreeNodeMenu(SchemaTree tree, DefaultMutableTreeNode node, HarmonyModel harmonyModel)
	{
		this.tree = tree;
		this.node = node;
		this.harmonyModel = harmonyModel;
		
		// Get the schema and element associated with this menu
		Object obj = node.getUserObject();
		Integer schemaID = SchemaTree.getSchema(node);
		Integer elementID = obj instanceof Integer ? (Integer)obj : null;
		
		// Show collapse and expand menu options only if not leaf node
		if(!tree.getModel().isLeaf(node))
		{
			add(expand);
			add(collapse);
		}

		// Retrieve focus information for the selected node
		Focus focus = harmonyModel.getFilters().getFocus(tree.getSide(), schemaID);
		boolean isFocus = focus!=null && focus.getFocusedPaths().contains(SchemaTree.getElementPath(node));
		boolean isHidden = focus!=null && focus.getHiddenElements().contains(elementID);

		// Show menu option for allowing focus to be set and cleared
		if(getComponentCount()>0) add(new JSeparator());
		if(!isFocus && !isHidden) add(setFocus);
		if(isFocus) add(clearFocus);
		add(clearAllFoci);

		// Allow "matched" statistics to be viewed if node is schema
		if(obj instanceof Schema)
		{
			// Place menu option for editing the schema
			add(new JSeparator());
			add(statistics);
			add(properties);
		}
		
		// Show focus and mark/unmark menu options as needed
		if(obj instanceof Integer)
		{			
			// Show menu options for allowing elements to be hidden
			if(focus==null || !focus.getHiddenIDs().contains(elementID))
			{
				HierarchicalSchemaInfo schemaInfo = harmonyModel.getSchemaManager().getSchemaInfo(schemaID);
				if(schemaInfo.getChildElements(elementID).size()>0) add(hideChildElement);
			}
			if(focus!=null && focus.getHiddenIDs().contains(elementID)) add(unhideChildElement);			
			
			// Show menu option to mark/unmark items as finished
			if(harmonyModel.getFilters().inFocus(tree.getSide(),schemaID,elementID))
			{
				// Determine if the node and descendants are finished
				boolean isUnfinished = !harmonyModel.getPreferences().isFinished(schemaID,elementID);
				boolean isFinished = harmonyModel.getPreferences().isFinished(schemaID,elementID);
				for(SchemaElement descendant : harmonyModel.getSchemaManager().getDescendantElements(schemaID, elementID))
				{
					boolean isElementFinished = harmonyModel.getPreferences().isFinished(schemaID,descendant.getId());
					isUnfinished &= !isElementFinished;
					isFinished &= isElementFinished;
				}
				
				// Display options to mark the node and descendants as finished/unfinished
				add(new JSeparator());
				if(!isUnfinished) add(markUnfinished);
				if(!isFinished) add(markFinished);
			}
		}
		
		// Add listeners to all menu items
		expand.addActionListener(this);
		collapse.addActionListener(this);
		setFocus.addActionListener(this);
		clearFocus.addActionListener(this);
		clearAllFoci.addActionListener(this);
		hideChildElement.addActionListener(this);
		unhideChildElement.addActionListener(this);
		markUnfinished.addActionListener(this);
		markFinished.addActionListener(this);
		statistics.addActionListener(this);		
		properties.addActionListener(this);
	}
	
	/** Reacts to selection of various menu options */
	public void actionPerformed(ActionEvent e)
	{
		// Generate easy reference to managers
		FilterManager filters = harmonyModel.getFilters();
		
		// Retrieve various attributes associated with this node
		Integer side = tree.getSide();
		Integer schemaID = SchemaTree.getSchema(node);
		Integer elementID = node.getUserObject() instanceof Integer ? (Integer)node.getUserObject() : null;
		
		// Handles manipulation of schema tree
		if(e.getSource()==expand) tree.expandNode(node);
		if(e.getSource()==collapse) tree.collapseNode(node);
		if(e.getSource()==markUnfinished) tree.markNodeAsFinished(node,false);
		if(e.getSource()==markFinished) tree.markNodeAsFinished(node,true);

		// Handles focus settings
		if(e.getSource()==setFocus) filters.addFocus(side, schemaID, SchemaTree.getElementPath(node));
		if(e.getSource()==clearFocus) filters.removeFocus(side, schemaID, SchemaTree.getElementPath(node));
		if(e.getSource()==clearAllFoci) filters.removeAllFoci(side);

		// Handles hidden element settings
		if(e.getSource()==hideChildElement) filters.hideElement(side, schemaID, elementID);
		if(e.getSource()==unhideChildElement) filters.unhideElement(side, schemaID, elementID);
		
		// Handles the viewing of schema statistics
		if(e.getSource()==statistics)
		{
			SchemaStatisticsDialog dialog = new SchemaStatisticsDialog(schemaID,harmonyModel);
			//harmonyModel.getDialogManager().openDialog(dialog);
		}

		// Handles the viewing of schema properties
		if(e.getSource()==properties)
		{
			SchemaPropertiesDialog dialog = new SchemaPropertiesDialog(schemaID,harmonyModel);
			//harmonyModel.getDialogManager().openDialog(dialog);
		}
	}
}