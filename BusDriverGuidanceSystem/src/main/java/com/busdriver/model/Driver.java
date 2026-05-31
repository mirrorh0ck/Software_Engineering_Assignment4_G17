package com.busdriver.model;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

// Driver entity - the 6 fields listed in the assignment spec.
// All the business rule checks live in DriverValidator so this class
// just holds the data and provides a couple of helpers.
public class Driver {

    // birthdate is always DD-MM-YYYY (spec rule D3) so keep the formatter handy
    public static final DateTimeFormatter BIRTHDATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // ---- fields ----
    private String driverID;        // 10 chars, see D1
    private String name;            // D5 - can't change after creation
    private int experienceYears;    // used by B4 (electric bus rule)
    private String licenseType;     // Light / Medium / Heavy / PublicTransport
    private String address;         // D2 pipe-separated
    private String birthdate;       // D3 DD-MM-YYYY

    // Constructor doesn't validate on purpose - the validator does that.
    // This way we can still build "bad" drivers in tests to check rejection.
    public Driver(String driverID, String name, int experienceYears,
                  String licenseType, String address, String birthdate) {
        this.driverID = driverID;
        this.name = name;
        this.experienceYears = experienceYears;
        this.licenseType = licenseType;
        this.address = address;
        this.birthdate = birthdate;
    }

    // ---- getters ----
    public String getDriverID()        { return driverID; }
    public String getName()            { return name; }
    public int    getExperienceYears() { return experienceYears; }
    public String getLicenseType()     { return licenseType; }
    public String getAddress()         { return address; }
    public String getBirthdate()       { return birthdate; }

    // ---- setters ----
    // Note: no setter for driverID or name on purpose (D5 = immutable).
    // Repo.update() must run validator D4 check before calling these.
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

    // Compute age from birthdate - needed for B3 (drivers older than 50).
    // Period.between returns completed years, which is what we want.
    public int getAge() {
        LocalDate dob = LocalDate.parse(birthdate, BIRTHDATE_FORMAT);
        return Period.between(dob, LocalDate.now()).getYears();
    }

    // Treat two drivers as equal if their IDs match.
    // D1 says IDs are unique so this is safe.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Driver)) return false;
        Driver other = (Driver) o;
        return Objects.equals(driverID, other.driverID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverID);
    }

    // Serialise to one line for the TXT file.
    // Address already contains '|' (D2) so use '~' as the outer separator,
    // otherwise the parser gets confused (learned this the hard way).
    public String toRecord() {
        return String.join("~",
                driverID,
                name,
                String.valueOf(experienceYears),
                licenseType,
                address,
                birthdate);
    }

    @Override
    public String toString() {
        return "Driver{" + driverID + ", " + name + ", " + experienceYears
                + "y, " + licenseType + "}";
    }
}
