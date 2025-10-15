package org.unibl.etf.pj2.transport.util;

import org.unibl.etf.pj2.transport.generator.TransportDataGenerator;
import java.util.*;

/**
 * SimpleRouteFinder - pronalazi optimalnu kombinovanu rutu (BUS + TRAIN)
 * između dva grada koristeći Dijkstrin algoritam.
 */
public class SimpleRouteFinder {

    public enum Criteria { CHEAPEST, FASTEST, MIN_TRANSFER }

    public static class RouteStep {
        public String type;
        public String from;
        public String to;
        public String departureTime;
        public String arrivalTime;
        public int duration;
        public int price;
        public int minTransferTime;

        @Override
        public String toString() {
            return type + " " + from + " → " + to +
                    " | polazak: " + departureTime +
                    " | dolazak: " + arrivalTime +
                    " | trajanje: " + duration + " min" +
                    " | cijena: " + price;
        }
    }

    // ========================= PODACI =========================
    private final List<TransportDataGenerator.Station> stations;
    private final List<TransportDataGenerator.Departure> departures;

    private final Map<String, List<Edge>> graph = new HashMap<>();
    private final Map<String, String> cityToBus = new HashMap<>();
    private final Map<String, String> cityToTrain = new HashMap<>();

    public SimpleRouteFinder(List<TransportDataGenerator.Station> stations,
                             List<TransportDataGenerator.Departure> departures) {
        this.stations = stations;
        this.departures = departures;
        buildStationMaps();
        buildGraph();
        addTransferEdges(); // važno — omogućava prelaz između BUS ↔ TRAIN
    }

    // ========================= UNUTRAŠNJE KLASE =========================
    private static class Edge {
        String to;
        TransportDataGenerator.Departure departure;
        int extraCost; // koristi se za transfer
        Edge(String to, TransportDataGenerator.Departure departure, int extraCost) {
            this.to = to;
            this.departure = departure;
            this.extraCost = extraCost;
        }
    }

    private static class Node {
        String station;
        Node previous;
        TransportDataGenerator.Departure departure;
        int cost;
        Node(String station, Node previous, TransportDataGenerator.Departure departure, int cost) {
            this.station = station;
            this.previous = previous;
            this.departure = departure;
            this.cost = cost;
        }
    }

    // ========================= GRAFIČKE STRUKTURE =========================
    private void buildStationMaps() {
        for (TransportDataGenerator.Station s : stations) {
            cityToBus.put(s.city, s.busStation);
            cityToTrain.put(s.city, s.trainStation);
        }
    }

    private void buildGraph() {
        int added = 0;
        for (TransportDataGenerator.Departure d : departures) {
            String from = d.from;
            String to = d.to;

            // ako destinacija je grad (G_*), mapiraj u odgovarajuću stanicu
            if (to.startsWith("G_")) {
                String mapped = null;
                if ("autobus".equalsIgnoreCase(d.type) || "bus".equalsIgnoreCase(d.type)) {
                    mapped = cityToBus.get(to);
                } else if ("voz".equalsIgnoreCase(d.type) || "train".equalsIgnoreCase(d.type)) {
                    mapped = cityToTrain.get(to);
                }
                if (mapped != null) {
                    to = mapped;
                }
            }

            if (from != null && to != null) {
                graph.computeIfAbsent(from, k -> new ArrayList<>())
                        .add(new Edge(to, d, 0));
                added++;
            }
        }
        System.out.println("[DEBUG] Kreiran graf sa " + graph.size() + " čvorova i " + added + " veza.");
    }

    /**
     * Dodaje veze između autobuske i željezničke stanice istog grada
     * sa cijenom i trajanjem transfera izračunatim iz minTransferTime.
     */
    private void addTransferEdges() {
        int added = 0;
        final int DEFAULT_TRANSFER_TIME = 10; // koristi 10 min ako station nema minTransferTime

        for (TransportDataGenerator.Station s : stations) {
            String bus = s.busStation;
            String train = s.trainStation;

            if (bus != null && train != null) {
                TransportDataGenerator.Departure transfer = new TransportDataGenerator.Departure(
                        "TRANSFER", bus, train, "", "", DEFAULT_TRANSFER_TIME, 0, DEFAULT_TRANSFER_TIME);
                graph.computeIfAbsent(bus, k -> new ArrayList<>())
                        .add(new Edge(train, transfer, DEFAULT_TRANSFER_TIME));
                graph.computeIfAbsent(train, k -> new ArrayList<>())
                        .add(new Edge(bus, transfer, DEFAULT_TRANSFER_TIME));
                added += 2;
            }
        }

        System.out.println("[DEBUG] Dodano " + added + " transfer veza između stanica istog grada.");
    }


