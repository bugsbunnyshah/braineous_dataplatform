// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URI;

import javax.swing.JFrame;

import org.mitre.harmony.model.ConfigManager;
import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.SchemaStoreManager;
import org.mitre.harmony.model.project.ProjectListener;
import org.mitre.harmony.view.harmonyPane.HarmonyFrame;
import org.mitre.schemastore.client.Repository;

/** Main Harmony class */
public class Harmony extends JFrame implements ProjectListener, WindowListener
{
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** Constructs the Harmony frame */
    public Harmony()
    {
    	super();
    	harmonyModel = new HarmonyModel();
    	
    	// Place title on application
		String projectName = harmonyModel.getProjectManager().getProject().getName();
		setTitle("Harmony Schema Matcher" + (projectName!=null ? " - " + harmonyModel.getProjectManager().getProject().getName() : ""));
    	
    	// Set dialog pane settings
	   	setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/org/mitre/org.org.mitre.harmony/view/graphics/SSM.jpg")));
	   	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setContentPane(new HarmonyFrame(harmonyModel));
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	   	setSize(dim.width*3/4,dim.height*3/4);
	   	setLocationRelativeTo(null);
 	   	setVisible(true);

	   	// Add in application listeners
 	   	harmonyModel.getProjectManager().addListener(this);
		addWindowListener(this);
    }

    /** Adjusts Harmony title to indicate currently opened project file */
	public void projectModified()
	{
		String projectName = harmonyModel.getProjectManager().getProject().getName();
		setTitle("Harmony Schema Matcher" + (projectName!=null ? " - " + harmonyModel.getProjectManager().getProject().getName() : ""));
	}

	/** Disposes of the Harmony frame */
	public void dispose()
	{
		/*int option = 1;
		if(harmonyModel.getProjectManager().isModified())
    		option = JOptionPane.showConfirmDialog(harmonyModel.getBaseFrame(),
    			"This mapping has been modified.  Do you want to save changes?",
				"Save Mapping", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if(option==2) return;
		if(option==0) harmonyModel.getDialogManager().openDialog(new SaveMappingDialog(harmonyModel));
		super.dispose();*/
	}
	
	/** Forces graceful closing of Harmony */
	public void windowClosing(WindowEvent event) { dispose(); }

	// Unused event listeners
	public void windowActivated(WindowEvent arg0) {}
	public void windowClosed(WindowEvent arg0) {}
	public void windowDeactivated(WindowEvent arg0) {}
	public void windowDeiconified(WindowEvent arg0) {}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}

	/** Launches Harmony */
	static public void main(String args[])
	{
		Repository repository = null;
		try {
			// Retrieve the repository information
			String typeParm = ConfigManager.getParm("repository.type");
			String locationParm = ConfigManager.getParm("repository.location");
			String databaseParm = ConfigManager.getParm("repository.database");
			String usernameParm = ConfigManager.getParm("repository.username");
			String passwordParm = ConfigManager.getParm("repository.password");
			
			// Create a repository connection
			Integer type = typeParm.equals("service")?Repository.SERVICE:typeParm.equals("postgres")?Repository.POSTGRES:Repository.DERBY;
			URI uri = null;
			if(new File(locationParm).exists())
				try { uri = new File(locationParm).toURI(); } catch(Exception e) {}
			else try { uri = new URI(locationParm); } catch(Exception e) {}
			repository = new Repository(type,uri,databaseParm,usernameParm,passwordParm);
		}
		catch(Exception e) { repository = new Repository(Repository.DERBY,new File("").toURI(), "org/mitre/schemastore","postgres","postgres"); }
			
		// Launch Harmony
		try {
			SchemaStoreManager.setClient(repository);
			new Harmony();
		} catch(Exception e) { System.out.println("(E) Failed to connect to SchemaStore"); }
	}

	// Unused event listeners
	public void schemaAdded(Integer schemaID) {}
	public void schemaModelModified(Integer schemaID) {}
	public void schemaRemoved(Integer schemaID) {}
}