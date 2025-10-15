module org.unibl.etf.pj2.transport {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    // Otvaranje paketa JavaFX-u za refleksiju
    opens org.unibl.etf.pj2.transport to javafx.graphics, javafx.fxml;
    opens org.unibl.etf.pj2.transport.util to com.google.gson;
    opens org.unibl.etf.pj2.transport.generator to com.google.gson;
    opens org.unibl.etf.pj2.transport.model to com.google.gson, javafx.fxml, javafx.graphics;
    opens org.unibl.etf.pj2.transport.gui to javafx.fxml, javafx.graphics;

    // Ako koristiš FXML i kontrolere u ovom paketu
}
