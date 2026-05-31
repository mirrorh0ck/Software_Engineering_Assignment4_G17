package com.busdriver.model;

import java.util.Objects;

// Bus entity. Original spec gives us 4 fields - we added assignedDriverID
// so we can pair a driver with a bus (needed to check B3/B4/B5).
public class Bus {

    // ---- fields ----
    private String busID;         // 8-digit (B1)
    private int capacity;         // B2 - cannot grow on update
    private double fuelLevel;
    private String fuelType;      // Diesel / Hybrid / Electricity (B4, B5)

    // which driver is operating this bus right now, or null if no one assigned
    private String assignedDriverID;

    public Bus(String busID, int capacity, double fuelLevel, String fuelType) {
        this.busID = busID;
        this.capacity = capacity;
        this.fuelLevel = fuelLevel;
        this.fuelType = fuelType;
        this.assignedDriverID = null;     // no driver to start with
    }

    // ---- getters ----
    public String getBusID()            { return busID; }
    public int    getCapacity()         { return capacity; }
    public double getFuelLevel()        { return fuelLevel; }
    public String getFuelType()         { return fuelType; }
    public String getAssignedDriverID() { return assignedDriverID; }

    // ---- setters ----
    // busID has no setter - it's the identity (B1)
    public void setCapacity(int capacity)        { this.capacity = capacity; }
    public void setFuelLevel(double fuelLevel)   { this.fuelLevel = fuelLevel; }
    public void setFuelType(String fuelType)     { this.fuelType = fuelType; }
    public void setAssignedDriverID(String id)   { this.assignedDriverID = id; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bus)) return false;
        return Objects.equals(busID, ((Bus) o).busID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(busID);
    }

    // One-line form for the TXT file. Bus fields don't have '|' inside
    // so we can use '|' as the separator without escaping.
    // Use '-' when no driver assigned so the file stays human-readable.
    public String toRecord() {
        return String.join("|",
                busID,
                String.valueOf(capacity),
                String.valueOf(fuelLevel),
                fuelType,
                assignedDriverID == null ? "-" : assignedDriverID);
    }

    @Override
    public String toString() {
        return "Bus{" + busID + ", cap=" + capacity + ", fuel=" + fuelLevel
                + " " + fuelType + "}";
    }
}
