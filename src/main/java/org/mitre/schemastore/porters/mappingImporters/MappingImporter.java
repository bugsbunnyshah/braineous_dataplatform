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

package org.mitre.schemastore.porters.mappingImporters;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.mitre.schemastore.model.Function;
import org.mitre.schemastore.model.FunctionImp;
import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.Project;
import org.mitre.schemastore.model.ProjectSchema;
import org.mitre.schemastore.porters.Importer;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.ImporterException.ImporterExceptionType;

/** Abstract Mapping Importer class */
public abstract class MappingImporter extends Importer
{
	/** Stores the aligned source schema */
	protected ProjectSchema source;
	
	/** Stores the aligned target schema */
	protected ProjectSchema target;
	
	/** Stores the unidentified mapping cells */
	protected ArrayList<MappingCellPaths> unidentifiedMappingCellPaths = new ArrayList<MappingCellPaths>();
	
	/** Initializes the importer */
	abstract protected void initialize() throws ImporterException;

	/** Returns the source schema in the mapping */
	abstract public ProjectSchema getSourceSchema() throws ImporterException;

	/** Returns the target schema in the mapping */
	abstract public ProjectSchema getTargetSchema() throws ImporterException;

	/** Returns the functions from the specified URI */
	public HashMap<Function, ArrayList<FunctionImp>> getFunctions() throws ImporterException
		{ return new HashMap<Function, ArrayList<FunctionImp>>(); }
	
	/** Returns the mapping cells from the specified URI */
	abstract public ArrayList<MappingCell> getMappingCells() throws ImporterException;
	
	/** Initializes the importer for the specified URI */
	final public void initialize(URI uri) throws ImporterException
	{
		this.uri = uri;
		initialize();
	}

	/** Sets the source and target schemas */
	final public void setSchemas(Integer sourceID, Integer targetID) throws ImporterException
	{
		source = getSourceSchema(); if(sourceID!=null) source.setId(sourceID);
		target = getTargetSchema(); if(targetID!=null) target.setId(targetID);
	}

	/** Indicates if the two specified functions match */
	final private boolean functionsMatch(Function function1, Function function2)
	{
		// Get the function expressions
		String expression1 = function1.getExpression(); if(expression1==null) expression1="";
		String expression2 = function2.getExpression(); if(expression2==null) expression2="";
		
		// Compare functions
		if(!expression1.equals(expression2)) return false;
		if(!function1.getOutputType().equals(function2.getOutputType())) return false;
		if(function1.getInputTypes().length != function2.getInputTypes().length) return false;
		for(int i = 0; i < function1.getInputTypes().length; i++)
			if(!function1.getInputTypes()[i].equals(function2.getInputTypes()[i]))
				return false;
		return true;
	}
	
	/** Imports the specified functions */
	final private HashMap<Integer,Integer> importFunctions() throws Exception
	{
		HashMap<Integer,Integer> functionMap = new HashMap<Integer,Integer>();
		
		// Retrieve all functions currently in repository
		HashMap<String,Function> repositoryFunctions = new HashMap<String,Function>();
		for(Function function : client.getFunctions())
			repositoryFunctions.put(function.getName(), function);
		
		// Associate specified functions with repository functions
		HashMap<Function,ArrayList<FunctionImp>> functions = getFunctions();
		for(Function function : functions.keySet())
		{
			// Aligns the function with the function defined in the repository
			Function repositoryFunction = repositoryFunctions.get(function.getName());
			if(repositoryFunction != null)
			{
				if(functionsMatch(function, repositoryFunction))
					functionMap.put(function.getId(), repositoryFunction.getId());
				else throw new Exception("Mismatched function definition given for function '" + function.getName() + "'.");
			}
			else
			{
				// Creates a new function as needed
				Integer functionID = client.addFunction(function);
				if(functionID == null)
					throw new Exception("Unable to save function " + function.getName());
				functionMap.put(function.getId(), functionID);
			}
			
			// Store the function implementations
			for(FunctionImp functionImp : functions.get(function))
				if(!client.setFunctionImp(functionImp))
					throw new Exception("Failed to store the function implementation for function '" + function.getName() + "'.");
		}
		
		// Return the generated function mapping
		return functionMap;
	}
	
	/** Imports the mapping specified by the currently set URI */
	final public Integer importMapping(Project project, Integer sourceID, Integer targetID) throws ImporterException
	{
		// Set the schema information
		setSchemas(sourceID, targetID);

		// Generate the mapping and mapping cells
		Mapping mapping = new Mapping(null, project.getId(), source.getId(), target.getId());
		ArrayList<MappingCell> mappingCells = getMappingCells();
			
		// Imports the mapping (and associated functions)
		try {
			// Imports the functions and converts the mapping cells to properly reference the functions
			HashMap<Integer,Integer> functionMap = importFunctions();
			for(MappingCell mappingCell : mappingCells)
			{
				Integer functionID = mappingCell.getFunctionID();
				if(functionID != null)
				{
					Integer newFunctionID = functionMap.get(functionID);
					if(newFunctionID == null)
						throw new Exception("Undefined functions were encountered!");
					mappingCell.setFunctionID(newFunctionID);
				}
			}

			// Imports the mapping
			Integer mappingID = client.addMapping(mapping);
			if(mappingID == null)
				throw new Exception("Unable to save the mapping to the database!");
			mapping.setId(mappingID);
			if(!client.saveMappingCells(mapping.getId(), mappingCells))
				throw new Exception("Unable to save the mapping cells to the database!");
		}
		catch(Exception e) {
			// Delete the mapping if import wasn't totally successful
			try {
				client.deleteMapping(mapping.getId());
			} catch(Exception e2) {}
			throw new ImporterException(ImporterExceptionType.IMPORT_FAILURE, "A failure occurred in transferring the mapping to the repository. " + e.getMessage());
		}

		// Returns the id of the imported mapping
		return mapping.getId();
	}

	/** Returns the unidentified mapping cells */
	final public ArrayList<MappingCellPaths> getUnidentifiedMappingCellPaths()
		{ return unidentifiedMappingCellPaths; }
}