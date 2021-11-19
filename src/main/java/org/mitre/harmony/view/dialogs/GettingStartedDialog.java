// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import org.mitre.harmony.model.HarmonyModel;

/**
 * Displays the dialog for displaying information on getting started with Harmony
 * @author CWOLF
 */
public class GettingStartedDialog extends JInternalFrame
{
	/** Initializes the "Getting Started" pane */
	private JPanel gettingStartedPane()
	{		
		// Generates the instructions for getting started
		JTextPane instructions = new JTextPane();
		instructions.setContentType("text/html");
		instructions.setBackground(Color.white);
		instructions.setEditable(false);
		instructions.setText(
			"<b>Starting From Scratch</b>" +
			"<ul style='margin-left:10px'>" +
			"<li>Select &quot;New&quot; under the &quot;Project&quot; menu</li>" +
			"<li>Bring the schemas you want to match into Harmony's database by  clicking " +
			    "&quot;Import Schema&quot;.  Select the importer appropriate to your schema " +
			    "file type (e.g., .xsd, .ddl) from the &quot;Importers&quot; drop down menu</li>" +
			"<li>Once imported, select the schemas you want to match from the &quot;Available " +
				"Schemas&quot; menu on the left.  Also set their display side (i.e., left or " +
				"right) and the model by which they are viewed (default models usually work)</li>" +
			"<li>Now both schemas should be visible.  Schema nodes can be expanded or collapsed " +
				"by clicking on them; right clicking provides further options</li>" +
			"<li>To do a match, select one of the matching options under the &quot;Matchers&quot; " +
				"menu</li>" +
			"<li>Match lines connecting schema elements should become visible.  The set of " +
				"visible lines is altered by adjusting the &quot;confidence&quot; and " +
				"&quot;depth&quot; slider bars, and by expanding / contracting schema nodes.  " +
				"For large schemas, it helps to &quot;set focus&quot; (via right click) on the " +
				"schema region you are currently working with.  Explore!</li>" +
			"<li>Right clicking on a specific match line displays a menu allowing you to annotate " +
				"and accept / reject a match</li>" +
			"<li>Once you are done working, select &quot;Save&quot; under the &quot;Mapping&quot; " +
				"menu to name and save your matches in the Harmony database.  Select " +
				"&quot;Export&quot; to export match information (e.g., into an Excel-readable " +
				".csv file)</li>" +
			"</ul><br>" +
			"<b>Resuming Previous Work</b>" +
			"<ul style='margin-left:10px'>" +
				"<li>Select &quot;Open&quot; under the &quot;Project&quot; menu</li>" +
				"<li>Select your project from the menu on the left, and click &quot;OK&quot;</li>" +
			"</ul><br>" +
			"<b>For Large Matching Problems</b>" +
			"<p>Harmony has been used sucessfully with schemas of having 1000's " +
			   "of elements.  A case study is described in: Ken Smith, Peter Mork, " +
			   "Len Seligman, Arnon Rosenthal, Michael Morse, David M. Allen, and " +
			   "Maya Li, &quot;The Role of Schema Matching in Large Enterprises&quot;, " +
			   "Conference on Innovative Database Research (CIDR '09), Jan 2009.");
		instructions.setCaretPosition(0);
		
		// Generate the "Getting Started" pane
		JPanel pane = new JPanel();
		pane.setBorder(new EmptyBorder(10,10,10,10));
		pane.setLayout(new BorderLayout());
		pane.add(new JScrollPane(instructions,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		return pane;
	}
	
	/** Initializes "Getting Started" dialog */
	public GettingStartedDialog(HarmonyModel harmonyModel)
	{
		super("Getting Started");
		
		// Initialize all settings for the project dialog
		setSize(600,375);
		setClosable(true);
		setContentPane(gettingStartedPane());
    	setVisible(true);
   	}
}
