// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs.widgets;

import java.awt.Font;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

/**
 * Displays a list of selectable options to choose from
 * @author CWOLF
 */
public class OptionPane extends JPanel
{
	/** Stores the button group */
	private ButtonGroup buttonGroup = new ButtonGroup();
	
	/** Stores the array of radio buttons */
	private ArrayList<JRadioButton> buttons = new ArrayList<JRadioButton>();
		
	/** Generates the option pane */
	public OptionPane(String label, String[] optionLabels)
	{
		// Generates the label
		setBorder(new EmptyBorder(3,0,0,4));
		setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
		add(new JLabel(label + ": "));

		// Generate the buttons
		for(String optionLabel : optionLabels)
		{
			JRadioButton button = new JRadioButton(optionLabel);
			buttons.add(button);
			button.setFont(new Font("Arial",Font.PLAIN,12));
			button.setFocusable(false);
		}
		
		// Place the buttons in a button group
		for(JRadioButton button : buttons)
		{
			buttonGroup.add(button);
			add(button);
		}
	}

	/** Enables the radio buttons */
	public void setEnabled(boolean enabled)
		{ for(JRadioButton button : buttons) button.setEnabled(enabled); }
	
	/** Set the selected radio button */
	public void setSelectedButton(String label)
		{ for(JRadioButton button : buttons) button.setSelected(button.getText().equals(label)); }
	
	/** Gets the selected radio button */
	public String getSelectedButton()
	{
		for(JRadioButton button : buttons)
			if(button.isSelected()) return button.getText();
		return null;
	}
}