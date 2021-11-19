package org.mitre.schemastore.porters.projectExporters.matchmaker;

/**
 * This is an extension of a node element. The difference is that it keeps track
 * of the schema ID, which is needed to avoid constant look up during
 * clustering.
 * 
 * @author HAOLI
 * 
 */

public class SynsetTerm extends Node implements Comparable<SynsetTerm> {
	public Integer elementId;
	public String elementName;
	public String elementDescription;
	public Integer schemaId;

	public SynsetTerm(Integer schemaID, Integer elementId, String elementName, String elementDescription) {
		super(elementId.toString());
		this.schemaId = schemaID;
		this.elementId = elementId;
		this.elementName = elementName;
		this.elementDescription = elementDescription;
	}

	public int compareTo(SynsetTerm o) {
		return this.toString().compareTo(o.toString());
	}

	public String toString() {
		return schemaId + elementName + elementId;
	}
	
	public boolean equals(SynsetTerm o) {
		return this.compareTo(o)==0; 
	}
	

} // End SynsetTerm
