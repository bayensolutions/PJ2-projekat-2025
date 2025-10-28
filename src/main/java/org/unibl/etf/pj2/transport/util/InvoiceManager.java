package org.unibl.etf.pj2.transport.util;

import org.unibl.etf.pj2.transport.Config;
import org.unibl.etf.pj2.transport.util.SimpleRouteFinder.RouteStep;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InvoiceManager {

    public static void generateInvoice(List<RouteStep> route) throws IOException {
        Path dir = Path.of(Config.getInvoicesDir());
        if (!Files.exists(dir)) Files.createDirectory(dir);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        Path file = dir.resolve("racun_" + timestamp + ".txt");

        try (BufferedWriter w = Files.newBufferedWriter(file)) {
            RouteStep first = route.get(0);
            RouteStep last = route.get(route.size() - 1);

            w.write("Relacija: " + first.from + " → " + last.to); w.newLine();
            w.write("Polazak: " + first.departureTime); w.newLine();
            w.write("Dolazak: " + last.arrivalTime); w.newLine();
            w.write("Trajanje: " + totalDuration(route)); w.newLine();
            w.write("Ukupna cijena: " + totalPrice(route) + " KM"); w.newLine();
            w.write("Broj presjedanja: " + (route.size() - 1)); w.newLine();
            w.write("Datum kupovine: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
    }

    private static int totalPrice(List<RouteStep> route) {
        return route.stream().mapToInt(r -> r.price).sum();
    }

    private static String totalDuration(List<RouteStep> route) {
        int total = route.stream().mapToInt(r -> r.duration).sum();
        return (total / 60) + "h " + (total % 60) + "min";
    }
}
