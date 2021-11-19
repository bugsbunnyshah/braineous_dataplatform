// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.porters.schemaImporters;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.URIType;
import com.healthmarketscience.jackcess.*;
import com.healthmarketscience.jackcess.Index.ColumnDescriptor;

/**
 * DOCUMENT ME!
 *
 * @author piekut
 * @version $Revision: 1.0 $
 */
public class GDBImporter extends SchemaImporter {
	private ArrayList<Entity> _entities;
	private ArrayList<Attribute> _attributes;
	private ArrayList<Domain> _domains;
	private ArrayList<DomainValue> _enumerants;
	private ArrayList<Containment> _containments;
	Database mdb = null;
            
	/** Returns the importer name */
	public String getName() { return "GDB Importer~"; }

	/** Returns the importer description */
    public String getDescription()  { return "This imports schemas from the NAS-specific Access MDB"; }

	/** Returns the importer URI type */
    public URIType getURIType() { return URIType.LIST; }
	
	/** Returns the list of URIs which can be selected from (only needed when URI type is LIST) */
	public ArrayList<URI> getList()
	{
		/** Replace with the real way of accessing list items */
		ArrayList<URI> items = new ArrayList<URI>();
		try {
			items.add(new URI("Item1"));
			items.add(new URI("Item2"));
			items.add(new URI("./GDB.mdb"));	
		} catch(Exception e) {}
		return items;
	}
	
	/** Initializes the importer for the specified URI */
	protected void initialize() throws ImporterException {
		String filename = "";
		try {
			//uri comes in as ./foo, needs to be foo
			filename = uri.toString().substring(2);
			
			//uris replace spaces with %20s.  Fix that.
			filename = filename.replaceAll("%20"," ");
			filename = "C:\\apache-tomcat-6.0.28\\webapps\\" + filename;
			
System.out.println(filename);
            java.io.File mdbFile = new java.io.File(filename);
            mdb = Database.open(mdbFile);
			
			
			System.out.println ("Database connection established."); 
	        _entities = new ArrayList<Entity>();
			_attributes = new ArrayList<Attribute>();
			_domains = new ArrayList<Domain>();
			_enumerants = new ArrayList<DomainValue>();
			_containments = new ArrayList<Containment>();
		}
		catch(Exception e) { 
			// If anything goes wrong, alert on the command line.
		    System.err.println ("Cannot connect to database server " + filename + "!"); 
		    e.printStackTrace(); 
			//throw new ImporterException(ImporterException.PARSE_FAILURE,e.getMessage()); 
	    }		
	}

	/** Returns the schema elements from the specified URI */
	public ArrayList<SchemaElement> generateSchemaElements() throws ImporterException {	
		generate();
		return generateSchemaElementList();
	}
	
