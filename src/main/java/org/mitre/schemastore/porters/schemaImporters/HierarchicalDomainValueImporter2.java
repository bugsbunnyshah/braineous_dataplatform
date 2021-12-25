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
 * Imports domain value listed in an excel spreadsheet with the following column sequence 
 * [domain][domain value level][domain value][description] 
 * 
 * Domain value level indicates the hierarchical structure of the domain value.  
 * This is custom made for Mark Bahre. 
 * @author HAOLI
 *
 */
public class HierarchicalDomainValueImporter2 extends DomainValueImporter {
	protected HashMap<String, Subtype> subtypes = new HashMap<String, Subtype>();

	protected ArrayList<SchemaElement> generateSchemaElements() throws ImporterException {
		int numSheets = workbook.getNumberOfSheets();
		ArrayList<SchemaElement> schemaElements = new ArrayList<SchemaElement>();

		// iterate and load individual work sheets
		for (int s = 0; s < numSheets; s++) {
			HSSFSheet sheet = workbook.getSheetAt(s);
			if (sheet == null) { break; }

			ArrayList<SchemaElement> hierarchy = new ArrayList<SchemaElement>();

			// iterate through rows and create table/attribute nodes
			for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
				HSSFRow row = sheet.getRow(i);
				if (row == null || row.getPhysicalNumberOfCells() == 0) { break; }

				String domainName = "";
				String outlineLevelStr = "";
				String domainValueStr = "";
				String documentation = "";

				// get domain name, assume cell contains string value
				HSSFCell domainCell = row.getCell(0);
				HSSFCell levelCell = row.getCell(1);
				HSSFCell valueCell = row.getCell(2);
				HSSFCell descrCell = row.getCell(3);

				// Ignore rows without domain specified
				if (domainCell != null) { domainName = getCellValue(domainCell); }
				// Get parent value
				if (levelCell != null) { outlineLevelStr = getCellValue(levelCell); }
				// Get domain value
				if (valueCell != null) { domainValueStr = getCellValue(valueCell); }
				// Get documentation value
				if (descrCell != null) { documentation = getCellValue(descrCell); }

				if (domainName.length() == 0) { break; }
				if (outlineLevelStr.length() == 0) { outlineLevelStr = Integer.toString(1); } 

				int level = Integer.parseInt(outlineLevelStr.substring(0, outlineLevelStr.indexOf(".")));
				if (level == 0) { hierarchy = new ArrayList<SchemaElement>(); }

				Domain domain;
				DomainValue domainValue;
				DomainValue parentValue;

				// Create domain
				domain = domains.get(domainName);
				if (domain == null) {
					domain = new Domain(nextId(), domainName, "", 0);
					domains.put(domainName, domain);
					schemaElements.add(domain);
				}

				// Set documentation for domain only rows
				if (valueCell == null || domainValueStr.length() == 0) {
					domain.setDescription(documentation);
					continue;
				}

				// Create a domain value
				String valueHashKey = domainName + "/" + domainValueStr;
				domainValue = domainValues.get(valueHashKey);
				if (domainValue == null) {
					domainValue = new DomainValue(nextId(), domainValueStr, documentation, domain.getId(), 0);
					domainValues.put(valueHashKey, domainValue);
					schemaElements.add(domainValue);
				}

				// Update the hierarchy in the middle or adding to the end
				if (level > (hierarchy.size()-1)) {
					hierarchy.add(level, domainValue);
				} else {
					hierarchy.set(level, domainValue);
				}

				// Create a subtype relationship for the parent
				if (level > 0) {
					// First get parent DomainValue
					parentValue = (DomainValue) hierarchy.get(level - 1);

					// Then create subtype
					String subtypeHash = parentValue.getId() + "/" + valueHashKey;
					if (!subtypes.containsKey(subtypeHash)) {
						Subtype subtype = new Subtype(nextId(), parentValue.getId(), domainValue.getId(), 0);
						subtypes.put(subtypeHash, subtype);
						schemaElements.add(subtype);
					}
				} // End creating subtype
			} // End iterating through all rows
		} // End iterating all sheets

		return schemaElements;
	}

	public String getDescription() {
		return "Imports Excel formatted domain and domain values with hierarchy information as numbered levels";
	}

	public String getName() {
		return "Hierarchical Domain Value Importer 2";
	}
}