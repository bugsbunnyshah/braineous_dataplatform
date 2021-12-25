// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.controlPane;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.search.HarmonySearchListener;
import org.mitre.harmony.view.schemaTree.SchemaTree;

/**
 * Handles all keyword searching done of schema tree
 * @author CWOLF
 */
public class SchemaTreeSearch extends JPanel implements KeyListener, HarmonySearchListener
{
	/** Indicates the SchemaTree to which this search is tied */
	private SchemaTree tree;

	/** Field for storing the search string */
	private JTextField searchField = new JTextField();
	
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** Constructs the schema tree search field */
	public SchemaTreeSearch(SchemaTree tree, HarmonyModel harmonyModel)
	{
		this.tree = tree;
		this.harmonyModel = harmonyModel;
		
		// Lay out search box
		setLayout(new BorderLayout());
		add(new JLabel(" Search: "), BorderLayout.WEST);
		add(searchField, BorderLayout.CENTER);

		// Add listeners
		searchField.addKeyListener(this);
		harmonyModel.getSearchManager().addListener(this);
	}
	
	/** Update matches every time a new search keyword is entered */
	public void keyTyped(KeyEvent e)
	{
		if(e.getKeyChar() == KeyEvent.VK_ENTER)
			harmonyModel.getSearchManager().runQuery(tree.getSide(), searchField.getText());
	}

	/** React to search results being modified */
	public void searchResultsModified(Integer side)
	{
		if(tree.getSide().equals(side))
			searchField.setText(harmonyModel.getSearchManager().getQuery(side));
	}
	
	/** Unused listener actions */
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void highlightSettingChanged() {}
}
