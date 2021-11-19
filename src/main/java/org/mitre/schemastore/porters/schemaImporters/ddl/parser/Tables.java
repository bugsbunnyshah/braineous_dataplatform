package org.mitre.schemastore.porters.schemaImporters.ddl.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.mitre.schemastore.porters.schemaImporters.ddl.parser.schemaObjects.Column;
import org.mitre.schemastore.porters.schemaImporters.ddl.parser.schemaObjects.Comment;
import org.mitre.schemastore.porters.schemaImporters.ddl.parser.schemaObjects.Element;
import org.mitre.schemastore.porters.schemaImporters.ddl.parser.schemaObjects.ForeignKey;
import org.mitre.schemastore.porters.schemaImporters.ddl.parser.schemaObjects.PrimaryKey;
import org.mitre.schemastore.porters.schemaImporters.ddl.parser.schemaObjects.Table;

public class Tables {
	private HashMap<String, Table> tables = new HashMap<String, Table>(); // string is the name of the table
	private HashMap<String, HashMap<String,Column>> columns = new HashMap<String, HashMap<String, Column>>(); // string is the name of the table the column is attached to
	private HashMap<String, PrimaryKey> primaryKeys = new HashMap<String, PrimaryKey>(); // string is the name of the table the primary key is attached to
	private ArrayList<ForeignKey> foreignKeys = new ArrayList<ForeignKey>();

