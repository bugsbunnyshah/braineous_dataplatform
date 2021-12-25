// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.mappingPane;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import org.mitre.harmony.model.HarmonyConsts;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.selectedInfo.SelectedInfoListener;
import org.mitre.harmony.view.controlPane.ControlPane;
import org.mitre.harmony.view.functionPane.FunctionPane;
import org.mitre.schemastore.model.MappingCell;

/**
 * Displays the entire mapping pane including schema tree and linkages
 * @author CWOLF
 */
public class MappingPane extends JDesktopPane implements ComponentListener, LinesListener, SelectedInfoListener
{	
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** Stores the left and right schema tree associated with the mapping pane */
	private SchemaTreeImp leftTree=null, rightTree=null;

	/** Stores the function pane */
	private FunctionPane functionPane = null;
	
	/** Stores the mapping lines */
	private MappingLines mappingLines;

	// Reference to the various panes
	private JPanel mousePane = null;
	private JPanel linkPane = null;
	private JPanel mainPane = null;
	private JPanel leftInfoPane = null;
	private JPanel rightInfoPane = null;
		
	/** Subclass used to accept links */
	private class AcceptLink extends AbstractAction
	{
		/** Accept selected link */
		public void actionPerformed(ActionEvent arg0)
		{
			List<Integer> mappingCellIDs = harmonyModel.getSelectedInfo().getSelectedMappingCells();
			List<MappingCell> mappingCells = harmonyModel.getMappingManager().getMappingCellsByID(mappingCellIDs);
			harmonyModel.getMappingManager().validateMappingCells(mappingCells);
			harmonyModel.getSelectedInfo().setMappingCells(new ArrayList<Integer>(),false);
		}
	};
	
	/** Subclass used to reject links */
	private class RejectLink extends AbstractAction
	{
		/** Reject selected link */
		public void actionPerformed(ActionEvent arg0)
		{
			List<Integer> mappingCellIDs = harmonyModel.getSelectedInfo().getSelectedMappingCells();
			List<MappingCell> mappingCells = harmonyModel.getMappingManager().getMappingCellsByID(mappingCellIDs);
			harmonyModel.getMappingManager().deleteMappingCells(mappingCells);
			harmonyModel.getSelectedInfo().setMappingCells(new ArrayList<Integer>(),false);
		}
	};
	
	/** Constructs a schema tree pane */
	private JPanel getSchemaTreePane(SchemaTreeImp tree)
	{
		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		pane.add(new SchemaScrollPane(this,tree,harmonyModel),BorderLayout.CENTER);
		pane.add(new ControlPane(tree,harmonyModel),BorderLayout.SOUTH);
		return pane;
	}
	
	/** Initializes the mapping pane */
	public MappingPane(JComponent parent, HarmonyModel harmonyModel)
	{
		this.harmonyModel = harmonyModel;
		
		// Initialize the child panes of the main pane
		leftTree = new SchemaTreeImp(HarmonyConsts.LEFT, harmonyModel);
		functionPane = new FunctionPane(this, harmonyModel);
		rightTree = new SchemaTreeImp(HarmonyConsts.RIGHT, harmonyModel);
		mappingLines = new MappingLines(this, harmonyModel);

		// Create the main pane
		mainPane = new JPanel();
		mainPane.setLayout(new GridLayout(1,3));  
		mainPane.add(getSchemaTreePane(leftTree));
		mainPane.add(functionPane);
		mainPane.add(getSchemaTreePane(rightTree));
		
		// Set up the various layers to be displayed
		add(mainPane,DEFAULT_LAYER);
		add(linkPane = new MappingCellsPane(this),PALETTE_LAYER);
		add(leftInfoPane = new SelectedNodePane(HarmonyConsts.LEFT,harmonyModel),MODAL_LAYER);
		add(rightInfoPane = new SelectedNodePane(HarmonyConsts.RIGHT,harmonyModel),MODAL_LAYER);
		add(mousePane = new MousePane(this,harmonyModel),DRAG_LAYER);
		
		// Register keyboard actions for accepting links
		KeyStroke acceptKey = KeyStroke.getKeyStroke((char) KeyEvent.VK_SPACE);
		parent.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(acceptKey, "acceptLink");
		parent.getActionMap().put("acceptLink", new AcceptLink());
		
		// Register keyboard actions for deleting links
		KeyStroke deleteKey = KeyStroke.getKeyStroke((char) KeyEvent.VK_DELETE);
		parent.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(deleteKey, "deleteLink");
		KeyStroke backspaceKey = KeyStroke.getKeyStroke((char) KeyEvent.VK_BACK_SPACE);
		parent.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(backspaceKey, "deleteLink");
		parent.getActionMap().put("deleteLink", new RejectLink());
		
		// Adds listeners to watch for events where the mapping pane need to be redrawn
		addComponentListener(this);	
		getTreeViewport(HarmonyConsts.LEFT).addComponentListener(this);		
		mappingLines.addLinesListener(this);
		harmonyModel.getSelectedInfo().addListener(this);
	}	
	
