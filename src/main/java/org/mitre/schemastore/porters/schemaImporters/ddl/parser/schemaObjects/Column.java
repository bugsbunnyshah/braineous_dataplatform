package org.mitre.schemastore.porters.schemaImporters.ddl.parser.schemaObjects;

import java.util.Arrays;
import java.util.List;

/** Stores a relational database column */
public class Column extends Element
{
	/** Stores enumeration of the various column data type */
	public static enum ColumnType {NONE,INTEGER,REAL,STRING,DATETIME,BOOLEAN};

	/** Defines the various integer values */
	private static final List<String> IntegerValues = Arrays.asList(new String[]{
		"BIGINT","BIGSERIAL","DEC","DECIMAL","INT","INT8","INTEGER","NUM","NUMBER","NUMERIC","OID","SMALLINT",
		"ROWID","ROWVERSION","SERIAL","SERIAL4","SERIAL8","TINYINT","UNIQUEIDENTIFIER","UROWID"});
	
	/** Defines the various real values	*/
	private static final List<String> RealValues = Arrays.asList(new String[]{
		"DOUBLE","FLOAT","REAL","BINARY_FLOAT","BINARY_DOUBLE","MONEY","SMALLMONEY","FLOAT4","FLOAT8"});

	/** Defines the various string values */
	private static final List<String> StringValues = Arrays.asList(new String[]{
		"BLOB","CLOB","DBCLOB","IMAGE","BINARY","CHAR","CHARACTER","VARCHAR","VARCHAR2","VARGRAPHIC","VARBINARY",
		"ENUM","LONG","LONGBLOB","LONGTEXT","MEDIUMBLOB","MEDIUMTEXT","NCHAR","NVARCHAR","NVARCHAR2","NATIONAL",
		"NCLOB","TEXT","SET","BFILE","RAW","XMLTYPE","XML","CURSOR","UNIQUEID","BOX","BYTEA","CIDR","CIRCLE",
		"INET","LINE","LSEG","MACADDR","PATH","POINT","POLYGON","GEOMETRY", "SDO_GEOMETRY"});

	/** Defines the various date/time values */
	private static final List<String> DateTimeValues = Arrays.asList(new String[]{
		"TIME","TIMESTAMP","DATE","DATETIME","INTERVAL","TEMPORAL","SMALLDATETIME","TIMESPAN","TIMETZ","SASODATE"});
	
	/** Defines the various boolean values */
	private static final List<String> BooleanValues = Arrays.asList(new String[]{"BIT","BOOL","BOOLEAN"});
	
	// Stores various aspects about the relational database column
	private String tableName = null;
	private String description = null;
	private ColumnType type = ColumnType.NONE;
	private boolean isNullable = false;

	/** Constructs the column */
	public Column(String name, String tableName, String type, boolean isNullable) throws Exception
		{ setTableName(tableName); setName(name); setType(type); setIsNullable(isNullable); }

	// Column setters
	public void setType(String type) throws Exception { this.type = getColumnTypeConversion(type); }
	public void setIsNullable(boolean isNullable) { this.isNullable = isNullable; }
	
	/** Sets the table name associated with the column */
	public void setTableName(String tableName) throws Exception
	{
		if(tableName==null) throw new Exception("Could not create primary key. No table name given.");
		this.tableName = tableName.toUpperCase();
	}

	/** Sets the column description */
	public void setDescription(String description)
	{
		if (description != null) { description = description.trim(); }
		if (description != null && description.length() == 0) { description = null; }
		this.description = description;
	}

	// Column getters
	public ColumnType getType() { return type; }
	public String getTableName() { return tableName; }
	public String getDescription() { return description; }
	public boolean isNullable() { return isNullable; }

	/** Indicates if a given value is a valid column data type */
	public static boolean isValidColumnType(String value)
		{ try { getColumnTypeConversion(value); return true; } catch(Exception e) { return false; }}

	/** Converts from string to the needed column type */
	public static ColumnType getColumnTypeConversion(String value) throws Exception
	{
		// Convert the value to upper case
		if(value==null) throw new Exception("Could not get column type. No column type given");
		value = value.toUpperCase();
		if (value.equalsIgnoreCase("SDO_GEOMETRY")){
			System.out.println("get ready");
		}
		// Identify the column type
		if(IntegerValues.contains(value)) return ColumnType.INTEGER;
		if(RealValues.contains(value)) return ColumnType.REAL;
		if(StringValues.contains(value)) return ColumnType.STRING;
		if(DateTimeValues.contains(value)) return ColumnType.DATETIME;
		if(BooleanValues.contains(value)) return ColumnType.BOOLEAN;
		throw new Exception("Could not match data type to '" + value + "'.");
	}
}