	public void createTable(String tableName, ArrayList<Element> elements) throws Exception {
		// make sure we can create the table
		if (tables.containsKey(tableName)) {
			throw new Exception("Could not create table '" + tableName + "'. Table already exists.");
		}

		HashMap<String, Column> tempColumns = new HashMap<String, Column>();
		ArrayList<ForeignKey> tempForeignKeys = new ArrayList<ForeignKey>();
		PrimaryKey tempPrimaryKey = null;
		String tempTableComment = null;

		System.out.println("Creating table '" + tableName + "'.");
		Table table = new Table(tableName);

		for (int i = 0; i < elements.size(); i++) {
			Element element = elements.get(i);

			if (element instanceof Column) {
				Column column = (Column) element;

				// verify that the column doesn't already exist
				if (tempColumns.containsKey(column.getName())) {
					throw new Exception("Could not add column '" + column.getName() + "' to table '" + tableName + "'. Column already exists.");						
				}

				// add the column to the list
				System.out.println("Adding column '" + column.getName() + "' to table '" + tableName + "'.");
				tempColumns.put(column.getName(), column);
			}

			if (element instanceof ForeignKey) {
				ForeignKey foreignKey = (ForeignKey) element;

				// verify that the target table exists
				// the target could be ourselves so check our current table name
				if (!tableName.equals(foreignKey.getTargetTable()) && !tables.containsKey(foreignKey.getTargetTable())) {
					throw new Exception("Could not add foreign key from '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'. Target table does not exist.");
				}

				// verify that the source columns exist on this table
				for (int j = 0; j < foreignKey.getSourceColumns().size(); j++) {
					if (!tempColumns.containsKey(foreignKey.getSourceColumns().get(j))) {
						throw new Exception("Could not add foreign key from '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'. Column '" + foreignKey.getSourceColumns().get(j) + "' does not exist in the source table.");
					}
				}

				// verify that the target columns exist on the target table
				for (int j = 0; j < foreignKey.getTargetColumns().size(); j++) {
					if (!tempColumns.containsKey(foreignKey.getTargetColumns().get(j)) && !columns.get(foreignKey.getTargetTable()).containsKey(foreignKey.getTargetColumns().get(j))) {
						throw new Exception("Could not add foreign key from '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'. Column '" + foreignKey.getTargetColumns().get(j) + "' does not exist in the target table.");
					}
				}

				// verify that this foreign key combination doesn't already exist
				for (int j = 0; j < tempForeignKeys.size(); j++) {
					if (tempForeignKeys.get(j).getSourceTable().equals(foreignKey.getSourceTable()) &&
						tempForeignKeys.get(j).getTargetTable().equals(foreignKey.getTargetTable()) &&
						tempForeignKeys.get(j).getSourceColumns().containsAll(foreignKey.getSourceColumns()) &&
						tempForeignKeys.get(j).getTargetColumns().containsAll(foreignKey.getTargetColumns())) {
						throw new Exception("Could not add foreign key from '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'. A constraint on these columns already exists.");
					}
				}
				for (int j = 0; j < foreignKeys.size(); j++) {
					if (foreignKeys.get(j).getSourceTable().equals(foreignKey.getSourceTable()) &&
						foreignKeys.get(j).getTargetTable().equals(foreignKey.getTargetTable()) &&
						foreignKeys.get(j).getSourceColumns().containsAll(foreignKey.getSourceColumns()) &&
						foreignKeys.get(j).getTargetColumns().containsAll(foreignKey.getTargetColumns())) {
						throw new Exception("Could not add foreign key from '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'. A constraint on these columns already exists.");
					}
				}

				// verify that this foreign key name doesn't already exist in any other constraints
				for (int j = 0; j < tempForeignKeys.size(); j++) {
					if (tempForeignKeys.get(j).getName().equals(foreignKey.getName())) {
						throw new Exception("Could not add foreign key from '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'. Constraint name '" + foreignKey.getName() + "' is already in use.");
					}
				}
				for (int j = 0; j < foreignKeys.size(); j++) {
					if (foreignKeys.get(j).getName().equals(foreignKey.getName())) {
						throw new Exception("Could not add foreign key from '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'. Constraint name '" + foreignKey.getName() + "' is already in use.");
					}
				}
				if (tempPrimaryKey != null) {
					if (tempPrimaryKey.getName().equals(foreignKey.getName())) {
						throw new Exception("Could not add foreign key from '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'. Constraint name '" + foreignKey.getName() + "' is already in use.");
					}
				}
				for (String pkTableName : primaryKeys.keySet()) {
					if (primaryKeys.get(pkTableName).getName().equals(foreignKey.getName())) {
						throw new Exception("Could not add foreign key from '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'. Constraint name '" + foreignKey.getName() + "' is already in use.");
					}
				}

				System.out.println("Adding foreign key from table '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'.");
				tempForeignKeys.add(foreignKey);
			}

			if (element instanceof PrimaryKey) {
				PrimaryKey primaryKey = (PrimaryKey) element;

				// ensure that we don't already have a primary key on this table
				if (tempPrimaryKey != null) {
					throw new Exception("Could not add primary key to table '" + tableName + "'. Table already has a primary key.");
				}

				// ensure that the columns in the primary key are in the table
				for (int j = 0; j < primaryKey.getColumns().size(); j++) {
					if (!tempColumns.containsKey(primaryKey.getColumns().get(j))) {
						throw new Exception("Could not add primary key to table '" + tableName + "'. Column '" + primaryKey.getColumns().get(j) + "' was not found.");
					}
				}

				// ensure that the primary key name isn't already being used in any other constraints
				for (int j = 0; j < tempForeignKeys.size(); j++) {
					if (tempForeignKeys.get(j).getName().equals(primaryKey.getName())) {
						throw new Exception("Could not add primary key on '" + tableName + "'. Constraint name '" + primaryKey.getName() + " is already in use.");
					}
				}
				for (int j = 0; j < foreignKeys.size(); j++) {
					if (foreignKeys.get(j).getName().equals(primaryKey.getName())) {
						throw new Exception("Could not add primary key on '" + tableName + "'. Constraint name '" + primaryKey.getName() + " is already in use.");
					}
				}
				for (String pkTableName : primaryKeys.keySet()) {
					if (primaryKeys.get(pkTableName).getName().equals(primaryKey.getName())) {
						throw new Exception("Could not add primary key on '" + tableName + "'. Constraint name '" + primaryKey.getName() + " is already in use.");
					}
				}

				System.out.println("Adding primary key to table '" + tableName + "'.");
				tempPrimaryKey = primaryKey;
			}

			if (element instanceof Comment) {
				Comment comment = (Comment) element;

				if (comment.getColumnName() != null) {
					// attaching a comment to a column
					// verify that the column exists
					if (!tempColumns.containsKey(comment.getColumnName())) {
						throw new Exception("Could not add comment to column '" + comment.getColumnName() + "' on table '" + comment.getTableName() + "'. Column does not exist.");
					}

					// attach comment to column
					System.out.println("Adding comment to column '" + comment.getColumnName() + "' on table '" + tableName + "'.");
					tempColumns.get(comment.getColumnName()).setDescription(comment.getComment());
				} else {
					// attaching a comment to the table
					System.out.println("Adding comment to table '" + tableName + "'.");
					tempTableComment = comment.getComment();
				}
			}
		}

		// make sure we have some columns
		if (tempColumns.size() == 0) {
			throw new Exception("Cannot create table '" + tableName + "'. No columns were specified.");
		}

		// wrap up all the temp values and put them into our permanent collection
		System.out.println("Finished creating table '" + tableName + "'.");
		tables.put(tableName, table);
		columns.put(tableName, tempColumns);
		if (tempForeignKeys.size() > 0) { foreignKeys.addAll(tempForeignKeys); }
		if (tempPrimaryKey != null) { primaryKeys.put(tableName, tempPrimaryKey); }
		if (tempTableComment != null) { table.setDescription(tempTableComment); }
	}

