package org.mitre.schemastore.porters.schemaImporters;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.porters.ImporterException;

/**
 * Based on original file: HierarchicalDomainValueImporter.java
 * @author HAOLI
 *
 * Imports domain value listed in an excel spreadsheet
 * with a variable number of domain levels per row.
 * Will determine when we just have a value and not another domain.
 *
 * Customized for CoT xml files.
 * Example lines from CoT xml file
 * <cot cot="b-r-E-D-F-I-t-r" desc="DISPATCH/FIRE/INVESTGATION/TRAFFIC" />
 * <cot cot="b-r-E-D-F-I-t-r" desc="DISPATCH/FIRE/INVESTGATION/TRAFFIC/Tire Change" /> 
 * <cot cot="b-r-E-D-M-P-A" desc="DISPATCH/MEDICAL/PAIN/ABDOMINAL" />
 * Goal is to create hierarchical domain from the desc string.
 * 
 * Steps:
 * 1. Grab portion of XML file you want and save it to a .csv file.
 * 2. Replace all '/' with commas.
 * 3. You can now open the file in Excel and save it as .xls.
 * 4. Now you can use this importer.
 * 
 * This importer will take a variable number of cells per row.
 * But the code basically works on the last 3 cells of the row.
 * Or fewer if less than 3 cells in the row.
 * It will know when to create a new domain.
 */
public class HierarchicalDomainValueImporterCoT extends DomainValueImporter {
	protected HashMap<String, Subtype> subtypes = new HashMap<String, Subtype>();

	protected ArrayList<SchemaElement> generateSchemaElements() throws ImporterException {
		int numSheets = workbook.getNumberOfSheets();
		ArrayList<SchemaElement> schemaElements = new ArrayList<SchemaElement>();
				
		// iterate and load individual work sheets
		for (int s = 0; s < numSheets; s++) {
			HSSFSheet sheet = workbook.getSheetAt(s);
			if (sheet == null) { break; }

			// need to track number of cells in curr and prev rows
			int currRowNumberOfCells = 0;

			int rowCounter = 0;
			// iterate through rows and create table/attribute nodes
			for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
				HSSFRow row = sheet.getRow(i);

				if (row == null || row.getPhysicalNumberOfCells() == 0) { break; }

				String domainValueStr = ""; // value of last cell in current row
				String domainName = ""; // 2nd to last cell, can be null
				String parentName = ""; // 3rd to last cell, can be null

				// get current row's info
				currRowNumberOfCells = row.getPhysicalNumberOfCells(); // cell count
				
				// get domain value, domain and parent info
				HSSFCell domainValueCell = row.getCell(currRowNumberOfCells-1);
				HSSFCell domainCell = row.getCell(currRowNumberOfCells-2);
				HSSFCell parentCell = row.getCell(currRowNumberOfCells-3);
				domainValueStr = getCellValue(domainValueCell); // value of last cell
				
				if (domainCell != null)
				{
					domainName = getCellValue(domainCell); // value of 2nd to last cell
				
					if (parentCell != null)
					{
						parentName = getCellValue(parentCell); // value of 3rd to last cell
					}
				}

				rowCounter++;
				if (domainValueStr.length() == 0) { break; } // last cell can't be empty

				//createDomain(domainValue, domainName, parentName);
				Domain domain;
				DomainValue domainValue;
				DomainValue parentValue;
				String documentation = "";
				String valueHashKey = "";

				if (domainName.length() == 0)
				{
					// then we have a root entity in domainValueStr (FIRE, MEDICAL)
					// so set that as domain, and don't create domain value
					domainName = domainValueStr;
					domainValueStr = "";
				}

				// Create domain
				domain = domains.get(domainName);
				if (domain == null ){
					domain = new Domain(nextId(), domainName, "", 0);
					domains.put(domainName, domain);
					schemaElements.add(domain);
				}

				// Create a domain value
				if (domainValueStr.length() > 0)
				{
					valueHashKey = domainName + "/" + domainValueStr;
					domainValue = domainValues.get(valueHashKey);
					if (domainValue == null) {
						domainValue = new DomainValue(nextId(), domainValueStr, documentation, domain.getId(), 0);
						domainValues.put(valueHashKey, domainValue);
						schemaElements.add(domainValue);
					}

					// Create a subtype relationship for the parent
					if (parentName.length() > 0) {
						// First get parent DomainValue
						String parentHashKey = parentName + "/" + domainName;
						parentValue = domainValues.get(parentHashKey);
						if (parentValue == null)
						{
							parentValue = new DomainValue(nextId(), parentName, "", domain.getId(), 0);
						}
			
						// Then create subtype
						String subtypeHash = parentValue.getId() + "/" + valueHashKey;
						if (!subtypes.containsKey(subtypeHash)) {
							Subtype subtype = new Subtype(nextId(), parentValue.getId(), domainValue.getId(), 0);
							subtypes.put(subtypeHash, subtype);
							schemaElements.add(subtype);
						}
		
					} // End creating subtype

		
				} // End create a domain value

				
			} // End iterating through all rows
		} // End iterating all sheets

		return schemaElements;
	}

	public String getDescription() {
		return "Imports Excel formatted domain and domain values with hierarchy information with parent names specified ";
	}

	public String getName() {
		return "Hierarchical Domain Value Importer CoT";
	}

}