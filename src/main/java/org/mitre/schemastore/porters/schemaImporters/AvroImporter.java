package org.mitre.schemastore.porters.schemaImporters;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.avro.Protocol;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.compiler.idl.Idl;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.mitre.schemastore.client.Repository;
import org.mitre.schemastore.client.SchemaStoreClient;
import org.mitre.schemastore.model.Alias;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.ImporterException.ImporterExceptionType;
import org.mitre.schemastore.porters.URIType;

public class AvroImporter extends SchemaImporter {

	// Stores the M3 schema elements (entities, attributes, domain, relationships, etc.) 
		private static Integer _autoInc = 10;
		
		private static Integer nextAutoInc(){
			return _autoInc++;
		}

		protected HashMap<Integer, SchemaElement> _schemaElementsAvro = new HashMap<Integer, SchemaElement>();
		protected HashMap<String, SchemaElement> _reusableSchemaElements = new HashMap<String, SchemaElement>();

		
		/** stores the list of domains seen (used to import elements) **/
		protected HashMap<String,Domain> _domainList = new HashMap<String,Domain>();
		protected List<org.apache.avro.Schema> avroSchemas = new ArrayList<org.apache.avro.Schema>();
		
		private Protocol protocol = null;
		/** testing main **/ 
		public static void main(String[] args) throws URISyntaxException, ImporterException{
			AvroImporter avroImporter = new AvroImporter();
			String basePath = "file:/Users/mgreer/share/schemas/avro/";
			String protoPath = basePath + "Address_book.avdl";
	        String avscPath = basePath + "Person.avsc";
	        String avroPath = basePath + "twitter.avro";
			Repository repository = null;
			try {
				repository = new Repository(Repository.DERBY,new URI("C:/Temp/"), "org/mitre/schemastore","postgres","postgres");
			} catch (URISyntaxException e2) {
				e2.printStackTrace();
			}		
			try {
				avroImporter.client = new SchemaStoreClient(repository);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
				
			// Initialize the importer
			avroImporter.uri = new URI(protoPath);
			//xsdImporter.uri = new URI("C:/tempSchemas/niem-2.1/niem/domains/maritime/2.1/maritime.xsd");
			avroImporter.initialize();
			Collection<SchemaElement> elems = avroImporter.generateSchemaElements();
			for (SchemaElement elem : elems)
			{
				System.out.println(elem);
			}
			
		}
	public AvroImporter() {
		super();
		baseDomains = new String[][]{{INTEGER + " ","The integer domain", Type.INT.name()},
		{INTEGER + " ", "The long domain", Type.LONG.name()},
		{REAL + " ","The float domain", Type.FLOAT.name()}, 
		{REAL + " ","The double domain", Type.DOUBLE.name()},
		{BOOLEAN + " ", "The boolean domain", Type.BOOLEAN.name()},
		{STRING + " ","The string domain", Type.STRING.name()}, 
		{"bytes","The bytes domain", Type.BYTES.name()}};
	}
	/* returns the importer URI type */
	@Override
	public URIType getURIType() {

		return URIType.FILE;
	}
    /* returns the importer name */
	@Override
	public String getName() {
		
		return "Avro Schema Importer";
	}

	/* returns the importer description */
	@Override
	public String getDescription() {
		
		return "This importer can be used to import schemas from avro schema files";
	}
	/** Returns the importer URI file types */
	
	public ArrayList<String> getFileTypes() {
		ArrayList<String> fileTypes = new ArrayList<String>();
		fileTypes.add(".avdl");
		fileTypes.add(".avsc");
		fileTypes.add(".avro");
		return fileTypes;
	}
	
	/** Initializes the importer for the specified URI 
	 * @throws ImporterException 
	 * @throws URISyntaxException */
	protected void initialize() throws ImporterException
	{	
		try {

			/** reset the Importer **/
			_schemaElementsAvro = new HashMap<Integer, SchemaElement>();
			_domainList = new HashMap<String, Domain>();
			avroSchemas = new ArrayList<org.apache.avro.Schema>();
			_reusableSchemaElements = new HashMap<String, SchemaElement>();
			
			/** Preset domains and then process this schema **/
			loadDomains();

			/** create DOM tree for main schema **/
			String fileString = uri.toString();
			if (fileString.endsWith(".avdl"))
			{
				Idl idl = new Idl(new File(uri));            
				protocol = idl.CompilationUnit();

				avroSchemas.addAll(protocol.getTypes());
			}
			else if (fileString.endsWith(".avsc"))
			{
				org.apache.avro.Schema.Parser parser = new org.apache.avro.Schema.Parser();
				avroSchemas.add(parser.parse(new File(uri)));
			}
			else if (fileString.endsWith(".avro"))
			{
				DatumReader<GenericRecord> reader = new GenericDatumReader<GenericRecord>();
				DataFileStream<GenericRecord> dataFileReader = new DataFileStream<GenericRecord>(new FileInputStream(new File(uri)),reader);
				avroSchemas.add(dataFileReader.getSchema());

			}
			else {
				throw new ImporterException(ImporterExceptionType.INVALID_URI, "Invalid file type.  Must be .avsc, .avro or .avdl");
			}



		}
		catch(Exception e) { 			
			e.printStackTrace();
			throw new ImporterException(ImporterExceptionType.PARSE_FAILURE,e.getMessage()); 
		}
	}
	/* returns the schema elements from the specified URI */
	public ArrayList<SchemaElement> generateSchemaElements() throws ImporterException {
	    try {

	    	for (org.apache.avro.Schema avroSchema : avroSchemas)
	    	{
	    		if (avroSchema.getType()==Type.RECORD) {
	    		_processRecord(avroSchema, null, null, null, false, false, null);
	    		}
	    		else if (avroSchema.getType()==Type.ENUM){
	    			_addEnum(avroSchema);
	    		}
	    			
	    	}
	    }catch (Exception e)
	    {
	        e.printStackTrace();
	    }
	    return new ArrayList<SchemaElement>(_schemaElementsAvro.values());
	    
	}
	protected SchemaElement _processRecord(org.apache.avro.Schema recordSchema, SchemaElement parent, String containerName, String doc,
			boolean allowNull, boolean noLimit, Set<String> aliases) throws ImporterException {
		return _processRecord(recordSchema, parent, containerName, doc, allowNull, noLimit, aliases, false);
	}
	protected SchemaElement _processRecord(org.apache.avro.Schema recordSchema, SchemaElement parent, String containerName, String doc, 
			boolean allowNull, boolean noLimit, Set<String> aliases, boolean isSubtype) throws ImporterException
	{
		SchemaElement entity = _reusableSchemaElements.get(recordSchema.getFullName());

		if (entity == null )
		{
			entity = new Entity(nextAutoInc(), recordSchema.getName(), recordSchema.getDoc(),0);
		
			if (recordSchema.getAliases()!= null)
				for (String alias : recordSchema.getAliases())
				{
					Alias aliasElem = new  Alias(nextAutoInc(), alias, entity.getId(), 0);
					_schemaElementsAvro.put(aliasElem.hashCode(), aliasElem);
				
				}
			_schemaElementsAvro.put(entity.hashCode(), entity);
			_reusableSchemaElements.put(recordSchema.getFullName(), entity);
		
			for (Field field : recordSchema.getFields()){
				_processField(entity, field, false, false, false);
			}
		}
		if (!isSubtype && parent != null) {
			_createContainment(entity, parent, containerName, doc, allowNull, noLimit, aliases);
		}
		return entity;
	}
	protected void _createContainment(SchemaElement child, SchemaElement parent, String name, String doc, boolean allowNull, boolean noLimit, Set<String> aliases)
	{
		Containment containment = new Containment(nextAutoInc(), name, doc, parent!=null?parent.getId():0, child.getId(), allowNull?0:1, noLimit?-1:1, 0);
		_schemaElementsAvro.put(containment.hashCode(), containment);
		if (aliases != null)
			for (String alias : aliases)
			{
				Alias aliasElem = new  Alias(nextAutoInc(), alias, containment.getId(), 0);
				_schemaElementsAvro.put(aliasElem.hashCode(), aliasElem);
			}
	}

	protected SchemaElement _processField(SchemaElement parent, Field schemaField, boolean allowNull, boolean noLimit, boolean isSubtype) throws ImporterException{
		String doc = _processDoc(schemaField);
		return _processField(schemaField.schema(), parent, schemaField.name(), doc, allowNull, noLimit, schemaField.aliases(), isSubtype);
	}

	protected SchemaElement _processField(org.apache.avro.Schema schema, SchemaElement parent, String name, 
			String doc,  boolean allowNull, boolean noLimit, Set<String> aliases, boolean isSubtype) throws ImporterException{
		switch(schema.getType()){
		case RECORD : return _processRecord(schema, parent, name, doc, allowNull, noLimit, aliases, isSubtype);
		
		case UNION:  return _processUnion(schema, parent, name, doc, allowNull, noLimit, aliases, isSubtype);
		           
		case FIXED:  return _processFixed(schema, parent, name, doc, allowNull, noLimit, aliases, isSubtype);
		           
		case ENUM: return _processEnum(schema, parent, name, doc, allowNull, noLimit, aliases, isSubtype);
		            
		case MAP: return _processMap(schema, parent, name, doc, aliases, isSubtype);
					
		case ARRAY:  return _processArray(schema, parent, name, doc, aliases, isSubtype);
				    
		case INT:
		case FLOAT:
		case LONG:
		case DOUBLE:
		case BOOLEAN:
		case BYTES:
		case STRING: return _processPrimitiveType(schema, parent, name, doc, allowNull, noLimit, aliases, isSubtype);
		}
		return null;
	}
	
	protected String _processDoc(Field field)
	{
		return field.doc();
	}

	protected SchemaElement _createAttribute(SchemaElement domain, SchemaElement parent, String name, String doc,
			boolean allowNull, boolean noLimit, Set<String> aliases) {
		return _createAttribute(domain, parent, name, doc, allowNull, noLimit, aliases, false);
	}
	protected SchemaElement _createAttribute(SchemaElement domain, SchemaElement parent, String name, String doc, 
			boolean allowNull, boolean noLimit, Set<String>aliases, boolean isKey) {
		Attribute attr = new Attribute(nextAutoInc(), name, doc, parent!=null?parent.getId():0, domain.getId(), allowNull?0:1, noLimit?-1:1, isKey, 0);
		_schemaElementsAvro.put(attr.hashCode(), attr);
		if (aliases != null) {
			for (String alias : aliases)
			{
				Alias aliasElem = new  Alias(nextAutoInc(), alias, attr.getId(), 0);
				_schemaElementsAvro.put(aliasElem.hashCode(), aliasElem);
			}
		}
		return attr;
	}
	protected SchemaElement _processPrimitiveType(org.apache.avro.Schema schema, SchemaElement parent, String name, String doc, boolean allowNull, boolean noLimit, Set<String> aliases, boolean isSubtype) throws ImporterException {
		SchemaElement elem = _domainList.get(schema.getType().name());
		if (elem == null)
		{
			throw new ImporterException(ImporterExceptionType.IMPORT_FAILURE, "Domain value " + schema.getType().name() + " doesn't exist in domain");
		}
		if (isSubtype) {
			return elem;
		}
			return _createAttribute(elem, parent, name, doc, allowNull, noLimit, aliases);
		
	}
	protected SchemaElement _processUnion(org.apache.avro.Schema schema, SchemaElement parent, String name, String doc, boolean allowNull, boolean noLimit, Set<String> aliases, boolean isSubtype) throws ImporterException
	{
		boolean includesNull = allowNull;
		ArrayList<org.apache.avro.Schema> unionTypes = new ArrayList<org.apache.avro.Schema>();
		for (org.apache.avro.Schema subSchema : schema.getTypes()){
			if (subSchema.getType()== Type.NULL)
			{
				includesNull = true;
			}
			else
			{
				unionTypes.add(subSchema);
			}
		}
		if (unionTypes.size()==1)
		{
			return _processField(unionTypes.get(0), parent, name, doc, includesNull, noLimit, aliases, isSubtype);
		}
		else
		{
			Entity unionParent = new Entity(nextAutoInc(), null, null, 0);
			_createContainment(unionParent, parent, name, doc, allowNull, noLimit, aliases);
			_schemaElementsAvro.put(unionParent.hashCode(), unionParent);
			for (org.apache.avro.Schema subSchema : unionTypes) {
				SchemaElement elem = _processField(subSchema, unionParent,  null, null, includesNull, false, null, true);
				Subtype subtype = new Subtype(nextAutoInc(), unionParent.getId(), elem.getId(), 0);
				_schemaElementsAvro.put(subtype.hashCode(), subtype);
			}
			
			return unionParent;
		}
	}


	protected SchemaElement _processArray(org.apache.avro.Schema schema, SchemaElement parent, String name, String doc, Set<String> aliases, boolean isSubtype) throws ImporterException{
		if (schema.getElementType().getType() == Type.MAP || isSubtype){
			Entity arrayParent = new Entity(nextAutoInc(), null, null, 0);
			_schemaElementsAvro.put(arrayParent.hashCode(), arrayParent);
			if (!isSubtype){
				_createContainment(arrayParent, parent, name, doc, true, true, aliases);
			}
			_processField(schema.getElementType(), arrayParent, null, null, false, false, null, false);



			return arrayParent;
		}
		else{
			return _processField(schema.getElementType(), parent, name, doc, true, true, aliases, false);
		}
	}

	protected SchemaElement _processFixed(org.apache.avro.Schema schema, SchemaElement parent, String name, String doc, boolean allowNull, boolean noLimit, Set<String> aliases, boolean isSubtype) throws ImporterException{
		

		Domain elem = _domainList.get(schema.getFullName());
		if (elem== null)
		{
			elem = new Domain(nextAutoInc(), schema.getName(), "fixed size: " + schema.getFixedSize() + " bytes", 0);
			_schemaElementsAvro.put(elem.hashCode(), elem);
			_domainList.put(schema.getFullName(), elem);
		}
		if (! isSubtype) {
			_createContainment(elem, parent, name, doc, allowNull, noLimit, aliases);
		}
		return elem;
	}

	protected SchemaElement _processMap(org.apache.avro.Schema schema, SchemaElement parent, String name, String doc, Set<String> aliases, boolean isSubtype) throws ImporterException{
		Entity mapParent = new Entity(nextAutoInc(), null, null, 0);
		_schemaElementsAvro.put(mapParent.hashCode(), mapParent);
		if (!isSubtype){
			_createContainment(mapParent, parent, name, doc, true, true, aliases);
		}
		_createAttribute(_domainList.get(Type.STRING.name()), mapParent, null, null, false, false, null, true);

		_processField(schema.getValueType(), mapParent, null, null, false, false, null, false);


		return mapParent;
	}
	protected Domain _addEnum(org.apache.avro.Schema schema) {
		Domain dom = _domainList.get(schema.getFullName());
		if (dom == null)
		{
			dom = new Domain(nextAutoInc(), schema.getName(), schema.getDoc()==null || schema.getDoc().isEmpty()?schema.getName():schema.getDoc(), 0 );
			_schemaElementsAvro.put(dom.hashCode(), dom);
			_domainList.put(schema.getFullName(), dom);
			if (schema.getAliases()!= null)
				for (String alias : schema.getAliases())
				{
					Alias aliasElem = new  Alias(nextAutoInc(), alias, dom.getId(), 0);
					_schemaElementsAvro.put(aliasElem.hashCode(), aliasElem);

				}
			for (String value : schema.getEnumSymbols()){
				DomainValue val = new DomainValue(nextAutoInc(), value, null, dom.getId(),  0);
				_schemaElementsAvro.put(val.hashCode(), val);
			}
		}
		return dom;
	}
	protected SchemaElement _processEnum(org.apache.avro.Schema schema, SchemaElement parent, String name, String doc, boolean allowNull, boolean noLimit, Set<String> aliases, boolean isSubtype){
		Domain dom = _addEnum(schema);
		if (!isSubtype) {
			_createAttribute(dom, parent, name, doc, allowNull, noLimit, aliases);
		}
		return dom;
	}		
	
	/**
	 * Function for loading the preset domains into the Schema and into a list
	 * for use during Attribute creation
	 */
	private void loadDomains() {
		for (int i = 0; i < baseDomains.length; i++){
			Domain domain = new Domain(nextAutoInc(), baseDomains[i][0], baseDomains[i][1], 0);
			_schemaElementsAvro.put(domain.hashCode(), domain);
			_domainList.put(baseDomains[i][2],  domain);
		}
		
	}

}



