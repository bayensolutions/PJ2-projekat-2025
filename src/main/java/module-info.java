module org.unibl.etf.pj2.transport {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    opens org.unibl.etf.pj2.transport.util to com.google.gson;
    opens org.unibl.etf.pj2.transport.generator to com.google.gson;
    opens org.unibl.etf.pj2.transport.schedule to com.google.gson;
    opens org.unibl.etf.pj2.transport.station to com.google.gson;
    opens org.unibl.etf.pj2.transport.country to com.google.gson;
}
