package org.mitre.schemastore.porters.schemaImporters;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.ImporterException.ImporterExceptionType;
import org.mitre.schemastore.porters.URIType;

/**
 * 
 * ExcelConverter is a poor man's importer. It imports a relational definition
 * stored in an excel sheet The format of the schema is [table name][column
 * name] [documentation] A row with only table name (with no column name)
 * contains name and documentation for the table.
 * 
 * @author HAOLI
 * 
 */

public abstract class ExcelImporter extends SchemaImporter {
	protected HSSFWorkbook workbook;
	protected HashMap<String, Entity> entities;
	protected HashMap<String, Attribute> attributes;
	protected ArrayList<SchemaElement> schemaElements;
	protected HashMap<String, Subtype> subtypes; 
	protected static Domain D_ANY = new Domain(nextId(), ANY, null, 0);
	
	/** Scrub the cell value */
	protected String scrub(String s)
		{ return s.trim().replaceAll("'", "\'").replaceAll("\"", "\\\""); }

	/** Returns the cell value */
	protected String getCellValue(HSSFCell cell)
	{
		if(cell==null) return "";
		switch (cell.getCellType())
		{
			case HSSFCell.CELL_TYPE_BOOLEAN: return Boolean.toString(cell.getBooleanCellValue());
			case HSSFCell.CELL_TYPE_NUMERIC: return Double.toString(cell.getNumericCellValue());
			case HSSFCell.CELL_TYPE_STRING: return scrub(cell.getRichStringCellValue().toString());
			case HSSFCell.CELL_TYPE_BLANK: return "";
			case HSSFCell.CELL_TYPE_FORMULA: return cell.getCellFormula().trim();
			case HSSFCell.CELL_TYPE_ERROR: return String.valueOf(cell.getErrorCellValue()).trim();
			default: return "";
		}
	}

	/** Returns the importer URI type */
	public URIType getURIType() {
		return URIType.FILE;
	}

	/** Returns the importer URI file types */
	public ArrayList<String> getFileTypes() {
		return new ArrayList<String>(Arrays.asList(new String[]{"xls","csv"}));
	}

	/** Initializes the excel importer */
	protected void initialize() throws ImporterException{
		try {
			// Clear out the hash maps
			entities = new HashMap<String, Entity>();
			attributes = new HashMap<String, Attribute>();
			subtypes = new HashMap<String, Subtype>();
			schemaElements = new ArrayList<SchemaElement>();

			// Connect to the Excel workbook
			InputStream excelStream;
			excelStream = uri.toURL().openStream();
			workbook = new HSSFWorkbook(excelStream);
			excelStream.close();
		} catch (IOException e) {
			throw new ImporterException(ImporterExceptionType.PARSE_FAILURE, e.getMessage());
		}
	}
}