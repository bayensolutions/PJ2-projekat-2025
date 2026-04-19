package org.unibl.etf.pj2.transport.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.unibl.etf.pj2.transport.generator.TransportDataGenerator;
import org.unibl.etf.pj2.transport.util.SimpleRouteFinder;

import java.net.URL;
import java.util.*;

/**
 * Kontroler za vizualizaciju transportnog grafa koristeći JavaFX Canvas.
 * Prikazuje gradove kao čvorove i veze između njih kao ivice.
 *
 * @author Student
 * @version 1.0
 */
public class GraphVisualizationController implements Initializable {

    @FXML private Canvas graphCanvas;
    @FXML private Button btnReset;
    @FXML private CheckBox chkShowLabels;
    @FXML private CheckBox chkShowBusStations;
    @FXML private CheckBox chkShowTrainStations;
    @FXML private Label lblInfo;

    private TransportDataGenerator.TransportData transportData;
    private List<SimpleRouteFinder.RouteStep> currentRoute;

    // Pozicije čvorova (city/station -> [x, y])
    private Map<String, double[]> nodePositions = new HashMap<>();

    // Zoom i pan kontrole
    private double scale = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    private double lastMouseX;
    private double lastMouseY;

    // Parametri crtanja
    private static final double NODE_RADIUS = 8;
    private static final double CITY_RADIUS = 12;
    private static final double STATION_SIZE = 10;
    private static final double SPACING = 100; // Razmak između gradova

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chkShowLabels.setSelected(true);
        chkShowBusStations.setSelected(true);
        chkShowTrainStations.setSelected(true);

        // Event handlers za zoom i pan
        graphCanvas.setOnScroll(this::handleScroll);
        graphCanvas.setOnMousePressed(this::handleMousePressed);
        graphCanvas.setOnMouseDragged(this::handleMouseDragged);

