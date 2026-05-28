package com.busdriver.model;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Domain entity representing a bus driver in the Intelligent
 * Bus Driver Guidance System.
 *
 * The fields are exactly those required by the Assignment 4
 * specification. Validation of business rules (D1-D5) is delegated
 * to DriverValidator; this class only holds state and
 * provides simple derived helpers such as .getAge().
 */
public class Driver {

    /** Date format mandated by D3 (e.g. "21-04-1995"). */
    public static final DateTimeFormatter BIRTHDATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /** Unique 10-character identifier for the driver (see D1). */
    private String driverID;

    /** Full name of the driver. Immutable after creation per D5. */
    private String name;

    /** Years of driving experience. Drives B4 (electric bus rule). */
    private int experienceYears;

    /** License type. One of: Light, Medium, Heavy, PublicTransport. */
    private String licenseType;

    /** Address in pipe-separated form per D2. */
    private String address;

    /** Birthdate string in DD-MM-YYYY format per D3. */
    private String birthdate;

    /**
     * Full constructor. The constructor itself does NOT validate;
     * validation is the responsibility of
     * DriverValidator.validateForAdd(...) when adding to a
     * repository. This keeps the model class lightweight and lets
     * unit tests construct intentionally-invalid drivers when
     * exercising negative cases.
     *
     * @param driverID        unique 10-char identifier
     * @param name            driver's full name
     * @param experienceYears years of driving experience (>= 0)
     * @param licenseType     license category
     * @param address         address in D2 pipe-separated format
     * @param birthdate       birthdate string in DD-MM-YYYY
     */
    public Driver(String driverID, String name, int experienceYears,
                  String licenseType, String address, String birthdate) {
        this.driverID = driverID;
        this.name = name;
        this.experienceYears = experienceYears;
        this.licenseType = licenseType;
        this.address = address;
        this.birthdate = birthdate;
    }

    // --- Getters -----------------------------------------------------

    public String getDriverID()        { return driverID; }
    public String getName()            { return name; }
    public int    getExperienceYears() { return experienceYears; }
    public String getLicenseType()     { return licenseType; }
    public String getAddress()         { return address; }
    public String getBirthdate()       { return birthdate; }

    // --- Setters -----------------------------------------------------
    // Note: driverID and name have no public setter (D5 - immutable).
    //       The repository's update() method enforces D4 (license lock
    //       for >10 years experience) before calling these setters.

    /** Set experience years. Used by repository update operations. */
    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    /** Set license type. Repository must enforce D4 before calling. */
    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    /** Set address. Repository must validate D2 before calling. */
    public void setAddress(String address) {
        this.address = address;
    }

    /** Set birthdate. Repository must validate D3 before calling. */
    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    // --- Derived helpers --------------------------------------------

    /**
     * Compute the driver's age in completed years based on the
     * stored birthdate. Used by Bus rule B3 (drivers over 50
     * cannot operate large-capacity buses).
     *
     * @return the driver's age in whole years
     */
    public int getAge() {
        LocalDate dob = LocalDate.parse(birthdate, BIRTHDATE_FORMAT);
        return Period.between(dob, LocalDate.now()).getYears();
    }

    // --- equals/hashCode/toString -----------------------------------

    /**
     * Two drivers are considered equal iff their driverIDs match,
     * since D1 guarantees driverID uniqueness.
     */
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

    /**
     * Serialised pipe-delimited form used when writing to the TXT
     * "database" file. Format:
     * driverID~name~experienceYears~licenseType~address~birthdate
     *
     * Note: the address itself contains pipes (D2), so "~" is used as
     * the outer field separator to avoid ambiguity when parsing.
     */
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
