package org.mitre.schemastore.porters.schemaImporters;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.porters.ImporterException;

/**
 * Imports domain values from an Excel sheet. The excel sheet format is [domain
 * name] [domain value] [documentation]
 * 
 * @author HAOLI
 * 
 */
public class DomainValueImporter extends ExcelImporter {
	protected HashMap<String, Domain> domains; 
	protected HashMap<String, DomainValue> domainValues;

	/** Returns the importer name */
	public String getName() {
		return "Domain Importer";
	}
	
	/** Returns the importer description */
	public String getDescription() {
		return "Imports Excel formatted domain and domain values. Domains are synonymous to " + "referenced look up lists for controled vocabulary or controled data inputs.";
	}

	protected void initialize() {
		try {
			super.initialize();
		} catch (ImporterException e) {
			throw new RuntimeException(e);
		}
		
		domains  = new HashMap<String, Domain>();
		domainValues  = new HashMap<String, DomainValue>();
	}

	protected ArrayList<SchemaElement> generateSchemaElements() throws ImporterException {
		int numSheets = workbook.getNumberOfSheets();
		ArrayList<SchemaElement>  schemaElements = new ArrayList<SchemaElement>();

		// iterate and load individual work sheets
		for (int s = 0; s < numSheets; s++) {
			HSSFSheet sheet = workbook.getSheetAt(s);
			if (sheet == null) { break; }

			// iterate through rows and create table/attribute nodes
			for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
				HSSFRow row = sheet.getRow(i);
				if (row == null || row.getPhysicalNumberOfCells() == 0) { break; }

				// if a row has only domain defined, the description is for the domain
				boolean domainDefOnly = false;

				// get domain name, assume cell contains string value
				HSSFCell domainCell = row.getCell(0);
				HSSFCell valueCell = row.getCell(1);
				HSSFCell descrCell = row.getCell(2);

				// ignore rows without domain specified
				if (domainCell == null) { continue; }
				String domainName = getCellValue(domainCell);
				if (domainName.length() == 0) { break; }
				if (valueCell == null) { domainDefOnly = true; }

				String domainValueStr = "";

				// get domain values. this can be either string or number
				if (!domainDefOnly) {
					domainValueStr = getCellValue(valueCell);
					if (domainValueStr.trim().length() == 0) { domainDefOnly = true; }
				}

				String documentation = "";
				if (descrCell != null) { documentation = getCellValue(descrCell); }

				Domain domain;
				DomainValue domainValue;

				if (!domains.containsKey(domainName)) {
					domain = new Domain(nextId(), domainName, "", 0);
					domains.put(domainName, domain);
					schemaElements.add(domain); 
				} else {
					domain = domains.get(domainName);
				}

				if (domainDefOnly) {
					domain.setDescription(documentation);
				} else {
					// create a domain value
					domainValue = new DomainValue(nextId(), domainValueStr, documentation, domain.getId(), 0);
					String hashKey = domainName + "/" + domainValueStr + "/" + domainValue.getId();
					domainValues.put(hashKey, domainValue);
					schemaElements.add(domainValue);
				}

			}
		}

		return schemaElements;
	}
}