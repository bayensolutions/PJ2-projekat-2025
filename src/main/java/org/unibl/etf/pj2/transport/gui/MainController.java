package org.unibl.etf.pj2.transport.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import org.unibl.etf.pj2.transport.generator.TransportDataGenerator;
import org.unibl.etf.pj2.transport.util.SimpleRouteFinder;
import org.unibl.etf.pj2.transport.util.TransportDataLoader;
import org.unibl.etf.pj2.transport.util.TransportGraph;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private Canvas mapCanvas;
    @FXML private ComboBox<String> comboStart;
    @FXML private ComboBox<String> comboDestination;
    @FXML private ComboBox<String> comboCriteria;
    @FXML private TableView<SimpleRouteFinder.RouteStep> routeTable;
    @FXML private TableColumn<SimpleRouteFinder.RouteStep, String> colTransport;
    @FXML private TableColumn<SimpleRouteFinder.RouteStep, String> colStart;
    @FXML private TableColumn<SimpleRouteFinder.RouteStep, String> colDestination;
    @FXML private TableColumn<SimpleRouteFinder.RouteStep, String> colTime;
    @FXML private TableColumn<SimpleRouteFinder.RouteStep, Integer> colPrice;

    private TransportDataGenerator.TransportData transportData;
    private SimpleRouteFinder.Criteria selectedCriteria = SimpleRouteFinder.Criteria.CHEAPEST;
    private String selectedStartCity = null;
    private String selectedDestinationCity = null;

    private TransportGraph graph; // ➕ Dodato polje grafa

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            String filePath = getClass().getResource("/files/transport_data.json").getPath();
            transportData = TransportDataLoader.load(filePath);

            if (transportData == null || transportData.stations == null || transportData.departures == null) {
                showAlert("Greška", "Ne mogu učitati podatke iz transport_data.json");
                return;
            }

            System.out.println("✅ Učitani podaci iz JSON-a:");
            System.out.println("Broj gradova: " + transportData.stations.size());
            System.out.println("Broj polazaka: " + transportData.departures.size());

            // ➕ Kreiraj graf odmah nakon učitavanja
            graph = new TransportGraph(transportData.stations, transportData.departures);

            ObservableList<String> cities = FXCollections.observableArrayList();
            for (TransportDataGenerator.Station s : transportData.stations)
                if (s != null && s.city != null && !cities.contains(s.city))
                    cities.add(s.city);

            comboStart.setItems(cities);
            comboDestination.setItems(cities);

            comboCriteria.setItems(FXCollections.observableArrayList(
                    "Najjeftinije", "Najbrže", "Najmanje presjedanja"
            ));

            comboCriteria.setOnAction(e -> {
                String val = comboCriteria.getValue();
                if (val == null) return;
                if (val.equals("Najjeftinije")) selectedCriteria = SimpleRouteFinder.Criteria.CHEAPEST;
                else if (val.equals("Najbrže")) selectedCriteria = SimpleRouteFinder.Criteria.FASTEST;
                else selectedCriteria = SimpleRouteFinder.Criteria.MIN_TRANSFER;
            });

            comboStart.setOnAction(e -> {
                selectedStartCity = comboStart.getValue();
                redrawMapWithHighlights();
            });

            comboDestination.setOnAction(e -> {
                selectedDestinationCity = comboDestination.getValue();
                redrawMapWithHighlights();
            });

            colTransport.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().type));
            colStart.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().from));
            colDestination.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().to));
            colTime.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                    c.getValue().departureTime + " - " + c.getValue().arrivalTime));
            colPrice.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().price).asObject());

            if (transportData.countryMap != null)
                drawMap(transportData.countryMap.length, transportData.countryMap[0].length, transportData.countryMap);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Greška", "Greška prilikom učitavanja JSON fajla.");
        }
    }

    @FXML
    private void findRoute() {
        if (selectedStartCity == null || selectedDestinationCity == null) {
            showAlert("Greška", "Morate odabrati početni i krajnji grad.");
            return;
        }

        System.out.println("=== POZIV findRoute ===");
        System.out.println("Start: " + selectedStartCity);
        System.out.println("Cilj: " + selectedDestinationCity);
        System.out.println("Kriterijum: " + selectedCriteria);

        // ✅ Novi konstruktor (sa grafom)
        SimpleRouteFinder finder = new SimpleRouteFinder(
                transportData.stations,
                transportData.departures
        );

        List<SimpleRouteFinder.RouteStep> route = finder.findRoute(selectedStartCity, selectedDestinationCity, selectedCriteria);

        if (route.isEmpty()) {
            System.out.println("⚠️ Nije pronađena ruta.");
            showAlert("Nema rute", "Nije pronađena dostupna ruta između odabranih gradova.");
            routeTable.getItems().clear();
            redrawMapWithHighlights();
            return;
        }

        System.out.println("✅ PRONAĐENA RUTA:");
        for (var step : route)
            System.out.println(step);

        ObservableList<SimpleRouteFinder.RouteStep> tableData = FXCollections.observableArrayList(route);
        routeTable.setItems(tableData);
        redrawMapWithHighlights(route);
    }

    private void drawMap(int rows, int cols, String[][] countryMap) {
        if (countryMap == null) return;
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());

        double cellWidth = mapCanvas.getWidth() / cols;
        double cellHeight = mapCanvas.getHeight() / rows;
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double x = j * cellWidth;
                double y = i * cellHeight;
                gc.strokeRect(x, y, cellWidth, cellHeight);
                String city = countryMap[i][j];
                if (city != null) {
                    gc.setFill(Color.BLACK);
                    gc.fillText(city, x + cellWidth / 4, y + cellHeight / 2);
                }
            }
        }
    }

    private void redrawMapWithHighlights() { redrawMapWithHighlights(null); }

    private void redrawMapWithHighlights(List<SimpleRouteFinder.RouteStep> route) {
        if (transportData == null || transportData.countryMap == null) return;

        int rows = transportData.countryMap.length;
        int cols = transportData.countryMap[0].length;
        drawMap(rows, cols, transportData.countryMap);

        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        double cellWidth = mapCanvas.getWidth() / cols;
        double cellHeight = mapCanvas.getHeight() / rows;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                String city = transportData.countryMap[i][j];
                if (city == null) continue;

                if (city.equals(selectedStartCity)) {
                    gc.setFill(Color.LIGHTBLUE);
                    gc.fillRect(j * cellWidth, i * cellHeight, cellWidth, cellHeight);
                    gc.setFill(Color.BLACK);
                    gc.fillText(city, j * cellWidth + cellWidth / 4, i * cellHeight + cellHeight / 2);
                } else if (city.equals(selectedDestinationCity)) {
                    gc.setFill(Color.PINK);
                    gc.fillRect(j * cellWidth, i * cellHeight, cellWidth, cellHeight);
                    gc.setFill(Color.BLACK);
                    gc.fillText(city, j * cellWidth + cellWidth / 4, i * cellHeight + cellHeight / 2);
                }
            }
        }

        if (route != null && !route.isEmpty()) {
            gc.setStroke(Color.GREEN);
            gc.setLineWidth(3);

            String prevCity = getCityNameFromStation(route.get(0).from);
            for (SimpleRouteFinder.RouteStep step : route) {
                String nextCity = getCityNameFromStation(step.to);
                if (prevCity == null || nextCity == null) continue;

                int[] prevCoords = findCityCoords(prevCity);
                int[] nextCoords = findCityCoords(nextCity);
                if (prevCoords != null && nextCoords != null) {
                    double x1 = prevCoords[1] * cellWidth + cellWidth / 2;
                    double y1 = prevCoords[0] * cellHeight + cellHeight / 2;
                    double x2 = nextCoords[1] * cellWidth + cellWidth / 2;
                    double y2 = nextCoords[0] * cellHeight + cellHeight / 2;
                    gc.strokeLine(x1, y1, x2, y2);
                }
                prevCity = nextCity;
            }
        }
    }

    private int[] findCityCoords(String city) {
        for (int i = 0; i < transportData.countryMap.length; i++) {
            for (int j = 0; j < transportData.countryMap[i].length; j++) {
                if (city.equals(transportData.countryMap[i][j])) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    private String getCityNameFromStation(String stationCode) {
        for (var s : transportData.stations) {
            if (s.busStation.equals(stationCode) || s.trainStation.equals(stationCode))
                return s.city;
        }
        return null;
    }

    @FXML private void showAdditionalRoutes() {
        showAlert("Informacija", "Prikaz dodatnih ruta biće implementiran kasnije.");
    }

    @FXML private void buyTicket() {
        showAlert("Informacija", "Kupovina karte biće implementirana kasnije.");
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
