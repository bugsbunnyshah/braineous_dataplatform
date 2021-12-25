package org.mitre.schemastore.porters.schemaImporters.ddl.parser.schemaObjects;

import java.util.ArrayList;

public class ForeignKey extends Element {
	private String sourceTable = null;
	private ArrayList<String> sourceColumns = new ArrayList<String>();
	private String targetTable;
	private ArrayList<String> targetColumns = new ArrayList<String>();

	static public String generateFakeName(String sourceTable, ArrayList<String> sourceColumns, String targetTable, ArrayList<String> targetColumns) {
		return "FK_From_" + sourceTable + "_" + sourceColumns + "_to_" + targetTable + "_" + targetColumns;
	}

	public ForeignKey(String sourceTable, ArrayList<String> sourceColumns, String targetTable, ArrayList<String> targetColumns) throws Exception {
		setName(generateFakeName(sourceTable, sourceColumns, targetTable, targetColumns));
		setSourceTable(sourceTable);
		setSourceColumns(sourceColumns);
		setTargetTable(targetTable);
		setTargetColumns(targetColumns);

		// if the target column is null, use the source column
		if (getTargetColumns().size() == 0) { setTargetColumns(getSourceColumns()); }

		System.out.println("Generating foreign key named '" + getName() + "' from " + sourceTable + "." + sourceColumns + " to " + targetTable + "." + targetColumns);
	}

	public ForeignKey(String name, String sourceTable, ArrayList<String> sourceColumns, String targetTable, ArrayList<String> targetColumns) throws Exception {
		if (name == null) { name = generateFakeName(sourceTable, sourceColumns, targetTable, targetColumns); }
		setName(name);
		setSourceTable(sourceTable);
		setSourceColumns(sourceColumns);
		setTargetTable(targetTable);
		setTargetColumns(targetColumns);

		// if the target column is null, use the source column
		if (getTargetColumns().size() == 0) { setTargetColumns(getSourceColumns()); }

		System.out.println("Generating foreign key named '" + getName() + "' from " + sourceTable + "." + sourceColumns + " to " + targetTable + "." + targetColumns);
	}

	// setters
	public void setSourceTable(String tableName) throws Exception {
		if (tableName == null) { throw new Exception("Could not create foreign key. No source table name given."); }
		this.sourceTable = tableName.toUpperCase();
	}

	public void setSourceColumns(ArrayList<String> sourceColumns) {
		ArrayList<String> upperSourceColumns = new ArrayList<String>();
		if (sourceColumns != null) {
			for (int i = 0; i < sourceColumns.size(); i++) {
				upperSourceColumns.add(sourceColumns.get(i).toUpperCase());
			}
		}
		this.sourceColumns = upperSourceColumns;
	}

	public void setTargetTable(String tableName) throws Exception {
		if (tableName == null) { throw new Exception("Could not create foreign key. No target table name given."); }
		this.targetTable = tableName.toUpperCase();
	}

	public void setTargetColumns(ArrayList<String> targetColumns) {
		ArrayList<String> upperTargetColumns = new ArrayList<String>();
		if (targetColumns != null) {
			for (int i = 0; i < targetColumns.size(); i++) {
				upperTargetColumns.add(targetColumns.get(i).toUpperCase());
			}
		}
		this.targetColumns = upperTargetColumns;
	}

	// getters
	public String getSourceTable() {
		return this.sourceTable;
	}

	public ArrayList<String> getSourceColumns() {
		return this.sourceColumns;
	}

	public String getTargetTable() {
		return this.targetTable;
	}

	public ArrayList<String> getTargetColumns() {
		return this.targetColumns;
	}
}