	public void addComment(Comment comment) throws Exception {
		// make sure the table exists
		if (!tables.containsKey(comment.getTableName())) {
			throw new Exception("Could not add comment to table '" + comment.getTableName() + "'. Table does not exist.");
		}

		// make sure the column exists
		if (comment.getColumnName() != null && !columns.get(comment.getTableName()).containsKey(comment.getColumnName())) {
			throw new Exception("Could not add comment to column '" + comment.getColumnName() + "' on table '" + comment.getTableName() + "'. Column does not exist.");
		}

		if (comment.getColumnName() != null) {
			System.out.println("Adding comment to column '" + comment.getColumnName() + "' on table '" + comment.getTableName() + "'.");
			columns.get(comment.getTableName()).get(comment.getColumnName()).setDescription(comment.getComment());
		} else {
			System.out.println("Adding comment to table '" + comment.getTableName() + "'.");
			tables.get(comment.getTableName()).setDescription(comment.getComment());
		}
		System.out.println("Finished adding comment.");
	}

	/** Drop the specified tables */
	public void dropTables(ArrayList<String> tableNames) throws Exception
	{
		// Cycle through all tables
		for(String tableName : tableNames)
		{
			// Remove table, columns, and primary keys
			tables.remove(tableName);
			columns.remove(tableName);
			primaryKeys.remove(tableName);
			
			// Remove all foreign keys
			for(int j=0; j<foreignKeys.size(); j++)
			{
				ForeignKey foreignKey = foreignKeys.get(j);
				if(foreignKey.getSourceTable().equals(tableName) || foreignKey.getTargetTable().equals(tableName))
					foreignKeys.remove(j--);
			}
		}
	}

