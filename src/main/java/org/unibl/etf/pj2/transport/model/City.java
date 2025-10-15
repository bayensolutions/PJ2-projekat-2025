package org.unibl.etf.pj2.transport.model;

import org.unibl.etf.pj2.transport.model.Station;

public class City {
    private String name;
    private Station[] stations;

    public City(String name) {
        this.name = name;
        stations = new Station[2];
    }

    public void setStation(int index, Station station) {
        stations[index] = station;
    }

    public Station getStation(int index) {
        return stations[index];
    }

    public String getName() {
        return name;
    }

}
