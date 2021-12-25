// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.mitre.schemastore.model.DataSource;

/**
 * Handles data source data calls in the database
 * @author CWOLF
 */
public class DataSourceDataCalls extends AbstractDataCalls
{	
	/** Constructs the data call class */
	DataSourceDataCalls(DatabaseConnection connection) { super(connection); }

	/** Retrieve a list of data sources for the specified schema (or all if no schemaID given) */
	public ArrayList<DataSource> getDataSources(Integer schemaID)
	{
		ArrayList<DataSource> dataSources = new ArrayList<DataSource>();
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT id,name,url,schema_id,element_id FROM data_source" + (schemaID!=null?" WHERE schema_id="+schemaID:""));
			while(rs.next())
			{
				Integer elementID = rs.getString("element_id")==null?null:rs.getInt("element_id");
				dataSources.add(new DataSource(rs.getInt("id"),rs.getString("name"),rs.getString("url"),rs.getInt("schema_id"),elementID));
			}
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) DataSourceDataCalls:getDataSources - "+e.getMessage()); }
		return dataSources;
	}

	/** Retrieve the specified data source */
	public DataSource getDataSource(Integer dataSourceID)
	{
		DataSource dataSource = null;
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT id,name,url,schema_id,element_id FROM data_source WHERE id="+dataSourceID);
			if(rs.next())
			{
				Integer elementID = rs.getInt("element_id"); if(elementID==0) elementID=null;
				dataSource = new DataSource(rs.getInt("id"),rs.getString("name"),rs.getString("url"),rs.getInt("schema_id"),elementID);
			}
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) DataSourceDataCalls:getDataSource - "+e.getMessage()); }
		return dataSource;
	}

	/** Adds a data source to the specified schema */
	public Integer addDataSource(DataSource dataSource)
	{
		Integer dataSourceID = 0;
		try {
			dataSourceID = getUniversalIDs(1);
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("INSERT INTO data_source(id,name,url,schema_id,element_id) VALUES("+dataSourceID+",'"+scrub(dataSource.getName(),100)+"','"+scrub(dataSource.getUrl(),200)+"',"+dataSource.getSchemaID()+","+(dataSource.getElementID()==null?"null":dataSource.getElementID())+")");
			stmt.close();
			connection.commit();
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			dataSourceID = 0;
			System.out.println("(E) DataSourceDataCalls:addDataSource - "+e.getMessage());
		}
		return dataSourceID;
	}

	/** Updates the specified dataSource */
	public boolean updateDataSource(DataSource dataSource)
	{
		boolean success = false;
		try {
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("UPDATE data_source SET name='"+scrub(dataSource.getName(),100)+"', url='"+scrub(dataSource.getUrl(),200)+"', schema_id="+dataSource.getSchemaID()+", element_id="+(dataSource.getElementID()==null?"null":dataSource.getElementID())+" WHERE id="+dataSource.getId());
			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) DataSourceDataCalls:updateDataSource - "+e.getMessage());
		}
		return success;
	}

	/** Deletes the specified dataSource */
	public boolean deleteDataSource(int dataSourceID)
	{
		boolean success = false;
		try {
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("DELETE FROM data_source WHERE id="+dataSourceID);
			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) DataSourceDataCalls:deleteDataSource - "+e.getMessage());
		}
		return success;
	}
}