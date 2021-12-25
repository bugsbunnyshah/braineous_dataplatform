package org.mitre.schemastore.porters.schemaImporters;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avro.Schema.Field;
import org.mitre.schemastore.client.Repository;
import org.mitre.schemastore.client.SchemaStoreClient;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.porters.ImporterException;

public class CAASD_AvroImporter extends AvroImporter {
	private Pattern enumMatch = Pattern
			.compile("^(\\s*([^=|]+?)\\s*(=[^=|]*)?[|])+\\s*([^=|]+?)\\s*(=[^=|]*)?$");
	private Pattern individualEnumPattern = Pattern
			.compile("\\s*([^|=]+?)\\s*((=[^=|]*)|(etc[.]{3}))?([|]|$)");
	private ArrayList<String> currentEnumeration = null;
	public static void main(String[] args) throws URISyntaxException, ImporterException{
		CAASD_AvroImporter avroImporter = new CAASD_AvroImporter();

		String basePath = "file:/Users/mgreer/Documents/NetBeansProjects/AvroTest/";
		String protoPath = basePath + "schemas/caasd-avro-schemas-caasd-avro-schemas/src/main/avro/ttrack/TT_Aggregates.avdl";
        String avscPath = basePath + "schemas/caasd-avro-schemas-caasd-avro-schemas/src/main/avro/common_segment/TrajectoryType.avsc";
        String avroPath = basePath + "schemas/twitter.avro";
		Repository repository = null;
		try {
			repository = new Repository(Repository.DERBY,new URI("C:/Temp/"), "org/mitre/schemastore","postgres","postgres");
		} catch (URISyntaxException e2) {
			e2.printStackTrace();
		}		
		try {
			avroImporter.client = new SchemaStoreClient(repository);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
			
		// Initialize the importer
		avroImporter.uri = new URI(protoPath);
		//xsdImporter.uri = new URI("C:/tempSchemas/niem-2.1/niem/domains/maritime/2.1/maritime.xsd");
		avroImporter.initialize();
		Collection<SchemaElement> elems = avroImporter.generateSchemaElements();
		for (SchemaElement elem : elems)
		{
			System.out.println(elem);
		}
		
	}

	/* returns the importer name */
	@Override
	public String getName() {

		return "Avro Schema Importer - CAASD";
	}

	@Override
	protected SchemaElement _processPrimitiveType(org.apache.avro.Schema schema,
			SchemaElement parent, String name, String doc, boolean allowNull,
			boolean noLimit, Set<String> aliases, boolean isSubtype) throws ImporterException {
		SchemaElement elem = null;
		if (currentEnumeration != null && !currentEnumeration.isEmpty()) {
			org.apache.avro.Schema enumSchema = org.apache.avro.Schema
					.createEnum(name + "_domain", doc, "", currentEnumeration);
			elem= _processEnum(enumSchema, parent, name, doc, allowNull, noLimit,
					aliases, isSubtype);
		} else {
			elem =  super._processPrimitiveType(schema, parent, name, doc, allowNull, noLimit, aliases, isSubtype);
		}
		currentEnumeration = null;
		return elem;

	}

	@Override
	protected String _processDoc(Field field) {
		String returnDoc = "";
		currentEnumeration = new ArrayList<String>();
		if (field.doc() != null){
			String cleanField = field.doc().replaceAll("^%?\\d+[.]\\d+(f|e)", "").trim();
			returnDoc = _matchEnumerationsInDoc(
				cleanField, returnDoc);
		}
		if (returnDoc.isEmpty()) {
			String label = field.getProp("label");
			if (label != null && !label.isEmpty()) {
				returnDoc = label;
			}
		}
		returnDoc = _matchEnumerationsInDoc(field.getProp("units"), returnDoc);
		return returnDoc;
	}

	private String _matchEnumerationsInDoc(String field, String currentDocField) {
		if (field != null && !field.isEmpty()) {
			Matcher enumMatcher = enumMatch.matcher(field);

			if (enumMatcher.matches()) {
				Matcher individualMatcher = individualEnumPattern
						.matcher(field);
				while (individualMatcher.find()) {
					currentEnumeration.add(individualMatcher.group(1).trim().replaceAll("[- ]","_"));
				}

			} else {
				if (!currentDocField.isEmpty()) {
					currentDocField += " ";
				}
				currentDocField += field;
			}
		}
		return currentDocField;
	}

}
