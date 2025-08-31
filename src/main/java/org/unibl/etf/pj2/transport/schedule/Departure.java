package org.unibl.etf.pj2.transport.schedule;

import java.time.LocalDateTime;

public class Departure {
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private double price;
    private int minWaitingTime;

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
