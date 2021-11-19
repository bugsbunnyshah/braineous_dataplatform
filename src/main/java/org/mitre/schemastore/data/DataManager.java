// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data;

import java.sql.SQLException;

import org.mitre.schemastore.data.database.Database;
import org.mitre.schemastore.data.database.DatabaseConnection;

/** Class for managing the SchemaStore data */
public class DataManager
{	
	/** Stores the database */
	private Database database = null;
	
	// Stores the various data caches
	private AnnotationCache annotationCache = null;
	private DataSourceCache dataSourceCache = null;
	private TagCache tagCache = null;
	private FunctionCache functionCache = null;
	private ProjectCache projectCache = null;
	private SchemaElementCache schemaElementCache = null;
	private SchemaRelationshipCache schemaRelationshipCache = null;
	private SchemaCache schemaCache = null;

	/** Constructs the data manager */
	public DataManager(DatabaseConnection connection)
	{
		// Constructs the database
		this.database = new Database(connection);

		// Constructs the data caches
		annotationCache = new AnnotationCache(this,database.getAnnotationDataCalls());
		dataSourceCache = new DataSourceCache(this,database.getDataSourceDataCalls());
		tagCache = new TagCache(this,database.getTagDataCalls());
		functionCache = new FunctionCache(this,database.getFunctionDataCalls());
		projectCache = new ProjectCache(this,database.getProjectDataCalls());
		schemaElementCache = new SchemaElementCache(this,database.getSchemaElementDataCalls());
		schemaRelationshipCache = new SchemaRelationshipCache(this,database.getSchemaRelationshipsDataCalls());
		schemaCache = new SchemaCache(this,database.getSchemaDataCalls());
	}
	
	// Returns the various data caches
	public AnnotationCache getAnnotationCache() { return annotationCache; }
	public DataSourceCache getDataSourceCache() { return dataSourceCache; }
	public TagCache getTagCache() { return tagCache; }
	public FunctionCache getFunctionCache() { return functionCache; }
	public ProjectCache getProjectCache() { return projectCache; }
	public SchemaElementCache getSchemaElementCache() { return schemaElementCache; }
	public SchemaRelationshipCache getSchemaRelationshipCache() { return schemaRelationshipCache; }
	public SchemaCache getSchemaCache() { return schemaCache; }
	
	/** Retrieves a universal ID from the database */
	public Integer getUniversalIDs(Integer count) throws SQLException
		{ return database.getUniversalIDs(count); }
}