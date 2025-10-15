package org.unibl.etf.pj2.transport.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.unibl.etf.pj2.transport.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HelloController {

    @FXML
    private Label lblKarte;

    @FXML
    private Label lblPrihod;

    @FXML
    public void initialize() {
        // Ovdje možeš ubaciti učitavanje statistike o prodaji karata (ako imaš InvoiceManager)
        Path racuniDir = Path.of(Config.getInvoicesDir());
        long brojKarata = 0;
        double prihod = 0;
        try {
            if (Files.exists(racuniDir)) {
                brojKarata = Files.list(racuniDir)
                        .filter(f -> f.toString().endsWith(".txt"))
                        .count();

                prihod = Files.list(racuniDir)
                        .filter(f -> f.toString().endsWith(".txt"))
                        .flatMap(p -> {
                            try {
                                return Files.readAllLines(p).stream();
                            } catch (IOException e) {
                                return java.util.stream.Stream.empty();
                            }
                        })
                        .filter(line -> line.startsWith("Ukupna cijena:"))
                        .mapToDouble(line -> {
                            try {
                                return Double.parseDouble(line.split(":")[1].trim());
                            } catch (Exception e) {
                                return 0;
                            }
                        })
                        .sum();
            }
        } catch (IOException ignored) {}

        lblKarte.setText("Ukupan broj prodatih karata: " + brojKarata);
        lblPrihod.setText(String.format("Ukupan prihod: %.2f KM", prihod));
    }

    @FXML
    public void pokreniAplikaciju(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        Scene scene = new Scene(loader.load(), 900, 700);
        Stage stage = new Stage();
        stage.setTitle("Transport aplikacija");
        stage.setScene(scene);
        stage.show();

        // Zatvori početni prozor
        Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }
}
