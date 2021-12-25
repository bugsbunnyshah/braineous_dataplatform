// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.mitre.harmony.model.HarmonyModel;

/**
 * Displays the dialog for displaying general information about Harmony
 * @author CWOLF
 */
public class AboutDialog extends JInternalFrame implements MouseListener, MouseMotionListener
{
	/** Generates info pane */
	private JPanel infoPane()
	{
		// Sets up pane with Harmony image
		JPanel imagePane = new JPanel();
		imagePane.setBackground(Color.WHITE);
		imagePane.setBorder(new LineBorder(Color.DARK_GRAY));
		Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/org/mitre/org.org.mitre.harmony/view/graphics/SSM.jpg"));
		imagePane.add(new JLabel(new ImageIcon(image.getScaledInstance(75,75,Image.SCALE_SMOOTH))));

		// Set up link to MITRE website
		JLabel linkLabel = new JLabel("<html>Developed by The MITRE Corporation, McLean, VA, USA, <a href=www.org.mitre.org>www.org.mitre.org</a></html>");
		linkLabel.addMouseListener(this);
		linkLabel.addMouseMotionListener(this);
		
		// Sets up pane with all info text
		JPanel textPane = new JPanel();
		textPane.setBorder(new EmptyBorder(10,10,0,0));
		textPane.setLayout(new BoxLayout(textPane,BoxLayout.PAGE_AXIS));
		textPane.add(linkLabel);
		textPane.add(new JLabel("\u00a9 The MITRE Corporation 2009"));
		textPane.add(new JLabel("ALL RIGHTS RESERVED"));
		textPane.add(new JLabel("Harmony is available for distribution under the terms of the Apache 2.0 license."));
		
		// Sets up pane with all info
		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		pane.add(imagePane,BorderLayout.WEST);
		pane.add(textPane,BorderLayout.CENTER);
		return pane;
	}
	
	/** Generates description pane */
	private JPanel descriptionPane()
	{
		// Sets up the description in a text box
		JTextArea descPane = new JTextArea();
		descPane.setBorder(new EmptyBorder(10,0,0,0));
		descPane.setBackground(null);
		descPane.setLineWrap(true);
		descPane.setWrapStyleWord(true);
		descPane.setEditable(false);
		descPane.setText("The Harmony Schema Matcher automatically proposes candidate correspondences " +
						 "between source and target schema elements.  The Harmony GUI allows an integration " +
						 "engineer to view a set of correspondences and manually edit the correspondences.  " +
						 "Harmony manages user schemata and mappings in a bundled repository.\n\n" +
						 "An overview of Harmony appears in: Peter Mork, Len Seligman, Arnon Rosenthal, " +
						 "Joel Korb, and Chris Wolf, (c)The Harmony Integration Workbench,(c) Journal on GraphData " +
						 "Semantics, vol. 11, pp. 65(c)93, Dec 2008.  Harmony(c)s use in large scale schema " +
						 "matching problems is described in:  Ken Smith, Peter Mork, Len Seligman, Arnon " +
						 "Rosenthal, Michael Morse, David M. Allen, and Maya Li, (c)The Role of Schema Matching " +
						 "in Large Enterprises(c), Conference on Innovative Database Research (CIDR (c)09), Jan 2009.\n\n" +
						 "Harmony is a component of the OpenII (Open Information Integration) framework; OpenII " +
						 "is a collaborative effort to create a suite of open-source tools for information " +
						 "integration.  For more information, see openintegration.org.");

		// Place description into a pane
		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		pane.add(descPane,BorderLayout.CENTER);
		return pane;
	}
	
	/** Initializes About pane within About dialog */
	private JPanel aboutPane()
	{		
		JPanel aboutPane = new JPanel();
		aboutPane.setBorder(new EmptyBorder(10,10,10,10));
		aboutPane.setLayout(new BorderLayout());
		aboutPane.add(infoPane(),BorderLayout.NORTH);
		aboutPane.add(descriptionPane(),BorderLayout.CENTER);
		return aboutPane;
	}
	
	/** Initializes About dialog */
	public AboutDialog(HarmonyModel harmonyModel)
	{
		super("About Harmony");
		setSize(600,390);
		setClosable(true);
		setContentPane(aboutPane());
    	setVisible(true);
   	}
	
	/** Launches MITRE webpage when link is selected */
	public void mouseClicked(MouseEvent e)
	{
		try {
			Graphics g = getGraphics();
			double min = g.getFontMetrics().getStringBounds("The MITRE Corporation, McLean, VA, USA, ",g).getWidth();
			double max = min + g.getFontMetrics().getStringBounds("www.org.mitre.org",g).getWidth();
			if(e.getX()>min && e.getX()<max)
			{
				String os = System.getProperty("os.name");		
				if(os != null && os.startsWith("Windows"))
					Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler http://www.mitre.org");
				else
					Runtime.getRuntime().exec("netscape http://www.javaworld.com");
			}
		} catch(IOException e2) {}
	}
	
	/** Changes cursor to a hand whenever over web link */
	public void mouseMoved(MouseEvent e)
	{
		// Calculate out the location of the web link
		Graphics g = getGraphics();
		double min = g.getFontMetrics().getStringBounds("Developed by The MITRE Corporation, McLean, VA, USA, ",g).getWidth();
		double max = min + g.getFontMetrics().getStringBounds("www.org.mitre.org",g).getWidth();

		// Change the mouse cursor dependent on if mouse is over web link or not
		if(e.getX()>min && e.getX()<max)
		{
			if(getCursor()!=Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
		else if(getCursor()!=Cursor.getDefaultCursor())
			setCursor(Cursor.getDefaultCursor());

	}
	
	/** Always changes the mouse back to default when it exits the info component */
	public void mouseExited(MouseEvent e)
		{ setCursor(Cursor.getDefaultCursor()); }
	
	// Unused mouse listener actions
	public void mouseEntered(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
}
