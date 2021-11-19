// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.harmonyPane;

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashSet;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.dialogs.DialogManager;
import org.mitre.harmony.view.mappingPane.MappingPane;
import org.mitre.harmony.view.menu.HarmonyMenuBar;

/**
 * Displays main Harmony window
 * 
 * @author CWOLF
 */
public class HarmonyFrame extends JInternalFrame implements ComponentListener
{
	// Stores reference to various panes in this frame
	private JDesktopPane desktop = new JDesktopPane();
	private JPanel mainPane = new JPanel();
	
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** Generates the main pane */
	private JDesktopPane getMainPane()
	{
		// Initialize the various panes shown in the main Harmony pane
		TitledPane evidencePane = new TitledPane("Evidence", new EvidencePane(harmonyModel));
		TitledPane filterPane = new TitledPane("Filters", new FilterPane(harmonyModel));

		// Layout the view pane of Harmony
		JPanel viewPane = new JPanel();
		viewPane.setLayout(new BorderLayout());
		viewPane.add(new TitledPane(null, new MappingPane(this,harmonyModel)), BorderLayout.CENTER);

		// Layout the side pane of Harmony
		JPanel sidePane = new JPanel();
		sidePane.setLayout(new BorderLayout());
		sidePane.add(evidencePane, BorderLayout.CENTER);
		sidePane.add(filterPane, BorderLayout.SOUTH);

		// Generate the main pane of Harmony
		mainPane.setLayout(new BorderLayout());
		mainPane.add(viewPane, BorderLayout.CENTER);
		mainPane.add(sidePane, BorderLayout.EAST);

		// Generate the desktop pane
		desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		desktop.add(mainPane,JDesktopPane.DEFAULT_LAYER);
		
		// Initialize the dialog manager
		DialogManager dialogManager = new DialogManager(this);
		//harmonyModel.setDialogManager(dialogManager);
		desktop.setDesktopManager(dialogManager);

		return desktop;
	}

	/** Constructs the Harmony pane */
	public HarmonyFrame(HarmonyModel harmonyModel)
	{
		super();
		this.harmonyModel = harmonyModel;
		
		// Place title on application
		String mappingName = harmonyModel.getProjectManager().getProject().getName();
		setTitle("Harmony Schema Matcher" + (mappingName != null ? " - " + harmonyModel.getProjectManager().getProject().getName() : ""));

		// Set dialog pane settings
		((javax.swing.plaf.basic.BasicInternalFrameUI) getUI()).setNorthPane(null);
		setBorder(new EmptyBorder(0, 0, 0, 0));
		try { setMaximum(true); } catch (Exception e) {}
		setJMenuBar(new HarmonyMenuBar(harmonyModel));
		setContentPane(getMainPane());
		addComponentListener(this);
		setVisible(true);
	}

	/** Adjust the size of the various components when this pane is resized */
	public void componentResized(ComponentEvent e)
	{
		mainPane.setBounds(0,0,desktop.getWidth(),desktop.getHeight());
		desktop.revalidate();
		desktop.repaint();
	}
	
	// Unused event listeners
	public void componentShown(ComponentEvent e) {}
	public void componentHidden(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}
	public void elementsMarkedAsFinished(Integer schemaID, HashSet<Integer> elementIDs) {}
	public void elementsMarkedAsUnfinished(Integer schemaID, HashSet<Integer> elementIDs) {}
	public void showSchemaTypesChanged() {}
	public void alphabetizedChanged() {}
}