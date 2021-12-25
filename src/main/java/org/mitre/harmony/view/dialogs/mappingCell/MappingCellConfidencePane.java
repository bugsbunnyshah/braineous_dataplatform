// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs.mappingCell;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.mitre.schemastore.model.MappingCell;

/**
 * Displays the confidence information about the mapping cell
 * @author CWOLF
 */
public class MappingCellConfidencePane extends JPanel implements ActionListener
{
	/** Stores the list of mapping cells to which this dialog pertains */
	private List<MappingCell> mappingCells;
	
	// Components used within the mapping cell confidence pane
	private JCheckBox acceptCheckbox;
	private JCheckBox rejectCheckbox;
	
	//Functional Panel
	private MappingCellFunctionPane functionPanel;
	
	/** Indicates if all of the mapping cells are accepted */
	private boolean mappingCellsAccepted()
	{
		for(MappingCell mappingCell : mappingCells)
			if(!mappingCell.isValidated() || mappingCell.getScore()!=1.0) return false;
		return true;
	}

	/** Indicates if all of the mapping cells are rejected */
	private boolean mappingCellsRejected()
	{
		for(MappingCell mappingCell : mappingCells)
			if(!mappingCell.isValidated() || mappingCell.getScore()!=-1.0) return false;
		return true;
	}
	
	/**
	 * @return The generated confidence pane
	 */
	MappingCellConfidencePane(List<MappingCell> mappingCells, MappingCellFunctionPane funcPanel)
	{
		this.mappingCells = mappingCells;
		
		this.functionPanel = funcPanel;
		
		// Initialize checkboxes
		acceptCheckbox = new JCheckBox("Accept");
		rejectCheckbox = new JCheckBox("Reject");

		// Add listeners to the checkboxes
		acceptCheckbox.addActionListener(this);
		rejectCheckbox.addActionListener(this);
		
		// Mark checkboxes as needed
		acceptCheckbox.setSelected(mappingCellsAccepted());
		rejectCheckbox.setSelected(mappingCellsRejected());
		
		// Set up accept and reject panes
		JPanel acceptPane = new JPanel();
		acceptPane.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
		acceptPane.add(acceptCheckbox);
		JPanel rejectPane = new JPanel();
		rejectPane.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
		rejectPane.add(rejectCheckbox);
		
		// Set up checkbox item pane
		JPanel checkboxPane = new JPanel();
		checkboxPane.setLayout(new GridLayout(1,2));
		checkboxPane.add(acceptPane);
		checkboxPane.add(rejectPane);
		
		// Set up link info pane
		JPanel linkInfoPane = new JPanel();
		linkInfoPane.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
		linkInfoPane.add(new MappingCellInfoPane(mappingCells));
		
		// Set up confidence pane
		setBorder(BorderFactory.createTitledBorder("Confidence"));
		setLayout(new BorderLayout());
		add(linkInfoPane,BorderLayout.CENTER);
		add(checkboxPane,BorderLayout.SOUTH);
	}

	/** Indicates if the mapping cells have been accepted */
	boolean isAccepted()
		{ return acceptCheckbox.isSelected(); }
	
	/** Indicates if the mapping cells have been rejected */
	boolean isRejected()
		{ return rejectCheckbox.isSelected(); }
	
	/** Handles the pressing of dialog buttons */
	public void actionPerformed(ActionEvent e)
	{
		// If "Accept" checkbox chosen, unselect "Reject"
		if(e.getSource()==acceptCheckbox)
		{
			if(acceptCheckbox.isSelected()) {
				rejectCheckbox.setSelected(false);
				
				//set the function panel
				functionPanel.setDisable(false);
			}
			else if(mappingCellsAccepted() || mappingCellsRejected()) {
				acceptCheckbox.setSelected(true);
			}
		}
		
		// If "Reject" checkbox chosen, unselect "Accept"
		if(e.getSource()==rejectCheckbox)
		{
			if(rejectCheckbox.isSelected()) {
				acceptCheckbox.setSelected(false);
				functionPanel.setDisable(true);
			}
			else if(mappingCellsAccepted() || mappingCellsRejected()) {
				rejectCheckbox.setSelected(true);
			}
		}
	}
	
	/** set enable fields */
    public void setDisable(boolean set){
    	if(set==false){
    		acceptCheckbox.setEnabled(true);
    		rejectCheckbox.setEnabled(true);
    	}
    	else{
    		acceptCheckbox.setEnabled(false);
    		rejectCheckbox.setEnabled(false);
    	}
    }
}
