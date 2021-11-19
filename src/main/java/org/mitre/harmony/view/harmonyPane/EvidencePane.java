// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.harmonyPane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.plaf.metal.MetalSliderUI;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.project.ProjectMapping;
import org.mitre.harmony.model.selectedInfo.SelectedInfoListener;
import org.mitre.schemastore.model.MappingCell;

/**
 * Pane to display currently set confidence level
 * @author CWOLF
 */
public class EvidencePane extends JPanel implements SelectedInfoListener
{
	// Manages confidence range constants within Harmony
	public static final int CONFIDENCE_SCALE = 100;
	private static final int MIN_CONFIDENCE = new Double(ProjectMapping.MIN_CONFIDENCE*CONFIDENCE_SCALE).intValue();
	private static final int MAX_CONFIDENCE = new Double(ProjectMapping.MAX_CONFIDENCE*CONFIDENCE_SCALE).intValue();
	
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** Class constructing the evidence slider */
	private class EvidenceSlider extends JSlider
	{
		/** Class to handle display and action of the evidence slider */
		private class EvidenceSliderUI extends MetalSliderUI implements MouseListener, MouseMotionListener
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

			//--------------------------------------------------------------
			// Purpose: Helps get and set positions on the evidence slider
			//--------------------------------------------------------------
			private int getMinPos() { return (int)trackRect.getMaxY() - (minValue-slider.getMinimum())*trackRect.height/(slider.getMaximum()-slider.getMinimum()); }	
			private int getMaxPos() { return (int)trackRect.getMaxY() - (maxValue-slider.getMinimum())*trackRect.height/(slider.getMaximum()-slider.getMinimum()); }	
			private void setMinPos(int pos) { minValue = valueForYPosition(pos); }
			private void setMaxPos(int pos) { maxValue = valueForYPosition(pos); }
		
			/** Initializes evidence level slider */
			EvidenceSliderUI(JSlider slider)
			{
				slider.addMouseListener(this);
				slider.addMouseMotionListener(this);
			}
	
			/** Paints the min and max thumbs for the evidence slider */
			public void paintThumb(Graphics g)
			{
				// Store original clip shape
				Shape origShape = g.getClip();
				g.setColor(isFocusOwner() ? new Color(99,130,191) : Color.black);
				
				// Draw min thumb
				thumbRect.y = getMinPos()-thumbRect.height/2;
				g.setClip(thumbRect.x,getMinPos(),thumbRect.width,thumbRect.height/2+1);
				super.paintThumb(g);
				g.drawLine(thumbRect.x,(int)thumbRect.getCenterY(),(int)thumbRect.getMaxX(),(int)thumbRect.getCenterY());
				
				// Draw max thumb
				thumbRect.y = getMaxPos()-thumbRect.height/2;
				g.setClip(thumbRect.x,getMaxPos()-thumbRect.height/2,thumbRect.width,thumbRect.height/2+1);
				super.paintThumb(g);
				g.drawLine(thumbRect.x,(int)thumbRect.getCenterY(),(int)thumbRect.getMaxX(),(int)thumbRect.getCenterY());
				
				// Restore original clip shape
				g.setClip(origShape);
			}
			
			/** Paints the track for the evidence slider */
			public void paintTrack(Graphics g)
			{
				// First, draw entire track as empty
				thumbRect.y=0;
				super.paintTrack(g);
				
				// Save original graphic clip shape and then temporarily clip to defined range
				Shape origShape = g.getClip();
				g.setClip(getX(),getMaxPos(),getWidth(),getMinPos()-getMaxPos());
				
				// Calculate out the various color range end points
				int min=yPositionForValue(MIN_CONFIDENCE);
				int max=yPositionForValue(MAX_CONFIDENCE);
				int pt1=(min-max)/2+max;
				
				// Paint the color ranges
				Graphics2D g2d = (Graphics2D)g;
				g2d.setPaint(new GradientPaint(21,max,Color.green,21,pt1,Color.yellow));
				g2d.fillRect(21,max,4,pt1-max+1);
				g2d.setPaint(new GradientPaint(21,pt1,Color.yellow,21,min,Color.orange));
				g2d.fillRect(21,pt1,4,min-pt1);
				
				// Reset graphic clip shape with original
				g.setClip(origShape);
			}
			
