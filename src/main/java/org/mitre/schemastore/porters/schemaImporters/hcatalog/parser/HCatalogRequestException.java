package org.mitre.schemastore.porters.schemaImporters.hcatalog.parser;

public class HCatalogRequestException extends Exception {
	public HCatalogRequestException(){
		super("Error connecting to HCatalog");	
	}
	public HCatalogRequestException(String msg) {
		super(msg);
	}
}
