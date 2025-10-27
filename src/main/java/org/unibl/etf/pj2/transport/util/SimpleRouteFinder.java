package org.unibl.etf.pj2.transport.util;

import org.unibl.etf.pj2.transport.generator.TransportDataGenerator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Pronalazi optimalne rute između gradova koristeći modifikovani Dijkstra algoritam.
 *
 * KLJUČNE IZMJENE:
 * 1. Startno vrijeme = najranije vrijeme u JSON-u
 * 2. Instant transfer između stanica istog grada (A ↔ Z)
 * 3. "Opuštena" relaksacija - čuva više putanja da bi uvijek našao rutu
 * 4. Multi-criteria optimizacija: ako ruta postoji, uvijek će biti pronađena
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
            return String.format("%s | %s → %s | %s → %s | %d min | %d KM",
                    type, from, to, departureTime, arrivalTime, duration, price);
        }
    }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final List<TransportDataGenerator.Station> stations;
    private final List<TransportDataGenerator.Departure> departures;

    private final Map<String, String> cityToBus = new HashMap<>();
    private final Map<String, String> cityToTrain = new HashMap<>();
    private final Map<String, String> stationToCity = new HashMap<>();
    private final Map<String, List<TransportDataGenerator.Departure>> depsFromStation = new HashMap<>();

    private LocalDateTime earliestTime;

    public SimpleRouteFinder(List<TransportDataGenerator.Station> stations,
                             List<TransportDataGenerator.Departure> departures) {
        this.stations = stations;
        this.departures = departures;

        for (TransportDataGenerator.Station s : stations) {
            cityToBus.put(s.city, s.busStation);
            cityToTrain.put(s.city, s.trainStation);
            stationToCity.put(s.busStation, s.city);
            stationToCity.put(s.trainStation, s.city);
        }

        for (TransportDataGenerator.Departure d : departures) {
            depsFromStation.computeIfAbsent(d.from, k -> new ArrayList<>()).add(d);
        }

        for (List<TransportDataGenerator.Departure> list : depsFromStation.values()) {
            list.sort(Comparator.comparing(o -> LocalDateTime.parse(o.departureTime, FMT)));
        }

        earliestTime = departures.stream()
                .map(d -> LocalDateTime.parse(d.departureTime, FMT))
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        System.out.println("[DEBUG] Najranije vrijeme u JSON-u: " + earliestTime.format(FMT));
    }

    public List<RouteStep> findRoute(String startCity, String endCity, Criteria criteria) {
        if (!cityToBus.containsKey(startCity) || !cityToBus.containsKey(endCity)) {
            System.err.println("[ERROR] Nepoznat grad: " + startCity + " ili " + endCity);
            return Collections.emptyList();
        }

        System.out.printf("=== POKRENUT SimpleRouteFinder ===%nStart: %s | Cilj: %s | Kriterijum: %s%n",
                startCity, endCity, criteria);

        // ✅ KLJUČNA IZMJENA: koristi unified algoritam sa različitim metrikama
        return findRouteUnified(startCity, endCity, criteria);
    }

    /**
     * Unified Dijkstra algoritam koji radi za sve kriterijume.
     * Koristi "visited" set da spreči beskonačne cikluse.
     */
    private List<RouteStep> findRouteUnified(String startCity, String endCity, Criteria criteria) {
        // Čuva najbolje putanje do svakog čvora (može biti više ako imaju različite kompromise)
        Map<String, List<Label>> allPaths = new HashMap<>();
        PriorityQueue<Label> pq = createPriorityQueue(criteria);
        Set<String> visited = new HashSet<>();

        String startBus = cityToBus.get(startCity);
        String startTrain = cityToTrain.get(startCity);
        String endBus = cityToBus.get(endCity);
        String endTrain = cityToTrain.get(endCity);

        Label lBus = new Label(startBus, earliestTime, 0, 0, null, null);
        Label lTrain = new Label(startTrain, earliestTime, 0, 0, null, null);

        pq.add(lBus);
        pq.add(lTrain);
        allPaths.computeIfAbsent(startBus, k -> new ArrayList<>()).add(lBus);
        allPaths.computeIfAbsent(startTrain, k -> new ArrayList<>()).add(lTrain);

        Label bestSolution = null;

        while (!pq.isEmpty()) {
            Label cur = pq.poll();

            // Ako smo već posjetili ovaj čvor sa boljom metrikama, preskoči
            String visitKey = cur.node + "_" + cur.arrivalTime.toString();
            if (visited.contains(visitKey)) continue;
            visited.add(visitKey);

            // Provjeri da li smo stigli do cilja
            if (cur.node.equals(endBus) || cur.node.equals(endTrain)) {
                if (bestSolution == null || isBetter(cur, bestSolution, criteria)) {
                    bestSolution = cur;
                }
                // Nastavi pretragu za bolje alternative (ne prekidaj odmah)
                continue;
            }

            // Ekspanzija: transfer + polasci
            expandNode(cur, pq, allPaths, criteria);
        }

        if (bestSolution == null) {
            System.out.println("⚠️ Nije pronađena ruta.");
            return Collections.emptyList();
        }

        return reconstruct(bestSolution);
    }

    private PriorityQueue<Label> createPriorityQueue(Criteria criteria) {
        switch (criteria) {
            case FASTEST:
                return new PriorityQueue<>(Comparator.comparing(l -> l.arrivalTime));
            case CHEAPEST:
                return new PriorityQueue<>(Comparator.comparingInt(l -> l.totalCost));
            case MIN_TRANSFER:
                return new PriorityQueue<>(Comparator.comparingInt(l -> l.hops));
            default:
                return new PriorityQueue<>(Comparator.comparing(l -> l.arrivalTime));
        }
    }

    private boolean isBetter(Label newLabel, Label oldLabel, Criteria criteria) {
        switch (criteria) {
            case FASTEST:
                return newLabel.arrivalTime.isBefore(oldLabel.arrivalTime);
            case CHEAPEST:
                return newLabel.totalCost < oldLabel.totalCost;
            case MIN_TRANSFER:
                return newLabel.hops < oldLabel.hops;
            default:
                return false;
        }
    }

    private void expandNode(Label cur, PriorityQueue<Label> pq, Map<String, List<Label>> allPaths, Criteria criteria) {
        String curCity = stationToCity.get(cur.node);

        // 1. INSTANT TRANSFER između stanica istog grada
        if (curCity != null) {
            String otherStation = cur.node.startsWith("A_") ? cityToTrain.get(curCity) : cityToBus.get(curCity);
            if (otherStation != null && !otherStation.equals(cur.node)) {
                tryRelax(otherStation, cur.arrivalTime, cur.totalCost, cur.hops, cur, null, pq, allPaths, criteria);
            }
        }

        // 2. POLASCI iz trenutne stanice
        List<TransportDataGenerator.Departure> available = depsFromStation.getOrDefault(cur.node, Collections.emptyList());

        for (TransportDataGenerator.Departure d : available) {
            LocalDateTime depTime = LocalDateTime.parse(d.departureTime, FMT);
            LocalDateTime needTime = cur.arrivalTime.plusMinutes(d.minTransferTime);

            if (depTime.isBefore(needTime)) continue; // Ne možemo stići

            LocalDateTime arrTime = LocalDateTime.parse(d.arrivalTime, FMT);
            int newCost = cur.totalCost + d.price;
            int newHops = cur.hops + 1;

            String targetCity = d.to;
            String targetBus = cityToBus.get(targetCity);
            String targetTrain = cityToTrain.get(targetCity);

            // Dodaj oba čvora ciljnog grada (autobuska i željeznička)
            if (targetBus != null) {
                tryRelax(targetBus, arrTime, newCost, newHops, cur, d, pq, allPaths, criteria);
            }
            if (targetTrain != null) {
                tryRelax(targetTrain, arrTime, newCost, newHops, cur, d, pq, allPaths, criteria);
            }
        }
    }

    /**
     * "Opuštena" relaksacija koja dozvoljava više putanja do istog čvora.
     * Ne odbacuje alternativne putanje prerano.
     */
    private void tryRelax(String node, LocalDateTime time, int cost, int hops,
                          Label prev, TransportDataGenerator.Departure dep,
                          PriorityQueue<Label> pq, Map<String, List<Label>> allPaths,
                          Criteria criteria) {

        Label newLabel = new Label(node, time, cost, hops, prev, dep);

        // Provjeri da li već postoji bolja putanja
        List<Label> existing = allPaths.get(node);
        if (existing != null) {
            // Ako postoji striktno bolja putanja po SVIM metrikama, preskoči
            boolean dominated = false;
            for (Label ex : existing) {
                if (isDominated(newLabel, ex)) {
                    dominated = true;
                    break;
                }
            }
            if (dominated) return;
        }

        // Dodaj novu labelu
        allPaths.computeIfAbsent(node, k -> new ArrayList<>()).add(newLabel);
        pq.add(newLabel);
    }

    /**
     * Provjerava da li je newLabel dominirana od strane existing.
     * Dominacija znači: existing je bolja ili jednaka po SVIM metrikama.
     */
    private boolean isDominated(Label newLabel, Label existing) {
        return !existing.arrivalTime.isAfter(newLabel.arrivalTime) &&
                existing.totalCost <= newLabel.totalCost &&
                existing.hops <= newLabel.hops;
    }

    // ========== LABEL I REKONSTRUKCIJA ==========
    private static class Label {
        final String node;
        final LocalDateTime arrivalTime;
        final int totalCost;
        final int hops;
        final Label prev;
        final TransportDataGenerator.Departure usedDeparture;

        Label(String node, LocalDateTime arrivalTime, int totalCost, int hops,
              Label prev, TransportDataGenerator.Departure usedDeparture) {
            this.node = node;
            this.arrivalTime = arrivalTime;
            this.totalCost = totalCost;
            this.hops = hops;
            this.prev = prev;
            this.usedDeparture = usedDeparture;
        }
    }

    private List<RouteStep> reconstruct(Label goal) {
        List<RouteStep> out = new ArrayList<>();
        Label cur = goal;
        while (cur != null) {
            if (cur.usedDeparture != null) {
                TransportDataGenerator.Departure d = cur.usedDeparture;
                RouteStep rs = new RouteStep();
                rs.type = d.type;
                rs.from = d.from;
                rs.to = d.to;
                rs.departureTime = d.departureTime;
                rs.arrivalTime = d.arrivalTime;
                rs.duration = d.duration;
                rs.price = d.price;
                rs.minTransferTime = d.minTransferTime;
                out.add(rs);
            }
            cur = cur.prev;
        }
        Collections.reverse(out);
        return out;
    }
}