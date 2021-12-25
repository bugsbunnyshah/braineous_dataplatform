//(c) The MITRE Corporation 2006
//ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs.mappingCell;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.mitre.harmony.model.HarmonyConsts;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.SchemaStoreManager;
import org.mitre.schemastore.model.DataType;
import org.mitre.schemastore.model.Function;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.MappingCellInput;
import org.mitre.schemastore.model.schemaInfo.SchemaInfo;

/**
* Displays the function information about the mapping cell
* @author KZheng
*/
public class MappingCellFunctionPane extends JPanel implements ActionListener, KeyListener
{
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** Stores the list of mapping cells to which this dialog pertains */
	private List<MappingCell> mappingCells;
	
	// Components used within the mapping cell confidence pane
	//private JCheckBox acceptCheckbox;
	//private JCheckBox rejectCheckbox;
	private JComboBox functionComboBox;
	private JTextField formulaTextField;
	private JComboBox[] variableComboBox = new JComboBox[5];
	private JLabel[] selectVariableLabel = new JLabel[5];
	private JLabel selectFunctionLabel = new JLabel("  Built-in Function:");;
	private JLabel functionFormulaLabel;
	//private JButton editButton;
	
	private int numParameters = 0;
	private boolean hasAppliedFunction = true;
	private int currentParameter =0; //for selected parameter
	
	private String currentExpression="";
	
	private String functionName = "";

	private int selectedFuncIndex = 0; //for selected function to edit
	
	private LinkedList inputList = new LinkedList();
	
	private LinkedList functionList = new LinkedList();
	
	private LinkedList variables;
	
	private Integer output;
	
	private HashMap<Integer, String> inputHash;
	
	private MappingCellDialog mappingCDialog;
	
	//Titled borders
	private TitledBorder title;
	
