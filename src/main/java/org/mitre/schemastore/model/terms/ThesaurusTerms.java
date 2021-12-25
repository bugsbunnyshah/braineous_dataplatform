// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.terms;

/**
 * Class storing thesaurus terms
 * @author CWOLF
 */
public class ThesaurusTerms extends Terms
{
	/** Stores the thesaurus id */
	private Integer thesaurusID;
	
	/** Constructs the default list of thesaurus terms */ public ThesaurusTerms() {}
	
	/** Constructs the thesaurus terms */
	public ThesaurusTerms(Integer thesaurusID, Term[] terms)
		{ super(terms); this.thesaurusID = thesaurusID; }
	
	/** Copies the thesaurus terms */
	public ThesaurusTerms copy()
		{ return new ThesaurusTerms(getThesaurusId(),copyTerms()); }
	
	/** Retrieves the thesaurus ID */
	public Integer getThesaurusId() { return thesaurusID; }

	/** Sets the thesaurus ID */
	public void setThesaurusId(Integer thesaurusID) { this.thesaurusID = thesaurusID; }
}