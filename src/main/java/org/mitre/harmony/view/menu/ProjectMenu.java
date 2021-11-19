// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.mitre.harmony.controllers.ProjectController;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.project.ProjectMapping;
import org.mitre.harmony.view.dialogs.exporters.ExportMappingDialog;
import org.mitre.harmony.view.dialogs.exporters.ExportProjectDialog;
import org.mitre.harmony.view.dialogs.importers.ImportProjectDialog;
import org.mitre.harmony.view.dialogs.project.ProjectDialog;
import org.mitre.harmony.view.dialogs.schema.SchemaDialog;

/** Drop-down menu found under project menu bar heading */
class ProjectMenu extends AbstractMenu implements MenuListener
{
	/** Stores a mapping menu item */
	class MappingMenuItem extends JMenuItem implements ActionListener
	{
		private ProjectMapping mapping = null;
		
		/** Constructs the mapping menu item */
		private MappingMenuItem(ProjectMapping mapping)
			{ super(mapping.getName()); this.mapping = mapping; addActionListener(this); }

		/** Handles the selection of a mapping menu item */
		public void actionPerformed(ActionEvent e)
			{ new ExportMappingDialog(mapping).export(harmonyModel); }
	}
	
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** Stores the menu of mappings to export */
	private JMenu exportMappingMenu;
	
	/** Initializes the project drop-down menu */
	ProjectMenu(HarmonyModel harmonyModel)
	{
		// Gives the drop-down menu the title of "Project"
		super("Project");
		this.harmonyModel = harmonyModel;
		setMnemonic(KeyEvent.VK_P);
		
		// Set up menu for stand-alone mode
		/*if(harmonyModel.getInstantiationType()!=InstantiationType.EMBEDDED)
		{	
			// Constructs the new, open, and save menu items
			JMenuItem newProject = createMenuItem("New", KeyEvent.VK_N, new CreateProjectAction());
			JMenuItem openProject = createMenuItem("Open...", KeyEvent.VK_O, new OpenProjectAction());
			JMenuItem saveProject = createMenuItem("Save...", KeyEvent.VK_S, new SaveProjectAction());
			
			// Initialize the import menu
			JMenu importMenu = new JMenu("Import");
			importMenu.setMnemonic(KeyEvent.VK_I);
			importMenu.add(createMenuItem("Import Project", KeyEvent.VK_P, new ImportProjectAction()));
			importMenu.add(createMenuItem("Import Mapping", KeyEvent.VK_M, new ImportMappingAction()));
			
			// Initialize the export mapping menu
			exportMappingMenu = new JMenu("Export Mapping");
			exportMappingMenu.setMnemonic(KeyEvent.VK_M);
			exportMappingMenu.addMenuListener(this);

			// Initialize the export menu
			JMenu exportMenu = new JMenu("Export");
			exportMenu.setMnemonic(KeyEvent.VK_E);
			exportMenu.add(createMenuItem("Export Project", KeyEvent.VK_P, new ExportProjectAction()));
			exportMenu.add(exportMappingMenu);
			
			// Add project drop-down items to project drop-down menu
			add(newProject);
			add(openProject);
			add(saveProject);
			addSeparator();
			add(createMenuItem("Configure Project...", KeyEvent.VK_C, new ProjectSettingsAction()));
			add(createMenuItem("Manage Schemas...", KeyEvent.VK_M, new ManageSchemaAction()));
			addSeparator();
			add(importMenu);
			add(exportMenu);
			addSeparator();
		    add(createMenuItem("Exit", KeyEvent.VK_X, new ExitAction()));
		    
			// Set accelerator keys for the menu items
			newProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
			openProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
			saveProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		}

		// Otherwise, set up menu to be embedded in OpenII
		else
		{
			// Constructs the save menu item
			JMenuItem saveProject = createMenuItem("Save", KeyEvent.VK_S, new SaveProjectAction());
			
			// Add project drop-down items to project drop-down menu
			add(saveProject);
			addSeparator();
			add(createMenuItem("Configure Project...", KeyEvent.VK_P, new ProjectSettingsAction()));

			// Set accelerator keys for the save menu item
			saveProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		}*/
	}

