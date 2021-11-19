package org.mitre.harmony.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.SchemaInfo;
import org.mitre.schemastore.model.schemaInfo.model.SchemaModel;

/**
 * Class for managing the schemas in the schema repository
 * @author CWOLF
 */
public class SchemaManager
{	
	/** Stores the Harmony model */
	private HarmonyModel harmonyModel;
	
	/** Caches schema information */
	protected HashMap<Integer,Schema> schemas = null;

	/** Caches schema element information */
	protected HashMap<Integer,SchemaElement> schemaElements = new HashMap<Integer,SchemaElement>();
	
	/** Caches schema info associated with schemas */
	protected HashMap<Integer,HierarchicalSchemaInfo> schemaInfoList = new HashMap<Integer,HierarchicalSchemaInfo>();
	
	//** Constructs the Schema Manager */
	public SchemaManager(HarmonyModel harmonyModel)
		{ this.harmonyModel = harmonyModel; }
	
	//------------------
	// Schema Functions
	//------------------
	
	/** Initializes the schema list */
	public void initSchemas()
	{
		schemas = new HashMap<Integer,Schema>();
		for(Schema schema : SchemaStoreManager.getSchemas())
			schemas.put(schema.getId(),schema);
	}
	
	/** Returns a list of all schemas */
	public ArrayList<Schema> getSchemas()
	{
		if(schemas==null) initSchemas();
		return new ArrayList<Schema>(schemas.values());
	}
	
	/** Returns the list of all schema IDs */
	public ArrayList<Integer> getSchemaIDs()
	{
		ArrayList<Integer> schemaIDs = new ArrayList<Integer>();
		for(Schema schema : getSchemas())
			schemaIDs.add(schema.getId());
		return schemaIDs;
	}
	
	/** Retrieve the list of descendent schemas */
	public ArrayList<Integer> getDescendentSchemas(Integer schemaID)
	{
		try { return SchemaStoreManager.getDescendantSchemas(schemaID); }
		catch(Exception e) { System.out.println("(E) SchemaManager.getDescendentSchemas - " + e.getMessage()); }
		return new ArrayList<Integer>();
	}
		
	/** Returns the deletable schemas */
	public ArrayList<Integer> getDeletableSchemas()
	{
		try { return SchemaStoreManager.getDeletableSchemas(); }
		catch(Exception e) { System.out.println("(E) SchemaManager.getDeletableSchemas - " + e.getMessage()); }
		return new ArrayList<Integer>();
	}
	
	/** Returns the specified schema */
	public Schema getSchema(Integer schemaID)
	{
		if(schemas==null) initSchemas();
		if(!schemas.containsKey(schemaID))
			schemas.put(schemaID, SchemaStoreManager.getSchema(schemaID));
		return schemas.get(schemaID);
	}

	/** Deletes the specified schema */
	public boolean deleteSchema(Integer schemaID)
	{
		try {
			boolean success = SchemaStoreManager.deleteSchema(schemaID);
			if(success) { schemas.remove(schemaID); return true; }
		} catch(Exception e) { System.out.println("(E) SchemaManager.deleteSchema - " + e.getMessage()); }
		return false;
	}
	
	//--------------------------
	// Schema Element Functions
	//--------------------------
	
	/** Returns the schema info for the specified schema */
	public HierarchicalSchemaInfo getSchemaInfo(Integer schemaID)
	{
		// Fill cache if needed
		if(!schemaInfoList.containsKey(schemaID))
		{
			SchemaInfo schemaInfo = SchemaStoreManager.getSchemaInfo(schemaID);
			SchemaModel model = harmonyModel.getProjectManager().getSchemaModel(schemaID);
			HierarchicalSchemaInfo hSchemaInfo = new HierarchicalSchemaInfo(schemaInfo,model);
			for(SchemaElement schemaElement : hSchemaInfo.getElements(null))
				schemaElements.put(schemaElement.getId(), schemaElement);
			schemaInfoList.put(schemaID, hSchemaInfo);
		}
		return schemaInfoList.get(schemaID);
	}
	
	/** Retrieves the schema elements for the specified schema and type from the web service */
	public ArrayList<SchemaElement> getSchemaElements(Integer schemaID, Class type)
		{ return getSchemaInfo(schemaID).getElements(type); }	
	
	/** Gets the specified schema element */
	public SchemaElement getSchemaElement(Integer schemaElementID)
	{
		if(!schemaElements.containsKey(schemaElementID))
			schemaElements.put(schemaElementID, SchemaStoreManager.getSchemaElement(schemaElementID));
		return schemaElements.get(schemaElementID);
	}

	/** Returns the list of descendant elements for the specified schema and element */
	public ArrayList<SchemaElement> getDescendantElements(Integer schemaID, Integer elementID)
		{ return getSchemaInfo(schemaID).getDescendantElements(elementID); }

	/** Returns the various depths of the specified schema element */
	public ArrayList<Integer> getDepths(Integer schemaID, Integer elementID)
		{ return getSchemaInfo(schemaID).getDepths(elementID); }
}