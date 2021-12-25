/*
 *  Copyright 2008 The MITRE Corporation (http://www.mitre.org/). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mitre.schemastore.porters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.mitre.schemastore.client.SchemaStoreClient;
import org.mitre.schemastore.porters.mappingExporters.MappingExporter;
import org.mitre.schemastore.porters.mappingImporters.MappingImporter;
import org.mitre.schemastore.porters.projectExporters.ProjectExporter;
import org.mitre.schemastore.porters.projectImporters.ProjectImporter;
import org.mitre.schemastore.porters.schemaExporters.SchemaExporter;
import org.mitre.schemastore.porters.schemaImporters.SchemaImporter;
import org.mitre.schemastore.porters.vocabularyExporters.VocabularyExporter;

/** Class for managing the various porters */
public class PorterManager
{
	/** Stores listings of the porters */
	private HashMap<PorterType, PorterList<? extends Porter>> porters = new HashMap<PorterType, PorterList<? extends Porter>>();
	
	/** Constructs the porter manager class */
	public PorterManager(SchemaStoreClient client)
	{
		// Retrieve porter configuration file
		StringBuffer buffer = new StringBuffer("");
		try {
			InputStream configStream = PorterList.class.getResourceAsStream("/porters.xml");
			BufferedReader in = new BufferedReader(new InputStreamReader(configStream));
			String line; while((line=in.readLine())!=null) buffer.append(line);
			in.close();
		}
		catch(IOException e)
			{ System.out.println("(E)PorterManager - porters.xml has failed to load!\n"+e.getMessage()); }
		
		// Parse out the various importers and exporters
		porters.put(PorterType.SCHEMA_IMPORTERS, new PorterList<SchemaImporter>(buffer,"schemaImporter",client));
		porters.put(PorterType.SCHEMA_EXPORTERS, new PorterList<SchemaExporter>(buffer,"schemaExporter",client));
		porters.put(PorterType.PROJECT_IMPORTERS, new PorterList<ProjectImporter>(buffer,"projectImporter",client));
		porters.put(PorterType.PROJECT_EXPORTERS, new PorterList<ProjectExporter>(buffer,"projectExporter",client));
		porters.put(PorterType.MAPPING_IMPORTERS, new PorterList<MappingImporter>(buffer,"mappingImporter",client));
		porters.put(PorterType.MAPPING_EXPORTERS, new PorterList<MappingExporter>(buffer,"mappingExporter",client));
		porters.put(PorterType.VOCABULARY_EXPORTERS, new PorterList<VocabularyExporter>(buffer, "vocabularyExporter", client));
		porters.put(PorterType.THESAURUS_EXPORTERS, new PorterList<SchemaExporter>(buffer, "schemaExporter", client));
	}
	
	/** Returns the list of specified porters */ @SuppressWarnings("unchecked")
	public <T> ArrayList<T> getPorters(PorterType type)
	{
		ArrayList<T> porterList = new ArrayList<T>();
		for(Porter porter : porters.get(type)) porterList.add((T)porter);
		return porterList;
	}
	
	/** Returns the specified porter */
	public Porter getPorter(Class<?> porterClass)
	{
		for(ArrayList<? extends Porter> porterList : porters.values())
			for(Porter porter : porterList)
				if(porter.getClass().equals(porterClass)) return porter;
		return null;
	}
}