	// for adding elements to an already existing table
	public void addToTable(String tableName, ArrayList<Element> elements) throws Exception {
		// get the table we are adding to
		if (!tables.containsKey(tableName)) {
			throw new Exception("Could not add columns and constraints to table '" + tableName + "'. Table does not exist.");
		}

		HashMap<String, Column> tempColumns = new HashMap<String, Column>();
		ArrayList<ForeignKey> tempForeignKeys = new ArrayList<ForeignKey>();
		PrimaryKey tempPrimaryKey = null;
		String tempTableComment = null;

		System.out.println("Adding to table '" + tableName + "'.");

		for (int i = 0; i < elements.size(); i++) {
			Element element = elements.get(i);

			if (element instanceof Column) {
				Column column = (Column) element;

				// verify that the column doesn't already exist
				if (tempColumns.containsKey(column.getName()) || columns.get(tableName).containsKey(column.getName())) {
					throw new Exception("Could not add column '" + column.getName() + "' to table '" + tableName + "'. Column already exists.");						
				}

				// add the column to the list
				System.out.println("Adding column '" + column.getName() + "' to table '" + tableName + "'.");
				tempColumns.put(column.getName(), column);
			}

			if (element instanceof ForeignKey) {
				ForeignKey foreignKey = (ForeignKey) element;

				// verify that the target table exists
				if (!tables.containsKey(foreignKey.getTargetTable())) {
					throw new Exception("Could not add foreign key from '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'. Target table does not exist.");
				}

				// verify that the source columns exist on this table
				for (int j = 0; j < foreignKey.getSourceColumns().size(); j++) {
					if (!tempColumns.containsKey(foreignKey.getSourceColumns().get(j)) && !columns.get(tableName).containsKey(foreignKey.getSourceColumns().get(j))) {
						throw new Exception("Could not add foreign key from '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'. Column '" + foreignKey.getSourceColumns().get(j) + "' does not exist in the source table.");
					}
				}

				// verify that the target columns exist on the target table
				for (int j = 0; j < foreignKey.getTargetColumns().size(); j++) {
					if (!columns.get(foreignKey.getTargetTable()).containsKey(foreignKey.getTargetColumns().get(j))) {
						throw new Exception("Could not add foreign key from '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'. Column '" + foreignKey.getTargetColumns().get(j) + "' does not exist in the target table.");
					}
				}

				// verify that this foreign key combination doesn't already exist
				for (int j = 0; j < tempForeignKeys.size(); j++) {
					if (tempForeignKeys.get(j).getSourceTable().equals(foreignKey.getSourceTable()) &&
						tempForeignKeys.get(j).getTargetTable().equals(foreignKey.getTargetTable()) &&
						tempForeignKeys.get(j).getSourceColumns().containsAll(foreignKey.getSourceColumns()) &&
						tempForeignKeys.get(j).getTargetColumns().containsAll(foreignKey.getTargetColumns())) {
						throw new Exception("Could not add foreign key from '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'. A constraint on these columns already exists.");
					}
				}
				for (int j = 0; j < foreignKeys.size(); j++) {
					if (foreignKeys.get(j).getSourceTable().equals(foreignKey.getSourceTable()) &&
						foreignKeys.get(j).getTargetTable().equals(foreignKey.getTargetTable()) &&
						foreignKeys.get(j).getSourceColumns().containsAll(foreignKey.getSourceColumns()) &&
						foreignKeys.get(j).getTargetColumns().containsAll(foreignKey.getTargetColumns())) {
						throw new Exception("Could not add foreign key from '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'. A constraint on these columns already exists.");
					}
				}

				// verify that this foreign key name doesn't already exist in any other constraints
				for (int j = 0; j < tempForeignKeys.size(); j++) {
					if (tempForeignKeys.get(j).getName().equals(foreignKey.getName())) {
						throw new Exception("Could not add foreign key from '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'. Constraint name '" + foreignKey.getName() + "' is already in use.");
					}
				}
				for (int j = 0; j < foreignKeys.size(); j++) {
					if (foreignKeys.get(j).getName().equals(foreignKey.getName())) {
						throw new Exception("Could not add foreign key from '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'. Constraint name '" + foreignKey.getName() + "' is already in use.");
					}
				}
				if (tempPrimaryKey != null) {
					if (tempPrimaryKey.getName().equals(foreignKey.getName())) {
						throw new Exception("Could not add foreign key from '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'. Constraint name '" + foreignKey.getName() + "' is already in use.");
					}
				}
				for (String pkTableName : primaryKeys.keySet()) {
					if (primaryKeys.get(pkTableName).getName().equals(foreignKey.getName())) {
						throw new Exception("Could not add foreign key from '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'. Constraint name '" + foreignKey.getName() + "' is already in use.");
					}
				}

				System.out.println("Adding foreign key from table '" + tableName + "' to table '" + foreignKey.getTargetTable() + "'.");
				tempForeignKeys.add(foreignKey);
			}

			if (element instanceof PrimaryKey) {
				PrimaryKey primaryKey = (PrimaryKey) element;

				// ensure that we don't already have a primary key on this table
				if (tempPrimaryKey != null || primaryKeys.get(tableName) != null) {
					throw new Exception("Could not add primary key to table '" + tableName + "'. Table already has a primary key.");
				}

				// ensure that the columns in the primary key are in the table
				for (int j = 0; j < primaryKey.getColumns().size(); j++) {
					if (!tempColumns.containsKey(primaryKey.getColumns().get(j)) && !columns.get(tableName).containsKey(primaryKey.getColumns().get(j))) {
						throw new Exception("Could not add primary key to table '" + tableName + "'. Column '" + primaryKey.getColumns().get(j) + "' was not found.");
					}
				}

				// ensure that the primary key name isn't already being used in any other constraints
				for (int j = 0; j < tempForeignKeys.size(); j++) {
					if (tempForeignKeys.get(j).getName().equals(primaryKey.getName())) {
						throw new Exception("Could not add primary key on '" + tableName + "'. Constraint name '" + primaryKey.getName() + " is already in use.");
					}
				}
				for (int j = 0; j < foreignKeys.size(); j++) {
					if (foreignKeys.get(j).getName().equals(primaryKey.getName())) {
						throw new Exception("Could not add primary key on '" + tableName + "'. Constraint name '" + primaryKey.getName() + " is already in use.");
					}
				}
				for (String pkTableName : primaryKeys.keySet()) {
					if (primaryKeys.get(pkTableName).getName().equals(primaryKey.getName())) {
						throw new Exception("Could not add primary key on '" + tableName + "'. Constraint name '" + primaryKey.getName() + " is already in use.");
					}
				}

				System.out.println("Adding primary key to table '" + tableName + "'.");
				tempPrimaryKey = primaryKey;
			}

			if (element instanceof Comment) {
				Comment comment = (Comment) element;

				if (comment.getColumnName() != null) {
					// attaching a comment to a column
					// verify that the column exists
					if (!tempColumns.containsKey(comment.getColumnName())) {
						throw new Exception("Could not add comment to column '" + comment.getColumnName() + "' on table '" + comment.getTableName() + "'. Column does not exist.");
					}

					// attach comment to column
					System.out.println("Adding comment to column '" + comment.getColumnName() + "' on table '" + tableName + "'.");
					tempColumns.get(comment.getColumnName()).setDescription(comment.getComment());
				} else {
					// attaching a comment to the table
					System.out.println("Adding comment to table '" + tableName + "'.");
					tempTableComment = comment.getComment();
				}
			}
		}

		// wrap up all the temp values and put them into our permanent collection
		System.out.println("Finished adding to table '" + tableName + "'.");
		columns.get(tableName).putAll(tempColumns);
		if (tempForeignKeys.size() > 0) { foreignKeys.addAll(tempForeignKeys); }
		if (tempPrimaryKey != null) { primaryKeys.put(tableName, tempPrimaryKey); }
		if (tempTableComment != null) { tables.get(tableName).setDescription(tempTableComment); }
	}