	/**
	 * @return The generated function pane
	 */
	MappingCellFunctionPane(List<MappingCell> mappingCells, HarmonyModel theHarmonyModel, MappingCellDialog md)
	{
		this.mappingCells = mappingCells;
		
		this.harmonyModel = theHarmonyModel;
		
		this.mappingCDialog = md;
		
		//get schema id
		HashSet<Integer> h = harmonyModel.getProjectManager().getSchemaIDs(HarmonyConsts.LEFT);
		Iterator it = h.iterator();
		Integer leftSchemaID = null;
	    while(it.hasNext()) {
	      Object val = it.next();      
	      leftSchemaID = (Integer) val;  //assume only one schema on left side        
	    }
	    
	    SchemaInfo leftSchemaInfo = harmonyModel.getSchemaManager().getSchemaInfo(leftSchemaID);
	    
	    //variable list
	    variables = new LinkedList();
	    
	    //find the node names for selected elements
	    inputHash = new HashMap();
	    
		LinkedList nameList = new LinkedList();
		
		//Function name in the list
		LinkedList funcNamesToShow = new LinkedList();
		funcNamesToShow.add("<None>");
		
		// Set up functionSelection info pane
		JPanel functionSelectionPane = new JPanel();
		
		//set title
		title = BorderFactory.createTitledBorder("Mapping Function");
		
		//edit mode
		boolean isEditMode = false;
		
		//determine if it is existing function mapping cell
		if(mappingCells.size()==1 && !((((MappingCell)mappingCells.get(0)).isIdentityFunction())))
		{	
			isEditMode = true;
			
			//An existing function mapping cell 
			MappingCell funcMc = (MappingCell)mappingCells.get(0);
			
			//get the input list
			Integer[] inputsId = funcMc.getElementInputIDs();
			numParameters = inputsId.length;
			
			for(int i=0; i< inputsId.length; i++){	
				//SchemaInfo schemaInfo = harmonyModel.getSchemaManager().getSchemaInfo(leftSchemaID);
				variables.add(leftSchemaInfo.getDisplayName(inputsId[i]));	
				inputHash.put(inputsId[i], leftSchemaInfo.getDisplayName(inputsId[i]));
			}
			
			//get output list
			Integer outputId = funcMc.getOutput();
			setOutput(outputId);
			
			//get function name
			Integer funcId = funcMc.getFunctionID();
			try {				
				HashMap<Integer,DataType> dataTypes = new HashMap<Integer,DataType>();
				for(DataType dataType : SchemaStoreManager.getDataTypes()){
					dataTypes.put(dataType.getId(), dataType);
				}
						
				for(Function function : SchemaStoreManager.getFunctions())
				{
					int numDataType=0;
					functionList.add(function);
					if(function.getId()==funcId){
						this.setFunctionName(function.getName());
					}
					String out = function.getName().toUpperCase() + "(";
					for(Integer input : function.getInputTypes())
					{
						out += dataTypes.get(input).getName() + ",";
						numDataType++;
					}
					nameList.add(new String(out.substring(0, out.length()-1))+ ")");
					
					//only show the function with the right number of parameters
					if(numParameters==numDataType){
						funcNamesToShow.add(new String(out.substring(0, out.length()-1))+ ")");
						Collections.sort(funcNamesToShow);
					}
				}
	
				if(funcNamesToShow.size()==0){
					hasAppliedFunction = false;
				}
				
			}catch(Exception e) {
				System.out.println(e.getStackTrace());
			}
		}
		else {//create a new function mapping cell)

		    //get selected elements
			List<Integer> elementIdList = harmonyModel.getSelectedInfo().getSelectedElements(HarmonyConsts.LEFT);
			
			for(int i=0; i < elementIdList.size(); i++)
			{				
				SchemaInfo schemaInfo = harmonyModel.getSchemaManager().getSchemaInfo(leftSchemaID);
				variables.add(schemaInfo.getDisplayName(elementIdList.get(i)));	
				inputHash.put((Integer)elementIdList.get(i), schemaInfo.getDisplayName(elementIdList.get(i)));
			}
			
			numParameters = variables.size(); 
			
			//find the node names for selected elements on right
			h = harmonyModel.getProjectManager().getSchemaIDs(HarmonyConsts.RIGHT);
			it = h.iterator();
			Integer rightSchemaID = null;
			
		    while(it.hasNext()) {
		      Object val = it.next();      
		      rightSchemaID = (Integer) val;  //assume only one schema on right side        
		    }
		    
			List<Integer> rightElementIdList = harmonyModel.getSelectedInfo().getSelectedElements(HarmonyConsts.RIGHT);
			String selectedNodeNameOnRight = "";
			for(int i=0; i < rightElementIdList.size(); i++)
			{
				SchemaInfo rightSchemaInfo = harmonyModel.getSchemaManager().getSchemaInfo(rightSchemaID);
				selectedNodeNameOnRight = rightSchemaInfo.getDisplayName(rightElementIdList.get(i));
			}
				
			//assume only one output:
			if(rightElementIdList.size()==1){
				setOutput(rightElementIdList.get(0));
			}
			else{
				hasAppliedFunction = false;
			}
			
			// Set up functionSelection info pane
			//JPanel functionSelectionPane = new JPanel();
			functionSelectionPane.setLayout(new GridLayout(1,2));
		
			//selectFunctionLabel = new JLabel("  Built-in Function:");
			selectFunctionLabel.setFont(new Font("Arial", Font.BOLD, 11));
			
			try {
				
				HashMap<Integer,DataType> dataTypes = new HashMap<Integer,DataType>();
				for(DataType dataType : SchemaStoreManager.getDataTypes()){
					dataTypes.put(dataType.getId(), dataType);
				}
						
				for(Function function : SchemaStoreManager.getFunctions())
				{
					//functionNames[i]= new String(function.getName());
					int numDataType=0;
					functionList.add(function);
					String out = function.getName().toUpperCase() + "(";
					for(Integer input : function.getInputTypes())
					{
						out += dataTypes.get(input).getName() + ",";
						numDataType++;
					}
					nameList.add(new String(out.substring(0, out.length()-1))+ ")");
					
					//only show the function with the right number of parameters
					if(numParameters==numDataType){
						funcNamesToShow.add(new String(out.substring(0, out.length()-1))+ ")");
					}
				}
	
				if(funcNamesToShow.size()==0){
					hasAppliedFunction = false;
				}
				
			}catch(Exception e) {
				System.out.println(e.getStackTrace());
			}
		}
			
		String[] functionNames = new String[funcNamesToShow.size()+1];
		
		//functionNames[0]="<None>";
		functionNames[funcNamesToShow.size()]="<Add Function>";
		
		//find the selected function index
		for(int i=0; i< funcNamesToShow.size();i++ ){
			functionNames[i] = (String) funcNamesToShow.get(i);
			
			//for edit mode
			if(functionNames[i].contains((this.getFunctionName()).toUpperCase())&&isEditMode){
				selectedFuncIndex = i;
			}
		}
		
		//disable the function feature
		if(!hasAppliedFunction){
			String[] a = {"No Applicable Functions."};
			functionComboBox = new JComboBox(a);
			title.setTitleColor(Color.GRAY);
			//disable the radio button
			md.enableFunctionRadioButton(false);
		}
		//enable the function feature
		else{
			functionComboBox = new JComboBox(functionNames);
			functionComboBox.setSelectedIndex(selectedFuncIndex);
		}
		
		functionComboBox.setBackground(Color.WHITE);
		functionComboBox.setFont(new Font("Arial", Font.BOLD, 11));
		functionComboBox.addActionListener(this);
			
		//functionComboBox.setMaximumRowCount(50);
		JPanel functionComboBoxPane = new JPanel();
		functionComboBoxPane.setLayout (new GridLayout(3, 2, 8, 0)); 
		functionComboBoxPane.add(selectFunctionLabel);
		functionComboBoxPane.add(functionComboBox);
		
		//variable input
		String[] variableNames = new String[variables.size()+1];
		variableNames[0]="";
		for(int i =1; i< variables.size()+1; i++ ){
			//set up combobox for variable
			variableNames[i] = (String)variables.get(i-1);
		}
		

		//multiple parameters
		if(hasAppliedFunction){
			
			//enable function radio button
			md.enableFunctionRadioButton(true);
			
			//Only show these parameters when the function list > 0
			for(int i =0; i< variables.size(); i++ ){			
				selectVariableLabel[i] = new JLabel("Parameter $" + (i+1) + ":", SwingConstants.RIGHT);
				selectVariableLabel[i].setFont(new Font("Arial", Font.BOLD, 11));
				
				variableComboBox[i] = new JComboBox(variableNames);
				variableComboBox[i].setEnabled(false);
				
				if(isEditMode){
					variableComboBox[i].setEnabled(true);
					variableComboBox[i].setSelectedIndex(i+1);
				}
				variableComboBox[i].addActionListener(this);
				variableComboBox[i].addKeyListener(this);
				variableComboBox[i].setBackground(Color.WHITE);
				variableComboBox[i].setFont(new Font("Arial", Font.PLAIN, 11));


	
				functionComboBoxPane.add(selectVariableLabel[i]);
				functionComboBoxPane.add(variableComboBox[i]);
			}
		}
		functionSelectionPane.add(functionComboBoxPane);
		
		//formulaTextField = new JTextField(selectedNodeNameOnRight + "=");
		formulaTextField = new JTextField("Output = ");
		formulaTextField.setFont(new Font("Tahoma", Font.PLAIN, 12));
		formulaTextField.setColumns(25);
		formulaTextField.setSize(20, 6);
		
		//for edit mode
		if(isEditMode){
			String variableExp = "(";
			for(int i =1; i< variables.size()+1; i++ ){
				variableExp = variableExp + variableNames[i];
				if(i<variables.size()){
					variableExp = variableExp + ",";
				}
				else{
					variableExp = variableExp + ")";
				}
			}
			String outputExp = "Output =" + this.getFunctionName()+ variableExp; 
			addToExpression(outputExp);
		}
		
		formulaTextField.addKeyListener(this);
		
		currentExpression = formulaTextField.getText();
		
		JPanel functionDisplayPane = new JPanel(new FlowLayout());
		
		//editButton = new JButton("Validate...");
		
		functionFormulaLabel = new JLabel("  Function Expression:");
		functionFormulaLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		functionFormulaLabel.setHorizontalAlignment(SwingConstants.LEFT);
		functionFormulaLabel.setFont(new Font("Arial", Font.PLAIN, 10));
		
		functionDisplayPane.add(formulaTextField);
		//functionDisplayPane.add(editButton);
		
		
		// Set up confidence pane
		setBorder(title);
		
		setLayout(new BorderLayout());
		add(functionSelectionPane,BorderLayout.NORTH);
		add(functionDisplayPane,BorderLayout.SOUTH);
		//add(checkboxPane,BorderLayout.SOUTH);					
	}

	
	/** Handles the pressing of dialog buttons */
	public void actionPerformed(ActionEvent e)
	{	
		// function combobox
		if(e.getSource()==functionComboBox)
		{
			//set all fileds to original
			resetAllFields();
			
			String funName = (String)functionComboBox.getSelectedItem();
			if(funName=="<Add Function>")
			{
			  //do something here to add a new function
				AddNewFunction newFunctionPane = new AddNewFunction(mappingCells, harmonyModel, this);
				//newFunction.setLocation(adjustMouseLocation(e.getPoint(), null));
				newFunctionPane.setVisible(true);
				
		        currentParameter = 0;
		        variableComboBox[currentParameter].setEnabled(true);
		        
				return;
			}
			else if(funName=="<None>" || funName=="Identity")
			{
			  //do something here for selecting none function
				setFunctionName(funName);
				resetAllFields();
				mappingCDialog.setButtonPaneOK();
				return;
			}
			else
			{   
				funName = funName.substring(0, funName.indexOf("("));
				setFunctionName(funName);

		        addToExpression(funName+"(");
		        formulaTextField.requestFocus();
		        currentParameter = 0;
		        variableComboBox[currentParameter].setEnabled(true);
			}
		}
		
		// variable combobox

		if(e.getSource()==variableComboBox[currentParameter]) 
		{
			String varName = (String)variableComboBox[currentParameter].getSelectedItem();

	        if((currentParameter+1)<numParameters){
	        	currentParameter++;
	        	variableComboBox[currentParameter].setEnabled(true);
	        	addToExpression(varName + ",");
	        	formulaTextField.setEnabled(false);
	        	formulaTextField.setEditable(false);
	        	
	        	variableComboBox[currentParameter-1].setEnabled(false);
	        	
	        	//remove the selected item from the rest combobox
	        	for(int i=currentParameter; i < numParameters; i++){
	        		int rem = variableComboBox[currentParameter-1].getSelectedIndex();
	        		variableComboBox[i].removeItemAt(rem);
	        	}
	        }
	        else{
	        	addToExpression(varName + ")");
	        	formulaTextField.setEditable(false);
	        	/*
	        	functionComboBox.setEnabled(false);
	    		for(int i=0; i< numParameters; i++){
	    			variableComboBox[i].setEnabled(false);
	    		}
	    		*/
	        	variableComboBox[currentParameter].setEnabled(false);
	    		//editButton.setEnabled(true);
	        	
	        	//enable the OK button
	    		mappingCDialog.setButtonPaneOK();
	    		
	    		formulaTextField.setEnabled(true);
	        }
	        
        	//add to input array
        	addToInput(varName);
        	
	        formulaTextField.requestFocus();
		}	
	}
	
	
	//add item to function expression
	public void addToExpression(String item){
		currentExpression+=item;
		formulaTextField.setText(currentExpression);
		formulaTextField.setCaretPosition(0);
		//formulaTextField.setEditable(false);
		//formulaTextField.setEnabled(false);
	}
	
