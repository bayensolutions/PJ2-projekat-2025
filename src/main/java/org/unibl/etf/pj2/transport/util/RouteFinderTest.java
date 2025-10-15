package org.unibl.etf.pj2.transport.util;

import org.unibl.etf.pj2.transport.generator.TransportDataGenerator;
import java.util.List;

public class RouteFinderTest {
    public static void main(String[] args) {
        // 1️⃣ Učitaj JSON fajl sa putanjama
        String filePath = "src/main/resources/files/transport_data.json";
        TransportDataGenerator.TransportData data = TransportDataLoader.load(filePath);

        if (data == null) {
            System.err.println("Greška: nije učitan JSON fajl!");
            return;
        }

        System.out.println("✅ JSON uspješno učitan: " + data.departures.size() + " polazaka");

        // 2️⃣ Inicijalizuj finder (koristimo samo BUS linije)
        SimpleRouteFinder finder = new SimpleRouteFinder(data.stations, data.departures);

        // 3️⃣ Definiši start i cilj (gradove)
        String start = "G_0_0";
        String end = "G_3_2";

        // 4️⃣ Pokreni pretragu po kriterijumu
        List<SimpleRouteFinder.RouteStep> ruta =
                finder.findRoute(start, end, SimpleRouteFinder.Criteria.CHEAPEST);

        // 5️⃣ Prikaz rezultata
        if (ruta.isEmpty()) {
            System.out.println("❌ Nije pronađena nijedna BUS ruta između " + start + " i " + end);
        } else {
            System.out.println("✅ Pronađena ruta (" + ruta.size() + " koraka):");
            for (SimpleRouteFinder.RouteStep korak : ruta) {
                System.out.println("  " + korak);
            }
        }
    }
}
