// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.harmonyPane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.filters.FilterManager;

/**
 * Pane to display currently set filters
 * @author CWOLF
 */
public class FilterPane extends JPanel
{
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;

	/** Class for creating a filter checkbox */
	private class FilterCheckBox extends JCheckBox implements ActionListener
	{
		/** Stores the filter associated with this checkbox */
		private Integer filter;
		
		/** Constructs the checkbox */
		public FilterCheckBox(String label, Integer filter)
		{
			super(label);
			this.filter = filter;
			setFocusable(false);
			setSelected(harmonyModel.getFilters().getFilter(filter));
			addActionListener(this);
		}
		
		/** Informs the filter manager when a filter has been changed */
		public void actionPerformed(ActionEvent e)
			{ harmonyModel.getFilters().setFilter(filter,isSelected()); }
	}
	
	/** Initializes the assertion pane */
	public FilterPane(HarmonyModel harmonyModel)
	{
		this.harmonyModel = harmonyModel;
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		add(new FilterCheckBox("User",FilterManager.USER_FILTER));
		add(new FilterCheckBox("System",FilterManager.SYSTEM_FILTER));
		add(new JSeparator());
		add(new FilterCheckBox("Hierarchy",FilterManager.HIERARCHY_FILTER));
		add(new FilterCheckBox("Best",FilterManager.BEST_FILTER));
	}
}