package com.busdriver.model;

/**
 * Domain entity representing a bus driver in the Intelligent
 * Bus Driver Guidance System.
 *
 * <p>This is the very first cut of the class - it only carries
 * state (fields, constructor, getters, setters). Business rules
 * D1-D5 will be enforced by a dedicated DriverValidator that we
 * will introduce in a later commit.</p>
 */
public class Driver {

    // --- Fields specified by the Assignment 4 brief --------------

    private String driverID;
    private String name;
    private int    experienceYears;
    private String licenseType; // Light, Medium, Heavy, PublicTransport
    private String address;
    private String birthdate;   // expected format DD-MM-YYYY (D3)

    // --- Constructor ---------------------------------------------

    public Driver(String driverID, String name, int experienceYears,
                  String licenseType, String address, String birthdate) {
        this.driverID = driverID;
        this.name = name;
        this.experienceYears = experienceYears;
        this.licenseType = licenseType;
        this.address = address;
        this.birthdate = birthdate;
    }

    // --- Getters -------------------------------------------------

    public String getDriverID()        { return driverID; }
    public String getName()            { return name; }
    public int    getExperienceYears() { return experienceYears; }
    public String getLicenseType()     { return licenseType; }
    public String getAddress()         { return address; }
    public String getBirthdate()       { return birthdate; }

    // --- Setters -------------------------------------------------
    // NOTE: D5 says driverID and name must be immutable on update,
    //       so no setters will be provided for those once the
    //       validator is in place. For now we keep the fields
    //       package-final and offer setters only for the mutable
    //       fields the repository will need.

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    @Override
    public String toString() {
        return "Driver{" + driverID + ", " + name + "}";
    }
}
