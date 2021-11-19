// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.functionPane;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.dialogs.mappingCell.MappingCellDialog;
import org.mitre.harmony.view.mappingPane.MappingPane;
import org.mitre.schemastore.model.MappingCell;

/**
 * Holds function pane
 * @author KZHENG
 */
public class FunctionPane extends JPanel implements MouseListener, MouseMotionListener
{
	/** Variables used to keep track of mouse actions */
	private Point startPoint = null, endPoint = null;

	/** Stores the mappingPane to which the mouse pane is associated */
	private MappingPane mappingPane;

	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;

	/** Initializes the function pane */
	public FunctionPane(MappingPane mappingPane, HarmonyModel harmonyModel)
	{
		this.mappingPane = mappingPane;
		this.harmonyModel = harmonyModel;
		
		// Create the function pane
		JPanel functionPane = new JPanel();
		functionPane.setBackground(Color.white);
		functionPane.setOpaque(true);
		
		// Create the function details pane
		FunctionDetailsPane detailsPane = new FunctionDetailsPane();
		detailsPane.setPreferredSize(new Dimension(0, 72));
		
		// Create the function pane
		setBackground(Color.WHITE);
		setBorder(new CompoundBorder(new EmptyBorder(0,0,1,1),new LineBorder(Color.gray)));
		setOpaque(true);
		setLayout(new BorderLayout());
		add(functionPane, BorderLayout.CENTER);
		add(detailsPane, BorderLayout.SOUTH);		

		// Add mouse listeners		
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	/** Adjusts the point to the specified parent component */
	private Point adjustMouseLocation(Point point, Component referencePane)
	{
		int x = point.x, y = point.y;
		Component comp = this;
		while(comp!=null && !comp.equals(referencePane))
		{
			x += comp.getX();
			y += comp.getY();
			comp = comp.getParent();
		}
		return new Point(x, y);
	}
	
	/** Handles drawing of new mapping cells and selection of old mapping cells */
	public void mousePressed(MouseEvent e)
	{
		// If left button pressed, find start bounding box
		if(e.getButton() == MouseEvent.BUTTON1 && !e.isMetaDown())
			startPoint = e.getPoint();

		// If right button pressed, display mapping cell dialog box
		else if(e.getButton()==MouseEvent.BUTTON3 || e.isMetaDown())
		{
			// Determine what mapping cell was selected for showing the dialog box
			Point point = adjustMouseLocation(e.getPoint(), mappingPane);
			Integer mappingCellID = mappingPane.getLines().getClosestMappingCellToPoint(point); 
			if(mappingCellID != null)
			{
				// Mark the mapping cell as selected (if not already done)
				if(!harmonyModel.getSelectedInfo().isMappingCellSelected(mappingCellID))
				{
					ArrayList<Integer> mappingCellIDs = new ArrayList<Integer>();
					mappingCellIDs.add(mappingCellID);
					harmonyModel.getSelectedInfo().setMappingCells(mappingCellIDs, false);
				}

				// Display the dialog box next to the selected mapping cell
				ArrayList<MappingCell> mappingCells = new ArrayList<MappingCell>();
				for(Integer selectedMappingCellID : harmonyModel.getSelectedInfo().getSelectedMappingCells())
					mappingCells.add(harmonyModel.getMappingManager().getMappingCell(selectedMappingCellID));
				MappingCellDialog mappingCellDialog = new MappingCellDialog(mappingCells, harmonyModel);
				mappingCellDialog.setLocation(adjustMouseLocation(e.getPoint(), null));
				mappingCellDialog.setVisible(true);
			}
		}		
	}

	/** Handles the drawing of the new mapping cell or bounding box as the mouse is dragged around */
	public void mouseDragged(MouseEvent e)
		{ endPoint = e.getPoint(); repaint(); }

	/** Handles the selection of mapping cells in bounding box when mouse is released */
	public void mouseReleased(MouseEvent e)
	{
		// Only take action if left button is released
		if(e.getButton() == MouseEvent.BUTTON1)
		{
			// Adjust the mouse start and end points to correspond to the mapping pane
			startPoint = adjustMouseLocation(startPoint, mappingPane);
			if(endPoint!=null) endPoint = adjustMouseLocation(endPoint, mappingPane);
			
			// Select all mapping cells next to the clicked location or within the bounding box
			if(startPoint != null)
			{
				// Handles the case where a single mapping cell is selected
				if(endPoint == null)
				{
					Integer mappingCell = mappingPane.getLines().getClosestMappingCellToPoint(startPoint);
					ArrayList<Integer> mappingCells = new ArrayList<Integer>();
					if (mappingCell != null) mappingCells.add(mappingCell);
					harmonyModel.getSelectedInfo().setMappingCells(mappingCells, e.isControlDown());
				}

				// Handles the case where a bounding box was drawn around mapping cells to select
				else
				{
					int x1 = startPoint.x < endPoint.x ? startPoint.x : endPoint.x;
					int y1 = startPoint.y < endPoint.y ? startPoint.y : endPoint.y;
					int width = Math.abs(startPoint.x - endPoint.x) + 1;
					int height = Math.abs(startPoint.y - endPoint.y) + 1;
					Rectangle rect = new Rectangle(x1, y1, width, height);
					harmonyModel.getSelectedInfo().setMappingCells(mappingPane.getLines().getMappingCellsInRegion(rect), e.isControlDown());
				}
			}

			// Reinitialize the left and right paths for future use
			startPoint = null; endPoint = null; 
			repaint();
		}
	}

	/** Draw the selection reference */
	public void paint(Graphics g)
	{
		super.paint(g);
		
		if (startPoint != null && endPoint != null)
		{
			// Calculate points on rectangle to be drawn
			int x1 = startPoint.x < endPoint.x ? startPoint.x : endPoint.x;
			int y1 = startPoint.y < endPoint.y ? startPoint.y : endPoint.y;
			int width = Math.abs(startPoint.x - endPoint.x);
			int height = Math.abs(startPoint.y - endPoint.y);
			
			// Draw the selection rectangle
			((Graphics2D) g).setStroke(new BasicStroke((float) 1.0, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, (float) 0.0, new float[] { 5, 5 }, (float) 2.0));
			g.setColor(Color.darkGray);
			g.drawRect(x1, y1, width, height);
		}
	}

	// Unused listener events
	public void mouseClicked(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
}