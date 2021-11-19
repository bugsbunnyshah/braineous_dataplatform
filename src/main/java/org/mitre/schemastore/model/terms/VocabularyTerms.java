// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.terms;

import java.util.HashSet;

/**
 * Class for storing a vocabulary's terms
 * @author CWOLF
 */
public class VocabularyTerms extends Terms
{
	/** Stores the project associated with this vocabulary */
	private Integer projectID;
	
	/** Constructs the default vocabulary */ public VocabularyTerms() {}
	
	/** Constructs the vocabulary */
	public VocabularyTerms(Integer projectID)
		{ this.projectID = projectID; }
	
	/** Constructs the vocabulary */
	public VocabularyTerms(Integer projectID, Term[] terms)
		{ super(terms); this.projectID = projectID; }
	
	/** Copies the vocabulary */
	public VocabularyTerms copy()
		{ return new VocabularyTerms(getProjectID(),copyTerms()); }
	
	/** Returns the project ID */
	public Integer getProjectID() { return projectID; }

	/** Sets the project ID */
	public void setProjectID(Integer projectID) { this.projectID = projectID; }
	
	/** Returns the schemas used in this vocabulary */
	public Integer[] getSchemaIDs()
	{
		HashSet<Integer> schemaIDs = new HashSet<Integer>();
		for(Term term : getTerms())
			for(AssociatedElement element : term.getElements())
				schemaIDs.add(element.getSchemaID());
		return schemaIDs.toArray(new Integer[0]);
	}
}