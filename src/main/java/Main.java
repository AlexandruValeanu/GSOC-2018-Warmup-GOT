import org.jgrapht.alg.NaiveLcaFinder;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.io.*;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

public class Main {
    // Internal class used to represent vertices (it is needed because some vertices have labels and we'd like to
    // print "Aerys II The Mad" instead of "Aerys II"
    private static class Node{
        private String id;
        private Map<String, Attribute> map;

        public Node(String id, Map<String, Attribute> map) {
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

    public static void main(String[] args) throws IOException, ImportException {
        if (args == null || args.length < 3){
            throw new IllegalArgumentException("not enough arguments: expected <file-name> <id1> <id2>");
        }

        // read the whole .dot file into string 'text'
        String text = new String(Files.readAllBytes(Paths.get(args[0])), StandardCharsets.UTF_8);

        VertexProvider<Node> vp = Node::new;
        EdgeProvider<Node, DefaultEdge> ep = (f, t, l, a) -> new DefaultEdge();

        GraphImporter<Node, DefaultEdge> importer = new DOTImporter<>(vp, ep);
        SimpleDirectedGraph<Node, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        importer.importGraph(graph, new StringReader(text));

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
