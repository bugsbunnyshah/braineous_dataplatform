package org.mitre.harmony.view.dialogs.projects;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.dialogs.widgets.TitledPane;
import org.mitre.schemastore.model.Project;

/** Private class for managing the project list */
class ProjectPane extends JPanel implements MouseListener
{
	/** Comparator used to alphabetize projects */
	private class ProjectComparator implements Comparator<Object>
	{
		public int compare(Object project1, Object project2)
		{
			if(project1.toString()==null) return -1;
			return project1.toString().compareTo(project2.toString());
		}	
	}
	
	/** Defines how the projects in the list should be rendered */
	private class ListRenderer extends DefaultListCellRenderer
	{
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
		{
			if(((Project)value).getId()==null) value = "<New Project>";
			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}
	 }
	
	/** Handles the deletion of a project */
	private class DeleteProject extends AbstractAction
	{
		/** Stores the project to be deleted */
		private Project project = null;
		
		/** Constructs the "Delete Project" action */
		private DeleteProject()
			{ super("Delete Project"); }
		
		/** Constructs the "Delete Project" action with the specified project */
		private DeleteProject(Project project)
			{ this(); this.project = project; }
		
		/** Deletes selected project */
		public void actionPerformed(ActionEvent e)
		{
			// Gets the selected project
			if(project==null) project = getProject();
			if(project.getId()!=null)
			{
				// Ask user before deleting project
				int option = 0;
				/*JOptionPane.showConfirmDialog(harmonyModel.getBaseFrame(),
		    		"Continue with deletion of project \"" + project.getName() + "\"?",
					"Delete Project", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE);*/
				if(option==2) return;
	
				// Delete the selected project
				if(harmonyModel.getProjectManager().deleteProject(project.getId()))
				{
					// Generate a list of projects with the specified project removed
					Vector<Project> projects = new Vector<Project>();
					for(int i=0; i<projectList.getModel().getSize(); i++)
						if(i!=projectList.getSelectedIndex())
							projects.add((Project)projectList.getModel().getElementAt(i));
				
					// Reset the list
					projectList.setListData(projects);
					projectList.setSelectedIndex(0);
				}
			}
		}
	}
	
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** Stores the project list */
	private JList projectList = null;
	
	/** Constructs the project pane */
	ProjectPane(Boolean saveMode, HarmonyModel harmonyModel)
	{
		this.harmonyModel = harmonyModel;
		
		// Retrieve the list of projects
		Vector<Project> projects = new Vector<Project>(harmonyModel.getProjectManager().getProjects());
		Collections.sort(projects, new ProjectComparator());
		if(saveMode)
		{
			Project newProject = harmonyModel.getProjectManager().getProject().copy();
			//newProject.setId(null); newProject.setName(""); newProject.setAuthor(harmonyModel.getUserName());
			projects.add(0,newProject);
		}
		
		// Initializes the project list
		projectList = new JList(projects);
		projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		projectList.setCellRenderer(new ListRenderer());
		projectList.setSelectedIndex(0);
		
		// Locks ability to switch projects if not in standalone mode
		/*if(harmonyModel.getInstantiationType()!=InstantiationType.EMBEDDED)
			projectList.addMouseListener(this);
		else projectList.setEnabled(false);*/

		// If in save mode, select current project
		if(saveMode)
		{
			Project project = harmonyModel.getProjectManager().getProject();
			if(project.getId()!=null)
				for(int i=0; i<projectList.getModel().getSize(); i++)
					if(project.getId().equals(((Project)projectList.getModel().getElementAt(i)).getId()))
						projectList.setSelectedIndex(i);
		}
		
		// Create a scroll pane to hold the project list
		JScrollPane projectScrollPane = new JScrollPane(projectList,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		projectScrollPane.setPreferredSize(new Dimension(130,200));
		JPanel mappingListPane = new TitledPane("Projects",projectScrollPane);
		
		// Create the project list pane
		setLayout(new BorderLayout());
		add(mappingListPane,BorderLayout.CENTER);
		
		// Set up the ability to delete projects
		KeyStroke deleteKey = KeyStroke.getKeyStroke((char) KeyEvent.VK_DELETE);
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(deleteKey, "deleteProject");
		getActionMap().put("deleteProject", new DeleteProject());
	}
	
	/** Returns the selected project */
	Project getProject()
		{ return (Project)projectList.getSelectedValue(); }	
	
	/** Adds a list listener */
	void addListSelectionListener(ListSelectionListener listener)
		{ projectList.addListSelectionListener(listener); }

	/** Handles the clicking of the mouse on a project in the list */
	public void mouseClicked(MouseEvent e)
	{
		if(e.getButton() == MouseEvent.BUTTON3 || e.isMetaDown())
		{
			int index = projectList.locationToIndex(e.getPoint());
			Project project = (Project)projectList.getModel().getElementAt(index);
			if(project.getId()!=null)
			{
				JPopupMenu menu = new JPopupMenu();
				menu.add(new JMenuItem(new DeleteProject(project)));
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	// Unused event actions
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
}