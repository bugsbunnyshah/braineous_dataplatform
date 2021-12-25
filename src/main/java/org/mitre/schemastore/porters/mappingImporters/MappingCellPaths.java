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

import java.util.List;

/** Class for storing mapping cell paths */
public class MappingCellPaths
{
	/** Store the input paths */
	private List<String> inputPaths;

	/** Store the output path */
	private String outputPath;
	
	/** Constructs the mapping cell paths */
	public MappingCellPaths(List<String> inputPaths, String outputPath)
		{ this.inputPaths = inputPaths; this.outputPath = outputPath; }

	/** Returns the input paths */
	public List<String> getInputPaths()
		{ return inputPaths; }
	
	/** Returns the output path */
	public String getOutputPath()
		{ return outputPath; }
}