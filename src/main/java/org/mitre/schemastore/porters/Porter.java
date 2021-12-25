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

import org.mitre.schemastore.client.SchemaStoreClient;

/** Abstract porter class */
public abstract class Porter
{
	/** Stores the schema store client being referenced by this porter */
	protected SchemaStoreClient client = null;

	/** Set the schema store client */
	public void setClient(SchemaStoreClient client)
		{ this.client = client; }
	
	/** Returns the importer name */
	abstract public String getName();
	
	/** Returns the importer description */
	abstract public String getDescription();

	/** Outputs the importer name */
	public String toString()
		{ return getName(); }
}