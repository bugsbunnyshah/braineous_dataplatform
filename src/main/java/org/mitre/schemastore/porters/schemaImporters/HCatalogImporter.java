package org.mitre.schemastore.porters.schemaImporters;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.mitre.schemastore.client.Repository;
import org.mitre.schemastore.client.SchemaStoreClient;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.URIType;
import org.mitre.schemastore.porters.ImporterException.ImporterExceptionType;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.WebHCatClientForSchemaImport;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.ArrayDataType;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.HCatRestColumn;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.HCatRestDatabase;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.HCatRestTable;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.HiveDataType;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.HiveDataType.DataType;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.MapDataType;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.StructDataType;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.UnionDataType;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.parser.HCatalogParseException;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.parser.HCatalogRequestException;

import de.susebox.jtopas.TokenizerException;

public class HCatalogImporter extends SchemaImporter {
	// Stores the M3 schema elements (entities, attributes, domain, relationships, etc.) 
	private static Integer _autoInc = 10;


	    /**
	     * @param args the command line arguments
	     */

	private static Integer nextAutoInc(){
		return _autoInc++;
	}

	protected HashMap<Integer, SchemaElement> _schemaElementsHive = new HashMap<Integer, SchemaElement>();
	protected HashMap<String, SchemaElement> _reusableSchemaElements = new HashMap<String, SchemaElement>();

	
	/** stores the list of domains seen (used to import elements) **/
	protected HashMap<DataType,Domain> _domainList = new HashMap<DataType,Domain>();
	protected HiveDataType _hiveSchema = null;
	protected WebHCatClientForSchemaImport _hcatClient = null;

	
	public HCatalogImporter() {
		super();
		baseDomains = new String[][]{{INTEGER + " ", "The integer domain", DataType.INT.toString() },
				{INTEGER + " ", "The tinyint domain", DataType.TINYINT.toString()},
				{INTEGER + " ", "The smallint domain", DataType.SMALLINT.toString()},
				{DATETIME + " ", "The timestamp domain", DataType.TIMESTAMP.toString()},
				{INTEGER + " ", "The long domain", DataType.BIGINT.toString()},
				{REAL + " ", "The float", DataType.FLOAT.toString()},
				{REAL + " ", "The double domain", DataType.DOUBLE.toString()},
				{REAL + " ", "The decimal domain", DataType.DECIMAL.toString()},
				{STRING + " ", "The string domain", DataType.STRING.toString()},
				{STRING + " ", "The char domain", DataType.CHAR.toString()},
				{STRING + " ", "The varchar domain", DataType.VARCHAR.toString()},
				{"bytes", "The binary domain", DataType.BINARY.toString()},
				{"null", "The null domain", DataType.VOID.toString()}};
	}

