package org.mitre.schemastore.porters.schemaImporters.hcatalog.parser;

public class HCatalogParseException extends Exception {
	public HCatalogParseException(){
		super("Error parsing HCatalog");	
	}
	public HCatalogParseException(String msg) {
		super(msg);
	}
}
