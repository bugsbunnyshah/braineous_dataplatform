package org.mitre.schemastore.porters.schemaImporters;

import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.porters.ImporterException;

/**
 * Imports a schema listed in an excel spreadsheet with the following column
 * sequence [entity][attribute][description][parent entity]
 * 
 * @author HAOLI
 */
public class HierarchicalExcelImporter extends ExcelImporter
{
	/** Returns the importer name */
	public String getName()
		{ return "Hierarchical Excel Importer"; }
	
	/** Returns the importer description */
	public String getDescription()
		{ return "Imports Excel formatted schema with hierarchy column. "; }

	/** Generate the schema elements */
	protected ArrayList<SchemaElement> generateSchemaElements() throws ImporterException 
	{
		int numSheets = workbook.getNumberOfSheets();
		schemaElements = new ArrayList<SchemaElement>();
		schemaElements.add(D_ANY);

		// Iterate and load individual work sheets
		for (int s = 0; s < numSheets; s++)
		{
			HSSFSheet sheet = workbook.getSheetAt(s);

			// Iterate through rows and create table/attribute nodes
			for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++)
			{
				// Retrieve the row
				HSSFRow row = sheet.getRow(i);
				if(row==null) continue;

				// Retrieve the cell values
				String tableName = getCellValue(row.getCell(0));
				String attributeName = getCellValue(row.getCell(1));
				String documentation = getCellValue(row.getCell(2));
				String parent = getCellValue(row.getCell(3));

				// Generate the entity (if needed)
				if (tableName.length() == 0) { break; }
				Entity entity = entities.get(tableName);
				if (entity == null)
				{
					entity = new Entity(nextId(), tableName, "", 0);
					entities.put(tableName, entity);
					schemaElements.add(entity);
				}

				// Generate the attribute
				Attribute attribute = null;
				if (!attributeName.equals(""))
				{
					attribute = attributes.get(attributeName);
					if (attribute == null)
					{
						attribute = new Attribute(nextId(), attributeName, documentation, entity.getId(), D_ANY.getId(), null, null, false, 0);
						attributes.put(attributeName, attribute);
						schemaElements.add(attribute);
					}
				}
				else if (!documentation.equals("")) entity.setDescription(documentation);

				// Generate the subtype
				if (!parent.equals(""))
				{
					// Create the parent entity
					Entity parentEntity = entities.get(parent);
					if (parentEntity == null)
					{
						parentEntity = new Entity(nextId(), parent, "", 0);
						entities.put(parent, parentEntity);
						schemaElements.add(parentEntity);
					}

					// Create the subtype linkage
					if (subtypes.get(parentEntity.getName() + "." + entity.getName()) == null)
					{
						Subtype subtype = new Subtype(nextId(), parentEntity.getId(), entity.getId(), 0);
						subtypes.put(parentEntity.getName() + "." + entity.getName(), subtype);
						schemaElements.add(subtype);
					}
				}
			}
		}

		return schemaElements;
	}
}