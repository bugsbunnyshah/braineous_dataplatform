package org.mitre.schemastore.porters.schemaImporters;

import java.util.ArrayList;
import java.util.HashMap;
import java.net.URI;
import java.io.File;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.mitre.schemastore.client.Repository;
import org.mitre.schemastore.client.SchemaStoreClient;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.porters.ImporterException;

/**
 * Imports domain value listed in an excel spreadsheet with the following column sequence 
 * [domain][parent entity][domain value][description] 
 * @author HAOLI
 *
 */
public class HierarchicalDomainValueImporter extends DomainValueImporter {
	protected HashMap<String, Subtype> subtypes = new HashMap<String, Subtype>();
	protected HashMap<String, DomainValue>domainHash = new HashMap<String, DomainValue>();

	protected ArrayList<SchemaElement> generateSchemaElements() throws ImporterException {
		int numSheets = workbook.getNumberOfSheets();
		ArrayList<SchemaElement> schemaElements = new ArrayList<SchemaElement>();

		// iterate and load individual work sheets
		for (int s = 0; s < numSheets; s++) {
			HSSFSheet sheet = workbook.getSheetAt(s);
			if (sheet == null) { break; }
			
			// iterate through rows and create table/attribute nodes
			for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
				HSSFRow row = sheet.getRow(i);
				if (row == null || row.getPhysicalNumberOfCells() == 0) { break; }

				String domainName = "";
				String parentValueStr = "";
				String domainValueStr = "";
				String documentation = "";
				String code = "";
				String parentCode = "";

				// get domain name, assume cell contains string value
				HSSFCell domainCell = row.getCell(0);
				HSSFCell parentCell = row.getCell(1);
				HSSFCell parentCodeCell = row.getCell(2);
				
				HSSFCell valueCell = row.getCell(3);
				HSSFCell codeCell = row.getCell(4);
				HSSFCell descrCell = row.getCell(5);

				// Ignore rows without domain specified
				if (domainCell != null) { domainName = getCellValue(domainCell); }
				// Get domain value
				if (valueCell != null) { domainValueStr = getCellValue(valueCell); }
				// Get parent value
				if (parentCell != null) { parentValueStr = getCellValue(parentCell); }
				// Get documentation value
				if (descrCell != null) { documentation = getCellValue(descrCell); }
				if (parentCodeCell != null) { parentCode = getCellValue(parentCodeCell);}
				if (codeCell != null) { code = getCellValue(codeCell);}

				if (domainName.length() == 0) { break; } 

				Domain domain;
				DomainValue domainValue;
				DomainValue parentValue;

				// Create domain
				domain = domains.get(domainName);
				if (domain == null ){
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
				//domainValue = domainValues.get(valueHashKey);
				domainValue = domainHash.get(code);
				if (domainValue == null) {
					domainValue = new DomainValue(nextId(), domainValueStr, documentation, domain.getId(), 0);
					domainHash.put(code, domainValue);
					schemaElements.add(domainValue);
				}
				else
				{
					domainValue.setDescription(documentation);
				}

				// Create a subtype relationship for the parent
				if (parentCell != null) {
					// First get parent DomainValue
					//String parentHashKey = domainName + "/" + parentValueStr;
					//parentValue = domainValues.get(parentHashKey);
					parentValue = domainHash.get(parentCode);
					if (parentValue == null){ parentValue = new DomainValue(nextId(), parentValueStr, "", domain.getId(), 0);
					 domainHash.put(parentCode, parentValue);
					}
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
		return "Imports Excel formatted domain and domain values with hierarchy information with parent names specified ";
	}

	public String getName() {
		return "Hierarchical Domain Value Importer";
	}
	public static void main(String[] args)
	{
		try {
		Repository repository = new Repository(Repository.DERBY,new File(".").toURI(),"testStore","postgres","postgres");
//		Repository repository = new Repository(Repository.POSTGRES,new URI("localhost"),"supplies","postgres","postgres");
//		Repository repository = new Repository(Repository.SERVICE,new URI("http://ygg:8080/D3-develop/services/SchemaStore"),"","","");
		SchemaStoreClient client = new SchemaStoreClient(repository);
		HierarchicalDomainValueImporter importer = new HierarchicalDomainValueImporter();
		importer.setClient(client);

	    System.out.println("I got here");
		int schemaNumber = importer.importSchema("test3", "mgreer", "test import", new URI("file:/home/mary/Documents/test.xls"));
		System.out.println("Here too");
		System.out.println(schemaNumber);

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}