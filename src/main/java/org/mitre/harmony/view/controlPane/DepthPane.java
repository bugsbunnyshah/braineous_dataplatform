// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.controlPane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;

import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.plaf.metal.MetalSliderUI;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.filters.FiltersListener;
import org.mitre.harmony.view.schemaTree.SchemaTree;
import org.mitre.harmony.view.schemaTree.SchemaTreeListener;

/**
 * Class used to display the depth slider
 */
public class DepthPane extends JPanel
{
	// Stores the slider's min and max labels
	private JLabel minDepthLabel = new JLabel("Depth 1 ");
	private JLabel maxDepthLabel = new JLabel(" Depth 1");
	
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** Class used to draw and manager the depth slider */
	private class DepthSlider extends JSlider implements SchemaTreeListener, FiltersListener, ComponentListener
	{
		/** Class serving as the depth slider's UI */
		private class DepthSliderUI extends MetalSliderUI implements MouseListener, MouseMotionListener
		{			
			@Override
			public void installUI(JComponent c)
			{
				UIManager.put("Slider.trackWidth",new Integer(7));
				UIManager.put("Slider.majorTickLength",new Integer(6));
				UIManager.put("Slider.horizontalThumbIcon",MetalIconFactory.getHorizontalSliderThumbIcon());
				UIManager.put("Slider.verticalThumbIcon",MetalIconFactory.getVerticalSliderThumbIcon());
				super.installUI(c);
			}

			/** Helps get and set positions on the depth slider */
			private int getMinPos() { return (int)trackRect.getMinX() + (minValue-slider.getMinimum())*trackRect.width/(slider.getMaximum()-slider.getMinimum()); }	
			private int getMaxPos() { return (int)trackRect.getMinX() + (maxValue-slider.getMinimum())*trackRect.width/(slider.getMaximum()-slider.getMinimum()); }	
			private void setMinPos(int pos) { minValue = valueForXPosition(pos); }
			private void setMaxPos(int pos) { maxValue = valueForXPosition(pos); }
		
			/** Initializes depth level slider UI */
			DepthSliderUI(JSlider slider)
			{
				slider.addMouseListener(this);
				slider.addMouseMotionListener(this);
			}
			
			/** Paints the min and max thumbs for the depth slider */
			public void paintThumb(Graphics g)
			{
				// Store original clip shape
				Shape origShape = g.getClip();
				g.setColor(isFocusOwner() ? new Color(99,130,191) : Color.black);
				
				// Draw min thumb
				thumbRect.x = getMinPos()-thumbRect.width/2;
				g.setClip(getMinPos()-thumbRect.width/2,thumbRect.y,thumbRect.width/2+1,thumbRect.height);
				super.paintThumb(g);
				g.drawLine((int)thumbRect.getCenterX(),thumbRect.y,(int)thumbRect.getCenterX(),(int)thumbRect.getMaxY());
				
				// Draw max thumb
				thumbRect.x = getMaxPos()-thumbRect.width/2;
				g.setClip(getMaxPos(),thumbRect.y,thumbRect.width/2+1,thumbRect.width);
				super.paintThumb(g);
				g.drawLine((int)thumbRect.getCenterX(),thumbRect.y,(int)thumbRect.getCenterX(),(int)thumbRect.getMaxY());
				
				// Restore original clip shape
				g.setClip(origShape);
			}
			
			/** Paint the depth slider track to be between the min and max thumbs */
			public void paintTrack(Graphics g)
			{
				// First, draw entire track as empty
				thumbRect.x=0;
				super.paintTrack(g);
				
				// Then, draw in a filled track between the min and max thumbs
				Shape origShape = g.getClip();
				g.setClip(getMinPos(),getY(),getMaxPos()-getMinPos(),getHeight());
				thumbRect.x=getMaxPos();
				super.paintTrack(g);
				g.setClip(origShape);
				
				// Finally, draw tick marks to mark slider divisions
				g.setColor(Color.darkGray);
				for(int i=1; i<getMaximum(); i++)
				{
					int xLoc = (int)(trackRect.getMinX() + i*trackRect.getWidth()/(getMaximum()-1));
					g.drawLine(xLoc,7,xLoc,12);
				}
			}
		
			/** Handles painting of the depth slider */
			public void paint(Graphics g, JComponent component)
			{
				paintTrack(g);
				paintThumb(g);
				paintTicks(g);
				paintLabels(g);
			}
			
			/** Variables to track which slider knob is currently active */
			private boolean minThumbSelected = false;
			private boolean maxThumbSelected = false;

			/** Determines what slider thumb to make active */
			public void mousePressed(MouseEvent e)
			{
				if(e.getY()>=thumbRect.y && e.getY()<=thumbRect.y+thumbRect.height)
				{
					if(e.getX()<=getMinPos() && e.getX()>=getMinPos()-thumbRect.getHeight()/2) minThumbSelected = true;
					if(e.getX()>=getMaxPos() && e.getX()<=getMaxPos()+thumbRect.getHeight()/2) maxThumbSelected = true;
				}
			}

