// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data;

import java.util.ArrayList;
import java.util.List;

import org.mitre.schemastore.data.database.AnnotationDataCalls;
import org.mitre.schemastore.model.Annotation;

/** Class for managing the annotations in the schema repository */
public class AnnotationCache extends DataCache
{
	/** Stores reference to the annotation data calls */
	private AnnotationDataCalls dataCalls = null;
	
	/** Constructs the annotation cache */
	AnnotationCache(DataManager manager, AnnotationDataCalls dataCalls)
		{ super(manager); this.dataCalls=dataCalls; }		
	
	/** Sets the specified annotation in the database */
	public boolean setAnnotation(int elementID, int groupID, String attribute, String value)
		{ return dataCalls.setAnnotation(elementID, groupID, attribute, value); }
	
	/** Sets the specified list of annotations in the database */
	public boolean setAnnotations(List<Annotation> annotations)
		{ return dataCalls.setAnnotations(annotations); }
	
	/** Gets the specified annotation in the database */
	public String getAnnotation(int elementID, int groupID, String attribute)
		{ return dataCalls.getAnnotation(elementID, groupID, attribute); }

	/** Gets the annotations for the specified group */
	public ArrayList<Annotation> getAnnotations(int elementID, String attribute)
		{ return dataCalls.getAnnotations(elementID, attribute); }
	
	/** Gets the annotations for the specified group */
	public ArrayList<Annotation> getAnnotationsByGroup(int groupID, String attribute)
		{ return dataCalls.getAnnotationsByGroup(groupID, attribute); }
	
 	/** Clears the specified annotation in the database */
 	public boolean clearAnnotation(int elementID, int groupID, String attribute)
 		{ return dataCalls.clearAnnotation(elementID, groupID, attribute); }
	
 	/** Clears the specified group annotations in the database */
 	public boolean clearAnnotations(int groupID, String attribute)
 		{ return dataCalls.clearAnnotations(groupID, attribute); }
}