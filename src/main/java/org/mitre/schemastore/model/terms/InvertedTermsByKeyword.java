// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.terms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.mitre.schemastore.utils.Stemmer;
import org.mitre.schemastore.utils.Tokenizer;

/**
 * Class storing an inverted list of terms
 * @author CWOLF
 */
public class InvertedTermsByKeyword implements Serializable
{	
	/** Stores a lookup of all phases based on their first word */
	private HashMap<String,ArrayList<String>> phraseList = new HashMap<String,ArrayList<String>>();
	
	/** Stores the inverted list of terms by keyword */
	private HashMap<String, Terms> invertedList = new HashMap<String, Terms>();

	/** Constructs the terms */
	public InvertedTermsByKeyword() {}	
	
	/** Constructs the terms */
	public InvertedTermsByKeyword(Terms terms)
		{ for(Term term : terms.getTerms()) addTerm(term); }
	
	/** Generates the list of keywords under which to store the term */
	private ArrayList<String> generateKeywords(Term term)
	{
		// Get the list of names under which to store the term
		ArrayList<String> names = new ArrayList<String>();
		names.add(term.getName());
		for(AssociatedElement element : term.getElements())
			names.add(element.getName());
		
		// Generate the list of keywords
		ArrayList<String> keywords = new ArrayList<String>();
		for(String name : names)
		{
			// Split and stem the keyword
			ArrayList<String> words = Tokenizer.tokenize(name);
			StringBuffer buffer = new StringBuffer();
			for(String word : words) buffer.append(Stemmer.stem(word) + " ");
			if(buffer.length()>0) keywords.add(buffer.substring(0, buffer.length()-1).toLowerCase());
		}
		return keywords;
	}
	
	/** Adds a term to the inverted list */
	public void addTerm(Term term)
	{
		// Cycle through each keyword
		for(String keyword : generateKeywords(term))
		{
			// Add term to inverted list
			Terms terms = invertedList.get(keyword);
			if(terms==null) invertedList.put(keyword, terms=new Terms());
			terms.addTerm(term);
			
			// Add keyword to phrase list
			String firstWord = keyword.replaceAll(" .*", "");
			ArrayList<String> phrases = phraseList.get(firstWord);
			if(phrases==null) phraseList.put(firstWord, phrases=new ArrayList<String>());
			phrases.add(keyword);
		}
	}
	
	/** Add a list of terms to the inverted list */
	public void addTerms(Terms terms)
		{ for(Term term : terms.getTerms()) addTerm(term); }
	
	/** Updates a term in the inverted list */
	public void updateTerm(Term oldTerm, Term newTerm)
		{ removeTerm(oldTerm); addTerm(newTerm); }
	
	/** Removes a term from the inverted list */
	public void removeTerm(Term term)
	{
		// Cycle through each keyword
		for(String keyword : generateKeywords(term))
		{
			// Remove the term from the inverted list
			Terms terms = invertedList.get(keyword);
			if(terms!=null) terms.removeTerm(term);
		
			// Remove the keyword from the phrase list
			String firstWord = keyword.replaceAll(" .*", "");
			ArrayList<String> phrases = phraseList.get(firstWord);
			if(phrases!=null) phrases.remove(keyword);
		}
	}
	
	/** Returns the list of phrases which start with the specified word */
	public HashSet<String> getPhrases(String word)
	{
		ArrayList<String> phrases = phraseList.get(word.toLowerCase());
		if(phrases==null) return new HashSet<String>();
		return new HashSet<String>(phrases);
	}
	
	/** Returns the list of synonyms for the specified keyword */
	public HashSet<String> getSynonyms(String keyword)
	{
		HashSet<String> synonyms = new HashSet<String>();
		Terms terms = invertedList.get(keyword.toLowerCase());
		if(terms!=null)
			for(Term term : terms.getTerms())
			{
				synonyms.addAll(Tokenizer.tokenize(term.getName()));
				for(AssociatedElement element : term.getElements())
					synonyms.addAll(Tokenizer.tokenize(element.getName()));
			}
		return synonyms;
	}
}