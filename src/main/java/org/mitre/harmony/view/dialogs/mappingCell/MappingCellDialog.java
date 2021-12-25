// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs.mappingCell;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.project.MappingManager;
import org.mitre.harmony.view.dialogs.widgets.AbstractButtonPane;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.MappingCellInput;

/**
 * Displays the dialog which allows mapping cells to be accepted/rejected
 * @author CWOLF
 */
public class MappingCellDialog extends JDialog implements MouseListener, MouseMotionListener, ActionListener
{
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;

	/** Stores the annotation pane */
	private MappingCellAnnotationPane annotationPane;
	
	/** Stores the confidence pane */
	private MappingCellConfidencePane confidencePane;
	
	private MappingCellFunctionPane functionPane;

	/** Stores the list of mapping cells to which this dialog pertains */
	private ArrayList<MappingCell> mappingCells;
	
	//private JRadioButton confidenceRadioButton;
	//private JRadioButton functionRadioButton;
	//private ButtonGroup bgroup;
	
	public ButtonPane btPane;
	
	private boolean usingFunction = true;
	
	/** Private class for defining the button pane */
	private class ButtonPane extends AbstractButtonPane
	{
		/** Constructs the button pane */
		public ButtonPane()	{ super(new String[]{"OK", "Cancel"},1,2); 
		}

		/** Handles selection of button */
		protected void buttonPressed(String label)
		{
			if(label.equals("OK"))
			{
					// Throw out mapping cells if they have been rejected
					if(confidencePane.isRejected()){
						harmonyModel.getMappingManager().deleteMappingCells(mappingCells);
					}
					else{
						//For none functional mapping cells
						if(!usingFunction ||functionPane.getFunctionName()==""||functionPane.getFunctionName()==null){
							// Throw out mapping cells if they have been rejected
							if(confidencePane.isRejected()){
								harmonyModel.getMappingManager().deleteMappingCells(mappingCells);
							}
							else
							{			
								// Adjust the mapping cells to reflect changes
								ArrayList<MappingCell> newMappingCells = annotationPane.getMappingCells();
								harmonyModel.getMappingManager().setMappingCells(newMappingCells);
								
								// If the mapping cells were accepted, validate as needed
								if(confidencePane.isAccepted()){
									harmonyModel.getMappingManager().validateMappingCells(newMappingCells);
								}
							}
						}
						else{
							/*
							if(functionPane.getFunctionName()==null||functionPane.getFunctionName()==""){
								//No function selected
								System.out.println("No function selected.");									
							}
							else{
							*/
							//for existing function mapping cell
							if(mappingCells.size()==1 && !((((MappingCell)mappingCells.get(0)).isIdentityFunction()))
									&&functionPane.getInputs().length==0&&!functionPane.getFunctionName().equals("<None>")){
								//no change
								System.out.println("functionName=" +functionPane.getFunctionName());
							}	
							else{ 
								//new mapping cell
								//using the function matching

								MappingManager manager = harmonyModel.getMappingManager();
					
								//get the mapping ID:
								Integer mappingId = mappingCells.get(0).getMappingId();			
								//System.out.println("mappingID=" + mappingId);
								String author = "author";
								Date date = Calendar.getInstance().getTime();
								//String function = IdentityFunction.class.getCanonicalName();
								String function = functionPane.getFunctionName();
								System.out.println("FuncName=" + function);
								MappingCellInput[] inputs = functionPane.getInputs();
								Integer output = functionPane.getOutput();
								Integer functionID = functionPane.getFunctionId();
								
								System.out.println("output=" + output);
								
								//delete mapping cells
								harmonyModel.getMappingManager().deleteMappingCells(mappingCells);
								
								//create a function mappingcell
								System.out.println("function=" +function);
								
								if(!function.equals("<None>")){
									MappingCell mappingCell = MappingCell.createFunctionMappingCell(null, mappingId, inputs, output, functionID, author, date, null);    	
									
									//MappingCell mappingCell = MappingCell.createValidatedMappingCell(id, mappingID, new Integer[]{leftID}, rightID, author, date, function, null);
									manager.getMapping(mappingId).setMappingCells(Arrays.asList(new MappingCell[]{mappingCell}));	
								}
							}
						}
					}
			}
				
			
			// Close link dialog
			dispose();
		}
	}
	
