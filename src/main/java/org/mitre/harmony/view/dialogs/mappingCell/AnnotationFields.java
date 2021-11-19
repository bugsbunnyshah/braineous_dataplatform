// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.dialogs.mappingCell;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mitre.harmony.model.ConfigManager;
import org.mitre.schemastore.model.MappingCell;

/**
 * Class for storing the various annotation fields
 * @author CWOLF
 */
public class AnnotationFields
{
	// Stores the basic annotation fields
	private String date;
	private String author;
	private String note;

	/** Stores the extra annotation fields */
	private LinkedHashMap<String, String> extraFields = new LinkedHashMap<String, String>();
	
	/** Constructs the annotation fields */
	public AnnotationFields(MappingCell mappingCell)
	{
		// Sets the date and author of the mapping cell
		date = mappingCell.getDate();
		author = mappingCell.getAuthor();
		
		// Set the extra note fields for the mapping cell
		note = mappingCell.getNotes();
		for(String field : ConfigManager.getArray("extraMappingCellFields"))
		{
			String fieldString = "<"+field+">(.*?)</"+field+">";
			Pattern fieldPattern = Pattern.compile(fieldString);
			Matcher fieldMatcher = fieldPattern.matcher(note);
			extraFields.put(field, fieldMatcher.find() ? fieldMatcher.group(1).trim() : "");
			note = note.replaceAll(fieldString, "");
		}
		
		// Set the note for the mapping cell
		note = note.trim();
	}

	// Gets the basic annotation fields
	public String getDate() { return date; }
	public String getAuthor() { return author; }
	public String getNote() { return note; }
	
	/** Gets the list of extra annotation fields */
	public ArrayList<String> getExtraFields()
		{ return new ArrayList<String>(extraFields.keySet()); }
	
	/** Gets the specified extra annotation fields */
	public String getExtraField(String field)
		{ return extraFields.get(field); }
	
	// Sets the basic annotation fields
	public void setDate(String date) { this.date=date; }
	public void setAuthor(String author) { this.author=author; }
	public void setNote(String note) { this.note=note; }	

	/** Sets the specified extra annotation field */
	public void setExtraField(String field, String value)
		{ extraFields.put(field,value); }
}