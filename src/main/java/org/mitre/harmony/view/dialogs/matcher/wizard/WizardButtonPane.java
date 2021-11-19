package org.mitre.harmony.view.dialogs.matcher.wizard;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import org.mitre.harmony.view.dialogs.matcher.MatchingStatusPanel;

/** Controls the buttons in the wizard */
public class WizardButtonPane extends JPanel implements ActionListener
{    
    // Define the wizard button command strings
    private static final String NEXT_BUTTON_ACTION_COMMAND = "NextButtonActionCommand";
    private static final String BACK_BUTTON_ACTION_COMMAND = "BackButtonActionCommand";
    private static final String CANCEL_BUTTON_ACTION_COMMAND = "CancelButtonActionCommand";
	
	/** Stores reference to the wizard */
    private Wizard wizard;

    // References to the various wizard buttons
    private JButton backButton = new JButton("Back");
    private JButton nextButton = new JButton("Next");
    private JButton cancelButton = new JButton("Cancel");
    
    /** Generates a button with the specified action command */
    private JPanel generateButton(JButton button, String command)
    {
    	// Generate the button
     	button.setActionCommand(command);
    	button.addActionListener(this);
    	button.setFocusable(false);

    	// Generate the button pane
    	JPanel pane = new JPanel();
    	pane.setBorder(new EmptyBorder(5,5,5,5));
    	pane.setLayout(new BorderLayout());
    	pane.add(button,BorderLayout.CENTER);
    	return pane; 
    }
    
    /** Constructs the Wizard controller */
    public WizardButtonPane(Wizard wizard)
    {
    	this.wizard = wizard;

    	// Initially start with the back button disabled
    	backButton.setEnabled(false);
    	
        // Generate the button box
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new GridLayout(1,3));
        buttonPane.add(generateButton(backButton, WizardButtonPane.BACK_BUTTON_ACTION_COMMAND));
        buttonPane.add(generateButton(nextButton, WizardButtonPane.NEXT_BUTTON_ACTION_COMMAND));
        buttonPane.add(generateButton(cancelButton, WizardButtonPane.CANCEL_BUTTON_ACTION_COMMAND));

        // Generate the button pane
        setLayout(new BorderLayout());
        add(new JSeparator(), BorderLayout.NORTH);
        add(buttonPane, BorderLayout.SOUTH);
    }
    
    /** Indicates if the pane is the match pane */
    private boolean isMatchPane(WizardPanel pane)
    	{ return pane instanceof MatchingStatusPanel; }
    
    /** Updates the buttons based on the current state of the wizard */
    public void updateButtons()
    {
       	// Gets the previous, current, and next wizard panels
        WizardPanel backPane = wizard.getPanel(wizard.getBackIndex());
        WizardPanel currentPane = wizard.getPanel(wizard.getCurrentIndex());
        WizardPanel nextPane = wizard.getPanel(wizard.getNextIndex());

        // Enable the back button as long as not on first pane or match pane
        backButton.setEnabled(backPane!=null && !isMatchPane(currentPane));

        // Amend the next button label (if running match) and enable as long as not on match pane
        nextButton.setText((isMatchPane(currentPane) || isMatchPane(nextPane)) ? "Run" : "Next");
        nextButton.setEnabled(!isMatchPane(currentPane));
    }
    
    /** Handles an action for any button pressed */
    public void actionPerformed(ActionEvent evt)
    {
    	// Handles the "Cancel" button
        if(evt.getActionCommand().equals(CANCEL_BUTTON_ACTION_COMMAND))
            wizard.close();

        // Handles the "Back" button
        else if(evt.getActionCommand().equals(BACK_BUTTON_ACTION_COMMAND))
        	wizard.setCurrentPanel(wizard.getBackIndex());
        
        // Handles the "Next" button
        else if(evt.getActionCommand().equals(NEXT_BUTTON_ACTION_COMMAND))
        	wizard.setCurrentPanel(wizard.getNextIndex());
    }
}