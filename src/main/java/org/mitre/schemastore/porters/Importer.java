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

import java.net.URI;
import java.util.ArrayList;

/** Abstract Importer class */
public abstract class Importer extends Porter
{
	/** Stores the URI being imported */
	protected URI uri;
	
	/** Returns the importer URI type */
	abstract public URIType getURIType();
	
	/** Returns the importer URI file types (only needed when URI type is FILE) */
	public ArrayList<String> getFileTypes() { return new ArrayList<String>(); }
	
	/** Returns the list of URIs which can be selected from (only needed when URI type is LIST) */
	public ArrayList<URI> getList() { return new ArrayList<URI>(); }
}