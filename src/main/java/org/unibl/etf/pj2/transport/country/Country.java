package org.unibl.etf.pj2.transport.country;

public class Country {

    private int rows;
    private int columns;
    private City[][] cities;

    public Country(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.cities = new City[rows][columns];
    }

    public void setCity(int row, int column, City city) {
        if (row >= 0 && row < rows && column > 0 && column < columns) {
            cities[row][column] = city;
        }
    }

    public City getCity(int row, int column) {
        if (row >= 0 && row < rows && column > 0 && column < columns) {
            return cities[row][column];
        }
        return null;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

}
