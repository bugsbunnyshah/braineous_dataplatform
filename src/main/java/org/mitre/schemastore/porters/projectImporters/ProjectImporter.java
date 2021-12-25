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

package org.mitre.schemastore.porters.projectImporters;

import java.net.URI;
import java.util.ArrayList;

import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.Project;
import org.mitre.schemastore.model.ProjectSchema;
import org.mitre.schemastore.porters.Importer;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.ImporterException.ImporterExceptionType;

/** Abstract Project Importer class */
public abstract class ProjectImporter extends Importer
{
	/** Defines a class to contain mapping information */
	class MappingContainer
	{
		private Mapping mapping;
		private ArrayList<MappingCell> mappingCells;
		MappingContainer(Mapping mapping, ArrayList<MappingCell> mappingCells)
			{ this.mapping = mapping; this.mappingCells = mappingCells; }
	}
	
	/** Initializes the importer */
	abstract protected void initialize() throws ImporterException;

	/** Returns the schemas in need of alignment */
	abstract public ArrayList<ProjectSchema> getSchemas() throws ImporterException;
	
	/** Returns the mapping cells from the specified URI */
	abstract protected ArrayList<MappingContainer> getMappings() throws ImporterException;

	/** Initializes the importer for the specified URI */
	final public void initialize(URI uri) throws ImporterException
		{ this.uri = uri; initialize(); }

	/** Imports the specified URI */
	final public Integer importProject(String name, String author, String description) throws ImporterException
	{
		// Generate the project and mappings
		Project project = new Project(null,name,description,author,getSchemas().toArray(new ProjectSchema[0]));
		ArrayList<MappingContainer> mappings = getMappings();
			
		// Imports the project and mappings
		try {
			// Imports the project
			Integer projectID = client.addProject(project);
			project.setId(projectID);

			// Imports the mappings
			for(MappingContainer mapping : mappings)
			{
				mapping.mapping.setProjectId(projectID);
				Integer mappingID = client.addMapping(mapping.mapping);
				mapping.mapping.setId(mappingID);
				client.saveMappingCells(mapping.mapping.getId(), mapping.mappingCells);
			}
		}

		// Delete the project if import wasn't totally successful
		catch(Exception e) 
		{
			try { client.deleteProject(project.getId()); } catch(Exception e2) {}
			throw new ImporterException(ImporterExceptionType.IMPORT_FAILURE,"A failure occurred in transferring the project to the repository");
		}

		// Returns the id of the imported project
		return project.getId();
	}
}