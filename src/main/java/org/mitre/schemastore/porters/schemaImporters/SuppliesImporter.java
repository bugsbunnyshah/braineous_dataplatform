package org.mitre.schemastore.porters.schemaImporters;

import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.porters.ImporterException;

/**
 * RescueSuppliesImporter imports a supply list from Excel containing two columns (supplies type and amount)
 * @author Chris Wolf
 */
public class SuppliesImporter extends ExcelImporter {
	/** Returns the importer name */
	public String getName() {
		return "Supplies Importer";
	}

	/** Returns the importer description */
	public String getDescription() {
		return "Imports a supplies list from Excel";
	}

	/** Generate the schema elements */
	protected ArrayList<SchemaElement> generateSchemaElements() throws ImporterException {
		// Retrieve the Excel workbook sheet
		HSSFSheet sheet = workbook.getSheetAt(0);
		
		// Generate the schema elements
		ArrayList<SchemaElement> schemaElements = new ArrayList<SchemaElement>();
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			try {
				// Retrieve information from the row
				HSSFRow row = sheet.getRow(i);
				String item = getCellValue(row.getCell(0));
				String amount = getCellValue(row.getCell(1));
				Entity entity = new Entity(nextId(),item,amount,null);
				schemaElements.add(entity);
			} catch(Exception e) {}
		}
		return schemaElements;
	}
}