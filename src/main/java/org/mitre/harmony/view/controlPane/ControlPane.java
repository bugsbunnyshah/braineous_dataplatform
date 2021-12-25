// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.controlPane;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.schemaTree.SchemaTree;

/** Pane used to control the various aspects of the specified schema tree */
public class ControlPane extends JPanel
{
	/** Constructs the control pane */
	public ControlPane(SchemaTree tree, HarmonyModel harmonyModel)
	{
		// Place depth pane and progress pane
		JPanel bottomPane = new JPanel();
		bottomPane.setBorder(new EmptyBorder(1,1,1,1));
		bottomPane.setLayout(new BorderLayout());
		bottomPane.add(new SchemaTreeSearch(tree, harmonyModel),BorderLayout.CENTER);
		bottomPane.add(new SchemaTreeFinished(tree, harmonyModel),BorderLayout.EAST);

		// Place together depth pane and bottom pane
		setBorder(new CompoundBorder(new EmptyBorder(0,0,1,1),new CompoundBorder(new LineBorder(Color.gray),new EmptyBorder(1,5,1,5))));
		setLayout(new BorderLayout());
		add(new DepthPane(tree,harmonyModel),BorderLayout.NORTH);
		add(bottomPane,BorderLayout.SOUTH);
	}
}