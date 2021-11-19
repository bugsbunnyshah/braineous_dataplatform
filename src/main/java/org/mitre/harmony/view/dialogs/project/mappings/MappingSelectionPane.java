package org.mitre.harmony.view.dialogs.project.mappings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.project.MappingManager;
import org.mitre.harmony.model.project.ProjectMapping;
import org.mitre.schemastore.model.Mapping;

/** Class for allowing the selection of mappings */
class MappingSelectionPane extends JPanel
{	
	/** Stores the org.org.mitre.harmony model  */
	private HarmonyModel harmonyModel;
	
	/** Set of check boxes containing the mapping selection */
	JPanel mappingList = null;
	
	/** Returns the list of items which make up this list */
	private ArrayList<MappingSelectionItem> getItems()
	{
		ArrayList<MappingSelectionItem> items = new ArrayList<MappingSelectionItem>();
		for(Component item : mappingList.getComponents())
			items.add((MappingSelectionItem)item);
		return items;
	}
	
	/** Constructs the Mapping List pane */
	MappingSelectionPane(HarmonyModel harmonyModel)
	{			
		this.harmonyModel = harmonyModel;
		
		// Create the mapping list
		mappingList = new JPanel();
		mappingList.setOpaque(false);
		mappingList.setLayout(new BoxLayout(mappingList,BoxLayout.Y_AXIS));

		// Force mapping list to not spread out
		JPanel mappingListPane = new JPanel();
		mappingListPane.setBackground(Color.white);
		mappingListPane.setLayout(new BorderLayout());
		mappingListPane.add(mappingList,BorderLayout.NORTH);
	
		// Create a scroll pane to hold the list of mappings
		JScrollPane mappingScrollPane = new JScrollPane(mappingListPane,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		mappingScrollPane.setPreferredSize(new Dimension(250, 200));

		// Creates the mapping selection pane
		setBorder(new EmptyBorder(0,0,5,0));
		setLayout(new BorderLayout());
		add(mappingScrollPane,BorderLayout.CENTER);
		
		// Populates the pane with current project mappings
		for(ProjectMapping mapping : harmonyModel.getMappingManager().getMappings())
			addMapping(mapping,mapping.isVisible());
	}
	
	/** Returns the list of currently listed mappings */
	ArrayList<Mapping> getMappings()
	{
		ArrayList<Mapping> mappings = new ArrayList<Mapping>();
		for(MappingSelectionItem item : getItems())
			mappings.add(item.getMapping());
		return mappings;
	}
	
	/** Adds mapping to the pane */
	void addMapping(Mapping mapping)
	{
		boolean selected = true;
		for(MappingSelectionItem item : getItems())
			if(item.isSelected())
			{
				if(mapping.getSourceId().equals(item.getMapping().getTargetId())) selected = false;
				if(mapping.getTargetId().equals(item.getMapping().getSourceId())) selected = false;
				if(selected==false) break;
			}
		addMapping(mapping, selected);
	}
	
	/** Adds mapping to the pane */
	void addMapping(Mapping mapping, boolean selected)
	{
		MappingSelectionItem newItem = new MappingSelectionItem(harmonyModel,mapping,selected);
		int loc;
		for(loc=0; loc<mappingList.getComponentCount(); loc++)
		{
			MappingSelectionItem item = (MappingSelectionItem)mappingList.getComponent(loc);
			if(item.toString().compareTo(newItem.toString())>0) break;
		}
		mappingList.add(newItem,loc);
		updateEnabledCheckboxes();
		revalidate(); repaint();
	}

	/** Deletes mapping from the pane */
	void deleteMappingItem(MappingSelectionItem item)
	{
		mappingList.remove(item);
		revalidate(); repaint();
	}
	
	/** Enable checkboxes based on selected mappings */
	void updateEnabledCheckboxes()
	{
		// Identify already used schemas
		HashSet<Integer> sourceSchemas = new HashSet<Integer>();
		HashSet<Integer> targetSchemas = new HashSet<Integer>();
		for(MappingSelectionItem item : getItems())
			if(item.isSelected())
			{
				sourceSchemas.add(item.getMapping().getSourceId());
				targetSchemas.add(item.getMapping().getTargetId());
			}
		
		// Enable/disable items based on used schemas
		for(MappingSelectionItem item : getItems())
			if(!item.isSelected())
			{
				Mapping mapping = item.getMapping();
				boolean enable = !sourceSchemas.contains(mapping.getTargetId()) && !targetSchemas.contains(mapping.getSourceId());
				item.setEnabled(enable);
			}
	}
	
	/** Saves the project mappings */
	public void save()
	{
		MappingManager mappingManager = harmonyModel.getMappingManager();
		
		// Generate a list of old mappings
		HashMap<String,ProjectMapping> oldMappings = new HashMap<String,ProjectMapping>();
		for(ProjectMapping mapping : mappingManager.getMappings())
			oldMappings.put(mapping.getSourceId()+"_"+mapping.getTargetId(), mapping);
		
		// Add any new mappings
		for(MappingSelectionItem item : getItems())
		{
			// Add any new mappings
			Mapping mapping = item.getMapping();
			String key = mapping.getSourceId()+"_"+mapping.getTargetId();
			if(!oldMappings.containsKey(key))
				mappingManager.addMapping(mapping);
			else oldMappings.remove(key);

			// Set the visibility of the mapping
			mappingManager.getMapping(mapping.getId()).setVisibility(item.isSelected());
		}
		
		// Remove any old mappings
		for(ProjectMapping mapping : oldMappings.values())
			mappingManager.removeMapping(mapping);
	}
}