	/** Removes the specified columns from the table */
	public void dropColumns(String tableName, ArrayList<String> columnNames) throws Exception
	{
		for(String columnName : columnNames)
		{
			HashMap<String,Column> tableColumns = columns.get(tableName);
			if(tableColumns!=null) tableColumns.remove(columnName);
		}
	}

	/** Drops the specified constraints from the specified table */
	public void dropConstraints(String tableName, ArrayList<String> constraintNames) throws Exception
	{
		// Cycle through each constraint
		for(String constraintName : constraintNames)
		{
			// Remove primary key with constraint name
			PrimaryKey primaryKey = primaryKeys.get(tableName);
			if(primaryKey!=null) primaryKeys.remove(tableName);

			// Remove foreign keys with constraint name
			for(int j=0; j<foreignKeys.size(); j++)
				if(foreignKeys.get(j).getSourceTable().equals(tableName) && foreignKeys.get(j).getName().equals(constraintName))
					foreignKeys.remove(j--);
		}
	}

	/** Drops the primary key for the specified table */
	public void dropPrimaryKey(String tableName) throws Exception
		{ primaryKeys.remove(tableName); }

	public void renameTable(String oldName, String newName) throws Exception {
		// make sure the old table exists
		if (!tables.containsKey(oldName)) {
			throw new Exception("Could not rename table '" + oldName + "' to '" + newName + "'. Source table does not exist.");
		}

		// make sure the new name doesn't exist
		if (tables.containsKey(newName)) {
			throw new Exception("Could not rename table '" + oldName + "' to '" + newName + "'. Target table already exists.");
		}

		// rename on the tables list
		Table table = tables.remove(oldName);
		table.setName(newName);
		tables.put(newName, table);

		// rename on the columns list
		HashMap<String, Column> columnList = columns.remove(oldName);
		for (String columnName : columnList.keySet()) {
			columnList.get(columnName).setTableName(newName);
		}
		columns.put(newName, columnList);

		// rename on the foreign key list
		for (int i = 0; i < foreignKeys.size(); i++) {
			if (foreignKeys.get(i).getSourceTable().equals(oldName)) {
				foreignKeys.get(i).setSourceTable(newName);
			}
			if (foreignKeys.get(i).getTargetTable().equals(oldName))  {
				foreignKeys.get(i).setTargetTable(newName);
			}
		}

		// rename on the primary key list
		if (primaryKeys.get(oldName) != null) {
			PrimaryKey primaryKey = primaryKeys.remove(oldName);
			primaryKey.setTableName(newName);
			primaryKeys.put(newName, primaryKey);
		}
	}

