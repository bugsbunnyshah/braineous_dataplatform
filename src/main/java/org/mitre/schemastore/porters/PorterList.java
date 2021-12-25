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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mitre.schemastore.client.SchemaStoreClient;
import org.mitre.schemastore.porters.schemaExporters.SchemaExporter;
import org.mitre.schemastore.porters.schemaImporters.SchemaImporter;

/** Class for managing the a list of porters */ @SuppressWarnings("serial")
public class PorterList<T extends Porter> extends ArrayList<T>
{
	/** Constructs the porter list */ @SuppressWarnings("unchecked")
	PorterList(StringBuffer buffer, String tag, SchemaStoreClient client)
	{
		Pattern pattern = Pattern.compile("<"+tag+">(.*?)</"+tag+">");
		Matcher matcher = pattern.matcher(buffer);
		while(matcher.find())
			try {
				T porter = (T)Class.forName(matcher.group(1)).newInstance();
				porter.setClient(client);
				add(porter);
			} catch(Exception e) {}
	}
	
	/** Returns the specified porter based on type */
	public T getByType(Class<?> type)
	{
		for(T porter : this)
			if(porter.getClass().equals(type)) return porter;
		return null;
	}
	
	/** Returns the specified porter based on name */
	public T getByName(String name)
	{
		for(T porter : this)
			if(porter.getName().equals(name)) return porter;
		return null;
	}

	/** Returns the list of porters based on file type */
	public ArrayList<T> getByFileType(String fileType)
	{
		ArrayList<T> filePorters = new ArrayList<T>();
		for(T porter : this)
		{
			if(fileType==null) filePorters.add(porter);
			if(porter instanceof SchemaImporter && ((SchemaImporter)porter).getFileTypes().contains(fileType)) filePorters.add(porter);
			if(porter instanceof SchemaExporter && ((SchemaExporter)porter).getFileType().equals(fileType)) filePorters.add(porter);
		}		
		return filePorters;
	}
}