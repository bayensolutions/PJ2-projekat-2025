package org.unibl.etf.pj2.transport.station;

import org.unibl.etf.pj2.transport.schedule.Departure;

import java.util.ArrayList;
import java.util.List;

public abstract class Station {
    protected String id;
    protected List<Departure> departures;

    public Station(String id) {
        this.id = id;
        this.departures = new ArrayList<Departure>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Departure> getDepartures() {
        return departures;
    }

    public void setDepartures(List<Departure> departures) {
        this.departures = departures;
    }

}
