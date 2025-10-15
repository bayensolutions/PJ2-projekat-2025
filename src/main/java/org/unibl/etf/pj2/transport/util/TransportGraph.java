package org.unibl.etf.pj2.transport.util;

import org.unibl.etf.pj2.transport.generator.TransportDataGenerator;
import java.util.*;

public class TransportGraph {

    public static class Edge {
        public final String to;
        public final TransportDataGenerator.Departure departure;
        public Edge(String to, TransportDataGenerator.Departure departure) {
            this.to = to;
            this.departure = departure;
        }
    }

    private final Map<String, List<Edge>> adjacency = new HashMap<>();

    public void addEdge(String from, String to, TransportDataGenerator.Departure d) {
        adjacency.computeIfAbsent(from, k -> new ArrayList<>()).add(new Edge(to, d));
    }

    public List<Edge> getNeighbors(String node) {
        return adjacency.getOrDefault(node, Collections.emptyList());
    }

    public Set<String> getNodes() {
        return adjacency.keySet();
    }
}