	protected void generate() {
		// Mapping of gdbTable_PK to SchemaElement id
		HashMap<String, Integer> EntityKey = new HashMap<String, Integer>();
      try {
    	Entity tblEntity = null;;      
    	ResultSet entities = null;  
    	Statement selectEntities = null;
		
    	Table t = mdb.getTable("T_AbstractTable");
    	Cursor c = Cursor.createCursor(t);

		while(c.moveToNextRow()) {
			int i = 0;
			String key = ((String) c.getCurrentRowValue(t.getColumn("gdbTable_PK")));  // key to get attributes.
			Integer ID_FK = (Integer) c.getCurrentRowValue(t.getColumn("itemIdentifier_FK"));  // key to get attributes.
			String tblName = ((String) c.getCurrentRowValue(t.getColumn("tableName"))).toLowerCase(); // note these match the columns in the  
        	tblEntity = createEntity(key, ID_FK, tblName, EntityKey);  			
			_entities.add(tblEntity);
		}
		
		
		// Create nodes for abstract classes in T_AbstractTable -- added by Joel
		t = mdb.getTable("T_Schema");
    	c = Cursor.createCursor(t);
    	c.moveToNextRow();
		Boolean IATStripIETName=(Boolean) c.getCurrentRowValue(t.getColumn("P_IATStripIETName"));
		String IATStripNameSeparator=(String) c.getCurrentRowValue(t.getColumn("P_IATStripNameSeparator"));
		Boolean IATStripNameSpaces=(Boolean) c.getCurrentRowValue(t.getColumn("P_IATStripNameSpaces"));
       	if (IATStripIETName && IATStripNameSpaces)  { IATStripNameSeparator = " " + IATStripNameSeparator + " "; }
		
       	t = mdb.getTable("T_Table");
    	c = Cursor.createCursor(t);

        while(c.moveToNextRow()) {
        	Integer ID_FK = (Integer) c.getCurrentRowValue(t.getColumn("itemIdentifier_FK"));  // key to get attributes.
        	String FTID = ((String) c.getCurrentRowValue(t.getColumn("gdbTable_PK")));  // key to get attributes.
        	String tblName = ((String) c.getCurrentRowValue(t.getColumn("tableName"))).toLowerCase(); // note these match the columns in the  
        	tblEntity = createEntity(FTID, ID_FK, tblName, EntityKey);  			
  			_entities.add(tblEntity);  			
        	    		
            // Find all attributes belonging to the feature type
            Attribute attribute;        
            Table t_att = mdb.getTable("T_Field");
			Cursor c_att = Cursor.createCursor(t_att);			
            int a=0;
            Hashtable<String, Integer> datatypeIDs = new Hashtable<String, Integer>();
            while(c_att.moveToNextRow()) {
            	String att_key = (String) c_att.getCurrentRowValue(t_att.getColumn("gdbTable_FK"));
            	if (!att_key.equals(FTID)) { continue; }
            	Integer attID= (Integer) c_att.getCurrentRowValue(t_att.getColumn("itemIdentifier_FK"));  
            	String attName= (String) c_att.getCurrentRowValue(t_att.getColumn("name"));  
            	String dtID = (String) c_att.getCurrentRowValue(t_att.getColumn("gdbField_PK")); // used to key on enumerants
            	String uniqueAttribute = ""+((Integer) c_att.getCurrentRowValue(t_att.getColumn("infoAttID_FK")));
            	if (IATStripIETName) {
            		attName= concatNonNullFields(tblEntity.getName(), attName, IATStripNameSeparator);
            	}
            	Table t_fs = mdb.getTable("T_FieldSpecification");
    			Cursor c_fs = Cursor.createCursor(t_fs);
    			c_fs.findRow(t_fs.getColumn("itemIdentifier_PK"), attID);
    				String attDefinition = (String) c_fs.getCurrentRowValue(t_fs.getColumn("definition"));
    				attDefinition = concatNonNullFields(attDefinition, ((String) c_fs.getCurrentRowValue(t_fs.getColumn("description"))), " [desc] ");
    				attDefinition = concatNonNullFields(attDefinition, ((String) c_fs.getCurrentRowValue(t_fs.getColumn("note"))), " [note] ");
    				String generalDatatype = (String) c_fs.getCurrentRowValue(t_fs.getColumn("generalDatatype"));
    				attDefinition = concatNonNullFields(attDefinition, generalDatatype, " [type] ");
    				String lexical = ((Boolean) c_fs.getCurrentRowValue(t_fs.getColumn("lexical")))+"";
    				if (lexical.equalsIgnoreCase("true")) { lexical ="Lexical"; }
    				else { lexical="Non-lexical"; }
                                               
    				String attLength = ""+((Short) c_fs.getCurrentRowValue(t_fs.getColumn("length")));
    				if ((attLength==null || attLength.equals("null") || attLength.equals(""))) { 
    					attLength="Unlimited";
    				}                
    				if (!(generalDatatype.equals("Integer") || generalDatatype.equals("Boolean") || generalDatatype.equals("Real"))) {
    					attDefinition = concatNonNullFields(attDefinition, lexical, " [lex] ");
    					attDefinition = concatNonNullFields(attDefinition, attLength, " [len] ");
    				}
    				attDefinition = concatNonNullFields(attDefinition, ((String) c_fs.getCurrentRowValue(t_fs.getColumn("structureSpecification"))), " [struc] ");
    				String measure = (String) c_fs.getCurrentRowValue(t_fs.getColumn("measure"));
    				
    				String validRange = (String) c_fs.getCurrentRowValue(t_fs.getColumn("validRange"));
    				String rMin = ""+((Double) c_fs.getCurrentRowValue(t_fs.getColumn("rangeMinimum")));
    				String rMax = ""+((Double) c_fs.getCurrentRowValue(t_fs.getColumn("rangeMaximum")));
            	
    				if (!generalDatatype.equals("Text")) {
    					attDefinition = concatNonNullFields(attDefinition, measure, " [UoM] ");
    					attDefinition = concatNonNullFields(attDefinition, validRange, " [range] ");
            			attDefinition = concatNonNullFields(attDefinition, rMin, " [min] ");
            			attDefinition = concatNonNullFields(attDefinition, rMax, " [max] ");    
    				}                
    				String datatype = (String) c_fs.getCurrentRowValue(t_fs.getColumn("datatypeNsgAlphaCode"));
                              
    				String domainType = datatype;
    				String ignoreEnumerants="no";
    				
    				if (generalDatatype.equals("Enumeration")) {
    					if (datatype.equals("Boolean")) { 
    						ignoreEnumerants="yes";
    						uniqueAttribute=datatype;
    					}
    				}
    				else if (generalDatatype.equals("Integer")||generalDatatype.equals("Real")) {
    					domainType = concatNonNullFields(domainType, measure, " : ");
    					domainType = concatNonNullFields(domainType, validRange, " ");
    					if (!((concatNonNullFields("",rMin,"")+concatNonNullFields("",rMax,"")).equals(""))) {
    						domainType += " <";
                			domainType = concatNonNullFields(domainType, rMin, "");
                			if (!((concatNonNullFields("",rMin,"")).equals(""))) {
                				domainType = concatNonNullFields(domainType, rMax, ", ");
                			}
                			else { domainType = concatNonNullFields(domainType, rMax, ""); }
                			domainType += ">";
    					}
    				}
    				else {
    					domainType+= " : " +lexical;
                	
    					domainType+= " : " +attLength;
    					if (!((generalDatatype.equals("Text")||generalDatatype.equals("Complex")))){
    						domainType += " : ERROR";
    					}
    				}                        	
            	
    				if (! datatypeIDs.containsKey(uniqueAttribute)) {
    					Domain domain = new Domain(nextId(), domainType, null, 0);
    					_domains.add(domain);
    					datatypeIDs.put(uniqueAttribute, domain.getId());
    				}         
    				else { ignoreEnumerants="yes"; }
            	
    				// create an attribute
    				if (attName.length() > 0 ) {   
    					a++;
    					attribute = new Attribute(nextId(), attName, attDefinition, tblEntity.getId(), 
    							datatypeIDs.get(uniqueAttribute), null, null, false, 0);
    					_attributes.add(attribute);
    				}
        		
    				if (ignoreEnumerants.equals("no")) { 
    					Table t_enum = mdb.getTable("T_CodedValue");
    					Cursor c_enum = Cursor.createCursor(t_enum);
    					// Find any enumerants to this attribute (most will have none).
    					while(c_enum.moveToNextRow()) { 
    						String e_key = (String) c_enum.getCurrentRowValue(t_enum.getColumn("gdbField_FK"));
    						if (!e_key.equals(dtID)) { continue; }
    						Integer enumID= (Integer) c_enum.getCurrentRowValue(t_enum.getColumn("itemIdentifier_FK"));
    						Table t_cs = mdb.getTable("T_CodedValueSpecification");
    		    			Cursor c_cs = Cursor.createCursor(t_cs);
    		    			c_cs.findRow(t_cs.getColumn("itemIdentifier_PK"), enumID);
    		    			String enumName= (String) c_cs.getCurrentRowValue(t_cs.getColumn("name"));
    						String enumDefinition = (String) c_cs.getCurrentRowValue(t_cs.getColumn("definition"));    		    			
    						enumDefinition = concatNonNullFields(enumDefinition, 
    							((String) c_cs.getCurrentRowValue(t_cs.getColumn("description"))), " [desc] ");
    						enumDefinition = concatNonNullFields(enumDefinition, 
    								((String) c_cs.getCurrentRowValue(t_cs.getColumn("note"))), " [note] ");
    						DomainValue enumerant = new DomainValue(nextId(), enumName, enumDefinition,
    								datatypeIDs.get(uniqueAttribute), 0);
    						_enumerants.add(enumerant);
    					} 		
    				}
            } // while attributes
        } // while Entities
		addContainments();
      }
      catch (Exception e) { e.printStackTrace(); }
	}

	
	//  Simple function to make all caps data easier on the eyes.
	public static String firstLetterCapitalize(String line){
	        char[] charArray = line.toCharArray();
	        int linLen = line.length();
	        charArray[0] = Character.toUpperCase(charArray[0]);
	        for (int i=1;i<linLen;i++) {
	            if (line.substring(i-1,i).equals("_") ||line.substring(i-1,i).equals(" ")) { charArray[i] = Character.toUpperCase(charArray[i]); }
	        }
		return new String(charArray);
	} 
	
