// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs.mappingCell;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.JPanel;

import org.mitre.schemastore.model.MappingCell;

/**
 * Displays the pane displaying current mapping cell information
 * @author CWOLF
 */
class MappingCellInfoPane extends JPanel
{
	/** Stores the mapping cells */
	private List<MappingCell> mappingCells = null;
	
	/** Initializes the height of the pane */
	MappingCellInfoPane(List<MappingCell> mappingCells)
	{
		this.mappingCells = mappingCells;
		int height = getFontMetrics(getFont()).getHeight();
		setPreferredSize(new Dimension(200,height*2+15));
	}
		
	/** Draws the pane displaying mapping cell information */
	public void paint(Graphics g)
	{
		// Calculate the confidence range of the selected mapping cells
		Double minConf = 1.0;
		Double maxConf = 0.0;
		for(MappingCell mappingCell : mappingCells)
		{
			Double conf = mappingCell.getScore();
			if(conf < minConf) minConf = conf;
			if(conf > maxConf) maxConf = conf;
		}
		minConf = Math.round(minConf*100.0)/100.0;
		maxConf = Math.round(maxConf*100.0)/100.0;
			
		// Retrieves the height of the font
		int height = getFontMetrics(getFont()).getHeight();
			
		// Label computer selection line
		g.setColor(Color.black);
		g.drawString("No Evidence",5,height);
		int width = (int)g.getFontMetrics().getStringBounds("Much Evidence",g).getWidth();
		g.drawString("Much Evidence",195-width,height);
	
		// Draw computer selection line
		Graphics2D g2d = (Graphics2D)g;
		g2d.setPaint(new GradientPaint(5,height+4,Color.orange,100,height+4,Color.yellow));
		g2d.fillRect(5,height+3,95,3);
		g2d.setPaint(new GradientPaint(100,height+4,Color.yellow,195,height+4,Color.green));
		g2d.fillRect(100,height+3,95,3);
		
		// Point at selected point on computer selection line
		g.setColor(Color.black);
		int minXLoc = (int)(minConf*190)+5;
		int maxXLoc = (int)(maxConf*190)+5;
		int midXLoc = (minXLoc+maxXLoc)/2;
		int yLoc = height+5;
		if(minXLoc==maxXLoc)
		{
			int x[] = {midXLoc,midXLoc-5,midXLoc+4};
			int y[] = {yLoc,yLoc+5,yLoc+5};
			g.fillPolygon(x,y,3);
		}
		else
		{
			g.drawLine(minXLoc,yLoc+1,minXLoc,yLoc+5);
			g.drawLine(maxXLoc,yLoc+1,maxXLoc,yLoc+5);
			g.drawLine(minXLoc,yLoc+5,maxXLoc,yLoc+5);
		}
		g.drawLine(midXLoc,yLoc+5,midXLoc,height*2+5);
		g.drawLine(midXLoc,height*2+5,100,height*2+5);
		
		// Get label to display in link dialog box
		String label;
		label = "Evidence ("+minConf+(!minConf.equals(maxConf)?"-"+maxConf:"")+")";

		// Display label with arrow going to confidence location
		width = (int)g.getFontMetrics().getStringBounds(label,g).getWidth();
		g.setColor(getBackground());
		g.fillRect(100-width/2,height+15,width,height);
		g.setColor(Color.black);
		g.drawString(label,100-width/2,height*2+10);
	}
}