	public void renameColumn(String tableName, String oldName, String newName) throws Exception {
		// make sure the table exists
		if (!tables.containsKey(tableName)) {
			throw new Exception("Could not rename column '" + oldName + "' on table '" + tableName + "' to '" + newName + "'. Table does not exist.");
		}

		// make sure the old column exists
		if (!columns.get(tableName).containsKey(oldName)) {
			throw new Exception("Could not rename column '" + oldName + "' on table '" + tableName + "' to '" + newName + "'. Source column does not exist.");
		}

		// make sure the new column doesn't exist
		if (columns.get(tableName).containsKey(newName)) {
			throw new Exception("Could not rename column '" + oldName + "' on table '" + tableName + "' to '" + newName + "'. Target column already exists.");
		}

		// rename the column on the columns list
		Column column = columns.get(tableName).remove(oldName);
		column.setName(newName);
		columns.get(tableName).put(newName, column);

		// rename the column on the foreign key list
		for (int i = 0; i < foreignKeys.size(); i++) {
			for (int j = 0; j < foreignKeys.get(i).getSourceColumns().size(); j++) {
				if (foreignKeys.get(i).getSourceColumns().get(j).equals(oldName)) {
					foreignKeys.get(i).getSourceColumns().remove(j);
					foreignKeys.get(i).getSourceColumns().add(j, newName);
				}
			}
			for (int j = 0; j < foreignKeys.get(i).getTargetColumns().size(); j++) {
				if (foreignKeys.get(i).getTargetColumns().get(j).equals(oldName)) {
					foreignKeys.get(i).getTargetColumns().remove(j);
					foreignKeys.get(i).getTargetColumns().add(j, newName);
				}
			}
		}

		// rename the column on the primary key list
		for (int i = 0; i < primaryKeys.get(tableName).getColumns().size(); i++) {
			if (primaryKeys.get(tableName).getColumns().get(i).equals(oldName)) {
				primaryKeys.get(tableName).getColumns().remove(i);
				primaryKeys.get(tableName).getColumns().add(i, newName);
			}
		}
	}

	public void renameConstraint(String oldName, String newName) throws Exception {
		// make sure the old constraint exists
		boolean oldConstraintExists = false;
		for (String tableName : primaryKeys.keySet()) {
			if (primaryKeys.get(tableName).getName().equals(oldName)) {
				oldConstraintExists = true;
			}
		}
		for (int i = 0; i < foreignKeys.size(); i++) {
			if (foreignKeys.get(i).getName().equals(oldName)) {
				oldConstraintExists = true;
			}
		}
		if (!oldConstraintExists) {
			throw new Exception("Could not rename constraint '" + oldName + "' to '" + newName + "'. Source constraint does not exist.");
		}

		// make sure the new constraint doesn't exist
		boolean newConstraintExists = false;
		for (String pkName : primaryKeys.keySet()) {
			if (primaryKeys.get(pkName).getName().equals(newName)) {
				newConstraintExists = true;
			}
		}
		for (int i = 0; i < foreignKeys.size(); i++) {
			if (foreignKeys.get(i).getName().equals(newName)) {
				newConstraintExists = true;
			}
		}
		if (newConstraintExists) {
			throw new Exception("Could not rename constraint '" + oldName + "' to '" + newName + "'. Target constraint does not exist.");
		}

		// rename the constraint
		for (String tableName : primaryKeys.keySet()) {
			if (primaryKeys.get(tableName).getName().equals(oldName)) {
				primaryKeys.get(tableName).setName(newName);
			}
		}
		for (int i = 0; i < foreignKeys.size(); i++) {
			if (foreignKeys.get(i).getName().equals(oldName)) {
				foreignKeys.get(i).setName(newName);
			}
		}

	}

