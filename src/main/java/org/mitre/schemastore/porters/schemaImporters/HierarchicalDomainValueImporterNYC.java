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
 * Based on original file: HierarchicalDomainValueImporterCoT.java
 * @author LMAK
 *
 * 
 * Imports domain value listed in an excel spreadsheet
 * with a variable number of domain levels per row.
 * Code will know when to create a new domain. 
 *
 * Customized for NYC .xls files.
 * Format:
 * <Category> <Subcategory> and/or <Resource Name (or Job Title)>
 * <Category> field must always have a value.
 *  
 * Example lines:
 * Construction	Carpentry	Lumber
 * Construction	Masonry
 * Construction	Plumbing	Pumps
 * Construction	Plumbing	Sandbags
 * <parent><subtype><subtype>...
 * Goal is to create hierarchical domain from the cell values.
 * 
 * Steps/Rules:
 * First tab in all the files had extra information so skipped.
 * First row in all files have headings that we don't need. So skipped.
 * Category column must always have a value.
 * If there are multiple rows with only Category value,
 * (i.e. no subcatgory or resource name) 
 * then might as well delete any extra rows with that same Category value.
 * Remove Subcategory column if it's empty for the whole sheet.
 * Don't want to have blanks between cells.
 */
public class HierarchicalDomainValueImporterNYC extends DomainValueImporter {
	protected HashMap<String, Subtype> subtypes = new HashMap<String, Subtype>();

	protected ArrayList<SchemaElement> generateSchemaElements() throws ImporterException {
		int numSheets = workbook.getNumberOfSheets();
		ArrayList<SchemaElement> schemaElements = new ArrayList<SchemaElement>();

		// iterate and load individual work sheets
		// 1st tab is just instructions so start at sheet[1]
		for (int s = 1; s < numSheets; s++) {
			HSSFSheet sheet = workbook.getSheetAt(s);
			if (sheet == null) { break; }

			// iterate through rows and create table/attribute nodes
			// start at 2nd row since we don't need headings
			for (int i = sheet.getFirstRowNum()+1; i <= sheet.getLastRowNum(); i++) {
				HSSFRow row = sheet.getRow(i);

				if (row == null || row.getPhysicalNumberOfCells() == 0) { break; }

				String domainValueStr = ""; // value of last cell in current row
				String domainName = ""; // 2nd to last cell, can be null
				String parentName = ""; // 3rd to last cell, can be null
				
				// get domain value, domain and parent info
				HSSFCell parentCell = row.getCell(0);
				HSSFCell domainCell = row.getCell(1);
				HSSFCell domainValueCell = row.getCell(2);

				// Get cell values
				// Must have at least a parent cell
				if (parentCell == null) { break; }

				parentName = getCellValue(parentCell);
				
				if (domainCell != null)
				{
					domainName = getCellValue(domainCell);
				}
				
				if (domainValueCell != null)
				{
					domainValueStr = getCellValue(domainValueCell);
				}

				if (parentName.length() == 0) { break; }
				
				// Start creating domains, values, subtypes.
				Domain domainParent;
				Domain domain;
				DomainValue domainValue;
				DomainValue parentValue;
				String documentation = "";
				String valueHashKey = "";
				

				// Create parent domain
				domainParent = domains.get(parentName);
				if (domainParent == null ){
					domainParent = new Domain(nextId(), parentName, "", 0);
					domains.put(parentName, domainParent);
					schemaElements.add(domainParent);
				}
				
				if (domainName.length() != 0)
				{
					// Create domain value (for subtype and parent cell) 
					valueHashKey = parentName + "/" + domainName;
					domainValue = domainValues.get(valueHashKey);
					if (domainValue == null) {
						domainValue = new DomainValue(nextId(), domainName, documentation, domainParent.getId(), 0);
						domainValues.put(valueHashKey, domainValue);
						schemaElements.add(domainValue);
					}
				}
					
				if (domainValueStr.length() != 0)
				{
					// Create domain (for 2nd cell)
					domain = domains.get(domainName);
					if (domain == null ){
						domain = new Domain(nextId(), domainName, "", 0);
						domains.put(domainName, domain);
						schemaElements.add(domain);
					}
										
					// Create domain value (for 3rd cell)
					valueHashKey = domainName + "/" + domainValueStr;
					domainValue = domainValues.get(valueHashKey);
					if (domainValue == null) {
						domainValue = new DomainValue(nextId(), domainValueStr, documentation, domain.getId(), 0);
						domainValues.put(valueHashKey, domainValue);
						schemaElements.add(domainValue);
					}
					
					// Create subtype
					String parentHashKey = parentName + "/" + domainName;
					parentValue = domainValues.get(parentHashKey);
					if (parentValue == null)
					{
						parentValue = new DomainValue(nextId(), parentName, "", domain.getId(), 0);
					}

					String subtypeHash = parentValue.getId() + "/" + valueHashKey;
					if (!subtypes.containsKey(subtypeHash)) {
						Subtype subtype = new Subtype(nextId(), parentValue.getId(), domainValue.getId(), 0);
						subtypes.put(subtypeHash, subtype);
						schemaElements.add(subtype);
					}

				} // End domainValueStr.length
				
			} // End iterating through all rows

		} // End iterating all sheets

		return schemaElements;
	}

	public String getDescription() {
		return "Imports Excel formatted domain and domain values with hierarchy information with parent names specified ";
	}

	public String getName() {
		return "Hierarchical Domain Value Importer NYC";
	}

}