package org.unibl.etf.pj2.transport.util;

import org.unibl.etf.pj2.transport.generator.TransportDataGenerator;
import java.util.List;

public class RouteFinderTest {

    public static void main(String[] args) {
        String filePath = "src/main/resources/files/transport_data.json"; // provjeri putanju

        TransportDataGenerator.TransportData data = TransportDataLoader.load(filePath);
        if (data == null) {
            System.err.println("❌ Greška: nije moguće učitati JSON.");
            return;
        }

        System.out.println("✅ JSON uspješno učitan!");
        System.out.println("Broj gradova: " + data.stations.size());
        System.out.println("Broj polazaka: " + data.departures.size());

        TransportGraph graph = new TransportGraph(data.stations, data.departures);

        SimpleRouteFinder finder = new SimpleRouteFinder(data.stations, data.departures);

        // Test 1: susjedni gradovi
        testRoute(finder, "G_0_0", "G_0_1");

        // Test 2: udaljeniji gradovi
        testRoute(finder, "G_0_0", "G_3_3");

        // Test 3: vertikalni skok
        testRoute(finder, "G_0_2", "G_4_2");
    }

    private static void testRoute(SimpleRouteFinder finder, String from, String to) {
        System.out.println("\n=== TEST: " + from + " → " + to + " ===");
        List<SimpleRouteFinder.RouteStep> route = finder.findRoute(from, to, SimpleRouteFinder.Criteria.CHEAPEST);

        if (route.isEmpty()) {
            System.out.println("⚠️ Nije pronađena nijedna ruta!");
        } else {
            System.out.println("✅ Ruta pronađena! Segmenti:");
            for (SimpleRouteFinder.RouteStep s : route)
                System.out.println("  " + s);
        }
    }
}
