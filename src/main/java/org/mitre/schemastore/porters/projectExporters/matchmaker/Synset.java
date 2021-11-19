package org.mitre.schemastore.porters.projectExporters.matchmaker;

import java.util.ArrayList;


/**
 * A Synset is just an arraylist of Nodes, that can do a few tricks. This object
 * came from Michael Morse's clustering code, with lots of modifications by
 * David and Maya. The way we actually use it in our code, a Synset is a
 * cluster. The hierarchical clustering code's result is a list of synsets,
 * which are "chains" of pairwise matches that all logically group together.
 * 
 * @author MDMORSE, HAOLI
 * 
 */
public class Synset implements Comparable<Synset> {
	public ArrayList<SynsetTerm> terms;
	public SynsetTerm leastNode = null;
	
	public Synset() {
		terms = new ArrayList<SynsetTerm>();
	}

	public Synset(SynsetTerm n) {
		this(); 
		add(n);
	}

	public void add(SynsetTerm n) {
		if (!terms.contains(n))
			terms.add(n);
		updateLeastNode(n);
	}

	/** 
	 * Combine another synsets into one
	 * @param two
	 */
	public void combine(Synset two) {
//		nodes.ensureCapacity(nodes.size() + two.nodes.size());
		terms.addAll(two.terms);
		updateLeastNode(two.leastNode);
	}
	
	/**
	 * removes a term from the current synset and return the term as a new synset
	 * @param removeTerm
	 * @return a new synset that's consisted of the removed term
	 */
	public Synset remove(SynsetTerm removeTerm) {
		for ( SynsetTerm term : terms )
			if ( term.equals(removeTerm) ) { 
				terms.remove(term);
				break;
			}
		updateLeastNode(null);
		return new Synset(removeTerm); 
	}
	

	private void updateLeastNode(SynsetTerm n) {
		if ( n == null )
		{
			for ( SynsetTerm term : terms ) {
				if (leastNode == null || leastNode.compareTo(n) > 0) 
					leastNode = term;
			}
		}
		else if (leastNode == null || leastNode.compareTo(n) > 0) leastNode = n;
	}

	/**
	 * full linkage. max distance is 1, least is 0 Simple Linkage
	 * 
	 * @param two
	 * @return the largest distance between two Synset nodes
	 */
	float completeLinkage(Synset two) {
		int i;
		int j;
		float maxDist = 0;
		for (i = 0; i < this.terms.size(); i++) {
			Node t_node = this.terms.get(i);
			for (j = 0; j < two.terms.size(); j++) {
				Node s_node = two.terms.get(j);
				int m = t_node.pointers.indexOf(s_node);
				if (m == -1) continue;
				// return 0;
				float st_dist = t_node.distances.get(m).floatValue();
				if (maxDist < st_dist) {
					maxDist = st_dist;
				}
			}
		}
		return maxDist;
	}

	// Returns minimum distance between two nodes
	public float simpleLinkage(Synset two) {
		int i, j;
		float minDist = 1;
		for (i = 0; i < this.terms.size(); i++) {
			Node t_node = this.terms.get(i);
			for (j = 0; j < two.terms.size(); j++) {
				Node s_node = two.terms.get(j);
				int m = t_node.pointers.indexOf(s_node);
				if (m == -1) return 0;
				float st_dist = t_node.distances.get(m).floatValue();
				if (minDist > st_dist) minDist = st_dist;
			}
		}
		return minDist;
	}

	public void groupEcombine(Synset two) {
		terms.ensureCapacity(terms.size() + two.terms.size());
		terms.addAll(two.terms);
		updateLeastNode(two.leastNode);
	}

	public ArrayList<SynsetTerm> getGroup() {
		return terms;
	}

	public int compareTo(Synset other) {
		return this.leastNode.compareTo(other.leastNode);
	}
	
	public boolean equals(Synset other) {
		if ( this.terms.size() != other.terms.size() ) return false; 
		
		for ( SynsetTerm thisNode: terms ) 
			if ( !other.terms.contains(thisNode) )
				return false;
		
		return true;
	}

	/**
	 * returns the schemaelement node that belongs to the specified schema
	 * 
	 * @param baseSchema
	 * @return SynsetTerm
	 */
	public SynsetTerm getTerm(Integer baseSchema) {
		for (SynsetTerm n : terms)
			if (n.schemaId.equals( baseSchema)) return n;
		return null;
	}
	

}
