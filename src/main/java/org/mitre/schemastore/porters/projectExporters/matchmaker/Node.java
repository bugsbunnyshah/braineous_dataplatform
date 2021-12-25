package org.mitre.schemastore.porters.projectExporters.matchmaker;

import java.util.ArrayList;

/**
 * Abstract "node" that is used in the clustering code.  Originally, these were used as hierarchical nodes, that
 * could have extra levels nested underneath them.  Because of the way the clustering works in matchmaker, it doesn't
 * go more than 2 levels deep.  
 * @see Synset
 * @author MDMORSE
 */
public class Node { 
        public String name;
        public ArrayList<Node> pointers;
        public ArrayList<Double> distances;

        public Node(String n) {
                name = n;
                pointers = new ArrayList<Node>();
                distances = new ArrayList<Double>();
        }

        public void add(Node n, Double d) {
                pointers.add(n);
                distances.add(new Double(d));
        }
                
        public String toString() {
                return name;
        }
}
