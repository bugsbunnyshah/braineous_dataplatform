// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.mappingPane;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.mitre.harmony.model.HarmonyConsts;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.project.MappingManager;
import org.mitre.harmony.view.schemaTree.SchemaTree;
import org.mitre.schemastore.model.MappingCell;

/**
 * Holds mouse pane which manages all mouse actions
 * 
 * @author CWOLF
 */
class MousePane extends JPanel implements MouseListener, MouseMotionListener
{
	/** Variables used to keep track of mouse actions */
	private TreePath leftPath = null, rightPath = null;
	private Point endPoint = null;

	/** Stores the mappingPane to which the mouse pane is associated */
	private MappingPane mappingPane;

	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;

	/** Initializes the mouse pane */
	MousePane(MappingPane mappingPane, HarmonyModel harmonyModel)
	{
		// Initialize the mouse pane
		this.mappingPane = mappingPane;
		this.harmonyModel = harmonyModel;
		setOpaque(false);

		// Add mouse listeners		
		mappingPane.getTree(HarmonyConsts.LEFT).addMouseListenr(this);
		mappingPane.getTree(HarmonyConsts.LEFT).addMouseMotionListenr(this);
		mappingPane.getTree(HarmonyConsts.RIGHT).addMouseListenr(this);
		mappingPane.getTree(HarmonyConsts.RIGHT).addMouseMotionListenr(this);
	}

	/** Gets the schema tree path associated with the specified point */
	private void getSelectedRow(Point point)
	{
		TreePath path;
		if(isValidPath(HarmonyConsts.LEFT, path = mappingPane.getTree(HarmonyConsts.LEFT).getPathForLoc(point.x, point.y))) leftPath = path;
		if(isValidPath(HarmonyConsts.RIGHT, path = mappingPane.getTree(HarmonyConsts.RIGHT).getPathForLoc(point.x, point.y))) rightPath = path;
	}

	/** Determines if the indicated path can be the left or right of gestures. */
	private boolean isValidPath(Integer role, TreePath path)
	{
		// If the path does not exist, or is the root disallow gestures.
		if(path == null) return false;
		if(path.getPathCount() <= 1) return false;

		// Otherwise, the path is valid if the indicated node is visible
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		if(!(node.getUserObject() instanceof Integer)) return false;
		return harmonyModel.getFilters().isVisibleNode(role, node);
	}

	/** Handles the selection of a node from which a mapping cell is being created */
	public void mousePressed(MouseEvent e)
	{
		if(e.getButton() == MouseEvent.BUTTON1 && !e.isMetaDown())
			getSelectedRow(e.getPoint());
	}

	/** Handles the drawing of the new mapping cell or bounding box as the mouse is dragged around */
	public void mouseDragged(MouseEvent e)
	{
		endPoint = e.getPoint();
		
		// Calculate selected row point
		Rectangle rect = null;
		if(leftPath != null) rect = mappingPane.getTree(HarmonyConsts.LEFT).getPthBounds(leftPath);
		if(rightPath != null) rect = mappingPane.getTree(HarmonyConsts.RIGHT).getPthBounds(rightPath);

		// Draw line between selected row and mouse
		if(rect != null) repaint();
	}

	/** Handles the creation of a new link or selects mapping cells in bounding box when mouse is released */
	public void mouseReleased(MouseEvent e)
	{
		// Only take action if left button is released
		if(e.getButton() == MouseEvent.BUTTON1)
		{
			// Determine if a node was selected
			getSelectedRow(e.getPoint());

			// Ensure that the link was drawn between a left and right element
			if(leftPath != null && rightPath != null)
			{
				// Retrieve the associated mapping
				Integer sourceSchemaID = SchemaTree.getSchema((DefaultMutableTreeNode)leftPath.getLastPathComponent());
				Integer targetSchemaID = SchemaTree.getSchema((DefaultMutableTreeNode)rightPath.getLastPathComponent());
				Integer mappingID = harmonyModel.getMappingManager().getMapping(sourceSchemaID, targetSchemaID).getId();				
				
				// Gather info on what left and right nodes are connected
				Integer leftID = SchemaTree.getElement(leftPath);
				Integer rightID = SchemaTree.getElement(rightPath);

				// Generate the mapping cell
				MappingManager manager = harmonyModel.getMappingManager();
				Integer id = manager.getMappingCellID(leftID, rightID);
				//String author = harmonyModel.getUserName();
				Date date = Calendar.getInstance().getTime();
				MappingCell mappingCell = MappingCell.createIdentityMappingCell(id, mappingID, leftID, rightID, "author", date, null);
				manager.getMapping(mappingID).setMappingCells(Arrays.asList(new MappingCell[]{mappingCell}));
			}
			repaint();

			// Reinitialize the left and right paths for future use
			endPoint = null; leftPath = null; rightPath = null;
		}
	}

	/** Draw the possible new mapping line */
	public void paint(Graphics g)
	{
		if((leftPath != null || rightPath != null) && endPoint != null)
		{
			Rectangle rect = null;
			if(leftPath != null) rect = mappingPane.getTree(HarmonyConsts.LEFT).getPthBounds(leftPath);
			if(rightPath != null) rect = mappingPane.getTree(HarmonyConsts.RIGHT).getPthBounds(rightPath);
			if(rect != null)
			{
				g.setColor(Color.red);
				g.drawLine((int) rect.getCenterX(), (int) rect.getCenterY(), endPoint.x, endPoint.y);
			}
		}
		super.paint(g);
	}

	// Unused listener events
	public void mouseClicked(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
}