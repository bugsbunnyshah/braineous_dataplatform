package org.mitre.schemastore.porters.schemaImporters.ddl.parser.schemaObjects;

import java.util.ArrayList;

public class PrimaryKey extends Element {
	private String tableName;
	private ArrayList<String> columns = new ArrayList<String>();

	static public String generateFakeName(String tableName) {
		return "TablePK_" + tableName;
	}

	public PrimaryKey(String tableName, ArrayList<String> columns) throws Exception {
		setName(generateFakeName(tableName));
		setTableName(tableName);
		this.columns = columns;	
		System.out.println("Generating primary key named '" + getName() + "' on table '" + getTableName() + "' using columns: " + columns.toString());
	}

	public PrimaryKey(String name, String tableName, ArrayList<String> columns) throws Exception {
		if (name == null) { name = generateFakeName(tableName); }
		setName(name);
		setTableName(tableName);
		this.columns = columns;
		System.out.println("Generating primary key named '" + getName() + "' on table '" + getTableName() + "' using columns: " + columns.toString());
	}

	// setters
	public void setTableName(String tableName) throws Exception {
		if (tableName == null) { throw new Exception("Could not create primary key. No table name given."); }
		this.tableName = tableName.toUpperCase();
	}

	// getters
	public String getTableName() {
		return this.tableName;
	}

	public ArrayList<String> getColumns() {
		return this.columns;
	}
}