	public String concatNonNullFields(String description, String field, String joinText) {
		if (!(field==null || field.equals("null") || field.equals(""))) { 
			description += joinText + field;
        }
		return description;
	}
	
	protected ArrayList<SchemaElement> generateSchemaElementList() {
		ArrayList<SchemaElement> schemaElements = new ArrayList<SchemaElement>();
		int i = 0; // just counting elements here.
		for (int j=0; j<_entities.size(); j++) { schemaElements.add(_entities.get(j)); i++;	}
		for (int j=0; j<_domains.size(); j++) { schemaElements.add(_domains.get(j)); i++; }
		for (int j=0; j<_attributes.size(); j++) { schemaElements.add(_attributes.get(j)); i++; }
		for (int j=0; j<_enumerants.size(); j++) { schemaElements.add(_enumerants.get(j)); i++;	}
		for (int j=0; j<_containments.size(); j++) { schemaElements.add(_containments.get(j)); i++;	}
		//System.out.println(i+ " elements.");
		return schemaElements;
	}
	
	//given a ResultSet (iterated elsewhere) with fields "tableName, definition, description, entityNote", return new Entity
	private Entity createEntity(String key, Integer FK, String tblName, HashMap<String, Integer> EntityKey){
		String definition = null;				
		try {
			Table t = mdb.getTable("T_TableSpecification");
			Column col = t.getColumn("itemIdentifier_PK");
			String iName = "";
			Iterator<Index> it = t.getIndexes().iterator();
			while ( it.hasNext()) {
				Index i = it.next();	
				if (i.getColumns().size()==1 ){
					ColumnDescriptor cd = i.getColumns().iterator().next();
					if (cd.getColumn().equals(col)) { iName=i.getName(); }
				}
			}
			Index index = t.getIndex(iName);
			Cursor C = Cursor.createIndexCursor(t, index);
			C.findRow(col, FK);			
			tblName = firstLetterCapitalize(tblName);   //Make nicer looking with 1st-cap.        	
			definition = (String) C.getCurrentRowValue(t.getColumn("definition"));
			definition = concatNonNullFields(definition, ((String) C.getCurrentRowValue(t.getColumn("description"))), " [desc] ");
			definition = concatNonNullFields(definition, ((String) C.getCurrentRowValue(t.getColumn("note"))), " [note] ");

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		int id = nextId();
		EntityKey.put(key, id);

		return new Entity(id, tblName, definition, 0);
	}
	
	private void addContainments() {
		try {
			Table t = mdb.getTable("T_Containment");
	    	Cursor C = Cursor.createCursor(t);
			while(C.moveToNextRow()) {
				Integer childID = (Integer) C.getCurrentRowValue(t.getColumn("gdbTable_FK"));
				Integer parentID = (Integer) C.getCurrentRowValue(t.getColumn("parentGdbTable_FK"));
				Containment c = new Containment(nextId(), "Containment", null, parentID, childID, 0, 0, 0);
				_containments.add(c);
			}
		}
		catch (Exception e) {
			System.out.println("Error adding containments.");
			e.printStackTrace();
		}
	}
}