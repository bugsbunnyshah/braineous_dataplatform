package org.mitre.schemastore.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.terms.AssociatedElement;

public class SchemaStoreTest
{
	static public void main(String args[])
	{
		// Display the schemas found within the repository
		try {
//			Repository repository = new Repository(Repository.DERBY,new File(".").toURI(),"testStore","postgres","postgres");
			Repository repository = new Repository(Repository.POSTGRES,new URI("localhost"),"supplies","postgres","postgres");
//			Repository repository = new Repository(Repository.SERVICE,new URI("http://ygg:8080/D3-develop/services/SchemaStore"),"","","");
			SchemaStoreClient client = new SchemaStoreClient(repository);
			
			Integer mappingID = 83452;
			Mapping mapping = client.getMapping(mappingID);
			for(MappingCell mappingCell : client.getMappingCells(mappingID))
				System.out.println(Arrays.asList(mappingCell.getInputs()).toString() + " " + mappingCell.getOutput());
			
			System.out.println("A");
			ArrayList<AssociatedElement> elements = new ArrayList<AssociatedElement>();
			elements.add(new AssociatedElement(mapping.getTargetId(), 83389, "", ""));
			for(MappingCell mappingCell : client.getMappingCellsByElement(mapping.getProjectId(), elements, 0.1))
				System.out.println(Arrays.asList(mappingCell.getInputs()).toString() + " " + mappingCell.getOutput());

			System.out.println("B");
			elements.add(new AssociatedElement(mapping.getSourceId(), 82572, "", ""));
			for(MappingCell mappingCell : client.getAssociatedMappingCells(mapping.getProjectId(), elements))
				System.out.println(Arrays.asList(mappingCell.getInputs()).toString() + " " + mappingCell.getOutput());
			
		} catch(Exception e) { e.printStackTrace(); }
	}
}
