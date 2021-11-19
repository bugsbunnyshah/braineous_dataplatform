/**
 * 
 */
package org.mitre.harmony.view.dialogs.mappingCell;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.SchemaStoreManager;
import org.mitre.harmony.view.dialogs.widgets.AbstractButtonPane;
import org.mitre.schemastore.model.DataType;
import org.mitre.schemastore.model.Function;
import org.mitre.schemastore.model.MappingCell;


/**
 * @author KZHENG
 *
 */
public class AddNewFunction extends JDialog implements MouseListener, MouseMotionListener, ActionListener, ListSelectionListener {

		/** Stores the Harmony model */
		private HarmonyModel harmonyModel;

		/** Stores the annotation pane */
		private MappingCellAnnotationPane annotationPane;
		
		/** Stores the confidence pane */
		private MappingCellConfidencePane confidencePane;
		
		private MappingCellFunctionPane functionPane;

		/** Stores the list of mapping cells to which this dialog pertains */
		private List<MappingCell> mappingCells;
		
		/** Stores the list of data types */
		private HashMap<Integer,DataType> dataTypeHash;
		//private LinkedList inputList = new LinkedList();
		
		
		private ButtonPane btPane;
		private final JTextField funcNameTextField;
		private final JTextField descriptionTextField;
		private final JList functionDetailList;
		private final JTextField expTextField; 
		private final JTextField catTextField;

		//private final JComboBox categoryComboBox;
		//private final JComboBox outputComboBox;
		
		//Function variable
		private String functionName;
		private String functionDes;
		private String functionExp = "";

		private String output;
		private Integer outputType;
		
		private String category;
		
		private LinkedList functionList = new LinkedList();
		private LinkedList dataTypeList = new LinkedList();
		
		private JList functionJList;
		
		private DefaultListModel funcNameListModel = new DefaultListModel();
		
		//Location for replacing the data type in function expression
		private int replaceA;
		private int replaceB;
		
		private MappingCellFunctionPane mcFunctionPane;

		
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
					//create a function abject
					functionName = funcNameTextField.getText();
					functionDes = descriptionTextField.getText();
					functionExp = expTextField.getText();
					category = catTextField.getText();
									
					Integer[] inputs = getInputs();
				
