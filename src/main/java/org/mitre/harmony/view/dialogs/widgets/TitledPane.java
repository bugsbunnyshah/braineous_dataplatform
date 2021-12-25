package org.mitre.harmony.view.dialogs.widgets;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;



/** Class used to construct a titled pane */
public class TitledPane extends JPanel
{
	/** Constructs the titled pane */
	public TitledPane(String title, JComponent pane)
	{
		setLayout(new BorderLayout());
		add(new JLabel(title),BorderLayout.NORTH);
		add(pane,BorderLayout.CENTER);			
	}

	/** Constructs the titled pane with a link */
	public TitledPane(String title, Link link, JComponent pane)
	{
		// Constructs the title pane
		JPanel titlePane = new JPanel();
		titlePane.setLayout(new BoxLayout(titlePane,BoxLayout.X_AXIS));
		titlePane.add(new JLabel(title));
		titlePane.add(link);
		
		// Constructs the titled pane
		setLayout(new BorderLayout());
		add(titlePane,BorderLayout.NORTH);
		add(pane,BorderLayout.CENTER);			
	}
}