	/** Initializes the mapping cell dialog */
	public MappingCellDialog(ArrayList<MappingCell> mappingCells, HarmonyModel harmonyModel)
	{
		super();
		
		// Initialize the selected links
		this.mappingCells = mappingCells;
		this.harmonyModel = harmonyModel;
		
		// Set up the main dialog pane
		JPanel pane = new JPanel();
		pane.setBorder(BorderFactory.createLineBorder(Color.black));
		pane.setLayout(new BorderLayout());
		pane.add(annotationPane = new MappingCellAnnotationPane(mappingCells),BorderLayout.NORTH);
		
		//For matching panel
		JPanel matchingPanel = new JPanel();
		matchingPanel.setLayout(new BorderLayout());
			
		//For radio button
		/*
		confidenceRadioButton = new JRadioButton("Confidence", true);
		confidenceRadioButton.setFont(new Font("Arial", Font.PLAIN, 11));
		confidenceRadioButton.setActionCommand("confidence");

		functionRadioButton = new JRadioButton("Function", false);
		functionRadioButton.setFont(new Font("Arial", Font.PLAIN, 11));
		functionRadioButton.setActionCommand("function");
		
		bgroup = new ButtonGroup();
		bgroup.add(confidenceRadioButton);
		bgroup.add(functionRadioButton);
		
		confidenceRadioButton.addActionListener(this);
	
		functionRadioButton.addActionListener(this);
		*/
		
		JPanel radioPanel = new JPanel();
		//radioPanel.setLayout(new GridLayout(1, 2));
		/*
		JLabel setMatchingLabel = new JLabel("Set matching type:");
		radioPanel.add(setMatchingLabel);
		radioPanel.add(confidenceRadioButton);
		radioPanel.add(functionRadioButton);
		*/
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());	

		centerPanel.add(functionPane = new MappingCellFunctionPane(mappingCells, harmonyModel, this), BorderLayout.SOUTH);
		centerPanel.add(confidencePane = new MappingCellConfidencePane(mappingCells, functionPane), BorderLayout.NORTH);	

		//centerPanel.setBorder(BorderFactory.createTitledBorder("Set Matching"));
		functionPane.setDisable(false);
		confidencePane.setDisable(false);
		
		//Determine if it is a function mapping cell for edition
		if(mappingCells.size()==1){
			for(MappingCell mappingCell : mappingCells){
				if(!(mappingCell.isIdentityFunction())){
					//It is a function mapping cell
					//confidenceRadioButton.setSelected(false);
					//functionRadioButton.setSelected(true);
					functionPane.setDisable(false);
					//confidencePane.setDisable(true);
					usingFunction = true;
				}
			}
		}

		
		matchingPanel.add(radioPanel, BorderLayout.NORTH);
		matchingPanel.add(centerPanel, BorderLayout.SOUTH);
		
		pane.add(matchingPanel,BorderLayout.CENTER);

		btPane = new ButtonPane();
		pane.add(btPane,BorderLayout.SOUTH);
	
		// Set up ability to escape out of dialog
		Action escape = new AbstractAction() { public void actionPerformed(ActionEvent arg0) { dispose(); } };
		pane.getInputMap().put(KeyStroke.getKeyStroke((char)KeyEvent.VK_ESCAPE),"escape");
		pane.getActionMap().put("escape", escape);
		
		// Initialize the dialog parameters
		setModal(true);
		setResizable(false);
		setUndecorated(true);
		setContentPane(pane);
		pack();
		
		// Listen for mouse actions to move the dialog pane
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	//-----------------------------------------------------------------
	// Purpose: Handles the movement of the dialog box by mouse actions
	//-----------------------------------------------------------------
	private Point mousePosition = null;
	public void mousePressed(MouseEvent e) { mousePosition=e.getPoint(); }
	public void mouseDragged(MouseEvent e)
	{
		if(mousePosition!=null)
		{
			int xShift=e.getPoint().x-mousePosition.x;
			int yShift=e.getPoint().y-mousePosition.y;
			setLocation(getLocation().x+xShift,getLocation().y+yShift);
		}
	}
	public void mouseReleased(MouseEvent e) { mousePosition=null; }
	public void mouseClicked(MouseEvent e) {}	
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand()=="function"){
			//set function panel enabled
			usingFunction = true;
			functionPane.setDisable(false);
			confidencePane.setDisable(true);
			btPane.setEnabled(0, false); //set the OK button to dis enabled. 
		}
		else{
			//set confidence panel enabled
			usingFunction = false;
			functionPane.setDisable(true);
			confidencePane.setDisable(false);
			btPane.setEnabled(0, true); //set the OK button to enabled.
		}
	}
	
	//set buttonPane OK enabled.
	public void setButtonPaneOK(){
		btPane.setEnabled(0, true);
	}
	
	//set buttonPane disabled.
	public void setButtonPaneOKDisabled(){
		btPane.setEnabled(0, false);
	}
	
	//set functionRadioButton disabled.
	public void enableFunctionRadioButton(boolean trueorfalse){
		//functionRadioButton.setEnabled(trueorfalse);
	}
	
}
