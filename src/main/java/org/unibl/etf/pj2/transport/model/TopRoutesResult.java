package org.unibl.etf.pj2.transport.model;

import org.unibl.etf.pj2.transport.util.SimpleRouteFinder;
import java.util.List;

/**
 * Wrapper klasa koja sadrži top N ruta sa metrikama.
 */
public class TopRoutesResult {
    private final List<SimpleRouteFinder.RouteStep> route;
    private final int totalPrice;
    private final long totalDurationMinutes;
    private final int transfers;
    private final String departureTime;
    private final String arrivalTime;

    public TopRoutesResult(List<SimpleRouteFinder.RouteStep> route) {
        this.route = route;

        // Izračunaj metrike
        this.totalPrice = route.stream().mapToInt(r -> r.price).sum();
        this.totalDurationMinutes = route.stream().mapToInt(r -> r.duration).sum();
        this.transfers = route.size();
        this.departureTime = route.isEmpty() ? "" : route.get(0).departureTime;
        this.arrivalTime = route.isEmpty() ? "" : route.get(route.size() - 1).arrivalTime;
    }

    public List<SimpleRouteFinder.RouteStep> getRoute() {
        return route;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public long getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public int getTransfers() {
        return transfers;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    /**
     * Formatiran prikaz trajanja (npr. "5h 30min")
     */
    public String getFormattedDuration() {
        long hours = totalDurationMinutes / 60;
        long mins = totalDurationMinutes % 60;
        return hours + "h " + mins + "min";
    }

    @Override
    public String toString() {
        return String.format("Cijena: %d KM | Trajanje: %s | Presjedanja: %d",
                totalPrice, getFormattedDuration(), transfers);
    }
}