// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs.importers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.URI;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import org.mitre.harmony.view.dialogs.widgets.AbstractButtonPane;

/**
 * Displays the URI list dialog
 * @author CWOLF
 */
public class URIListDialog extends JInternalFrame
{
	/** Holds the selected URI */
	private URI selectedURI = null;
	
	/** Private class for defining the button pane */
	private class ButtonPane extends AbstractButtonPane
	{
		/** Constructs the button pane */
		private ButtonPane()
			{ super(new String[]{"OK", "Cancel"},1,2); }
		
		/** Handles selection of button */
		protected void buttonPressed(String label)
		{		
			if(label.equals("OK"))
				selectedURI = (URI)uriList.getSelectedValue();
			dispose();
		}
	}
	
	/** Stores the schema list */
	private JList uriList = null;
	
	/** Generate the URI list */
	private JScrollPane getURIList(ArrayList<URI> uris)
	{
		// Initialize the schema list
		uriList = new JList();
		uriList.setListData(new Vector<URI>(uris));
		uriList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		uriList.setSelectedIndex(0);
		
		// Create a scroll pane to hold the project list
		JScrollPane schemaScrollPane = new JScrollPane(uriList,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		schemaScrollPane.setPreferredSize(new Dimension(130,200));
		return schemaScrollPane;
	}
	
	/** Initializes the URI list dialog */
	public URIListDialog(ArrayList<URI> uris)
	{
		super("Import Selector");
		
		// Constructs the content pane 
		JPanel pane = new JPanel();
		pane.setBorder(new EmptyBorder(10,10,0,10));
		pane.setLayout(new BorderLayout());
		pane.add(getURIList(uris), BorderLayout.CENTER);
		pane.add(new ButtonPane(), BorderLayout.SOUTH);
		
		// Set up loader dialog layout and contents
		setContentPane(pane);
		pack();
		setVisible(true);
   	}

	/** Retrieves the selected URI */
	URI getURI()
		{ return selectedURI; }
}