	public void modifyColumn(String tableName, String oldName, String newName, String columnType, String columnComment, boolean primaryKey) throws Exception {
		// make sure the table exists
		if (!tables.containsKey(tableName)) {
			throw new Exception("Could not modify column '" + oldName + "' on table '" + tableName + "' to '" + newName + "'. Table does not exist.");
		}

		// make sure the old column exists
		if (!columns.get(tableName).containsKey(oldName)) {
			throw new Exception("Could not modify column '" + oldName + "' on table '" + tableName + "' to '" + newName + "'. Source column does not exist.");
		}

		// make sure the new column doesn't exist
		if (columns.get(tableName).containsKey(newName)) {
			throw new Exception("Could not modify column '" + oldName + "' on table '" + tableName + "' to '" + newName + "'. Target column already exists.");
		}

		// make sure the column type is valid
		if (!Column.isValidColumnType(columnType)) {
			throw new Exception("Could not modify column '" + oldName + "' on table '" + tableName + "' to '" + newName + "'. Column type '" + columnType + "' is not valid.");
		}

		// make sure we don't already have a primary key if this is to be a primary key
		if (primaryKeys.get(tableName) != null && primaryKey) {
			throw new Exception("Could not modify column '" + oldName + "' on table '" + tableName + "' to '" + newName + "'. Set to primary key and table already has a primary key.");
		}

		// rename the column on the columns list
		Column column = columns.get(tableName).remove(oldName);
		column.setName(newName);
		columns.get(tableName).put(newName, column);

		// rename the column on the foreign key list
		for (int i = 0; i < foreignKeys.size(); i++) {
			for (int j = 0; j < foreignKeys.get(i).getSourceColumns().size(); j++) {
				if (foreignKeys.get(i).getSourceColumns().get(j).equals(oldName)) {
					foreignKeys.get(i).getSourceColumns().remove(j);
					foreignKeys.get(i).getSourceColumns().add(j, newName);
				}
			}
			for (int j = 0; j < foreignKeys.get(i).getTargetColumns().size(); j++) {
				if (foreignKeys.get(i).getTargetColumns().get(j).equals(oldName)) {
					foreignKeys.get(i).getTargetColumns().remove(j);
					foreignKeys.get(i).getTargetColumns().add(j, newName);
				}
			}
		}

		// rename the column on the primary key list
		for (int i = 0; i < primaryKeys.get(tableName).getColumns().size(); i++) {
			if (primaryKeys.get(tableName).getColumns().get(i).equals(oldName)) {
				primaryKeys.get(tableName).getColumns().remove(i);
				primaryKeys.get(tableName).getColumns().add(i, newName);
			}
		}

		// update the column's type
		columns.get(tableName).get(newName).setType(columnType);

		// reset the column's comment value
		columns.get(tableName).get(newName).setDescription(columnComment);

		// set the primary key value
		ArrayList<String> columnList = new ArrayList<String>();
		columnList.add(newName);
		primaryKeys.put(tableName, new PrimaryKey(tableName, columnList));
	}

	public HashMap<String, Column> getColumns(String tableName) {
		return columns.get(tableName);
	}

	public PrimaryKey getPrimaryKey(String tableName) {
		return primaryKeys.get(tableName);
	}

	public HashMap<String, Table> getTables() {
		return tables;
	}

	public ArrayList<ForeignKey> getForeignKeys() {
		return foreignKeys;
	}
}