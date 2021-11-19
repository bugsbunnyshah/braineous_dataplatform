package org.mitre.harmony.view.dialogs.matcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.mitre.harmony.matchers.mergers.MatchMerger;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.dialogs.matcher.wizard.Wizard;
import org.mitre.harmony.view.dialogs.matcher.wizard.WizardPanel;

/** Constructs the match pane for the matcher wizard */
public class MatchingStatusPanel extends WizardPanel implements MatchListener
{
    /** Stores the Harmony model for reference */
    private HarmonyModel harmonyModel;

    /** Stores the match merger to be used */
    private MatchMerger merger;

    /** Stores the currently running matching process */
    private MatchThread matchThread = null;

    // Stores references to components in the Match Pane
	private JProgressBar matcherProgressBar = new JProgressBar();
    private JLabel matcherProgressBarLabel = new JLabel();
	private JProgressBar overallProgressBar = new JProgressBar();

	/** Constructs the match pane */
    public MatchingStatusPanel(Wizard wizard, HarmonyModel harmonyModel, MatchMerger merger)
    {
    	super(wizard);
    	this.harmonyModel = harmonyModel;
        this.merger = merger;

		// Creates the progress pane
		JPanel panel = new JPanel();
		panel.setBorder(new CompoundBorder(new EmptyBorder(10,0,5,0),new CompoundBorder(new LineBorder(Color.gray), new EmptyBorder(5,10,5,10))));
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		panel.add(generateProgressBarPane(matcherProgressBarLabel, matcherProgressBar));
		panel.add(generateProgressBarPane(new JLabel("Overall Progress"), overallProgressBar));

		// Create the match pane
		setLayout(new BorderLayout());
		add(new JLabel("Progress of schema matching..."),BorderLayout.NORTH);
		add(panel,BorderLayout.CENTER);
    }

    /** Constructs a progress pane */
    public JPanel generateProgressBarPane(JLabel label, JProgressBar progressBar)
    {
		// Creates progress bar to show that processing is occurring
        progressBar.setStringPainted(true);

    	// Set the font of the progress bar description
        label.setFont(new Font("MS Sans Serif", Font.BOLD, 11));
        label.setBorder(new EmptyBorder(0,0,3,0));

        // Generate the progress bar pane
    	JPanel progressBarPane = new JPanel();
    	progressBarPane.setBorder(new EmptyBorder(15,0,0,0));
    	progressBarPane.setLayout(new GridLayout(2,1));
    	progressBarPane.add(label);
    	progressBarPane.add(progressBar);

    	// Push progress bar to top of pane
    	JPanel pane = new JPanel();
    	pane.setLayout(new BorderLayout());
    	pane.add(progressBarPane,BorderLayout.NORTH);
    	return pane;
    }

    /** Generates the schema matches when the panel is being displayed */ @Override
    public void displayingPanel()
    {
    	// Starts the match thread
    	matchThread = new MatchThread(harmonyModel, merger, getWizard().getSelectedMatchers(), getWizard().getSelectedMatchTypes());
        matchThread.addListener(this);
    	matchThread.start();
    }

    /** Handles the hiding of the match pane */ @Override
    public void aboutToHidePanel()
    	{ matchThread.stopThread(); }

    /** Updates the progress of the matchers */
	public void updateMatcherProgress(Double percentComplete, String status)
	{
		matcherProgressBar.setValue(percentComplete.intValue());
		matcherProgressBarLabel.setText(status);
	}

    /** Updates the progress of the schemas */
	public void updateOverallProgress(Double percentComplete)
		{ overallProgressBar.setValue(percentComplete.intValue()); }

	/** Handles the completion of the matching */
	public void matchCompleted()
		{ getWizard().close(); }
}