        // Redraw na promjenu opcija
        chkShowLabels.setOnAction(e -> drawGraph());
        chkShowBusStations.setOnAction(e -> drawGraph());
        chkShowTrainStations.setOnAction(e -> drawGraph());
    }

    /**
     * Postavlja transportne podatke i kreira graf.
     *
     * @param data Transportni podaci učitani iz JSON fajla
     */
    public void setTransportData(TransportDataGenerator.TransportData data) {
        this.transportData = data;
        calculateNodePositions();
        centerView();
        drawGraph();
        updateInfo();
    }

    /**
     * Postavlja rutu za prikaz na grafu.
     *
     * @param route Lista koraka rute
     */
    public void setRoute(List<SimpleRouteFinder.RouteStep> route) {
        this.currentRoute = route;
        drawGraph();
    }

    /**
     * Izračunava pozicije svih čvorova na osnovu mape države.
     */
    private void calculateNodePositions() {
        nodePositions.clear();

        int rows = transportData.countryMap.length;
        int cols = transportData.countryMap[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                String city = transportData.countryMap[i][j];

                // Pozicija grada (centrirana)
                double x = j * SPACING + SPACING;
                double y = i * SPACING + SPACING;
                nodePositions.put(city, new double[]{x, y});

                // Pozicije stanica oko grada
                for (TransportDataGenerator.Station s : transportData.stations) {
                    if (s.city.equals(city)) {
                        // Autobuska stanica (lijevo od grada)
                        nodePositions.put(s.busStation, new double[]{x - 25, y});

                        // Željeznička stanica (desno od grada)
                        nodePositions.put(s.trainStation, new double[]{x + 25, y});
                        break;
                    }
                }
            }
        }
    }

    /**
     * Centrira prikaz na sredini grafa.
     */
    private void centerView() {
        int rows = transportData.countryMap.length;
        int cols = transportData.countryMap[0].length;

        double graphWidth = cols * SPACING;
        double graphHeight = rows * SPACING;

        offsetX = (graphCanvas.getWidth() - graphWidth * scale) / 2;
        offsetY = (graphCanvas.getHeight() - graphHeight * scale) / 2;
    }

    /**
     * Crta kompletan graf sa svim elementima.
     */
    private void drawGraph() {
        GraphicsContext gc = graphCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, graphCanvas.getWidth(), graphCanvas.getHeight());

        // Pozadina
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, graphCanvas.getWidth(), graphCanvas.getHeight());

        // Primjeni transformaciju
        gc.save();
        gc.translate(offsetX, offsetY);
        gc.scale(scale, scale);

        // Crta sve ivice
        drawAllEdges(gc);

        // Crta rutu (ako postoji)
        if (currentRoute != null && !currentRoute.isEmpty()) {
            drawRoute(gc);
        }

        // Crta čvorove
        drawAllNodes(gc);

        gc.restore();
    }

    /**
     * Crta sve ivice između stanica.
     *
     * @param gc Graphics context za crtanje
     */
    private void drawAllEdges(GraphicsContext gc) {
        Set<String> drawnEdges = new HashSet<>();

        gc.setLineWidth(1.5);

        for (TransportDataGenerator.Departure dep : transportData.departures) {
            String edgeKey = dep.from + "-" + dep.to;
            if (drawnEdges.contains(edgeKey)) continue;
            drawnEdges.add(edgeKey);

            double[] from = nodePositions.get(dep.from);
            double[] to = nodePositions.get(dep.to);

            if (from == null || to == null) continue;

            // Boja prema tipu prevoza
            if (dep.type.equals("autobus")) {
                gc.setStroke(Color.rgb(255, 179, 71, 0.4)); // Narančasta
            } else {
                gc.setStroke(Color.rgb(177, 156, 217, 0.4)); // Ljubičasta
            }

            drawArrow(gc, from[0], from[1], to[0], to[1]);
        }

        // Veze grad-stanica (tanje linije)
        gc.setStroke(Color.rgb(200, 200, 200, 0.5));
        gc.setLineWidth(1);

        for (TransportDataGenerator.Station station : transportData.stations) {
            double[] cityPos = nodePositions.get(station.city);

            if (chkShowBusStations.isSelected()) {
                double[] busPos = nodePositions.get(station.busStation);
                if (cityPos != null && busPos != null) {
                    gc.strokeLine(cityPos[0], cityPos[1], busPos[0], busPos[1]);
                }
            }

            if (chkShowTrainStations.isSelected()) {
                double[] trainPos = nodePositions.get(station.trainStation);
                if (cityPos != null && trainPos != null) {
                    gc.strokeLine(cityPos[0], cityPos[1], trainPos[0], trainPos[1]);
                }
            }
        }
    }

    /**
     * Crta optimalnu rutu crvenom bojom.
     *
     * @param gc Graphics context za crtanje
     */
    private void drawRoute(GraphicsContext gc) {
        gc.setStroke(Color.RED);
        gc.setLineWidth(3);

        for (SimpleRouteFinder.RouteStep step : currentRoute) {
            double[] from = nodePositions.get(step.from);
            double[] to = nodePositions.get(step.to);

            if (from != null && to != null) {
                drawArrow(gc, from[0], from[1], to[0], to[1]);
            }
        }
    }

    /**
     * Crta sve čvorove (gradove i stanice).
     *
     * @param gc Graphics context za crtanje
     */
    private void drawAllNodes(GraphicsContext gc) {
        // Provjeri početni i krajnji grad rute
        String startCity = null;
        String endCity = null;
        Set<String> routeStations = new HashSet<>();

        if (currentRoute != null && !currentRoute.isEmpty()) {
            startCity = getCityFromStation(currentRoute.get(0).from);
            endCity = getCityFromStation(currentRoute.get(currentRoute.size() - 1).to);

            for (SimpleRouteFinder.RouteStep step : currentRoute) {
                routeStations.add(step.from);
                routeStations.add(step.to);
            }
        }

        // Crta gradove
        for (TransportDataGenerator.Station station : transportData.stations) {
            double[] pos = nodePositions.get(station.city);
            if (pos == null) continue;

            // Boja grada
            if (station.city.equals(startCity)) {
                gc.setFill(Color.LIGHTGREEN);
                gc.setStroke(Color.DARKGREEN);
            } else if (station.city.equals(endCity)) {
                gc.setFill(Color.LIGHTCORAL);
                gc.setStroke(Color.DARKRED);
            } else {
                gc.setFill(Color.rgb(80, 200, 120));
                gc.setStroke(Color.rgb(46, 92, 138));
            }

            gc.setLineWidth(2);
            gc.fillOval(pos[0] - CITY_RADIUS, pos[1] - CITY_RADIUS, CITY_RADIUS * 2, CITY_RADIUS * 2);
            gc.strokeOval(pos[0] - CITY_RADIUS, pos[1] - CITY_RADIUS, CITY_RADIUS * 2, CITY_RADIUS * 2);

            if (chkShowLabels.isSelected()) {
                gc.setFill(Color.BLACK);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                gc.fillText(station.city, pos[0] - 15, pos[1] - 18);
            }

            // Crta stanice
            if (chkShowBusStations.isSelected()) {
                drawBusStation(gc, station.busStation, routeStations.contains(station.busStation));
            }

            if (chkShowTrainStations.isSelected()) {
                drawTrainStation(gc, station.trainStation, routeStations.contains(station.trainStation));
            }
        }
    }

    /**
     * Crta autobusku stanicu (kvadrat).
     *
     * @param gc Graphics context
     * @param stationId ID stanice
     * @param inRoute Da li je stanica dio rute
     */
    private void drawBusStation(GraphicsContext gc, String stationId, boolean inRoute) {
        double[] pos = nodePositions.get(stationId);
        if (pos == null) return;

        if (inRoute) {
            gc.setFill(Color.ORANGE);
            gc.setStroke(Color.DARKORANGE);
            gc.setLineWidth(3);
        } else {
            gc.setFill(Color.rgb(255, 179, 71));
            gc.setStroke(Color.rgb(230, 150, 50));
            gc.setLineWidth(1.5);
        }

        gc.fillRect(pos[0] - STATION_SIZE/2, pos[1] - STATION_SIZE/2, STATION_SIZE, STATION_SIZE);
        gc.strokeRect(pos[0] - STATION_SIZE/2, pos[1] - STATION_SIZE/2, STATION_SIZE, STATION_SIZE);

        if (chkShowLabels.isSelected()) {
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 8));
            gc.fillText(stationId, pos[0] - 15, pos[1] + 18);
        }
    }

    /**
     * Crta željezničku stanicu (dijamant).
     *
     * @param gc Graphics context
     * @param stationId ID stanice
     * @param inRoute Da li je stanica dio rute
     */
    private void drawTrainStation(GraphicsContext gc, String stationId, boolean inRoute) {
        double[] pos = nodePositions.get(stationId);
        if (pos == null) return;

        if (inRoute) {
            gc.setFill(Color.MEDIUMPURPLE);
            gc.setStroke(Color.PURPLE);
            gc.setLineWidth(3);
        } else {
            gc.setFill(Color.rgb(177, 156, 217));
            gc.setStroke(Color.rgb(138, 116, 177));
            gc.setLineWidth(1.5);
        }

        // Dijamant oblik
        double[] xPoints = {pos[0], pos[0] + STATION_SIZE, pos[0], pos[0] - STATION_SIZE};
        double[] yPoints = {pos[1] - STATION_SIZE, pos[1], pos[1] + STATION_SIZE, pos[1]};

        gc.fillPolygon(xPoints, yPoints, 4);
        gc.strokePolygon(xPoints, yPoints, 4);

        if (chkShowLabels.isSelected()) {
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 8));
            gc.fillText(stationId, pos[0] - 15, pos[1] + 18);
        }
    }

    /**
     * Crta strelicu između dvije tačke.
     *
     * @param gc Graphics context
     * @param x1 X koordinata početne tačke
     * @param y1 Y koordinata početne tačke
     * @param x2 X koordinata krajnje tačke
     * @param y2 Y koordinata krajnje tačke
     */
    private void drawArrow(GraphicsContext gc, double x1, double y1, double x2, double y2) {
        gc.strokeLine(x1, y1, x2, y2);

        // Izračunaj ugao
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double arrowLength = 10;

        // Vrh strelice
        double x3 = x2 - arrowLength * Math.cos(angle - Math.PI / 6);
        double y3 = y2 - arrowLength * Math.sin(angle - Math.PI / 6);
        double x4 = x2 - arrowLength * Math.cos(angle + Math.PI / 6);
        double y4 = y2 - arrowLength * Math.sin(angle + Math.PI / 6);

        gc.strokeLine(x2, y2, x3, y3);
        gc.strokeLine(x2, y2, x4, y4);
    }

    /**
     * Vraća naziv grada za stanicu.
     *
     * @param stationCode Kod stanice (npr. A_0_0 ili Z_0_0)
     * @return Naziv grada ili null ako nije pronađen
     */
    private String getCityFromStation(String stationCode) {
        if (stationCode.startsWith("G_")) return stationCode;

        for (TransportDataGenerator.Station s : transportData.stations) {
            if (s.busStation.equals(stationCode) || s.trainStation.equals(stationCode)) {
                return s.city;
            }
        }
        return null;
    }

    /**
     * Ažurira informacije o grafu.
     */
    private void updateInfo() {
        int cities = transportData.stations.size();
        int routes = transportData.departures.size();
        lblInfo.setText(String.format("Gradovi: %d | Polasci: %d | Zoom: %.0f%%",
                cities, routes, scale * 100));
    }

    /**
     * Resetuje prikaz grafa na početne vrijednosti.
     */
    @FXML
    private void resetView() {
        scale = 1.0;
        centerView();
        currentRoute = null;
        drawGraph();
        updateInfo();
    }

    /**
     * Rukuje zoom funkcionalnosti pomoću scroll-a.
     *
     * @param event Scroll event
     */
    private void handleScroll(ScrollEvent event) {
        double delta = event.getDeltaY();
        double scaleFactor = (delta > 0) ? 1.1 : 0.9;

        scale *= scaleFactor;
        scale = Math.max(0.3, Math.min(scale, 3.0)); // Limit zoom

        drawGraph();
        updateInfo();
    }

    /**
     * Rukuje početkom drag operacije.
     *
     * @param event Mouse event
     */
    private void handleMousePressed(MouseEvent event) {
        lastMouseX = event.getX();
        lastMouseY = event.getY();
    }

    /**
     * Rukuje drag operacijom za pomicanje grafa.
     *
     * @param event Mouse event
     */
    private void handleMouseDragged(MouseEvent event) {
        double dx = event.getX() - lastMouseX;
        double dy = event.getY() - lastMouseY;

        offsetX += dx;
        offsetY += dy;

        lastMouseX = event.getX();
        lastMouseY = event.getY();

        drawGraph();
    }
}