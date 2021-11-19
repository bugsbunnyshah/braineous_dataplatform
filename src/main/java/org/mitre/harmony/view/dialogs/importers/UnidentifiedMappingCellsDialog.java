// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs.importers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.mitre.schemastore.porters.mappingImporters.MappingCellPaths;

/**
 * Displays the schema dialog
 * @author CWOLF
 */
public class UnidentifiedMappingCellsDialog extends JInternalFrame
{	
	/** Constructs a pane to display a set of mapping cell pane */
	private JPanel generateMappingCellPane(MappingCellPaths mappingCell)
	{
		List<String> inputPaths = mappingCell.getInputPaths();
		
		// Generate the labels pane
		JPanel labelsPane = new JPanel();
		labelsPane.setBorder(new EmptyBorder(1,0,1,1));
		labelsPane.setLayout(new GridLayout(inputPaths.size()+1,1));
		for(int i=0; i<=inputPaths.size(); i++)
			labelsPane.add(new JLabel(i==0?" Inputs: ":i==inputPaths.size()?"Output: ":""));

		// Generate the path pane
		JPanel pathsPane = new JPanel();
		pathsPane.setBorder(new EmptyBorder(1,0,1,1));
		pathsPane.setLayout(new GridLayout(inputPaths.size()+1,1));
		for(String path : mappingCell.getInputPaths())
			pathsPane.add(new JLabel(path));
		pathsPane.add(new JLabel(mappingCell.getOutputPath()));

		// Generate the mapping cell pane
		JPanel pane = new JPanel();
		pane.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED,Color.lightGray,Color.darkGray),new EmptyBorder(3,3,3,3)));
		pane.setLayout(new BorderLayout());				
		pane.add(labelsPane,BorderLayout.WEST);
		pane.add(pathsPane,BorderLayout.CENTER);
		return pane;
	}

	/** Initializes the Ignored Mapping Cells dialog */
	public UnidentifiedMappingCellsDialog(ArrayList<MappingCellPaths> mappingCells)
	{
		super("Unidentified Mapping Cells");

		// Constructs the mapping cells pane 
		JPanel mappingCellsPane = new JPanel();
		mappingCellsPane.setLayout(new BoxLayout(mappingCellsPane,BoxLayout.Y_AXIS));
		for(MappingCellPaths mappingCell : mappingCells)
			mappingCellsPane.add(generateMappingCellPane(mappingCell));

		// Constructs the view pane
		JPanel view = new JPanel();
		view.setLayout(new BorderLayout());
		view.add(mappingCellsPane,BorderLayout.NORTH);
		
		// Create a scroll pane to hold the list of unidentified mapping cells
		JScrollPane mappingCellScrollPane = new JScrollPane(view,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		mappingCellScrollPane.setPreferredSize(new Dimension(400,200));
		
		// Set up loader dialog layout and contents
		setClosable(true);
		setContentPane(mappingCellScrollPane);
		pack();
		setVisible(true);
   	}
}