	/** Dynamically generates the "Export Mapping" menu */
	public void menuSelected(MenuEvent e)
	{
		if(e.getSource().equals(exportMappingMenu))
		{
			exportMappingMenu.removeAll();
			for(ProjectMapping mapping : harmonyModel.getMappingManager().getMappings())
				exportMappingMenu.add(new MappingMenuItem(mapping));
			exportMappingMenu.revalidate();
		}
	}
    
    /** Attempts to save old projects before opening new projects */
    private boolean saveOldProject()
    {
		/*int option = 1;
		if(harmonyModel.getProjectManager().isModified())
    		option = JOptionPane.showConfirmDialog(harmonyModel.getBaseFrame(),
    			"This project has been modified.  Do you want to save changes?",
				"Save Project", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if(option==2) return false;
		if(option==0) new SaveMappingDialog(harmonyModel);*/
		return true;
    }
    
	/** Action for creating a new project */
	private class CreateProjectAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			if(saveOldProject())
				{ ProjectController.newProject(harmonyModel); new ProjectDialog(harmonyModel); }
		}
	}
    
	/** Action for opening a project */
	private class OpenProjectAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			if(saveOldProject()) {
				//harmonyModel.getDialogManager().openDialog(new LoadProjectDialog(harmonyModel));
			}
		}
	}
    
	/** Action for saving the project */
	private class SaveProjectAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
    		/*if(harmonyModel.getInstantiationType()!=InstantiationType.EMBEDDED || harmonyModel.getProjectManager().getProject().getId()==null)
    			harmonyModel.getDialogManager().openDialog(new SaveMappingDialog(harmonyModel));
    		else ProjectController.saveProject(harmonyModel,harmonyModel.getProjectManager().getProject());*/
		}
	}
	
	/** Action for launching the "Import Project" dialog */
	private class ImportProjectAction extends AbstractAction implements InternalFrameListener
	{
		public void actionPerformed(ActionEvent e)
		{
			ImportProjectDialog dialog = new ImportProjectDialog(harmonyModel);
			dialog.addInternalFrameListener(this);
			//harmonyModel.getDialogManager().openDialog(dialog);
		}
		
		/** Reselect the mappings with the newly imported project */
		public void internalFrameClosing(InternalFrameEvent e)
			{ ProjectController.selectMappings(harmonyModel); }
		
		// Unused event listeners
		public void internalFrameOpened(InternalFrameEvent e) {}
		public void internalFrameClosed(InternalFrameEvent e) {}
		public void internalFrameIconified(InternalFrameEvent e) {}
		public void internalFrameDeiconified(InternalFrameEvent e) {}
		public void internalFrameActivated(InternalFrameEvent e) {}
		public void internalFrameDeactivated(InternalFrameEvent e) {}
	}
    
	/** Action for launching the "Export Project" dialog */
	private class ExportProjectAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
			{ new ExportProjectDialog().export(harmonyModel); }
	}
    
	/** Action for launching the "Import Mapping" dialog */
	private class ImportMappingAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
			{
				//harmonyModel.getDialogManager().openDialog(new ImportMappingDialog(harmonyModel));
			}
	}
    
	/** Action for launching the configuration dialog */
	private class ProjectSettingsAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
			{
				//harmonyModel.getDialogManager().openDialog(new ProjectDialog(harmonyModel));
			}
	}
    
	/** Action for launching the schema management dialog */
	private class ManageSchemaAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			SchemaDialog dialog = new SchemaDialog(harmonyModel, new ArrayList<Integer>());
			//harmonyModel.getDialogManager().openDialog(dialog);
		}
	}
 
	/** Action for exiting Harmony */
	private class ExitAction extends AbstractAction
		{ public void actionPerformed(ActionEvent e) {
			//harmonyModel.getBaseFrame().dispose();
		}
		}
    
	// Unused action listener
	public void menuCanceled(MenuEvent e) {}
	public void menuDeselected(MenuEvent e) {}
}
