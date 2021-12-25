package org.mitre.schemastore.porters.schemaImporters;

//Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import org.mitre.schemastore.model.*;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.URIType;


public class DocumentImporter extends SchemaImporter {
	
	private ArrayList<SchemaElement> _schemaElements;
	private static final Integer DOCUMENTATION_LENGTH = 4096;
	
	
	public DocumentImporter() {
		super();
		baseDomains = new String[0][0];
	}
	/** Returns the importer name */
	public String getName() { return "Document Importer"; }

	/** Returns the importer description */
    public String getDescription()  { return "This imports schemas text documents"; }

	/** Returns the importer URI type */
	public URIType getURIType() { return URIType.FILE; }
	
	/** Returns the importer URI file types */
	public ArrayList<String> getFileTypes()
	{
		ArrayList<String> fileTypes = new ArrayList<String>();
		fileTypes.add(".txt");
		return fileTypes;
	}
	
	/** Initializes the importer for the specified URI */
	protected void initialize() throws ImporterException {

		try {
			Integer nextId = 1;
			_schemaElements = new ArrayList<SchemaElement>();
			Entity entity = new Entity(nextId++,"","",0);
			_schemaElements.add(entity);
		
	        BufferedReader in = new BufferedReader(new FileReader(new File(uri)));
	        StringBuffer docStringBuffer = new StringBuffer();
	        String string = null;
	        while ((string = in.readLine()) != null) 
	            docStringBuffer.append(" ").append(string);
	        in.close();
	        
	        String text = docStringBuffer.toString();
	        String[] docStringArray = text.split("\\s+");  
	        docStringBuffer = new StringBuffer();
	        for (String str : docStringArray){
	        	if (docStringBuffer.length() + str.length() < DOCUMENTATION_LENGTH - 1 ){
	        		docStringBuffer.append(" ").append(str);
	        	}
	        	else{
	        		Attribute attr = new Attribute(nextId++,"",docStringBuffer.toString(),entity.getId(),-3,0,1,false,0);
	        		docStringBuffer = new StringBuffer();
	        		docStringBuffer.append(str);
	        		_schemaElements.add(attr);
	        	}
	        }
	        Attribute attr = new Attribute(nextId++,"",docStringBuffer.toString(),entity.getId(),-3,0,1,false,0);
    		_schemaElements.add(attr);
	        
	   
	        
	    } catch (Exception e) {
	    	System.err.println("[E] Problem reading the document");
	    	e.printStackTrace();
	    }
		   
	}

	/** Returns the schema elements from the specified URI */
	public ArrayList<SchemaElement> generateSchemaElements() throws ImporterException
		{ return new ArrayList<SchemaElement>(_schemaElements); }

}