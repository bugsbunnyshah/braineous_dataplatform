package org.mitre.schemastore.porters.schemaImporters.ddl.parser.schemaObjects;

public class Comment extends Element {
	private String tableName = null;
	private String columnName = null;
	private String comment = null;

	public Comment(String tableName, String comment) throws Exception {
		if (tableName == null) { throw new Exception("Could not add comment. No table is specified."); }

		this.tableName = tableName;
		this.comment = comment;

		System.out.println("Generating comment for table '" + tableName + "'.");
	}

	public Comment(String tableName, String columnName, String comment) throws Exception {
		if (tableName == null) { throw new Exception("Could not add comment. No table is specified."); }
		if (columnName == null) { throw new Exception("Could not add comment. No column is specified."); }

		this.tableName = tableName;
		this.columnName = columnName;
		this.comment = comment;

		System.out.println("Generating comment for column '" + columnName + "' on table '" + tableName + "'.");
	}

	public String getTableName() {
		return tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getComment() {
		return comment;
	}
	
}
