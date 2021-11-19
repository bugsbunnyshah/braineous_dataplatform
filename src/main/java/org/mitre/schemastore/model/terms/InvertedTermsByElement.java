// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.terms;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Class storing an inverted list of terms
 * @author CWOLF
 */
public class InvertedTermsByElement implements Serializable
{	
	/** Stores the inverted list of terms by associated element */
	private HashMap<AssociatedElement, Terms> invertedList = new HashMap<AssociatedElement, Terms>();
	
	/** Constructs the terms */
	public InvertedTermsByElement(Terms terms)
		{ for(Term term : terms.getTerms()) addTerm(term); }
	
	/** Adds a term to the inverted list */
	public void addTerm(Term term)
	{
		for(AssociatedElement element : term.getElements())
		{
			Terms terms = invertedList.get(element);
			if(terms==null) invertedList.put(element, terms=new Terms());
			terms.addTerm(term);
		}
	}
	
	/** Updates a term in the inverted list */
	public void updateTerm(Term oldTerm, Term newTerm)
		{ removeTerm(oldTerm); addTerm(newTerm); }
	
	/** Removes a term from the inverted list */
	public void removeTerm(Term term)
	{
		for(AssociatedElement element : term.getElements())
		{
			Terms terms = invertedList.get(element);
			if(terms!=null) terms.removeTerm(term);
		}
	}
	
	/** Get the terms associated with the specified element */
	public Terms getTerms(AssociatedElement element)
	{
		Terms terms = invertedList.get(element);
		return terms==null ? new Terms() : terms;
	}
	
	/** Get the terms associated with the specified element */
	public Terms getTerms(Integer schemaID, Integer elementID)
		{ return getTerms(new AssociatedElement(schemaID,elementID,"","")); }
}