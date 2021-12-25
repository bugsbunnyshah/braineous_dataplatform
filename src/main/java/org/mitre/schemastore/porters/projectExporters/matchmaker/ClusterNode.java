package org.mitre.schemastore.porters.projectExporters.matchmaker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * The main clustering code, originally written by Michael Morse with lots of
 * modifications for MatchMaker.
 * <p>
 * This code takes as an input a list of SynsetTerm objects, and its
 * output is a list of sorted clusters. Each cluster is stored as a Synset
 * object.
 * 
 * @author MDMORSE, DMALLEN
 */
public class ClusterNode {
	public ArrayList<Synset> synsets;

	// I don't know how this really affects the Synset's . But any two Synset
	// with distance (or confidence) less than this number
	// is filtered out.
	public static float MAGIC_THRESHOLD = (float) 0.2;

	public ClusterNode(ArrayList<SynsetTerm> nodes) {
		synsets = new ArrayList<Synset>();
		for (SynsetTerm n : nodes) {
			synsets.add(new Synset(n));
		}
	}

	/**
	 * @param j
	 *            the index of the cluster
	 * @return the jth cluster in the object.
	 */
	public ArrayList<SynsetTerm> getGroup(int j) {
		return synsets.get(j).getGroup();
	}

	/**
	 * @return the number of clusters that ClusterNode currently has
	 */
	public int numGroups() {
		return synsets.size();
	}

	/**
	 * Sort synsets alphabetically using synsets comparator and rely on merge
	 * sort implemented for java.utils.Arrays.sort() which is O(n log n )
	 */
	public void sortAlpha() {
		Collections.sort(synsets);
	}

	public void sortByScore() {
		Collections.sort(synsets, new groupEScoreComparator());
	}

	/**
	 * Sort Synset alphabetically by specified particular schema ID.
	 */

	public void sort(Integer schemaId) {
		Collections.sort(synsets, new groupEComparator(schemaId));
	}

	/**
	 * Run the hierarchical clustering algorithm on the list of matches. The two
	 * arguments are not strictly necessary -- if you don't know them, then pass
	 * values both <= 0. If you do know them, it helps with status reporting to
	 * the user.
	 * 
	 * @param schemaCount
	 *            the total number of schemas being matched
	 * @param maxElements
	 *            the maximum number of elements in any of the schemas.
	 */
	public void cluster(int schemaCount, int maxElements) {
		float max_dist = 0;
		int spot1 = 0;
		int spot2 = 0;

		if (schemaCount < 0) schemaCount = 0;
		if (maxElements < 0) maxElements = 0;

		// We need at most maxElements clusters of schemaCount elements each.
		// With each pass through the while() loop, synsets gets shorter (adding
		// things to clusters)
		int totalLoops = synsets.size() - maxElements;
		int pctMod = 1;
		int everyN = (int) Math.ceil((double) totalLoops / (double) 100);
		// One step for every percentage.
		if (everyN == 0) {
			pctMod = 25;
			everyN = totalLoops / 4;
		} // Sometimes you don't have 100 total matches.
		if (everyN == 0) {
			pctMod = 100;
			everyN = totalLoops;
		} // Or even 4!

		int pctStatus = 0;
		int loopCounter = 0;

		// By making the "complete list" a separate array, we avoid a penalty on
		// remove() statements associated with moving completed clusters around
		// in the Synset array. Each time we combine two groups and remove one
		// of them, the array list has to do a lot of work. Say I have the list
		// [A, B, C, D, E] and we remove C, the array list has to shift D and E
		// over. When a cluster is completed, we move it out of the array so
		// moving
		// it never pays a penalty.
		ArrayList<Synset> completedClusters = new ArrayList<Synset>();

		// We'll probably end up needing more than this many slots, but this
		// will reduce the number of times the ArrayList has to reallocate more
		// space. That will save time.
		if (maxElements > 0) completedClusters.ensureCapacity(maxElements);

		while (1 == 1) {
			loopCounter++;

			if (everyN == 0) break;
			else if (loopCounter % everyN == 0) {
				pctStatus += pctMod;
				if (pctStatus >= 100) pctStatus = 100;
			}

			int i = 0, j = 0;
			max_dist = (float) MAGIC_THRESHOLD;

			for (i = 0; i < synsets.size(); i++) {
				Synset n1 = synsets.get(i);

				for (j = i + 1; j < synsets.size(); j++) {
					Synset n2 = synsets.get(j);

					float temp_dist = n1.simpleLinkage(n2);
					if (temp_dist > max_dist && 	notInSameSchema(synsets.get(i), synsets.get(j))) {
						max_dist = temp_dist;
						spot1 = i;
						spot2 = j;
					}
				}
			}

			// System.err.println("Loop " + loopCounter + ": " + synsets.size()
			// + " clusters");

			// If max distance between two groups is found greater than the
			// magic threshold, combine the two groups.
			if (max_dist > MAGIC_THRESHOLD) {
//				if (notInSameSchema(synsets.get(spot1), synsets.get(spot2))){
					Synset growing = synsets.get(spot1);
					growing.groupEcombine(synsets.get(spot2));
					removeCluster(spot2);

					if (growing.terms.size() >= schemaCount) {
						completedClusters.add(growing);
						removeCluster(spot1);
					}
//				} else System.err.println("(E)Found groupes in same schema: " + synsets.get(spot1).leastNode.elementName + " \n\t " + synsets.get(spot2).leastNode.elementName);
			} else {
				for (int x = synsets.size() - 1; x >= 0; x--)
					completedClusters.add(synsets.get(x));
				synsets = completedClusters;

				System.out.println("Clustering completed.");
				return;
			}
		}
	}

