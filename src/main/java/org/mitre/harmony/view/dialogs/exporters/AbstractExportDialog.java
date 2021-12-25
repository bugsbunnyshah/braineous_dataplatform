// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs.exporters;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.model.SchemaStoreManager;
import org.mitre.harmony.view.dialogs.widgets.AbstractButtonPane;
import org.mitre.schemastore.porters.Exporter;
import org.mitre.schemastore.porters.PorterType;

/**
 * Abstract dialog for exporting
 * @author CWOLF
 */
abstract class AbstractExportDialog
{	
	/** Private class for creating an exporter file filter */
	private static class ExportFileFilter extends FileFilter
	{
		/** Stores the porter */
		private Exporter exporter = null;
		
		/** Constructs the project file filter */
		private ExportFileFilter(Exporter exporter)
			{ this.exporter = exporter; }

		/** Indicates if the file should be accepted */
		public boolean accept(File file)
		{
			if(file.isDirectory()) return true;
			if(file.toString().endsWith(exporter.getFileType())) return true;
			return false;
		}

		/** Provides the exporter file description */
		public String getDescription()
			{ return exporter.getName() + "(" + exporter.getFileType() + ")"; }
	}
	
	/** Dialog for selecting exporter to use for export via web service */
	private class WebServiceDialog extends JInternalFrame
	{
		/** Stores the Harmony model */
		private HarmonyModel harmonyModel;
		
		/** Stores the selected exporter */
		private JComboBox exporterList = null;
		
		/** Private class for defining the button pane */
		private class ButtonPane extends AbstractButtonPane
		{
			/** Constructs the button pane */
			private ButtonPane()
				{ super(new String[]{"OK","Cancel"},1,2); }

			/** Handles selection of button */
			protected void buttonPressed(String label)
			{
				if(label.equals("OK"))
				{
					/*try {
						String filename = exportViaWebService(harmonyModel, (Exporter)exporterList.getSelectedItem());
						if(filename!=null)
						{

						}
							//harmonyModel.getApplet().getAppletContext().showDocument(new URL("javascript:exportFile(\""+filename+"\")"));
					} catch(MalformedURLException me) {}*/
				}
				dispose();
			}
		}
		
		/** Initializes the search dialog */
		public WebServiceDialog(HarmonyModel harmonyModel)
		{
			super(getDialogTitle());
			this.harmonyModel = harmonyModel;
			
			// Initialize the exporter list
			ArrayList<Exporter> exporters = SchemaStoreManager.getPorters(getExporterType());
			exporterList = new JComboBox(new Vector<Exporter>(exporters));
			exporterList.setBackground(Color.white);
			exporterList.setFocusable(false);
			exporterList.setSelectedIndex(0);			
			
			// Create the info pane
			JPanel infoPane = new JPanel();
			infoPane.setBorder(new CompoundBorder(new EmptyBorder(5,5,0,5),new CompoundBorder(new LineBorder(Color.gray),new EmptyBorder(8,8,8,8))));
			infoPane.setLayout(new GridLayout(2,1));
			infoPane.add(new JLabel("Select an exporter:"));
			infoPane.add(exporterList);
			
			// Generate the main dialog pane
			JPanel pane = new JPanel();
			pane.setBorder(BorderFactory.createLineBorder(Color.black));
			pane.setLayout(new BorderLayout());
			pane.add(infoPane,BorderLayout.CENTER);
			pane.add(new ButtonPane(),BorderLayout.SOUTH);
			
			// Initialize the dialog parameters
			setContentPane(pane);
			pack();
			setVisible(true);
		}
	}
	
	/** Abstract class for defining the type of exporters */
	abstract protected PorterType getExporterType();
	
	/** Abstract class for exporting to the specified file */
	abstract protected void export(HarmonyModel harmonyModel, Exporter exporter, File file) throws IOException;

	/** Abstract class for exporting to the specified file */
	abstract protected String exportViaWebService(HarmonyModel harmonyModel, Exporter exporter);
	
	/** Returns the dialog title */
	private String getDialogTitle()
	{
		if(getExporterType()==PorterType.SCHEMA_EXPORTERS) return "Export Schema";
		if(getExporterType()==PorterType.PROJECT_EXPORTERS) return "Export Project";
		if(getExporterType()==PorterType.MAPPING_EXPORTERS) return "Export Mapping";
		return null;
	}
		
	/** Exports via local client */
	private void exportViaLocalClient(HarmonyModel harmonyModel)
	{
		// Initialize the file chooser
		JFileChooser chooser = new JFileChooser(harmonyModel.getPreferences().getExportDir());
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setAcceptAllFileFilterUsed(false);

		// Set up file filters for the various project exporters
		ArrayList<Exporter> exporters = SchemaStoreManager.getPorters(getExporterType());
		for(Exporter exporter : exporters)
			chooser.addChoosableFileFilter(new ExportFileFilter(exporter));

		// Display the dialog for selecting which exporter to use
		/*if(chooser.showDialog(harmonyModel.getBaseFrame(),getDialogTitle())==JFileChooser.APPROVE_OPTION)
		{
			// Retrieve the selected file and exporter from the file chooser
			File file = chooser.getSelectedFile();
			harmonyModel.getPreferences().setExportDir(file.getParentFile());
			Exporter exporter = ((ExportFileFilter)chooser.getFileFilter()).exporter;

			// Ensure that file has the proper ending
			if(!file.getName().endsWith(exporter.getFileType()))
				file = new File(file.getPath()+exporter.getFileType());
			
			// Check to see if file already exists and checks to make sure it can be overwritten
			if(file!=null && file.exists() && exporter.isFileOverwritten())
    		{
	    		int option = JOptionPane.showConfirmDialog(harmonyModel.getBaseFrame(),
	        			file + " exists.  Overwrite?",
						"Overwrite File", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);
	    		if(option==1) return;
    		}

			// Export the project to the specified file
			try { export(harmonyModel, exporter, file); }
			catch(IOException e)
			{
				JOptionPane.showMessageDialog(harmonyModel.getBaseFrame(),
						"File " + file.toString() + " failed to be exported.\n"+e.getMessage(),
						"Export Error",JOptionPane.ERROR_MESSAGE);
			}
		}*/
	}
	
	/** Allows user to export */
	public void export(HarmonyModel harmonyModel)
	{		
		/*if(harmonyModel.getInstantiationType()!=InstantiationType.WEBAPP)
			exportViaLocalClient(harmonyModel);
		else harmonyModel.getDialogManager().openDialog(new WebServiceDialog(harmonyModel));*/
	}
}
