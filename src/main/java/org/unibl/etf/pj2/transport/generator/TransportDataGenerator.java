package org.unibl.etf.pj2.transport.generator;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TransportDataGenerator {

    private int rows;
    private int columns;
    private static final int DEPARTURES_PER_STATION = 5;
    private static final Random random = new Random();

    public TransportDataGenerator(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
    }

    // ------------------ STRUKTURE PODATAKA ------------------
    public static class TransportData {
        public String[][] countryMap;
        public List<Station> stations;
        public List<Departure> departures;
    }

    public static class Station {
        public String city;
        public String busStation;
        public String trainStation;

        public Station() {
        }

        public Station(String city, String busStation, String trainStation) {
            this.city = city;
            this.busStation = busStation;
            this.trainStation = trainStation;
        }
    }

    public static class Departure {
        public String type; // "autobus" ili "voz"
        public String from;
        public String to;
        public String departureTime;
        public String arrivalTime;
        public int duration; // u minutama
        public int price;
        public int minTransferTime; // u minutama

        public Departure() {
        }

        public Departure(String type, String from, String to, String departureTime, String arrivalTime, int duration, int price, int minTransferTime) {
            this.type = type;
            this.from = from;
            this.to = to;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
            this.duration = duration;
            this.price = price;
            this.minTransferTime = minTransferTime;
        }
    }

    // ------------------ GENERISANJE PODATAKA ------------------
    public TransportData generateData() {
        TransportData data = new TransportData();
        data.countryMap = generateCountryMap();
        data.stations = generateStations();
        data.departures = generateDepartures(data.stations);
        return data;
    }

    private String[][] generateCountryMap() {
        String[][] countryMap = new String[rows][columns];
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < columns; y++) {
                countryMap[x][y] = "G_" + x + "_" + y;
            }
        }
        return countryMap;
    }

    private List<Station> generateStations() {
        List<Station> stations = new ArrayList<>();
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < columns; y++) {
                String city = "G_" + x + "_" + y;
                String busStation = "A_" + x + "_" + y;
                String trainStation = "Z_" + x + "_" + y;
                Station station = new Station(city, busStation, trainStation);

                stations.add(station);
            }
        }
        return stations;
    }

    private List<Departure> generateDepartures(List<Station> stations) {
        List<Departure> departures = new ArrayList<>();
        for (Station station : stations) {
            int x = Integer.parseInt(station.city.split("_")[1]);
            int y = Integer.parseInt(station.city.split("_")[2]);

            for (int i = 0; i < DEPARTURES_PER_STATION; i++) {
                departures.add(generateDeparture("autobus", station.busStation, x, y));
            }
            for (int i = 0; i < DEPARTURES_PER_STATION; i++) {
                departures.add(generateDeparture("voz", station.trainStation, x, y));
            }
        }
        return departures;
    }

    private Departure generateDeparture(String type, String from, int x, int y) {
        Departure departure = new Departure();
        departure.type = type;
        departure.from = from;

        List<String> neighbors = getNeighbors(x, y);
        departure.to = neighbors.isEmpty() ? from : neighbors.get(random.nextInt(neighbors.size()));

        int hour = random.nextInt(24);
        int minute = random.nextInt(4) * 15;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime depTime = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        departure.departureTime = depTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        departure.duration = 30 + random.nextInt(151);
        LocalDateTime arrTime = depTime.plusMinutes(departure.duration);
        departure.arrivalTime = arrTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        departure.price = 100 + random.nextInt(901);
        departure.minTransferTime = 5 + random.nextInt(26);

        return departure;
    }

    private List<String> getNeighbors(int x, int y) {
        List<String> neighbors = new ArrayList<>();
        int[][] directions = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (nx >= 0 && nx < rows && ny >= 0 && ny < columns) {
                neighbors.add("G_" + nx + "_" + ny);
            }
        }
        return neighbors;
    }

    private void saveToJson(TransportData data, String filename) {
        try (FileWriter file = new FileWriter(filename)) {
            StringBuilder json = new StringBuilder();
            json.append("{\n");

            // Mapa drzave
            json.append("  \"countryMap\": [\n");
            for (int i = 0; i < rows; i++) {
                json.append("    [");
                for (int j = 0; j < columns; j++) {
                    json.append("\"").append(data.countryMap[i][j]).append("\"");
                    if (j < columns - 1) json.append(", ");
                }
                json.append("]");
                if (i < rows - 1) json.append(",");
                json.append("\n");
            }
            json.append("  ],\n");

            // Stanice
            json.append("  \"stations\": [\n");
            for (int i = 0; i < data.stations.size(); i++) {
                Station s = data.stations.get(i);
                json.append("    {\"city\": \"").append(s.city)
                        .append("\", \"busStation\": \"").append(s.busStation)
                        .append("\", \"trainStation\": \"").append(s.trainStation)
                        .append("\"}");
                if (i < data.stations.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("  ],\n");

            // Polasci
            json.append("  \"departures\": [\n");
            for (int i = 0; i < data.departures.size(); i++) {
                Departure d = data.departures.get(i);
                json.append("    {\"type\": \"").append(d.type)
                        .append("\", \"from\": \"").append(d.from)
                        .append("\", \"to\": \"").append(d.to)
                        .append("\", \"departureTime\": \"").append(d.departureTime)
                        .append("\", \"arrivalTime\": \"").append(d.arrivalTime)
                        .append("\", \"duration\": ").append(d.duration)
                        .append(", \"price\": ").append(d.price)
                        .append(", \"minTransferTime\": ").append(d.minTransferTime)
                        .append("}");
                if (i < data.departures.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("  ]\n");

            json.append("}");
            file.write(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ------------------ MAIN METODA ------------------
    public static void main(String[] args) {
        if(args.length < 2){
            System.out.println("Koristi: java TransportDataGenerator <rows> <columns>");
            return;
        }

        int rows = Integer.parseInt(args[0]);
        int columns = Integer.parseInt(args[1]);

        TransportDataGenerator generator = new TransportDataGenerator(rows, columns);
        TransportData data = generator.generateData();
        generator.saveToJson(data, "transport_data.json");
        System.out.println("Podaci su generisani i sačuvani u transport_data.json (" + rows + "x" + columns + ")");
    }
}