					Function funcObj = new Function(null, functionName, functionDes, functionExp, category, inputs, outputType); 
					System.out.println("FuncObj=" + funcObj.getId()+"," + funcObj.getName()+","+ funcObj.getDescription()+","+ funcObj.getExpression()+","+ funcObj.getCategory()+","+ funcObj.getOutputType()+"," + inputs[0]);
					try{
						Integer ret = SchemaStoreManager.addFunction(funcObj);
						System.out.println("New Func ID = " + ret);
					}
					catch( Exception e){
						e.printStackTrace();
					}
					
				}
				
				activateFunctionPane();
				
				// Close link dialog
				dispose();
			}
		}
		
		/** Initializes the mapping cell dialog */
		public AddNewFunction(List<MappingCell> mappingCells, HarmonyModel harmonyModel, MappingCellFunctionPane mcfp)
		{
			super();
			
			mcFunctionPane = mcfp;
			
			// Initialize the selected links
			this.mappingCells = mappingCells;
			this.harmonyModel = harmonyModel;
			
			//get project name
			category = this.harmonyModel.getProjectManager().getProject().getName();
			
			//get all the data type:
			LinkedList dataTypes = new LinkedList();
			dataTypeHash = new HashMap<Integer,DataType>();
			try {

				for(DataType dataType : SchemaStoreManager.getDataTypes()){
					dataTypes.add(dataType.toString());
					dataTypeList.add(dataType);
					dataTypeHash.put(dataType.getId(), dataType);
				}
			}catch(Exception e) {
				System.out.println(e.getStackTrace());
			}
				
			Collections.sort(dataTypes);
					
			// Set up the main dialog pane
			setBounds(450, 200, 600, 423);
			

			
			// Set up the main dialog pane
			JPanel mainPane = new JPanel();
			mainPane.setBackground(Color.LIGHT_GRAY);
			//centerPane.add(mainPane);
			//mainPane.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Add New Function", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 70, 213)));
			
			
			mainPane.setBorder(new LineBorder(new Color(108, 150, 225), 2, false));
			mainPane.getInputMap().put(KeyStroke.getKeyStroke((char)KeyEvent.VK_ESCAPE),"escape");
			mainPane.setLayout(new BorderLayout(10, 4));
			
			final JPanel panel_top = new JPanel();
			panel_top.setBackground(Color.LIGHT_GRAY);
			mainPane.add("North", panel_top);
			
			panel_top.setLayout(new GridLayout(3,1));
			panel_top.setBorder(BorderFactory.createTitledBorder("Add New Function"));
			panel_top.getInputMap().put(KeyStroke.getKeyStroke((char)KeyEvent.VK_ESCAPE),"escape");
			
			
			//function Name
			final JPanel funcNamePanel = new JPanel();
			panel_top.add(funcNamePanel);
			final FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(FlowLayout.LEFT);
			funcNamePanel.setLayout(flowLayout);

			final JLabel funcNameLabel = new JLabel();
			funcNamePanel.add(funcNameLabel);
			funcNameLabel.setText("Function Name:");

			funcNameTextField = new JTextField();
			//funcNameTextField.setForeground(Color.LIGHT_GRAY);
			
			funcNameTextField.setColumns(25);
			
			// Listen for changes in the text
			funcNameTextField.getDocument().addDocumentListener(new DocumentListener() {
					public void changedUpdate(DocumentEvent e) {
						// text was changed
							validateFunction();
							
						}
						public void removeUpdate(DocumentEvent e) {
						// text was deleted
							validateFunction();
							
						}
						public void insertUpdate(DocumentEvent e) {
						// text was inserted
							validateFunction();
							
						}
					});
			
			funcNamePanel.add(funcNameTextField);

			//Description
			final JPanel descPanel = new JPanel();
			panel_top.add(descPanel);
			final FlowLayout flowLayout_1 = new FlowLayout();
			flowLayout_1.setAlignment(FlowLayout.LEFT);
			descPanel.setLayout(flowLayout_1);

			final JLabel funcDescriptionLabel = new JLabel();
			funcDescriptionLabel.setText("Description:");
			descPanel.add(funcDescriptionLabel);

			descriptionTextField = new JTextField();
			descriptionTextField.setColumns(40);
			descPanel.add(descriptionTextField);
			
			//Category
			final JPanel catPanel = new JPanel();
			panel_top.add(catPanel);
			final FlowLayout flowLayout_3 = new FlowLayout();
			flowLayout_3.setAlignment(FlowLayout.LEFT);
			catPanel.setLayout(flowLayout_3);
			
			final JLabel funcCategoryLabel = new JLabel();
			funcCategoryLabel.setText("Category:");
			catPanel.add(funcCategoryLabel);

			catTextField = new JTextField(category);
			catTextField.setColumns(20);
			catPanel.add(catTextField);

			//String[] cat = {"Basic", "Math", "Date", "String"};
			
			//Expression
			final JPanel expPanel_t = new JPanel();
			expPanel_t.setBackground(Color.LIGHT_GRAY);
			final BorderLayout borderLayout = new BorderLayout();
			borderLayout.setVgap(0);
			borderLayout.setHgap(10);
			expPanel_t.setLayout(borderLayout);
			expPanel_t.setBorder(BorderFactory.createTitledBorder(""));
			expPanel_t.getInputMap().put(KeyStroke.getKeyStroke((char)KeyEvent.VK_ESCAPE),"escape");
			
			
			mainPane.add("Center", expPanel_t);
			
			final JPanel expPanel = new JPanel();
			expPanel_t.add("North", expPanel);
			final FlowLayout flowLayout_2 = new FlowLayout();
			flowLayout_2.setAlignment(FlowLayout.LEFT);
			expPanel.setLayout(flowLayout_2);
			
			final JLabel funcExpressionLabel = new JLabel();
			funcExpressionLabel.setText("Expression:");
			expPanel.add(funcExpressionLabel);
			
			expTextField = new JTextField();
			expTextField.setColumns(40);
			expTextField.setForeground(Color.red);
			expTextField.addMouseListener(this);
			
			// Listen for changes in the text
			expTextField.getDocument().addDocumentListener(new DocumentListener() {
					public void changedUpdate(DocumentEvent e) {
						// text was changed
							validateFunction();
							
						}
						public void removeUpdate(DocumentEvent e) {
						// text was deleted
							validateFunction();
							
						}
						public void insertUpdate(DocumentEvent e) {
						// text was inserted
							validateFunction();
							
						}
					});
	
			expPanel.add(expTextField);
			
			
			functionJList = new JList(dataTypes.toArray());
			functionJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			functionJList.setSelectedIndex(0);
			functionJList.addListSelectionListener(this);
	        
			LinkedList funcList = getFunctionInfo();
			setModelElements(funcList);
			//functionDetailList = new JList(funcNameList.toArray());
			functionDetailList = new JList(funcNameListModel);
			functionDetailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			//functionDetailList.setSelectedIndex(0);
			functionDetailList.addListSelectionListener(this);
			
			
			//outputComboBox.addActionListener(this);
			
	        //Create a split pane with the two scroll panes in it
			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			TitledBorder title;
			title = BorderFactory.createTitledBorder("Existing Functions");
			title.setTitleJustification(TitledBorder.CENTER);
			splitPane.setBorder(title);
			
		    JScrollPane scrollPane1 = new JScrollPane(functionJList);
		    JScrollPane scrollPane2 = new JScrollPane(functionDetailList);
	        splitPane.setLeftComponent(scrollPane1);
	        splitPane.setRightComponent(scrollPane2);

	        expPanel_t.add("Center", splitPane);
	        
			//buttons
			btPane = new ButtonPane();
			btPane.setBackground(Color.LIGHT_GRAY);
			btPane.setBounds(100, 351, 320, 45);
			mainPane.add("South", btPane);			
			
			//diable the button until valid inputs are given
			setButtonPaneOKDisabled();
			
			
			// Set up ability to escape out of dialog
			Action escape = new AbstractAction() { public void actionPerformed(ActionEvent arg0) { dispose(); } };
			mainPane.getInputMap().put(KeyStroke.getKeyStroke((char)KeyEvent.VK_ESCAPE),"escape");
			mainPane.getActionMap().put("escape", escape);
			
			// Initialize the dialog parameters
			setModal(true);
			//this.setSize(450, 670);
			setResizable(true);
			setUndecorated(true);
			setContentPane(mainPane);
			//pack();
			
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
		
		public void mouseClicked(MouseEvent e) {
			if(!(e.getSource() instanceof JTextField)) {
				return;
			}
			
			JTextField tf = (JTextField)e.getSource();
			
			//highlight the data type for replacement
			if(tf==expTextField){
				String content = expTextField.getText();
				replaceA = expTextField.getSelectionStart();
	            replaceB = expTextField.getSelectionEnd();
	            String selectedItem = content.substring(replaceA, replaceB);
	            
	            boolean isParameter = false;
	            
	            if(selectedItem.contains("$")){
	            	isParameter = true;
	            	//String typeName = getTypeNameFrom$(selectedItem);
    				LinkedList nameToShow = setDetailedList(output);
    				
    				//set selection for function data type
    				for(int i = 0; i < functionJList.getModel().getSize(); i++) {
    				     if((functionJList.getModel().getElementAt(i)).toString().equals(output)){
    				    	 functionJList.setSelectedIndex(i);
    				     }
    				     
    				 }

    	            setModelElements(nameToShow);
	            }
	            	
	            
	            //check to see if the selected item is a parameter
	            if(isParameter==false){
					expTextField.setSelectionStart(0);
		            expTextField.setSelectionEnd(0);
		            replaceA =0;
		            replaceB =0;
	            }
			}
		}
		public void mouseReleased(MouseEvent e) { mousePosition=null; }
	
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mouseMoved(MouseEvent e) {}
		
		
		//Action performed
		public void actionPerformed(ActionEvent e) {
		    JComboBox cb = (JComboBox)e.getSource();
		    		    
			//for Category
			//if(cb==categoryComboBox){
				//category = (String)cb.getSelectedItem();
			//}
			
		}
		
		//set buttonPane OK enabled.
		public void setButtonPaneOK(){
			btPane.setEnabled(0, true);
		}
		
		//set buttonPane disabled.
		public void setButtonPaneOKDisabled(){
			btPane.setEnabled(0, false);
		}
		
		//for generating function output
		public void setOutputType(String name){
			String funcName = name.substring(0, name.indexOf("("));
			for (int i=0; i < functionList.size(); i++){
				Function testFunc = (Function)functionList.get(i);
				if(funcName.equals(testFunc.getName())){
					outputType = testFunc.getOutputType();
					output = typeIntegerToName(outputType);				 
				}
			}
		}
		
		//get inputs
		public Integer[] getInputs() {
			LinkedList idList = new LinkedList();
			String funcExpression = expTextField.getText();
			
			StringTokenizer st = new StringTokenizer(funcExpression, "(),");
 
		     while (st.hasMoreTokens()) {
		    	 
		         String item = (st.nextToken());
		         if(item.contains("$")){
		        	 idList.add(outputType); 
		         }
		     }

			Integer[] inputs = new Integer[idList.size()];
					
			for(int i=0; i<idList.size(); i++ ){
				inputs[i] = (Integer)idList.get(i);
			}
			return inputs;
		}
		
		//get function by data type
		public String [] getFunctionsByType(Integer type) {
			return  new String[1];
		}
		
		//Get exist function information
		public LinkedList getFunctionInfo(){
			LinkedList nameList = new LinkedList();
			functionList.clear();
			
			try {
				
				HashMap<Integer,DataType> dataTypes = new HashMap<Integer,DataType>();
				for(DataType dataType : SchemaStoreManager.getDataTypes()){
					dataTypes.put(dataType.getId(), dataType);
				}
				
				for(Function function : SchemaStoreManager.getFunctions())
				{
					int j =1;
					int numDataType=0;
					functionList.add(function);
					
					//for base functions
					if(function.getExpression()==null){
						String out = function.getName() + "(";
						for(Integer input : function.getInputTypes())
						{
							String theType = dataTypes.get(input).getName();
							String parameter = "$" + j;
							out += "$" + j++ + ",";
						}
						nameList.add(new String(out.substring(0, out.length()-1))+ ")");
					}
					else{
						int k = 1;
						for(Integer input : function.getInputTypes())
						{
							String theType = dataTypes.get(input).getName();
							String parameter = "$" + k++;
						}
						nameList.add(function.getExpression());
					}
				}
	
				
			}catch(Exception e) {
				System.out.println(e.getStackTrace());
				return null;
			}
			
			//sort the list items
			Collections.sort(nameList);
			
			return nameList;
		}
		
		
		//function selection value changed
		public void valueChanged(ListSelectionEvent e) {

	        if (e.getValueIsAdjusting())
	            return;

	        JList theList = (JList)e.getSource();
	        if (theList.isSelectionEmpty()) {        	
	        	//System.out.println("Nothing is selected.");
	        } 
	        else if( theList == functionJList){
	            int index = theList.getSelectedIndex();
	            String currentTypeName = (String)theList.getModel().getElementAt(index);
	            LinkedList nameToShow = setDetailedList(currentTypeName);
	            setModelElements(nameToShow);
	        }
	        else{  //for detailed functions
	            int index = theList.getSelectedIndex();
	            functionExp = expTextField.getText();
	            String exp = (String) theList.getSelectedValue();
	            if(!functionExp.equals("")&&replaceA!=replaceB){
	            	functionExp = functionExp.substring(0, replaceA) + exp + functionExp.substring(replaceB, functionExp.length()); 
	            }
	            else{
	            	functionExp = exp;
	            	setOutputType(exp);
	            }
	            String outWith$ = reorderDollarSign(functionExp);
	            
	            expTextField.setText(outWith$);
	        }
	        
	        validateFunction();
	}


	//update Function Detail list
	public LinkedList setDetailedList(String typeName){
		LinkedList nameList = new LinkedList();
		
		if(typeName.equals("Any")){
			return nameList = getFunctionInfo();
		}
		
		LinkedList functionToShowList = new LinkedList();
		Integer typeId = StringTypeToIneger(typeName);
		
		for(int i=0; i< functionList.size(); i++){
			Integer output =((Function)functionList.get(i)).getOutputType();		
			if(output.equals(typeId)){
				functionToShowList.add((Function)functionList.get(i));
			}
		}

		
		for(int i = 0; i< functionToShowList.size(); i++)
		{
			int j = 1;
			Function function = (Function) functionToShowList.get(i);
			
			//for base functions
			if(function.getExpression()==null){
				String out = function.getName() + "(";
				for(Integer input : function.getInputTypes())
				{
					//out += dataTypeHash.get(input).getName() + ",";
					out += "$" + j++ + ",";
				}
				nameList.add(new String(out.substring(0, out.length()-1))+ ")");
			}
			else{
				nameList.add(function.getExpression());
			}
		}
		
		//sort the list items
		Collections.sort(nameList);
		
		return nameList;
		
	}
	
	//get type Id
	public Integer StringTypeToIneger(String typeName){
		Integer id = new Integer(0);
		for(int i = 0; i<dataTypeList.size(); i++){		
			DataType dt = (DataType)dataTypeList.get(i);              
			if(dt.getName().equals(typeName)){		 
				id = dt.getId();
			}
        }
		return  id;
	}
	
	//get Type Name
	public String typeIntegerToName(Integer id){
		String name = "";
		for(int i = 0; i<dataTypeList.size(); i++){		
			DataType dt = (DataType)dataTypeList.get(i);              
			if(id.equals(dt.getId())){		 
				name = dt.getName();
			}
        }
		return  name;
	}
	
	//Populate the JList
	private void setModelElements(LinkedList funcList){
		funcNameListModel.removeAllElements();
		for (int i=0; i< funcList.size(); i++){
			funcNameListModel.addElement((String)funcList.get(i));
		}
	}
	
	//Change parameter to dollar signs
	public String reorderDollarSign(String functionExp){

		int j = 1;
		for(int i=0; i < functionExp.length(); i++){
			char aChar = functionExp.charAt(i);
			if(aChar=='$'&& i<functionExp.length()-1){
				String oChar = Character.toString( functionExp.charAt(i+1));
				functionExp = functionExp.substring(0, i+1)+ j + functionExp.substring(i+2, functionExp.length());
				j++;
			}
		}
					
		return functionExp;
	}
	
	//activate parent OK Button
	public void activateFunctionPane(){
		Integer[] inputs = getInputs();
		
		String[] stInputs = new String[inputs.length];
		for(int i=0; i < inputs.length; i++){
			stInputs[i] = typeIntegerToName(inputs[i]);
		}
		mcFunctionPane.showNewAddedFunction(functionName, stInputs);
		mcFunctionPane.setDisable(false);
	}
	
	//function validation
	public void validateFunction(){
		//create a function abject
		boolean valid = true;
		
		String theFuncName = (funcNameTextField.getText()).trim();
		if(theFuncName.equals("")||theFuncName.contains(" ")||theFuncName.equals(null)){
			valid = false;
		}
		
		String theFuncExp = expTextField.getText();
		
		//verify function names and parameter orders
		StringTokenizer st = new StringTokenizer(theFuncExp, "(),");
		 
	     while (st.hasMoreTokens()) {   	 
	         String item = (st.nextToken());
	         if(item.contains("$")){
	        	 //verify variable in right order;
	        	 if(item.length()>2){
	        		 valid = false;
	        	 }
	        	 else if(item.length()==2){
	        		 try{
	        			 int number = Integer.parseInt(item.substring(1,2));
	        		 }
	        		 catch(NumberFormatException e){
	        			 valid = false;
	        		 }
	        	 }
	        	 else{
	        		 valid = false;
	        	 }
	         }
	         else{
	        	 //compare to the function names
	        	 if(!checkFunctionName(item)){
	        		 valid = false;
	        	 }
	         }
	     }
	     
	     //verify parenthesis 
    	 Stack stack = new Stack();
	     for(int i=0; i < theFuncExp.length(); i++){	 
	    	 char a = theFuncExp.charAt(i);
	    	 if(a=='('){
	    		 //add open		 
	    		 stack.push(a);
	    	 }
	    	 if (a==')'){
	    		 //add close
	    		 if(stack.isEmpty()){
	    			 valid = false;
	    		 }
	    		 else{
	    			 stack.pop();
	    		 }
	    	 }
	     }
	     
	     if(!stack.isEmpty()){
	    	 valid = false;
	     }
						
		if(valid){
			//diable the button until valid inputs are given
			expTextField.setForeground(new Color(0.0f,0.8f,0.0f));
			setButtonPaneOK();
		}
		else{
			//diable the button until valid inputs are given
			expTextField.setForeground(Color.red);
			setButtonPaneOKDisabled();
		}
	}
	
	//check function Name
	public boolean checkFunctionName(String name){
		
		boolean isFunctionName = false;
		for(int i =0; i < functionList.size(); i++ ){
			if(name.equals(((Function)functionList.get(i)).getName())){
				isFunctionName = true;
			}
		}
		return isFunctionName;
	}
	
}
