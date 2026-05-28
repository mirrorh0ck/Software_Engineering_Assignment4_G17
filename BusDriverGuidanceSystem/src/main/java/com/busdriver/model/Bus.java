package com.busdriver.model;

import java.util.Objects;

/**
 * Domain entity representing a bus in the Intelligent Bus Driver
 * Guidance System.
 *
 * Validation of business rules (B1-B5) is delegated to
 * BusValidator; this class only holds state.
 */
public class Bus {

    /** 8-digit unique identifier per B1. */
    private String busID;

    /** Seating capacity. Cannot be increased on update (B2). */
    private int capacity;

    /** Current fuel level (litres for Diesel, % for Electricity etc.). */
    private double fuelLevel;

    /** One of: "Diesel", "Hybrid", "Electricity". Drives B4/B5. */
    private String fuelType;

    /**
     * Optional assigned driver ID. Used to demonstrate the
     * driver-bus pairing rules (B3, B4, B5). May be null when the
     * bus is unassigned.
     */
    private String assignedDriverID;

    /**
     * Full constructor. Like Driver, the constructor does
     * not validate; validation is the repository's job.
     *
     * @param busID     8-digit identifier
     * @param capacity  seating capacity (positive)
     * @param fuelLevel current fuel level (>= 0)
     * @param fuelType  one of Diesel / Hybrid / Electricity
     */
    public Bus(String busID, int capacity, double fuelLevel, String fuelType) {
        this.busID = busID;
        this.capacity = capacity;
        this.fuelLevel = fuelLevel;
        this.fuelType = fuelType;
        this.assignedDriverID = null;
    }

    // --- Getters -----------------------------------------------------

    public String getBusID()            { return busID; }
    public int    getCapacity()         { return capacity; }
    public double getFuelLevel()        { return fuelLevel; }
    public String getFuelType()         { return fuelType; }
    public String getAssignedDriverID() { return assignedDriverID; }

    // --- Setters -----------------------------------------------------
    // busID has no setter (B1 - unique, treated as identity).
    // Capacity setter exists but BusRepository.update() must enforce
    // B2 (capacity cannot increase).

    public void setCapacity(int capacity)        { this.capacity = capacity; }
    public void setFuelLevel(double fuelLevel)   { this.fuelLevel = fuelLevel; }
    public void setFuelType(String fuelType)     { this.fuelType = fuelType; }
    public void setAssignedDriverID(String id)   { this.assignedDriverID = id; }

    // --- equals/hashCode/toString -----------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bus)) return false;
        Bus other = (Bus) o;
        return Objects.equals(busID, other.busID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(busID);
    }

    /**
     * Serialised pipe-delimited form used when writing to the TXT
     * "database" file. Format:
     * busID|capacity|fuelLevel|fuelType|assignedDriverID
     *
     * "-" is written when no driver has been assigned, so the file
     * remains human-readable.
     */
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
