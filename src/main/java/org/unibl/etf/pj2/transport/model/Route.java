package org.unibl.etf.pj2.transport.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Route {
    private final StringProperty transport;
    private final StringProperty start;
    private final StringProperty destination;
    private final StringProperty time;
    private final StringProperty price;

    public Route(String transport, String start, String destination, String time, String price) {
        this.transport = new SimpleStringProperty(transport);
        this.start = new SimpleStringProperty(start);
        this.destination = new SimpleStringProperty(destination);
        this.time = new SimpleStringProperty(time);
        this.price = new SimpleStringProperty(price);
    }

    public StringProperty transportProperty() { return transport; }
    public StringProperty startProperty() { return start; }
    public StringProperty destinationProperty() { return destination; }
    public StringProperty timeProperty() { return time; }
    public StringProperty priceProperty() { return price; }
}
