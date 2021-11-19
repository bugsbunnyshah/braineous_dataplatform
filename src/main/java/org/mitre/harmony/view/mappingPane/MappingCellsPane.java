// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.mappingPane;

import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * Holds mapping cell pane which manages display of all mapping cells between schemas
 * @author CWOLF
 */
public class MappingCellsPane extends JPanel
{
	/** Stores the mapping pane to which this pane is associated */
	private MappingPane mappingPane;
	
	/** Initializes the link pane */
	public MappingCellsPane(MappingPane mappingPane)
		{ this.mappingPane = mappingPane; setOpaque(false); }

	/** Paints all links */
	public void paint(Graphics g)
		{ super.paint(g); mappingPane.getLines().paint(g); }
}
