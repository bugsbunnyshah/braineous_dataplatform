// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.porters.schemaImporters;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.Relationship;
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
public class GSIPImporter extends SchemaImporter {
	private ArrayList<Entity> _entities;
	private ArrayList<Attribute> _attributes;
	private ArrayList<Domain> _domains;
	private ArrayList<DomainValue> _enumerants;
	private ArrayList<Containment> _containments;
	private ArrayList<Relationship> _relationships;
	private Boolean IATStripIETName=false;
	private String IATStripNameSeparator="";
	private Boolean IATStripNameSpaces=false;
	private HashMap<Integer, Entity> EntityKey;
	private HashMap<Integer, Relationship> oppositeRelationship; 
	Database mdb = null;
            
	/** Returns the importer name */
	public String getName() { return "GSIP Importer&"; }

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
			items.add(new URI("Item2"));
			items.add(new URI("Item3"));
			items.add(new URI("./NECv2.0.mdb"));			
		} catch(Exception e) {}
		return items;
	}
	
	/** Initializes the importer for the specified URI */
	protected void initialize() throws ImporterException {
		String filename = "";
		try {
			//  connect to MS Access database
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
			_relationships = new ArrayList<Relationship>();
			EntityKey = new HashMap<Integer, Entity>();
			oppositeRelationship = new HashMap<Integer, Relationship>();
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
	      try {
	    	Entity tblEntity = null;;      
			
	    	Table ec = mdb.getTable("T_EntityCatalogue");
	    	Cursor c = Cursor.createCursor(ec);
	    	c.moveToNextRow();
			IATStripIETName = (Boolean) c.getCurrentRowValue(ec.getColumn("P_IATStripIETName"));
			IATStripNameSeparator = (String) c.getCurrentRowValue(ec.getColumn("P_IATStripNameSeparator"));
	       	IATStripNameSpaces = (Boolean) c.getCurrentRowValue(ec.getColumn("P_IATStripNameSpaces"));
	       	if (IATStripIETName && IATStripNameSpaces)  { IATStripNameSeparator = " " + IATStripNameSeparator + " "; }
	       	Boolean useName = true; //schema.getBoolean("P_harmonyName");
	       	String nameCol = "";
	       	if (useName) { nameCol="name"; }
	       	else { nameCol="nsgAlphaCode"; }
					
	        // Set of Entities
	       	Table et = mdb.getTable("T_EntityType");
	    	c = Cursor.createCursor(et);
	        while(c.moveToNextRow()) { 
	        	tblEntity = createEntity(c, et, nameCol);  			
	  			_entities.add(tblEntity);
	        	Integer ETID = (Integer) c.getCurrentRowValue(et.getColumn("itemIdentifier_PK"));  // key to get attributes.
	        	EntityKey.put(ETID, tblEntity);
	        	addAttributes(ETID, ETID);  // This call does the majority of the work, including inheritance.
	        } // while Entities	
			addContainments();
			addRelationships();
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
		for (int j=0; j<_relationships.size(); j++) { schemaElements.add(_relationships.get(j)); i++; }
		return schemaElements;
	}
	
	//given a Cursor, use fields "tableName, definition, description, entityNote", return new Entity
	private Entity createEntity(Cursor c, Table t, String tableName){
		String tblName = null;
		String definition = null;
		try {
			tblName = (String) c.getCurrentRowValue(t.getColumn(tableName));  // note these match the columns in the  
			if (tblName.substring(0, 1).equals(tblName.substring(0, 1).toLowerCase())) {
				tblName = tblName.toLowerCase();
				tblName = firstLetterCapitalize(tblName);
			}
			definition = (String) c.getCurrentRowValue(t.getColumn("definition"));
			definition = concatNonNullFields(definition, (String) c.getCurrentRowValue(t.getColumn("description")), " [desc] ");
			definition = concatNonNullFields(definition, (String) c.getCurrentRowValue(t.getColumn("note")), " [note] ");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		int id = nextId();
		Entity e = new Entity(id, tblName, definition, 0);

		return e;
	}
	
	private void addAttributes(Integer ancestorID, Integer entityID) {  
		//  adds attributes for an entity, or implements inheritance of an ancestor's attributes.
		//  if ancestorID=entityID, then we're retrieving attributes for the entity itself.
		try { 
			// Find all attributes belonging to the feature type
			Table t = mdb.getTable("T_EntityAttribute");
	    	Cursor C = Cursor.createCursor(t); 
			Attribute attribute;            
			int a=0;
			Hashtable<String, Integer> datatypeIDs = new Hashtable<String, Integer>();
			while(C.moveToNextRow()) {    
				if (!ancestorID.equals(((Integer) C.getCurrentRowValue(t.getColumn("entityType_FK"))))) { continue; }
				String IAID = ((Integer) C.getCurrentRowValue(t.getColumn("itemIdentifier_PK"))) +"";
				String attName= (String) C.getCurrentRowValue(t.getColumn("name"));   
				//String dtName = attributes.getString("dtName");  
				if (IATStripIETName && attName.indexOf(IATStripNameSeparator)>-1) { // chop off the orig entity name that prepends.					
					attName = attName.substring(attName.indexOf(IATStripNameSeparator)+IATStripNameSeparator.length());  
				}
				String attDefinition = (String) C.getCurrentRowValue(t.getColumn("definition"));
				attDefinition = concatNonNullFields(attDefinition, ((String) C.getCurrentRowValue(t.getColumn("description"))), " [desc] ");
				attDefinition = concatNonNullFields(attDefinition, ((String) C.getCurrentRowValue(t.getColumn("note"))), " [note] ");
				String generalDatatype = (String) C.getCurrentRowValue(t.getColumn("valuePrimitive"));
				attDefinition = concatNonNullFields(attDefinition, generalDatatype, " [type] ");
				Boolean lex = (Boolean) C.getCurrentRowValue(t.getColumn("lexical"));
				String lexical;
				if (lex) { lexical ="Lexical"; }
				else { lexical="Non-lexical"; }
				
				String datatype = (String) C.getCurrentRowValue(t.getColumn("valueType"));
				String attLength = ((Short) C.getCurrentRowValue(t.getColumn("characterLength"))) +"";
				if (attLength.equals("")) { attLength="Unlimited";	}                
				if (!(datatype.startsWith("Integer") 
						|| datatype.equals("Boolean") 
						|| datatype.startsWith("Real"))) {
					attDefinition = concatNonNullFields(attDefinition, lexical, " [lex] ");
					attDefinition = concatNonNullFields(attDefinition, attLength, " [len] ");
				}
				attDefinition = concatNonNullFields(attDefinition, ((String) C.getCurrentRowValue(t.getColumn("structureSpecification"))), " [struc] ");
				String measure = (String) C.getCurrentRowValue(t.getColumn("valueMeasurementUnit"));
            
				//String validRange = attributes.getString("validRange");
				String rMin = ((Double) C.getCurrentRowValue(t.getColumn("rangeMinimum"))) +"";
				String rMax = ((Double) C.getCurrentRowValue(t.getColumn("rangeMaximum"))) +"";
            
				if (!generalDatatype.equals("String")) {
					attDefinition = concatNonNullFields(attDefinition, measure, " [UoM] ");
					//attDefinition = concatNonNullFields(attDefinition, validRange, " [range] ");
					attDefinition = concatNonNullFields(attDefinition, rMin, " [min] ");
					attDefinition = concatNonNullFields(attDefinition, rMax, " [max] ");    
				}
        	
				//String dtID = attributes.getString("DTID"); // used to key on enumerants
				
				String uniqueAttribute = ((Integer) C.getCurrentRowValue(t.getColumn("itemIdentifier_PK"))) + "_"+ entityID;

				String domainType = datatype;
				String ignoreEnumerants="yes";
            
				if (datatype.equals("Boolean")) { uniqueAttribute=datatype;	}
				else if (generalDatatype.equalsIgnoreCase("Enumeration")) { ignoreEnumerants="no"; } 
				else if (datatype.startsWith("Integer")||datatype.startsWith("Real")) {
					domainType = concatNonNullFields(domainType, measure, " : ");
					//domainType = concatNonNullFields(domainType, validRange, " ");
					if (!((concatNonNullFields("",rMin,"")+concatNonNullFields("",rMax,"")).equals(""))) {
						domainType += " &lt;";
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
					if (!(generalDatatype.equals("String")||generalDatatype.equals("Unspecified"))){
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
					attribute = new Attribute(nextId(), attName, attDefinition, EntityKey.get(entityID).getId(), 
							datatypeIDs.get(uniqueAttribute), null, null, false, 0);
					_attributes.add(attribute);
				}
    		
				if (ignoreEnumerants.equals("no")) { 					
					//Statement selectEnumerants = null;
					// Find any enumerants to this attribute (most will have none).
					Table t2 = mdb.getTable("T_ListedValue");
			    	Cursor C2 = Cursor.createCursor(t2); 
					int rc = 0;
					while(C2.moveToNextRow()) {	
						if (!IAID.equals(((Integer) C2.getCurrentRowValue(t2.getColumn("entityAttribute_FK")))+"")) { continue; }
						rc++;
						String enumName= (String) C2.getCurrentRowValue(t2.getColumn("name"));  
						String enumDefinition = (String) C2.getCurrentRowValue(t2.getColumn("definition"));
						enumDefinition = concatNonNullFields(enumDefinition, ((String) C2.getCurrentRowValue(t2.getColumn("description"))), " [desc] ");
						enumDefinition = concatNonNullFields(enumDefinition, ((String) C2.getCurrentRowValue(t2.getColumn("note"))), " [note] ");                    
                
						DomainValue enumerant = new DomainValue(nextId(), enumName, enumDefinition,
								datatypeIDs.get(uniqueAttribute), 0);
						_enumerants.add(enumerant);
					}     		
				}
			} // while attributes

			//  Now that the direct attributes are taken care of, 
			//  check if there are any inherited attributes to add. (recursive)
			t = mdb.getTable("T_InheritanceRelation");
	    	C = Cursor.createCursor(t);
			while(C.moveToNextRow()) { 
				if (!ancestorID.equals(((Integer) C.getCurrentRowValue(t.getColumn("entitySubType_FK"))))) { continue; }
				Integer superTypeID = (Integer) C.getCurrentRowValue(t.getColumn("entitySuperType_FK"));
				addAttributes(superTypeID, entityID);
			}	
		}
       	catch (Exception e) { e.printStackTrace(); }
	}
	
	private void addRelationships() {
		try {
			Table t = mdb.getTable("T_AssociationRole");
			Column col = t.getColumn("entityAssoc_FK");
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

			int lastET = -1;  int lastEA=-1;  String lastValueMultiplicity = ""; String lastName = "";
			String lastDefinition="";  String lastRoleType="";
			while(C.moveToNextRow()) { 
				int ETID = (Integer) C.getCurrentRowValue(t.getColumn("entityType_FK")); 
				int EAID = (Integer) C.getCurrentRowValue(t.getColumn("entityAssoc_FK")); 
				String valueMultiplicity = (String) C.getCurrentRowValue(t.getColumn("valueMultiplicity"));
				String name = (String) C.getCurrentRowValue(t.getColumn("name"));
				String definition = (String) C.getCurrentRowValue(t.getColumn("definition"));
				String description = (String) C.getCurrentRowValue(t.getColumn("description"));
				Boolean isOrdered = (Boolean) C.getCurrentRowValue(t.getColumn("isOrdered"));
				Boolean isUnique = (Boolean) C.getCurrentRowValue(t.getColumn("isUnique"));
				String note = (String) C.getCurrentRowValue(t.getColumn("note"));
				String roleType = (String) C.getCurrentRowValue(t.getColumn("roleType"));
				String append = " : ["+ valueMultiplicity +"]";
				if (isOrdered || isUnique) {
					append += " {";
					if (isOrdered) {  
						append+="ordered";
                        if (isUnique) { append+=", "; }
                    }
                    if (isUnique) { append+="unique"; }
                    append+="}";					
				}
				if (!roleType.equals("Ordinary")) { append+= " ("+roleType+")"; }
				definition = append+") "+ definition;
				definition = concatNonNullFields(definition, description, " [desc] ");
				definition = concatNonNullFields(definition, note, " [note] ");
				if (EAID==lastEA) {
					Integer[] mult1 = getMultiplicity(valueMultiplicity);
					Integer[] mult2 = getMultiplicity(lastValueMultiplicity);
					Integer key1 = EntityKey.get(ETID).getId();
					Integer key2 = EntityKey.get(lastET).getId();
					definition = "("+EntityKey.get(lastET).getName()+definition;
					lastDefinition = "("+EntityKey.get(ETID).getName()+ lastDefinition;
					
					Relationship r1 = new Relationship(nextId(), name, definition, key1, mult1[0], mult1[1], EntityKey.get(lastET).getId(), mult2[0], mult2[1], 0);					
					if (!lastRoleType.equals("Ordinary")) {
						Containment c = new Containment(nextId(), r1.getName(), r1.getDescription(), key1, r1.getId(), 0, 1, 0);
						_containments.add(c);
					}
					_relationships.add(r1);

					Relationship r2 = new Relationship(nextId(), lastName, lastDefinition, key2, mult2[0], mult2[1], EntityKey.get(ETID).getId(), mult1[0], mult1[1], 0);
					if (!roleType.equals("Ordinary")) {
						Containment c = new Containment(nextId(), r2.getName(), r2.getDescription(), key2, r2.getId(), 0, 1, 0);
						_containments.add(c);
					}
					_relationships.add(r2);
					
					//  Now that the direct associations are taken care of, 
					//  check if there are any inherited associations to add. (recursive)
					oppositeRelationship.put(lastET, r1);  
					oppositeRelationship.put(ETID, r2);
					inheritedRoles(r1,ETID, ETID);
					inheritedRoles(r2,lastET, lastET);					
				}
				lastET = ETID;  lastEA=EAID; lastValueMultiplicity = valueMultiplicity;  lastName = name;
				lastDefinition = definition;  lastRoleType=roleType;
			}			
		}
		catch (Exception e) {
			System.out.println("Error adding relationships.");
			e.printStackTrace();
		}	
	}
	
	private void inheritedRoles (Relationship r, Integer entityID, Integer origETID) {
		try {
			Table t = mdb.getTable("T_InheritanceRelation");
	    	Cursor C = Cursor.createCursor(t); 
			while(C.moveToNextRow()) {
				String y = entityID + "";
				String z = ((Integer) C.getCurrentRowValue(t.getColumn("entitySuperType_FK")))+"";
				if (!y.equals(z)) { continue; }				
				Integer subTypeID = (Integer) C.getCurrentRowValue(t.getColumn("entitySubType_FK"));				
				Relationship rNext = 
					new Relationship(nextId(), r.getName(), r.getDescription(), EntityKey.get(subTypeID).getId(), r.getLeftMin(), 
						r.getLeftMax(), r.getRightID(), r.getRightMin(), r.getRightMax(), 0);
				Relationship rOpp = oppositeRelationship.get(origETID);
				if (rOpp.getDescription().contains("(Aggregation)") || 
						rOpp.getDescription().contains("(Composition)")) {  
					Containment c = new 
						Containment(nextId(), r.getName(), r.getDescription(), EntityKey.get(subTypeID).getId(), rNext.getId(), 0, 1, 0);
					_containments.add(c);					
				}							
				_relationships.add(rNext);
				inheritedRoles(r, subTypeID, origETID);
			}
		}
		catch (Exception e) {
			System.out.println("Error adding inherited relationships.");
			e.printStackTrace();
		}	
	}
	
	private Integer[] getMultiplicity(String longString) {	
		String[] values = longString.split("\\.\\.");
		
		Integer[] multArray = {0,1};
		if (values.length>1) { 
			multArray[0] =new Integer(values[0]);
			multArray[1] = values[1].equals("*") ? null : new Integer(values[1]);
		}
		return multArray;
	}
	
	private void addContainments() {
		try {
			Table t = mdb.getTable("T_InheritanceRelation");
	    	Cursor C = Cursor.createCursor(t);
			
			HashMap<Integer, String> eNames = new HashMap<Integer,String>();
			for (int j=0; j<_entities.size(); j++) {
				eNames.put(_entities.get(j).getId(), _entities.get(j).getName());
			}	
			while(C.moveToNextRow()) { 
				int cID = (Integer) C.getCurrentRowValue(t.getColumn("entitySubType_FK"));     
				int pID = (Integer) C.getCurrentRowValue(t.getColumn("entitySuperType_FK")); 
				Integer childID = EntityKey.get(cID).getId(); 
				Integer parentID = EntityKey.get(pID).getId(); 
				String childName = eNames.get(childID);
				String parentName= eNames.get(parentID);

				Containment c = new Containment(nextId(), "[" + parentName + "->" + childName + "]", "", parentID, childID, 0, 1, 0);
				_containments.add(c);
			}
		}
		catch (Exception e) {
			System.out.println("Error adding containments.");
			e.printStackTrace();
		}
	}
}