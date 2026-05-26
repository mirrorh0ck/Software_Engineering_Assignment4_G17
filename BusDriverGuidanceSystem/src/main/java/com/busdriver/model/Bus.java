package com.busdriver.model;

/**
 * Domain entity representing a bus in the Intelligent Bus Driver
 * Guidance System.
 *
 * <p>First cut - fields, constructor, getters, setters only.
 * Bus business rules B1-B5 will be moved into a BusValidator in a
 * subsequent commit; for now this class just holds state.</p>
 */
public class Bus {

    // --- Fields specified by the Assignment 4 brief --------------

    private String busID;       // 8 digits (B1) - validated later
    private int    capacity;
    private double fuelLevel;
    private String fuelType;    // Diesel, Hybrid, Electricity

    // --- Constructor ---------------------------------------------

    public Bus(String busID, int capacity, double fuelLevel, String fuelType) {
        this.busID = busID;
        this.capacity = capacity;
        this.fuelLevel = fuelLevel;
        this.fuelType = fuelType;
    }

    // --- Getters -------------------------------------------------

    public String getBusID()    { return busID; }
    public int    getCapacity() { return capacity; }
    public double getFuelLevel(){ return fuelLevel; }
    public String getFuelType() { return fuelType; }

    // --- Setters -------------------------------------------------
    // busID is treated as identity (B1) and has no setter.

    public void setCapacity(int capacity)      { this.capacity = capacity; }
    public void setFuelLevel(double fuelLevel) { this.fuelLevel = fuelLevel; }
    public void setFuelType(String fuelType)   { this.fuelType = fuelType; }

    @Override
    public String toString() {
        return "Bus{" + busID + ", cap=" + capacity + "}";
    }
}
