// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.functionPane;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

/**
 * Constructs the pane to store function details
 * @author CWOLF
 */
public class FunctionDetailsPane extends JPanel
{
	/** Draw the function details pane */
	public void paint(Graphics g)
	{
		super.paint(g);
		g.setColor(Color.darkGray);
		g.drawLine(0, 0, getWidth()-1, 0);
	}

	// Unused listener events
	public void mouseClicked(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
}