	/** Handle the key typed event from the text field. */
    public void keyTyped(KeyEvent e) {
    	//displayInfo(e, "KEY TYPED: ");
    	    	
    }

    /** Handle the key-pressed event from the text field. */
    public void keyPressed(KeyEvent e) {
	//displayInfo(e, "KEY PRESSED: ");
    }

    /** Handle the key-released event from the text field. */
    public void keyReleased(KeyEvent e) {
	//displayInfo(e, "KEY RELEASED: ");
    	int id = e.getID();
    	if (id == KeyEvent.KEY_RELEASED) {
	    	currentExpression = formulaTextField.getText();
	    	
	    	formulaTextField.setText(currentExpression);
    	}
    }
    
    //get type character
    String getTypedCharacter(KeyEvent e)
    {
    	int id = e.getID();
        String keyString ="";
        if (id == KeyEvent.KEY_TYPED) {
            char c = e.getKeyChar();
            keyString =c + "";
        };
        
        return keyString;
    }
    
    //For testing purpose
    private void displayInfo(KeyEvent e, String keyStatus){
        
        //You should only rely on the key char if the event
        //is a key typed event.
        int id = e.getID();
        String keyString;
        if (id == KeyEvent.KEY_TYPED) {
            char c = e.getKeyChar();
            keyString = "key character = '" + c + "'";
        } else {
            int keyCode = e.getKeyCode();
            keyString = "key code = " + keyCode
                    + " ("
                    + KeyEvent.getKeyText(keyCode)
                    + ")";
        }
        
        int modifiersEx = e.getModifiersEx();
        String modString = "extended modifiers = " + modifiersEx;
        String tmpString = KeyEvent.getModifiersExText(modifiersEx);
        if (tmpString.length() > 0) {
            modString += " (" + tmpString + ")";
        } else {
            modString += " (no extended modifiers)";
        }
        
        String actionString = "action key? ";
        if (e.isActionKey()) {
            actionString += "YES";
        } else {
            actionString += "NO";
        }
        
        String locationString = "key location: ";
        int location = e.getKeyLocation();
        if (location == KeyEvent.KEY_LOCATION_STANDARD) {
            locationString += "standard";
        } else if (location == KeyEvent.KEY_LOCATION_LEFT) {
            locationString += "left";
        } else if (location == KeyEvent.KEY_LOCATION_RIGHT) {
            locationString += "right";
        } else if (location == KeyEvent.KEY_LOCATION_NUMPAD) {
            locationString += "numpad";
        } else { // (location == KeyEvent.KEY_LOCATION_UNKNOWN)
            locationString += "unknown";
        }
        
        //Display information about the KeyEvent...
        
        //System.out.println(keyString);
        //System.out.println(modString);
        //System.out.println(locationString);
        //System.out.println(actionString);

    }

