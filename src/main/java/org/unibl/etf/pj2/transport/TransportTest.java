package org.unibl.etf.pj2.transport;

import org.unibl.etf.pj2.transport.generator.TransportDataGenerator;
import org.unibl.etf.pj2.transport.util.TransportDataLoader;

public class TransportTest {
    public static void main(String[] args) {
        int rows = Config.getRows();
        int cols = Config.getCols();

        System.out.println("Generišem mapu dimenzija: " + rows + "x" + cols);

        // TODO: Ukloniti hardkodovanu putanju
        TransportDataGenerator.TransportData data = TransportDataLoader.load("C:\\Users\\Korisnik\\Desktop\\PJ2-PROJEKAT\\src\\main\\resources\\files\\transport_data.json");

        if (data == null) {
            System.out.println("Podaci nisu generisani!");
            return;
        }

        System.out.println("\n=== MAPA DRŽAVE ===");
        for (String[] row : data.countryMap) {
            for (String city : row) {
                System.out.print(city + "\t");
            }
            System.out.println();
        }

        System.out.println("\n=== STANICE ===");
        for (TransportDataGenerator.Station station : data.stations) {
            System.out.println("Grad: " + station.city +
                    " | Autobus: " + station.busStation +
                    " | Voz: " + station.trainStation);
        }

        System.out.println("\n=== POLASCI I DOLASCI PO GRADOVIMA ===");
        for (TransportDataGenerator.Station station : data.stations) {
            String grad = station.city;
            System.out.println("\n--- " + grad + " ---");

            long brojPolazaka = data.departures.stream()
                    .filter(d -> d.from.equals(station.busStation) || d.from.equals(station.trainStation))
                    .peek(d -> System.out.println("Polazak: " + d.type + " | " + d.from + " → " + d.to +
                            " | Polazak: " + d.departureTime +
                            " | Dolazak: " + d.arrivalTime +
                            " | Trajanje: " + d.duration + " min" +
                            " | Cena: " + d.price))
                    .count();

            long brojDolasaka = data.departures.stream()
                    .filter(d -> d.to.contains(grad))
                    .peek(d -> System.out.println("Dolazak: " + d.type + " | " + d.from + " → " + d.to +
                            " | Polazak: " + d.departureTime +
                            " | Dolazak: " + d.arrivalTime +
                            " | Trajanje: " + d.duration + " min" +
                            " | Cena: " + d.price))
                    .count();

            System.out.println("Ukupno: " + brojPolazaka + " polazaka, " + brojDolasaka + " dolazaka");
        }
    }
}
