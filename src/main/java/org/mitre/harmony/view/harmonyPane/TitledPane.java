// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.harmonyPane;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Component;
import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;

/**
 * Draws a 3D pane with a titled border around it
 * @author CWOLF
 */
public class TitledPane extends JPanel
{
	/** Draws a three dimensional border around a pane */
	public class LineBorder3D extends LineBorder
	{
		/** Initialize the border color as black */
		public LineBorder3D()
			{ super(Color.black); }
		
		/** Paint the border as three dimensional */
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
		{
			g.setColor(Color.white);
			g.drawLine(x,y,x+width-1,y);
			g.drawLine(x,y,x,y+height-1);
			g.setColor(Color.gray);
			g.drawLine(x+width-1,y+height-1,x+width-1,y);
			g.drawLine(x+width-1,y+height-1,x,y+height-1);
		}
	}
	
	/** Displays a 3D line border */
	private class BoxBorder extends LineBorder
	{
		/** Initializes the border to a color of gray and a width of 4 */
		BoxBorder()
			{ super(Color.gray,4); }
		
		/** Paint the border to be 3D */
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
		{
			// Set border color
			g.setColor(Color.gray);
			
			// Draw normal line rectangle
			g.drawLine(x+3,y+3,x+width-5,y+3);
			g.drawLine(x+3,y+3,x+3,y+height-5);
			g.drawLine(x+width-4,y+4,x+width-4,y+height-4);
			g.drawLine(x+4,y+height-4,x+width-4,y+height-4);
			
			// Draw 3D markings
			g.drawLine(x+width-3,y+5,x+width-3,y+height-3);
			g.drawLine(x+5,y+height-3,x+width-3,y+height-3);
		}
	}
	
	/** Generate the title pane */
	private JPanel getTitlePane(String title)
	{
		JPanel titlePane = new JPanel();
		titlePane.setBorder(new CompoundBorder(new LineBorder3D(), new EmptyBorder(2,2,2,2)));
		titlePane.setLayout(new BorderLayout());
		titlePane.add(new JLabel(title),BorderLayout.WEST);
		return titlePane;
	}
	
	/** Construct the 3D title pane */
	public TitledPane(String title, JComponent pane)
	{
		// Layout main pane
		JPanel mainPane = new JPanel();
		mainPane.setBorder(new LineBorder3D());
		mainPane.setLayout(new BorderLayout());
		mainPane.add(pane,BorderLayout.CENTER);
		
		// Layout titled pane
    	setBorder(new BoxBorder());
    	setLayout(new BorderLayout());
    	if(title!=null) add(getTitlePane(title),BorderLayout.NORTH);
    	add(mainPane,BorderLayout.CENTER);
   	}
}