    //set enable fields
    public void setDisable(boolean set){

    	if(set==false && hasAppliedFunction){ 
    		functionComboBox.setEnabled(true);
    		formulaTextField.setEnabled(true);
    		for(int i=0; i< numParameters; i++){
    			variableComboBox[i].setEnabled(false);
    			selectVariableLabel[i].setEnabled(true);
    		}
    		selectFunctionLabel.setEnabled(true);
    		functionFormulaLabel.setEnabled(true);
    		//editButton.setEnabled(true);

    		//title.setTitleColor(Color.BLACK); //Not working?

    	}
    	else{
    		functionComboBox.setEnabled(false);
    		formulaTextField.setEnabled(false);
    		if(hasAppliedFunction){ //Only shown the variables if the functions are there
	    		for(int i=0; i< numParameters; i++){
	    			variableComboBox[i].setEnabled(false);
	    			selectVariableLabel[i].setEnabled(false);
	    		}
    		}
    		selectFunctionLabel.setEnabled(false);
    		functionFormulaLabel.setEnabled(false);
    		//editButton.setEnabled(false);
    		
    		//title.setTitleColor(Color.GRAY);
    	}
    	
    }
    
	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public MappingCellInput[] getInputs() {
		MappingCellInput[] inputs = new MappingCellInput[inputList.size()];
		for(int i=0; i<inputList.size(); i++ ){
			inputs[i] = new MappingCellInput((Integer)inputList.get(i));
		}
		return inputs;
	}

