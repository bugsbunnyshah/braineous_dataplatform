// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.harmony.view.dialogs.importers;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;

import org.mitre.harmony.model.HarmonyModel;
import org.mitre.harmony.view.dialogs.importers.URIParameter.URIListener;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.porters.Importer;
import org.mitre.schemastore.porters.PorterType;
import org.mitre.schemastore.porters.URIType;
import org.mitre.schemastore.porters.schemaImporters.SchemaImporter;

/** Class for displaying the schema importer dialog */
public class ImportSchemaDialog extends AbstractImportDialog implements ActionListener, URIListener
{	
	/** Constructs the importer dialog */
	public ImportSchemaDialog(HarmonyModel harmonyModel)
	{
		super(harmonyModel);
		uriField.addListener(this);
		setVisible(true);
	}
	
	/** Returns the type of importer being run */
	protected PorterType getImporterType() { return PorterType.SCHEMA_IMPORTERS; }

	/** Returns the list of used schema names */
	protected ArrayList<String> getUsedNames()
	{ 
		ArrayList<String> usedNames = new ArrayList<String>();
		for(Schema schema : harmonyModel.getSchemaManager().getSchemas())
			usedNames.add(schema.getName());
		return usedNames;
	}
	
	/** Imports the specified schema locally */
	private void importItemLocally(Importer importer, String name, String author, String description, URI uri) throws Exception
		{ ((SchemaImporter)importer).importSchema(name, author, description, uri); }
	
	/** Imports the currently specified schema */
	protected void importItem(String name, String author, String description, URI uri) throws Exception
	{
		/*Importer importer = (Importer)selectionList.getSelectedItem();
		if(harmonyModel.getInstantiationType()!=InstantiationType.WEBAPP)
			importItemLocally(importer,name,author,description,uri);
		else SchemaStoreManager.importData(importer, name, author, description, uri);
		harmonyModel.getSchemaManager().initSchemas();*/
	}
	
	/** Handles the selection of the importer */
	public void actionPerformed(ActionEvent e)
	{
		super.actionPerformed(e);

		// Lock fields when importing a M3 schema 
		boolean isM3SchemaImporter = getImporter().getURIType()==URIType.M3MODEL;
		nameField.setEditable(!isM3SchemaImporter);
		descriptionField.setEditable(!isM3SchemaImporter);
		descriptionField.setBackground(isM3SchemaImporter ? new Color(0xeeeeee) : Color.white);
	}

	/** Retrieve schema from importer locally */
	private Schema getSchemaFromImporterLocally(Importer importer, URI uri) throws Exception
		{ return ((SchemaImporter)importer).getSchema(uri); }
	
	/** Handles changes to the uri */
	public void uriModified()
	{
		if(uriField.getURI()!=null)
			try {
				// Get the schema to be imported
				Schema schema = null;
				Importer importer = (Importer)selectionList.getSelectedItem();
				/*if(harmonyModel.getInstantiationType()!=InstantiationType.WEBAPP)
					schema = getSchemaFromImporterLocally(importer,uriField.getURI());
				else schema = SchemaStoreManager.getSchemaFromImporter(importer,uriField.getURI());*/
				
				// Populate data fields from the schema
				if(schema!=null)
				{
					nameField.setText(schema.getName());
					authorField.setText(schema.getAuthor());
					descriptionField.setText(schema.getDescription());
				}
			} catch(Exception e2) {}
	}
}