	/** Returns reference to the function pane */
	public FunctionPane getFunctionPane()
		{ return functionPane; }
	
	/** Returns the mapping pane lines */
	public MappingLines getLines()
		{ return mappingLines; }
	
	/** Returns the schema tree */
	public SchemaTreeImp getTree(Integer role)
		{ return role.equals(HarmonyConsts.LEFT) ? leftTree : rightTree; }
	
	/** Returns the schema tree viewer */
	public JViewport getTreeViewport(Integer role)
		{ return role.equals(HarmonyConsts.LEFT) ? (JViewport)leftTree.getParent() : (JViewport)rightTree.getParent(); }
	
	/** Get the bounds of the specified pane in association with the mapping pane */
	public Rectangle getPaneBounds(Container container)
	{
		Rectangle loc = new Rectangle(0,0,container.getWidth(),container.getHeight());
		while(!(container instanceof MappingPane))
		{
			loc.x += container.getLocation().x; loc.y += container.getLocation().y;
			container = container.getParent();
		}
		return loc;
	}
	
	/** Adjust the size of the various components when this pane is resized */
	public void componentResized(ComponentEvent e)
	{
		// Set the bounds for the three main mapping panes
		if(e.getSource()==this)
		{
			mainPane.setBounds(0,0,getWidth(),getHeight());
			linkPane.setBounds(0,0,getWidth(),getHeight());
			mousePane.setBounds(0,0,getWidth(),getHeight());
		}
			
		// Set the bounds for the info panes
		else
		{
			Rectangle leftTreeBounds = getPaneBounds(leftTree.getParent());
			leftInfoPane.setBounds(leftTreeBounds.x+10,leftTreeBounds.height-150,leftTreeBounds.width-20,140);
			Rectangle rightTreeBounds = getPaneBounds(rightTree.getParent());
			rightInfoPane.setBounds(rightTreeBounds.x+10,rightTreeBounds.height-150,rightTreeBounds.width-20,140);
		}
			
		// Redraw the mapping pane with these changes in place
		revalidate(); repaint();
	}	
	
	/** Handles changes to the schema tree links */
	public void linesModified() { repaint(); }
	
	/** Handles changes to the selected nodes */
	public void displayedElementModified(Integer role)
	{
		SchemaTreeImp tree = role==HarmonyConsts.LEFT ? leftTree : rightTree;
		Integer border = harmonyModel.getSelectedInfo().getDisplayedElement(role)!=null ? 160 : 10;
		tree.setBorder(new EmptyBorder(0,0,border,0));
		tree.fireTreeExpanded(tree.getPathForRow(0));
		repaint();
	}

	/** Handles changes to the selected links */
	public void selectedMappingCellsModified() { repaint(); }

	// Unused listener events
	public void componentShown(ComponentEvent e) {}
	public void componentHidden(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}
	public void selectedElementsModified(Integer role) {}
}