	public Integer getOutput() {
		return output;
	}

	public void setOutput(Integer outputId) {
		output = outputId;
	}
	
	//for generating function mapping cell with inputs
	public void addToInput(String name){
		
		Iterator iterator = inputHash.entrySet().iterator();

		while( iterator. hasNext() ){

			String element = (String) iterator.next().toString();
			if(element.contains(name)){		 
				String id = element.substring(0, element.indexOf("="));
				inputList.add(new Integer(id));
			}
		}
	}

	/** return the function Integer ID */
	public Integer getFunctionId(){
		
		String funcName = this.getFunctionName();
		Integer id = 10;
		for(int i=0; i< functionList.size(); i++){
			Function fn = (Function) functionList.get(i);
			if(fn.getName().toLowerCase().equals(funcName.toLowerCase())){
				id = fn.getId();
			}
		}
		return id;
	}
	
	//Empty all the items in parameter combobox and output function
	public void resetAllFields(){
		String[] variableNames = new String[variables.size()+1];
		variableNames[0]="";
		for(int i =1; i< variables.size()+1; i++ ){
			//set up combobox for variable
			variableNames[i] = (String)variables.get(i-1);
		}
		
		for(int i=0; i< numParameters; i++){
				variableComboBox[i].removeActionListener(this);
				variableComboBox[i].removeAllItems();
				for(int j=0; j<(variables.size()+1); j++){
					variableComboBox[i].addItem(variableNames[j]);
				}
				variableComboBox[i].addActionListener(this);
		}
		currentExpression = "";	
		addToExpression("Output=");
		
		//disable the OK button
		mappingCDialog.setButtonPaneOKDisabled();
	}
	
	//showNewAddedFunction
	public void showNewAddedFunction(String newFunction, String[] inputs){
		
		String functionToShow = newFunction + "(";

		for(int i=0; i < inputs.length; i++){
			functionToShow += inputs[i] + ",";
		}
		functionToShow = functionToShow.substring(0, functionToShow.length()-1) + ")";
		setFunctionName(newFunction);
		//Update function comboBox
		functionComboBox.addItem(functionToShow);
		functionComboBox.setSelectedIndex(functionComboBox.getItemCount()-1);
		
		//Update Variable ComboBoxes
        addToExpression(newFunction+"(");
        formulaTextField.requestFocus();
        
		//set OK Button enabled
		mappingCDialog.setButtonPaneOK();
	}
}

