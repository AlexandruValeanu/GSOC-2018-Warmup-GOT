import org.jgrapht.Graph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.NaiveLcaFinder;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.io.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

public class Main {
    // Internal class used to represent vertices (it is needed because some vertices have labels and we'd like to
    // print "Aerys II The Mad" instead of "Aerys II"
    private static class Node{
        private String id;
        private Map<String, Attribute> map;

        Node(String id, Map<String, Attribute> map) {
            this.id = id;
            this.map = map;
        }

        @Override
        public String toString() {
            /*
                If there is a label, then return its string representation.
                Otherwise, return the id of the node.
             */
            if (map.containsKey("label"))
                return map.get("label").toString();
            else
                return id;
        }
    }

    /**
     * Takes a graph (as a .dot file) and two ids (corresponding to valid nodes from the graph) and
     * computes the lowest common ancestor(s) of those two nodes.
     *
     * The result is printed to standard output.
     *
     * @param args array that contains the three arguments:
     *             <ul>
                        <li>name of the .dot file that contains the graph</li>
                        <li>id of the first node</li>
                        <li>id of the second node</li>
                    </ul>
     * @throws ImportException - if the .dot file is not well-formed
     * @throws IllegalArgumentException - if there are not enough arguments or if any id is invalid or if the graph contains cycles
     */
    public static void main(String[] args) throws ImportException {
        if (args == null || args.length < 3){
            throw new IllegalArgumentException("not enough arguments: expected <file-name> <id1> <id2>");
        }

        VertexProvider<Node> vp = Node::new;
        EdgeProvider<Node, DefaultEdge> ep = (f, t, l, a) -> new DefaultEdge();

        GraphImporter<Node, DefaultEdge> importer = new DOTImporter<>(vp, ep);
        DirectedPseudograph<Node, DefaultEdge> graph = new DirectedPseudograph<>(DefaultEdge.class);
        importer.importGraph(graph, new File(Paths.get(args[0]).toUri()));

        CycleDetector<Node, DefaultEdge> cycleDetector = new CycleDetector<>(graph);
        if (cycleDetector.detectCycles()){
            System.err.println("cycle: " + cycleDetector.findCycles());
            throw new IllegalArgumentException("input graph contains a cycle");
        }

        final String A = args[1];
        final String B = args[2];

        /*
            Find the nodes corresponding to our ids (i.e. A and B)
            Node: if there is no node with some id we set the node to 'null' and throw an exception later
         */
        Node nodeA = graph.vertexSet().stream().filter(n -> n.id.equals(A)).findFirst().orElse(null);
        Node nodeB = graph.vertexSet().stream().filter(n -> n.id.equals(B)).findFirst().orElse(null);

        // check that both ids were mapped to valid nodes
        if (nodeA == null || nodeB == null){
            throw new IllegalArgumentException("Error: incorrect arguments");
        }

        // find the set of all lcas for nodeA and nodeB
        Set<Node> lcas = new NaiveLcaFinder<>(graph).findLcas(nodeA, nodeB);

        if (lcas.isEmpty()){
            System.out.printf("No lowest common ancestor found for %s and %s\n", args[1], args[2]);
        }
        else{
            lcas.forEach(System.out::println);
        }
    }
}