			/** Make both slider thumbs inactive */
			public void mouseReleased(MouseEvent e)
				{ minThumbSelected = false; maxThumbSelected = false; }
			
			/** Moves the active slider thumb based on mouse movement */
			public void mouseDragged(MouseEvent e)
			{
				// First, store old values to make sure update is only done if needed
				int origMinValue = minValue;
				int origMaxValue = maxValue;
				
				// Move active thumb based on mouse movement
				if(minThumbSelected || maxThumbSelected)
				{
					if(minThumbSelected) { setMinPos(e.getX()-3); if(getMinPos()>getMaxPos()) setMaxPos(getMinPos()); }
					if(maxThumbSelected) { setMaxPos(e.getX()+3); if(getMaxPos()<getMinPos()) setMinPos(getMaxPos()); }
					if(getMinPos()<trackRect.x) setMinPos(trackRect.x);
					if(getMaxPos()<getMinPos()) setMaxPos(getMinPos());
					if(getMaxPos()>trackRect.getMaxX()) setMaxPos((int)trackRect.getMaxX());
					if(getMinPos()>getMaxPos()) setMinPos(getMaxPos());
					repaint();
				}
				
				// Reset the min and max value as needed
				if(minValue!=origMinValue) setMinValue(minValue);
				if(maxValue!=origMaxValue) setMaxValue(maxValue);
			}

			/** Events which can be ignored since they have no effect on the depth slider UI */
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseMoved(MouseEvent e) {}
		}

		// Stores schema tree to which this depth slider is associated
		private Integer side;
		
		// Stores the min and max values for the depth slider
		private int minValue = 1;
		private int maxValue = 1;

		/** Set min value and inform listeners */
		private void setMinValue(int minValueIn)
		{
			minValue = minValueIn;
			minDepthLabel.setText("Depth " + minValue + " ");
			harmonyModel.getFilters().setDepth(side,minValue,maxValue);
		}
		
		/** Set max value and inform listeners */
		private void setMaxValue(int maxValueIn)
		{
			maxValue = maxValueIn;
			maxDepthLabel.setText(" Depth " + maxValue);
			harmonyModel.getFilters().setDepth(side,minValue,maxValue);
		}		
		
		/** Depth slider constructor */
		private DepthSlider(SchemaTree tree)
		{
			// Initialize the role associated with this depth pane
			side = tree.getSide();

			// Initialize the min and max depth values
			setMinimum(minValue = 1);
			int maxValue = tree.root.getDepth()-1;
			if(maxValue<2) maxValue=2;
			setMaximum(maxValue);
			
			// Initialize all depth slider attributes
			setBorder(new EmptyBorder(2,0,2,2));
			setOrientation(JSlider.HORIZONTAL);
			setMajorTickSpacing(1);
			setUI(new DepthSliderUI(this));
			
			// Initialize the positioning of the sliders
			schemaStructureModified(tree);
			
			// Add listeners to monitor various events that affect depth range
			addComponentListener(this);
			tree.addSchemaTreeListener(this);
			harmonyModel.getFilters().addListener(this);
		}

		/** When the schema tree is modified, the slider's maximum value is updated and its value reset */
		public void schemaStructureModified(SchemaTree tree)
		{
			int maxValue = tree.root.getDepth()-1;
			if(maxValue<2) maxValue=2;
			setMaximum(maxValue);
			setMaxValue(getMaximum());
		}

		/** When the depth changes, the depth sliders must be repainted */
		public void depthChanged(Integer side)
			{ if(this.side.equals(side)) repaint(); }

		/** If the depth slider is resized, make sure that whole Harmony screen is refreshed */
		public void componentResized(ComponentEvent arg0)
		{
			Container parent = getParent();
			while(parent.getParent()!=null)
				parent = parent.getParent();
			parent.repaint();
		}
		
		/** Unused event listeners */
		public void schemaDisplayModified(SchemaTree tree) {}
		public void filterChanged(Integer filter) {}
		public void focusChanged(Integer side) {}
		public void confidenceChanged() {}
		public void maxConfidenceChanged(Integer schemaObjectID) {}
		public void componentHidden(ComponentEvent arg0) {}
		public void componentMoved(ComponentEvent arg0) {}
		public void componentShown(ComponentEvent arg0) {}
	}

	/** Creates a new depth pane linked to the indicated tree */
	public DepthPane(SchemaTree tree, HarmonyModel harmonyModel)
	{	
		this.harmonyModel = harmonyModel;
		
		// Place slider in pane
		setBorder(new EmptyBorder(2,5,2,5));
		setLayout(new BorderLayout());
		add(minDepthLabel,BorderLayout.WEST);
		add(new DepthSlider(tree),BorderLayout.CENTER);
		add(maxDepthLabel,BorderLayout.EAST);
	}
}