			/** Handles painting of the evidence slider */
			public void paint(Graphics g, JComponent component)
			{
				// Draw the evidence slider track
				paintTrack(g);
				
				// Marks the currently selected mapping cell confidence
				List<Integer> selectedMappingCells = harmonyModel.getSelectedInfo().getSelectedMappingCells();
				if(selectedMappingCells.size()==1)
				{
					MappingCell mappingCell = harmonyModel.getMappingManager().getMappingCell(selectedMappingCells.get(0));
					int yLoc = yPositionForValue((int)(100*mappingCell.getScore()));
					g.setColor(Color.blue);
					g.fillPolygon(new int[]{25,31,31},new int[]{yLoc,yLoc-5,yLoc+5},3);
					g.fillPolygon(new int[]{21,16,16},new int[]{yLoc,yLoc-5,yLoc+5},3);
				}			

				// Draws various parts of the evidence slider
				paintThumb(g);
				paintTicks(g);
				paintLabels(g);
			}
			
			// Variables to track which slider knob is currently active
			private boolean minThumbSelected = false;
			private boolean maxThumbSelected = false;

			/** Determines what slider thumb to make active */
			public void mousePressed(MouseEvent e)
			{
				if(e.getX()>=thumbRect.x && e.getX()<=thumbRect.x+thumbRect.width)
				{
					if(e.getY()>=getMinPos() && e.getY()<=getMinPos()+thumbRect.getHeight()/2) minThumbSelected = true;
					if(e.getY()<=getMaxPos() && e.getY()>=getMaxPos()-thumbRect.getHeight()/2) maxThumbSelected = true;
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
					if(minThumbSelected) { setMinPos(e.getY()-3); if(getMinPos()<getMaxPos()) setMaxPos(getMinPos()); }
					if(maxThumbSelected) { setMaxPos(e.getY()+3); if(getMaxPos()>getMinPos()) setMinPos(getMaxPos()); }
					if(getMinPos()>trackRect.getMaxY()) setMinPos((int)trackRect.getMaxY());
					if(getMaxPos()>getMinPos()) setMaxPos(getMinPos());
					if(getMaxPos()<trackRect.y) setMaxPos(trackRect.y);
					if(getMinPos()<getMaxPos()) setMinPos(getMaxPos());
					repaint();
				}
				
				// Adjust confidence filter settings if changes occurred
				if(minValue!=origMinValue || maxValue!=origMaxValue)
					{ harmonyModel.getFilters().setConfidence(1.0*minValue/CONFIDENCE_SCALE,1.0*maxValue/CONFIDENCE_SCALE); }
			}
	
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseMoved(MouseEvent e) {}
		}
		
		// Holds the min and max value being controlled by the slider
		private int minValue = 0;
		private int maxValue = 0;
		
		/** Initializes the evidence slider */
		private EvidenceSlider()
		{
			// Initialize super class of evidence slider
			super(MIN_CONFIDENCE,MAX_CONFIDENCE);
			
			// Initialize labels for evidence slider
			Hashtable<Integer,JLabel> labels = new Hashtable<Integer,JLabel>();
			for(int i=MIN_CONFIDENCE; i<=MAX_CONFIDENCE; i+=(MAX_CONFIDENCE-MIN_CONFIDENCE)/5)
			{
				String label = new Double(1.0*i/CONFIDENCE_SCALE).toString().replaceAll("(\\..).*","$1");
				labels.put(new Integer(i), new JLabel(" "+label));
			}
			
			// Set attributes of evidence slider
			setOrientation(JSlider.VERTICAL);
			setMajorTickSpacing((MAX_CONFIDENCE-MIN_CONFIDENCE) / 5);
			setMinorTickSpacing((MAX_CONFIDENCE-MIN_CONFIDENCE) / 20);
			setPaintTicks(true);
			setPaintLabels(true);
			setLabelTable(labels);
			setUI(new EvidenceSliderUI(this));
			
			// Initialize the min and max confidence values
			minValue = new Double(harmonyModel.getFilters().getMinConfThreshold()*CONFIDENCE_SCALE).intValue();
			maxValue = new Double(harmonyModel.getFilters().getMaxConfThreshold()*CONFIDENCE_SCALE).intValue();
		}
	}
	
	/** Initializes the evidence pane */
	public EvidencePane(HarmonyModel harmonyModel)
	{
		this.harmonyModel = harmonyModel;
		setLayout(new BorderLayout());
		add(new EvidenceSlider(),BorderLayout.CENTER);
		harmonyModel.getSelectedInfo().addListener(this);
	}

	/** Repaints the evidence pane if the selected mapping cells were modified */
	public void selectedMappingCellsModified()
		{ repaint(); }
	
	// Unused listener events
	public void displayedElementModified(Integer role) {}
	public void selectedElementsModified(Integer role) {}
}