// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data;

import java.util.ArrayList;

import org.mitre.schemastore.data.database.DataSourceDataCalls;
import org.mitre.schemastore.model.DataSource;

/** Class for managing the data sources in the schema repository */
public class DataSourceCache extends DataCache
{	
	/** Stores reference to the data source data calls */
	private DataSourceDataCalls dataCalls = null;
	
	/** Constructs the data sources cache */
	DataSourceCache(DataManager manager, DataSourceDataCalls dataCalls)
		{ super(manager); this.dataCalls=dataCalls; }
	
	/** Get a list of all data sources */
	public ArrayList<DataSource> getAllDataSources()
		{ return dataCalls.getDataSources(null); }
			
	/** Get a list of data sources for the specified schema */
	public ArrayList<DataSource> getDataSources(Integer schemaID)
		{ return dataCalls.getDataSources(schemaID); }

	/** Get the specified data source */
	public DataSource getDataSource(Integer dataSourceID)
		{ return dataCalls.getDataSource(dataSourceID); }

	/** Add the specified data source */
	public Integer addDataSource(DataSource dataSource)
		{ return dataCalls.addDataSource(dataSource); }

	/** Update the specified data source */
	public Boolean updateDataSource(DataSource dataSource)
		{ return dataCalls.updateDataSource(dataSource); }

	/** Delete the specified data source */
	public Boolean deleteDataSource(Integer dataSourceID)
		{ return dataCalls.deleteDataSource(dataSourceID); }
}