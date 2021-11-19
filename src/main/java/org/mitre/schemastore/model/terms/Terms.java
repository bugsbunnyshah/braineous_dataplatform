// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.terms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class storing terms
 * @author CWOLF
 */
public class Terms implements Serializable
{	
	/** Stores the list of terms */
	private Term[] terms = new Term[0];
	
	/** Constructs the default list of terms */ public Terms() {}
	
	/** Constructs the terms */
	protected Terms(Term[] terms)
		{ this.terms = terms; }
	
	/** Copies the terms */
	protected Term[] copyTerms()
	{
		ArrayList<Term> copiedTerms = new ArrayList<Term>();
		for(Term term : terms) copiedTerms.add(term.copy());
		return copiedTerms.toArray(new Term[0]);
	}
	
	/** Gets the specified term */
	public Term getTerm(int termID)
	{
		if(terms==null) return null;
		for(Term term : terms)
			if(termID==term.getId()) return term;
		return null;
	}
	
	/** Gets the list of terms */
	public Term[] getTerms() { return terms; }

	/** Sets the list of terms */
	public void setTerms(Term[] terms) { this.terms = terms; }
	
	/** Adds a term to the term list */
	public void addTerm(Term newTerm)
	{
		ArrayList<Term> terms = new ArrayList<Term>(Arrays.asList(this.terms));
		if(!terms.contains(newTerm))
		{
			terms.add(newTerm);
			this.terms = terms.toArray(new Term[0]);
		}
	}
	
	/** Removes a term from the term list */
	public void removeTerm(Term oldTerm)
	{
		ArrayList<Term> terms = new ArrayList<Term>(Arrays.asList(this.terms));
		terms.remove(oldTerm);
		this.terms = terms.toArray(new Term[0]);
	}
	
	/** Removes a term from the term list */
	public void removeTerm(int termID)
		{ removeTerm(getTerm(termID)); }
}