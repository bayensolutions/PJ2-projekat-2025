package org.unibl.etf.pj2.transport.model;

import java.time.LocalDateTime;

public class Departure {
    private final LocalDateTime departureTime;
    private final LocalDateTime arrivalTime;
    private final double price;
    private final int minWaitingTime;

    public Departure(LocalDateTime departureTime, LocalDateTime arrivalTime, double price, int minWaitingTime) {
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
        this.minWaitingTime = minWaitingTime;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public double getPrice() {
        return price;
    }

    public int getMinWaitingTime() {
        return minWaitingTime;
    }
}