    // ========================= DIJKSTRA =========================
    public List<RouteStep> findRoute(String startCity, String endCity, Criteria criteria) {
        String startBus = cityToBus.get(startCity);
        String startTrain = cityToTrain.get(startCity);
        String endBus = cityToBus.get(endCity);
        String endTrain = cityToTrain.get(endCity);

        if ((startBus == null && startTrain == null) || (endBus == null && endTrain == null)) {
            System.err.println("Nema stanica za: " + startCity + " ili " + endCity);
            return Collections.emptyList();
        }

        System.out.println("[DEBUG] Traženje rute od " + startCity + " do " + endCity);
        System.out.println("[DEBUG] Broj čvorova u grafu: " + graph.size());

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));
        Map<String, Node> visited = new HashMap<>();

        if (startBus != null) pq.add(new Node(startBus, null, null, 0));
        if (startTrain != null) pq.add(new Node(startTrain, null, null, 0));

        Set<String> endStations = new HashSet<>();
        if (endBus != null) endStations.add(endBus);
        if (endTrain != null) endStations.add(endTrain);

        Node finalNode = null;
        int iter = 0;

        while (!pq.isEmpty()) {
            iter++;
            Node current = pq.poll();
            if (visited.containsKey(current.station)) continue;
            visited.put(current.station, current);

            System.out.println("[" + iter + "] Obrada čvora: " + current.station + " | cost=" + current.cost);

            if (endStations.contains(current.station)) {
                finalNode = current;
                System.out.println("[DEBUG] Stigli do krajnje stanice!");
                break;
            }

            List<Edge> edges = graph.getOrDefault(current.station, Collections.emptyList());
            if (edges.isEmpty()) continue;

            for (Edge e : edges) {
                if (e.to == null) continue;
                int cost = current.cost;

                if (e.departure != null && !"TRANSFER".equalsIgnoreCase(e.departure.type)) {
                    switch (criteria) {
                        case CHEAPEST:
                            cost += e.departure.price;
                            break;
                        case FASTEST:
                            cost += e.departure.duration;
                            break;
                        case MIN_TRANSFER:
                            cost += e.departure.minTransferTime;
                            break;
                    }
                } else {
                    // TRANSFER – penalizuj minimalnim vremenom čekanja
                    cost += e.extraCost;
                }

                pq.add(new Node(e.to, current, e.departure, cost));
            }
        }

        if (finalNode == null) {
            System.out.println("[DEBUG] Nije pronađena ruta između " + startCity + " i " + endCity);
            System.out.println("[DEBUG] Posjećeni čvorovi: " + visited.keySet());
            return Collections.emptyList();
        }

        // ================== REKONSTRUKCIJA ==================
        System.out.println("[DEBUG] Rekonstrukcija rute...");
        List<RouteStep> route = new ArrayList<>();
        Node node = finalNode;
        while (node != null && node.departure != null) {
            if (!"TRANSFER".equalsIgnoreCase(node.departure.type)) {
                RouteStep step = new RouteStep();
                step.type = node.departure.type;
                step.from = node.departure.from;
                step.to = node.departure.to;
                step.departureTime = node.departure.departureTime;
                step.arrivalTime = node.departure.arrivalTime;
                step.duration = node.departure.duration;
                step.price = node.departure.price;
                step.minTransferTime = node.departure.minTransferTime;
                route.add(step);
            }
            node = node.previous;
        }
        Collections.reverse(route);

        System.out.println("[DEBUG] Ruta pronađena (" + route.size() + " koraka):");
        for (RouteStep r : route) System.out.println("    " + r);

        return route;
    }
}
