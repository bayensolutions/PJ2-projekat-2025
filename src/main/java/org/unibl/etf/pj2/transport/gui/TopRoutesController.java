package org.unibl.etf.pj2.transport.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.unibl.etf.pj2.transport.model.TopRoutesResult;
import org.unibl.etf.pj2.transport.util.SimpleRouteFinder;

import java.util.List;

/**
 * Kontroler za prikaz top 5 ruta.
 */
public class TopRoutesController {

    @FXML private ListView<VBox> routesList;

    private List<TopRoutesResult> topRoutes;

    /**
     * Postavlja podatke o rutama i prikazuje ih.
     */
    public void setRoutes(List<List<SimpleRouteFinder.RouteStep>> routes) {
        topRoutes = routes.stream()
                .map(TopRoutesResult::new)
                .collect(java.util.stream.Collectors.toList());

        ObservableList<VBox> items = FXCollections.observableArrayList();

        for (int i = 0; i < topRoutes.size(); i++) {
            TopRoutesResult route = topRoutes.get(i);
            VBox routeBox = createRouteBox(i + 1, route);
            items.add(routeBox);
        }

        routesList.setItems(items);
    }

    /**
     * Kreira VBox sa detaljima rute i dugmetom za kupovinu.
     */
    private VBox createRouteBox(int index, TopRoutesResult route) {
        VBox box = new VBox(5);
        box.setStyle("-fx-padding: 10; -fx-border-color: #ccc; -fx-border-width: 1;");

        Label titleLabel = new Label("Ruta #" + index);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label metricsLabel = new Label(String.format(
                "Cijena: %d KM | Trajanje: %s | Presjedanja: %d",
                route.getTotalPrice(),
                route.getFormattedDuration(),
                route.getTransfers()
        ));

        Label timeLabel = new Label(String.format(
                "Polazak: %s | Dolazak: %s",
                formatTime(route.getDepartureTime()),
                formatTime(route.getArrivalTime())
        ));

        // Detalji segmenata
        VBox segmentsBox = new VBox(3);
        for (SimpleRouteFinder.RouteStep step : route.getRoute()) {
            Label segmentLabel = new Label(String.format(
                    "  %s: %s → %s (%d min, %d KM)",
                    step.type, step.from, step.to, step.duration, step.price
            ));
            segmentLabel.setStyle("-fx-font-size: 11px;");
            segmentsBox.getChildren().add(segmentLabel);
        }

        Button buyButton = new Button("Kupi kartu");
        buyButton.setOnAction(e -> buyTicket(route));

        box.getChildren().addAll(titleLabel, metricsLabel, timeLabel, segmentsBox, buyButton);
        return box;
    }

    /**
     * Formatira ISO LocalDateTime u čitljiv format.
     */
    private String formatTime(String isoTime) {
        try {
            return isoTime.substring(0, 16).replace("T", " ");
        } catch (Exception e) {
            return isoTime;
        }
    }

    /**
     * Kupovina karte (za sada samo placeholder).
     */
    private void buyTicket(TopRoutesResult route) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Kupovina karte");
        alert.setHeaderText(null);
        alert.setContentText(String.format(
                "Kupili ste kartu!\n\nCijena: %d KM\nTrajanje: %s\nPresjedanja: %d",
                route.getTotalPrice(),
                route.getFormattedDuration(),
                route.getTransfers()
        ));
        alert.showAndWait();

        // TODO: Implementiraj generisanje računa
    }
}