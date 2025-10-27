package org.unibl.etf.pj2.transport.util;

import org.unibl.etf.pj2.transport.generator.TransportDataGenerator;
import java.util.*;

public class TransportGraph {

    private final Map<String, List<Edge>> adjacencyList = new HashMap<>();

    public static class Edge {
        public final String from;
        public final String to;
        public final TransportDataGenerator.Departure departure;

        public Edge(String from, String to, TransportDataGenerator.Departure departure) {
            this.from = from;
            this.to = to;
            this.departure = departure;
        }
    }

    public TransportGraph(List<TransportDataGenerator.Station> stations,
                          List<TransportDataGenerator.Departure> departures) {

        // 🔹 Dodaj sve čvorove (grad, autobuska, željeznička)
        for (TransportDataGenerator.Station s : stations) {
            adjacencyList.putIfAbsent(s.city, new ArrayList<>());
            adjacencyList.putIfAbsent(s.busStation, new ArrayList<>());
            adjacencyList.putIfAbsent(s.trainStation, new ArrayList<>());

            // 🔹 Dvosmjerne veze između grada i stanica
            connectBoth(s.city, s.busStation);
            connectBoth(s.city, s.trainStation);

            // 🔹 Autobus ↔ Voz unutar istog grada
            connectBoth(s.busStation, s.trainStation);
        }

        // 🔹 Dodaj veze iz polazaka (JSON)
        for (TransportDataGenerator.Departure d : departures) {
            adjacencyList.putIfAbsent(d.from, new ArrayList<>());
            adjacencyList.putIfAbsent(d.to, new ArrayList<>());
            adjacencyList.get(d.from).add(new Edge(d.from, d.to, d));

            // ➕ dodaj i povratnu ivicu radi lakšeg prolaska kroz graf
            adjacencyList.get(d.to).add(new Edge(d.to, d.from, null));
        }

        // ✅ Debug info
        System.out.println("[DEBUG] Kreiran graf sa " + adjacencyList.size() +
                " čvorova i " + departures.size() + " polazaka.");

        int edgeCount = adjacencyList.values().stream().mapToInt(List::size).sum();
        System.out.println("[DEBUG] Ukupno ivica (direktnih veza): " + edgeCount);
    }

    private void connectBoth(String a, String b) {
        adjacencyList.get(a).add(new Edge(a, b, null));
        adjacencyList.get(b).add(new Edge(b, a, null));
    }

    public List<Edge> getNeighbors(String station) {
        return adjacencyList.getOrDefault(station, Collections.emptyList());
    }

    public Set<String> getAllStations() {
        return adjacencyList.keySet();
    }
}
