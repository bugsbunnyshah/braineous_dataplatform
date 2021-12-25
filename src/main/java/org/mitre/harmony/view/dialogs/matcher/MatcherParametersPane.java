package org.mitre.harmony.view.dialogs.matcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.mitre.harmony.matchers.matchers.Matcher;
import org.mitre.harmony.matchers.parameters.MatcherCheckboxParameter;
import org.mitre.harmony.matchers.parameters.MatcherParameter;
import org.mitre.harmony.view.dialogs.matcher.wizard.Wizard;
import org.mitre.harmony.view.dialogs.matcher.wizard.WizardPanel;

/** Class for displaying a matchers parameters */
public class MatcherParametersPane extends WizardPanel
{
	/** Stores the matcher associated with these parameters */
	private Matcher matcher;
	
	/** Generates an parameter pane */
	private class ParameterPane extends JPanel implements ActionListener
	{
		/** Stores the matcher parameter */
		private MatcherCheckboxParameter parameter;

		/** Constructs the parameter pane */
		public ParameterPane(MatcherCheckboxParameter parameter)
		{
			this.parameter = parameter;

			// Create the parameter check box
			JCheckBox checkbox = new JCheckBox(parameter.getText(),parameter.isSelected());
			checkbox.setFont(new Font("Arial", Font.PLAIN, 12));
			checkbox.setAlignmentX(Component.LEFT_ALIGNMENT);
			checkbox.addActionListener(this);

			// Create the parameter pane
			setBorder(new EmptyBorder(2,2,2,2));
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(checkbox);
		}

		/** Handles changes to the matcher parameter */
		public void actionPerformed(ActionEvent e)
			{ parameter.setSelected(((JCheckBox)e.getSource()).isSelected()); }
	}
	
	/** Constructs the type pane */
    public MatcherParametersPane(Wizard wizard, Matcher matcher)
    {
    	super(wizard);
    	this.matcher = matcher;
    	
    	// Generate the title for this panel
		JLabel titleLabel = new JLabel("<html><u>" + matcher.getName() + " Parameters</u></html>");
		titleLabel.setBorder(new EmptyBorder(1,0,0,2));
		
		// Generate the parameters pane
		JPanel parametersPanel = new JPanel();
		parametersPanel.setLayout(new BoxLayout(parametersPanel, BoxLayout.Y_AXIS));
		for(MatcherParameter parameter : matcher.getParameters())
		{
			// Handles a checkbox parameter
			if(parameter instanceof MatcherCheckboxParameter)
				parametersPanel.add(new ParameterPane((MatcherCheckboxParameter)parameter));
		}

    	// Generate the panel
		setBorder(new CompoundBorder(new EmptyBorder(10,10,10,10),new CompoundBorder(new LineBorder(Color.lightGray),new EmptyBorder(5,5,5,5))));
		setLayout(new BorderLayout());
		add(titleLabel, BorderLayout.NORTH);
		add(parametersPanel, BorderLayout.CENTER);
    }
    
    /** Returns the ID for this pane */ @Override
    public String getID()
    	{ return getClass().getSimpleName() + " - " + matcher.getName(); }
    
    /** Returns the matcher associated with this parameter pane */
    public Matcher getMatcher()
    	{ return matcher; }
}