package org.mitre.schemastore.porters.schemaImporters;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.ImporterException.ImporterExceptionType;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.WebHCatClientForSchemaImport;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.parser.HCatalogParseException;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.parser.HCatalogRequestException;

public class HCatalogTableSchemaImporter extends HCatalogImporter {
	@Override
	public List<URI> getAssociatedURIs(String  uriString) throws ImporterException{

       try {
		return WebHCatClientForSchemaImport.getURIsForTables(uriString);
	} catch (HCatalogRequestException e) {
		throw new ImporterException(ImporterExceptionType.IMPORT_FAILURE, e.getMessage());
	} catch (HCatalogParseException e) {
		throw new ImporterException(ImporterExceptionType.PARSE_FAILURE, e.getMessage());
	} catch (URISyntaxException e) {
		throw new ImporterException(ImporterExceptionType.INVALID_URI, e.getMessage());
	}
	}
	
	/* returns the importer name */
	@Override
	public String getName() {
		
		return "HCatalog Schema Importer (Tables as Schema)";
	}

	/* returns the importer description */
	@Override
	public String getDescription() {
		
		return "This importer can be used to import table schemas from WebHCat HCatalog RESTful API";
	}

}