	/** testing main **/ 
	public static void main(String[] args) throws URISyntaxException, ImporterException{
		HCatalogImporter hiveImporter = new HCatalogImporter();
		
		Repository repository = null;
		try {
			repository = new Repository(Repository.DERBY,new URI("C:/Temp/"), "org/mitre/schemastore","postgres","postgres");
		} catch (URISyntaxException e2) {
			e2.printStackTrace();
		}		
		try {
			hiveImporter.client = new SchemaStoreClient(repository);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
			
		// Initialize the importer
		hiveImporter.uri = new URI("http://192.168.56.101:50111/templeton/v1/ddl/database/mydb");
		//xsdImporter.uri = new URI("C:/tempSchemas/niem-2.1/niem/domains/maritime/2.1/maritime.xsd");
		hiveImporter.initialize();
		Collection<SchemaElement> elems =hiveImporter.generateSchemaElements();
		for (SchemaElement elem : elems)
		{
			System.out.println(elem);
		}
		
	}
	@Override
	public List<URI> getAssociatedURIs(String  uriString) throws ImporterException{

       try {
		return WebHCatClientForSchemaImport.getURIsForDatabases(uriString);
	} catch (HCatalogRequestException e) {
		throw new ImporterException(ImporterExceptionType.IMPORT_FAILURE, e.getMessage());
	} catch (HCatalogParseException e) {
		throw new ImporterException(ImporterExceptionType.PARSE_FAILURE, e.getMessage());
	} catch (URISyntaxException e) {
		throw new ImporterException(ImporterExceptionType.INVALID_URI, e.getMessage());
	}
	}
	@Override
	public Schema getSchema(URI uri) throws ImporterException {
		try {
			HiveDataType schema = getHcatClient(uri).getSchema();
			String author = System.getProperty("user.name");
			String name;
			String description;
			if (schema.getDataType()== DataType.TABLE ) {
				HCatRestTable table = (HCatRestTable) schema;
				author = table.getOwner();
				name = table.getDatabase() + "." + table.getTable();
				description = table.getComment();
				
			}else if (schema.getDataType() == DataType.DATABASE){
				HCatRestDatabase database = (HCatRestDatabase) schema;
				name = database.getDatabase();
				description = database.getComment();
			}
			else { 
				throw new HCatalogRequestException("Invalid uri to return database or table schema");
			}
			return new Schema(nextId(), name, author, (uri == null ? "" : uri.toString()), getName(), description, false);
		}  catch (HCatalogRequestException e) {
			throw new ImporterException(ImporterExceptionType.IMPORT_FAILURE, e.getMessage());
		} catch (HCatalogParseException e) {
			throw new ImporterException(ImporterExceptionType.PARSE_FAILURE, e.getMessage());
		}
	}
	
	protected WebHCatClientForSchemaImport getHcatClient(URI uri) {
		if (_hcatClient == null || !_hcatClient.getRequestedURI().toString().equals(uri.toString())) {
			_hcatClient = new WebHCatClientForSchemaImport(uri);
		}
		return _hcatClient;
	}
		
/* returns the importer URI type */
@Override
public URIType getURIType() {

	return URIType.URI;
}
/* returns the importer name */
@Override
public String getName() {
	
	return "HCatalog Schema Importer (Databases as Schema)";
}

/* returns the importer description */
@Override
public String getDescription() {
	
	return "This importer can be used to import database schemas from WebHCat HCatalog RESTful API";
}
/** Returns the importer URI file types */

public ArrayList<String> getFileTypes() {
	ArrayList<String> fileTypes = new ArrayList<String>();
	return fileTypes;
}

/** Initializes the importer for the specified URI 
 * @throws ImporterException 
 * @throws URISyntaxException */
protected void initialize() throws ImporterException
{	
	try {

		/** reset the Importer **/
		_schemaElementsHive = new HashMap<Integer, SchemaElement>();
		_domainList = new HashMap<DataType, Domain>();
		_reusableSchemaElements = new HashMap<String, SchemaElement>();
		/** Preset domains and then process this schema **/
		loadDomains();

		/** create DOM tree for main schema **/
		String fileString = uri.toString();




	}
	catch(Exception e) { 			
		e.printStackTrace();
		throw new ImporterException(ImporterExceptionType.PARSE_FAILURE,e.getMessage()); 
	}
}

/* returns the schema elements from the specified URI */

public ArrayList<SchemaElement> generateSchemaElements() throws ImporterException {
	try {
    	WebHCatClientForSchemaImport currentClient = this.getHcatClient(uri);
    	HiveDataType  currentSchema = currentClient.getSchema();
    	if (currentSchema.getDataType() == DataType.DATABASE){
    		_processDatabase((HCatRestDatabase)currentSchema);
    	}
    	else if (currentSchema.getDataType() == DataType.TABLE) {
    		_processTable((HCatRestTable) currentSchema, null, null);
    	}
    	else {
    		throw new ImporterException(ImporterExceptionType.INVALID_URI, "URI does not describe a database or a table");
    	}
	}catch (HCatalogRequestException e) {
		throw new ImporterException (ImporterExceptionType.IMPORT_FAILURE, e.getMessage());
	} catch (HCatalogParseException e) {
		throw new ImporterException (ImporterExceptionType.PARSE_FAILURE, e.getMessage());
	}
    	

    return new ArrayList<SchemaElement>(_schemaElementsHive.values());
    
}
protected void _processDatabase(HCatRestDatabase database) throws ImporterException{
	try {
	for (HCatRestTable table : database.getTableSchemas()) {
		_processTable(table, null, table.getTable());
	}
	}catch (HCatalogRequestException e) {
		throw new ImporterException (ImporterExceptionType.IMPORT_FAILURE, e.getMessage());
	} catch (HCatalogParseException e) {
		throw new ImporterException (ImporterExceptionType.PARSE_FAILURE, e.getMessage());
	}
}
protected void _processTable(HCatRestTable table, SchemaElement parent, String containerName) throws ImporterException
{
	String fullname = table.getDatabase() + "." + table.getTable();
	SchemaElement entity = _reusableSchemaElements.get(table.getDatabase() + "." + table.getTable());
	if (containerName == null){
		containerName = table.getTable();
	}
	try {
	if (entity == null )
	{

		entity = new Entity(nextAutoInc(), table.getTable(), table.getComment(),0);
	
		
		_schemaElementsHive.put(entity.hashCode(), entity);
		_reusableSchemaElements.put(fullname, entity);
	
		for (HCatRestColumn column : table.getAllColumns()){
			_processField(column.getHiveType(), entity, column.getName(), column.getComment(), false, false, false);
		}

	}
	if (parent != null) {
		_createContainment(entity, parent, containerName, table.getComment(), false, false);
	}
	}
	catch (TokenizerException e){
		throw new ImporterException(ImporterExceptionType.PARSE_FAILURE, e.getMessage());
	} catch (HCatalogRequestException e) {
		throw new ImporterException (ImporterExceptionType.IMPORT_FAILURE, e.getMessage());
	} catch (HCatalogParseException e) {
		throw new ImporterException (ImporterExceptionType.PARSE_FAILURE, e.getMessage());
	}
}
protected void _createContainment(SchemaElement child, SchemaElement parent, String name, String doc, boolean allowNull, boolean noLimit)
{
	Containment containment = new Containment(nextAutoInc(), name, doc, parent!=null?parent.getId():0, child.getId(), allowNull?0:1, noLimit?-1:1, 0);
	_schemaElementsHive.put(containment.hashCode(), containment);
	
}

protected SchemaElement _processField(HiveDataType schema, SchemaElement parent, String name, String doc, 
		boolean allowNull, boolean noLimit, boolean isSubtype) throws ImporterException{
	switch(schema.getDataType()){
	case STRUCT : return _processStruct((StructDataType)schema, parent, name, doc, allowNull, noLimit, isSubtype);
	
	case UNION:  return _processUnion((UnionDataType)schema, parent, name, doc, allowNull, noLimit, isSubtype);
	     
	case MAP: return _processMap((MapDataType)schema, parent, name, doc, isSubtype );
		
	case ARRAY:  return _processArray((ArrayDataType)schema, parent, name, doc, isSubtype );
			    
	default: return _processPrimitiveType(schema, parent, name, doc, allowNull, noLimit, isSubtype);
	}
}

protected SchemaElement _processPrimitiveType(HiveDataType schema, SchemaElement parent, String name, String doc, boolean allowNull, boolean noLimit, boolean isSubtype) throws ImporterException {
	SchemaElement elem = _domainList.get(schema.getDataType());
	if (elem == null)
	{
		throw new ImporterException(ImporterExceptionType.IMPORT_FAILURE, "Domain value " + schema.getDataType().toString() + " doesn't exist in domain");
	}
	if (!isSubtype) {
		_createAttribute(elem, parent, name, doc, allowNull, noLimit);
	}
	return elem;
		
}
protected void _createAttribute(SchemaElement domain, SchemaElement parent, String name, String doc,
		boolean allowNull, boolean noLimit) {
	_createAttribute(domain, parent, name, doc, allowNull, noLimit, false);
}
protected void _createAttribute(SchemaElement domain, SchemaElement parent, String name, String doc, 
		boolean allowNull, boolean noLimit, boolean isKey) {
	Attribute attr = new Attribute(nextAutoInc(), name, doc, parent!=null?parent.getId():0, domain.getId(), allowNull?0:1, noLimit?-1:1, isKey, 0);
	_schemaElementsHive.put(attr.hashCode(), attr);
	
}

protected SchemaElement _processStruct(StructDataType schema, SchemaElement parent, String name, String doc, boolean allowNull, boolean noLimit, boolean isSubtype) throws ImporterException
{
	try {
		SchemaElement entity = new Entity(nextAutoInc(), name, doc,0);
		_schemaElementsHive.put(entity.hashCode(), entity);

	
		for (HCatRestColumn column : schema.getColumns()){
			_processField(column.getHiveType(), entity,  column.getName(), column.getComment(), allowNull, noLimit, false);
		}
		if (!isSubtype) {
			_createContainment(entity, parent, name, doc, false, false);
		}
		return entity;
	}
	catch (TokenizerException e) {
		throw new ImporterException(ImporterExceptionType.PARSE_FAILURE, e.getMessage());
	}
}
protected SchemaElement _processUnion(UnionDataType schema, SchemaElement parent, String name, String doc,  boolean allowNull, boolean noLimit, boolean isSubtype) throws ImporterException
{
	boolean includesNull = allowNull;
	ArrayList<HiveDataType> unionTypes = new ArrayList<HiveDataType>();
	for (HiveDataType  subSchema : schema.getDataTypes()){
		if (subSchema.getDataType()== DataType.VOID)
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
		return _processField(unionTypes.get(0), parent, name, doc, includesNull, noLimit, isSubtype);
	}
	else
	{
		Entity unionParent = new Entity(nextAutoInc(), null, null, 0);
		if (!isSubtype) {
			_createContainment(unionParent, parent, name, doc, allowNull, noLimit);
		}
		_schemaElementsHive.put(unionParent.hashCode(), unionParent);
		for (HiveDataType subSchema : unionTypes) {
			SchemaElement elem = _processField(subSchema, unionParent, null, null, includesNull, false, true);
			Subtype subtype = new Subtype(nextAutoInc(), unionParent.getId(), elem.getId(), 0);
			_schemaElementsHive.put(subtype.hashCode(), subtype);
		}
		return unionParent;
	} 
}

protected SchemaElement _processArray(ArrayDataType schema, SchemaElement parent, String name, String doc, boolean isSubtype) throws ImporterException{

		if (schema.getListType().getDataType() == DataType.MAP || isSubtype) {
			Entity arrayParent = new Entity(nextAutoInc(), null, null, 0);
			_schemaElementsHive.put(arrayParent.hashCode(), arrayParent);
			if (!isSubtype) {
				_createContainment(arrayParent, parent, name, doc, true, true);
			}
			_processField(schema.getListType(), arrayParent, null, null, false,
						false, false);
			return arrayParent;
		} else {
			return _processField(schema.getListType(), parent, name, doc, true, true, false);
		}
	}


protected SchemaElement _processMap(MapDataType schema, SchemaElement parent, String name, String doc, boolean isSubtype) throws ImporterException{
	Entity mapParent = new Entity(nextAutoInc(), null, null, 0);
    _schemaElementsHive.put(mapParent.hashCode(), mapParent);
	if (!isSubtype){
		_createContainment(mapParent, parent, name, doc, true, true);
	}
	_createAttribute(_domainList.get(schema.getKey().getDataType()), mapParent, null, null, false, false, true); 
	_processField(schema.getValue(), mapParent, null, null, false, false, false);
	



    return mapParent;
}

/**
* Function for loading the preset domains into the Schema and into a list
* for use during Attribute creation
*/
private void loadDomains() {

	for (int i = 0; i < baseDomains.length; i++){
		Domain domain = new Domain(nextAutoInc(), baseDomains[i][0], baseDomains[i][1], 0);
		_schemaElementsHive.put(domain.hashCode(), domain);
		_domainList.put(DataType.valueOf(baseDomains[i][2]),  domain);
	}
}
}