	/**
	 * Faster way of removing a cluster at a given index. In big lists, it's
	 * very bad to remove something in the middle of the list, because you have
	 * to shift everything after it to the left. Instead, swap the item you want
	 * to remove with the last one in the list, and remove only the last one.
	 * 
	 * @param idx
	 */
	public void removeCluster(int idx) {
		int lastIdx = synsets.size() - 1;
		Synset swap = synsets.get(lastIdx);
		synsets.set(idx, swap);
		synsets.remove(lastIdx);
	}

	/**
	 * Test if nodes in groupE1 are contained in the same schema as groupE2
	 * 
	 * @param groupE1
	 * @param groupE2
	 * @return
	 */
	private boolean notInSameSchema(Synset groupE1, Synset groupE2) {
		for (Node n1 : groupE1.getGroup()) {
			if (!(n1 instanceof SynsetTerm)) continue;
			for (Node n2 : groupE2.getGroup()) {
				if (!(n2 instanceof SynsetTerm)) continue;
				if (((SynsetTerm) n1).schemaId.equals(((SynsetTerm) n2).schemaId)) return false;
			}
		}
		return true;
	}

	/**
	 * Compares synsets by a Synset's average scores, rank from low to high.
	 * 
	 * @author HAOLI
	 * 
	 */
	class groupEScoreComparator implements Comparator<Synset> {
		groupEScoreComparator() {}

		public int compare(Synset g1, Synset g2) {
			return -(getAverageScore(g1).compareTo(getAverageScore(g2)));
		}

		private Double getAverageScore(Synset g) {
			Double score = 0.0;
			int numNodes = 0;
			for (SynsetTerm node : g.getGroup()) {
				for (Double s : node.distances) {
					score += s;
					numNodes++;
				}
			}
			if (numNodes == 0) return 0.0;
			else return score / numNodes;
		}
	}

	/**
	 * sort by a selected schema node in two Synset
	 * 
	 * @author HAOLI
	 * 
	 */
	public class groupEComparator implements Comparator<Synset> {
		Integer baseSchema;

		groupEComparator(Integer schemaId) {

			this.baseSchema = schemaId;
		}

		public int compare(Synset g1, Synset g2) {

			SynsetTerm n1 = g1.getTerm(baseSchema);
			SynsetTerm n2 = g2.getTerm(baseSchema);

			if (n1 == null && n2 == null) return 0;
			else if (n1 == null) return -1;
			else if (n2 == null) return 1;
			else return n1.elementName.compareToIgnoreCase(n2.elementName);
		}
	}

}
