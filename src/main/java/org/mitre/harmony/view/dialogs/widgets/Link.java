package org.mitre.harmony.view.dialogs.widgets;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JLabel;

/** Class to show a link in Harmony */
public class Link extends JLabel implements MouseListener, MouseMotionListener
{
	/** Stores the action listener to be triggered when the link is selected */
	private ActionListener listener = null;
	
	/** Constructs the link */
	public Link(String label, ActionListener listener)
	{
		super("("+label+")");
		this.listener = listener;
		setForeground(Color.blue);
		setFont(new Font("Default",Font.BOLD,getFont().getSize()-2));
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	/** Fires off the action listener */
	public void mouseClicked(MouseEvent e)
	{
		String label = getText();
		label = label.substring(1,label.length()-2);
		listener.actionPerformed(new ActionEvent(this,0,label));
	}

	// Handles the changing of the mouse icon
	public void mouseEntered(MouseEvent arg0) { setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
	public void mouseExited(MouseEvent arg0) { setCursor(Cursor.getDefaultCursor()); }
	
	// Unused mouse listeners
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
	public void mouseDragged(MouseEvent arg0) {}
	public void mouseMoved(MouseEvent arg0) {}
}
