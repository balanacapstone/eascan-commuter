package com.example.eascanfinal;

public class Transaction {
    private String conductorUsername;
    private double total;
    private String date;
    private String location;

    public Transaction(String conductorUsername, double total, String date, String location) {
        this.conductorUsername = conductorUsername;
        this.total = total;
        this.date = date;
        this.location = location;
    }

    public String getConductorUsername() {
        return conductorUsername;
    }

    public double getTotal() {
        return total;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }
}
