package com.busdriver.validator;

import com.busdriver.exception.ValidationException;
import com.busdriver.model.Driver;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Set;

// All the Driver rule checks (D1-D5) live here as static methods.
// Reminder:
//   D1 - driverID format + uniqueness
//   D2 - address Street|Name|City|State|Country
//   D3 - birthdate DD-MM-YYYY
//   D4 - >10 years experience -> license is locked
//   D5 - driverID and name can't change on update
public final class DriverValidator {

    // 4 allowed license values per the brief
    public static final Set<String> ALLOWED_LICENSE_TYPES =
            Set.of("Light", "Medium", "Heavy", "PublicTransport");

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // private ctor - nothing to instantiate
    private DriverValidator() { }

    // ===== D1: driverID format =====
    // 10 chars total, first 2 digits 2-9, at least 2 specials in chars 3-8,
    // last 2 uppercase. Uniqueness is checked separately.
    public static void validateDriverIdFormat(String driverID) {
        if (driverID == null) {
            throw new ValidationException("D1: driverID cannot be null");
        }
        if (driverID.length() != 10) {
            throw new ValidationException(
                    "D1: driverID must be 10 chars long, got " + driverID.length());
        }
        // first two must be digit between '2' and '9'
        for (int i = 0; i < 2; i++) {
            char c = driverID.charAt(i);
            if (c < '2' || c > '9') {
                throw new ValidationException(
                        "D1: first two chars must be digits 2-9, found '" + c + "'");
            }
        }
        // middle 6 chars (positions 3-8, i.e. index 2..7) need >= 2 specials.
        // I count "non alphanumeric" as special.
        int specialCount = 0;
        for (int i = 2; i <= 7; i++) {
            char c = driverID.charAt(i);
            boolean alphaNum =
                    (c >= '0' && c <= '9')
                            || (c >= 'A' && c <= 'Z')
                            || (c >= 'a' && c <= 'z');
            if (!alphaNum) specialCount++;
        }
        if (specialCount < 2) {
            throw new ValidationException(
                    "D1: need at least 2 special chars between positions 3 and 8, got "
                            + specialCount);
        }
        // last two: uppercase letters
        for (int i = 8; i < 10; i++) {
            char c = driverID.charAt(i);
            if (c < 'A' || c > 'Z') {
                throw new ValidationException(
                        "D1: last two chars must be uppercase A-Z, found '" + c + "'");
            }
        }
    }

    // unique part of D1 - repo passes in the set of IDs already stored
    public static void validateDriverIdUnique(String driverID, Set<String> existingIDs) {
        if (existingIDs.contains(driverID)) {
            throw new ValidationException(
                    "D1: driverID '" + driverID + "' already exists");
        }
    }

    // ===== D2: address format =====
    // Must split into exactly 5 non-empty parts on '|'
    public static void validateAddressFormat(String address) {
        if (address == null) {
            throw new ValidationException("D2: address cannot be null");
        }
        // limit -1 keeps trailing empty strings so "a|b|c|d|" doesn't sneak through
        String[] parts = address.split("\\|", -1);
        if (parts.length != 5) {
            throw new ValidationException(
                    "D2: address must be Street Number|Street Name|City|State|Country"
                            + " (got " + parts.length + " parts)");
        }
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].trim().isEmpty()) {
                throw new ValidationException(
                        "D2: address part " + (i + 1) + " is empty");
            }
        }
    }

    // ===== D3: birthdate DD-MM-YYYY =====
    public static void validateBirthdateFormat(String birthdate) {
        if (birthdate == null) {
            throw new ValidationException("D3: birthdate cannot be null");
        }
        LocalDate dob;
        try {
            dob = LocalDate.parse(birthdate, DATE_FMT);
        } catch (DateTimeParseException ex) {
            throw new ValidationException(
                    "D3: birthdate must be DD-MM-YYYY, got '" + birthdate + "'", ex);
        }
        // sanity - can't be born in the future
        if (dob.isAfter(LocalDate.now())) {
            throw new ValidationException("D3: birthdate cannot be in the future");
        }
    }

    // not strictly D1-D5 but we want to reject unknown license types
    public static void validateLicenseType(String licenseType) {
        if (!ALLOWED_LICENSE_TYPES.contains(licenseType)) {
            throw new ValidationException(
                    "license type must be one of " + ALLOWED_LICENSE_TYPES
                            + ", got '" + licenseType + "'");
        }
    }

    // ===== one-stop check used by Repo.add() =====
    public static void validateForAdd(Driver d, Set<String> existingIDs) {
        Objects.requireNonNull(d, "driver cannot be null");
        validateDriverIdFormat(d.getDriverID());
        validateDriverIdUnique(d.getDriverID(), existingIDs);
        validateLicenseType(d.getLicenseType());
        validateAddressFormat(d.getAddress());
        validateBirthdateFormat(d.getBirthdate());
        if (d.getExperienceYears() < 0) {
            throw new ValidationException("experienceYears must be >= 0");
        }
        if (d.getName() == null || d.getName().trim().isEmpty()) {
            throw new ValidationException("name is empty");
        }
    }

    // ===== check used by Repo.update() - D4 + D5 + re-check changed fields =====
    public static void validateForUpdate(Driver existing, Driver updated) {
        Objects.requireNonNull(existing, "existing cannot be null");
        Objects.requireNonNull(updated, "updated cannot be null");

        // D5: ID and name are locked
        if (!Objects.equals(existing.getDriverID(), updated.getDriverID())) {
            throw new ValidationException("D5: driverID cannot be changed");
        }
        if (!Objects.equals(existing.getName(), updated.getName())) {
            throw new ValidationException("D5: name cannot be changed");
        }

        // D4: license is locked if existing experience > 10 years
        // (strict >, so exactly 10 is still fine)
        if (existing.getExperienceYears() > 10
                && !Objects.equals(existing.getLicenseType(), updated.getLicenseType())) {
            throw new ValidationException(
                    "D4: cannot change licenseType, driver has > 10 years experience");
        }

        // re-validate the fields that are allowed to change
        validateLicenseType(updated.getLicenseType());
        validateAddressFormat(updated.getAddress());
        validateBirthdateFormat(updated.getBirthdate());
        if (updated.getExperienceYears() < 0) {
            throw new ValidationException("experienceYears must be >= 0");
        }
    }
}
