/**
 * 
 */
package org.mitre.schemastore.porters.mappingExporters;

/**
 * @author mgreer
 * 
 * Exporter to export mapping formatted in a .csv spreadsheet
 * exporting all elements whether there is any matching in
 * the container or not.
 *
 */
public class ExcelExporter2 extends ExcelExporter {
	public ExcelExporter2() {
		super();
		setIncludeAllUnmatched(true);

	}
	/** Returns the exporter name */
	public String getName()
		{ return "Excel Exporter 2"; }
	
	/** Returns the exporter description */
	public String getDescription()
		{ return "This exporter generates an Excel file with one set of columns for the source and another set for the target.  Selected mismatches are also exported. All unmatched are also exported."; }
	


}


