package com.busdriver.validator;

import com.busdriver.exception.ValidationException;
import com.busdriver.model.Driver;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Set;

/**
 * Static validator that enforces driver business rules D1-D5
 * as specified in the Assignment 4 brief.
 *
 * All methods throw ValidationException on rule violation
 * and return normally on success, so callers can simply chain calls
 * (or rely on the helper .validateForAdd(Driver, java.util.Set)).
 *
 * Rules:
 *
 *   - D1 - driverID: exactly 10 chars, first 2 digits 2-9,
 *       at least 2 special chars between positions 3-8,
 *       last 2 uppercase A-Z, must be unique.
 *   - D2 - address: must match
 *       Street Number|Street Name|City|State|Country.
 *   - D3 - birthdate: must parse as DD-MM-YYYY.
 *   - D4 - license update: drivers with > 10 years experience
 *       cannot change licenseType on update.
 *   - D5 - immutable fields: driverID and name cannot change on update.
 *
 */
public final class DriverValidator {

    /** Allowed license type values. */
    public static final Set<String> ALLOWED_LICENSE_TYPES =
            Set.of("Light", "Medium", "Heavy", "PublicTransport");

    /** Date format mandated by D3. */
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /** Utility class - prevent instantiation. */
    private DriverValidator() { }

    // =================================================================
    // D1 - driverID rules
    // =================================================================

    /**
     * Validate the driverID structural rules (length, digit prefix,
     * special chars in middle, uppercase suffix). Does NOT check
     * uniqueness; that is the repository's responsibility.
     *
     * @param driverID candidate ID
     * @throws ValidationException if any structural rule is violated
     */
    public static void validateDriverIdFormat(String driverID) {
        if (driverID == null) {
            throw new ValidationException("D1: driverID must not be null");
        }
        // Exactly 10 characters
        if (driverID.length() != 10) {
            throw new ValidationException(
                    "D1: driverID must be exactly 10 characters long "
                            + "(was " + driverID.length() + ")");
        }
        // First two characters: digits 2-9
        for (int i = 0; i < 2; i++) {
            char c = driverID.charAt(i);
            if (c < '2' || c > '9') {
                throw new ValidationException(
                        "D1: first two characters of driverID must be "
                                + "digits between 2 and 9 (found '" + c + "')");
            }
        }
        // Characters 3-8 (1-indexed) i.e. index 2..7 inclusive:
        // at least 2 special (non-alphanumeric) characters.
        int specialCount = 0;
        for (int i = 2; i <= 7; i++) {
            char c = driverID.charAt(i);
            boolean alphaNumeric =
                    (c >= '0' && c <= '9')
                            || (c >= 'A' && c <= 'Z')
                            || (c >= 'a' && c <= 'z');
            if (!alphaNumeric) {
                specialCount++;
            }
        }
        if (specialCount < 2) {
            throw new ValidationException(
                    "D1: driverID must contain at least 2 special "
                            + "characters between positions 3 and 8 "
                            + "(found " + specialCount + ")");
        }
        // Last two characters: uppercase A-Z
        for (int i = 8; i < 10; i++) {
            char c = driverID.charAt(i);
            if (c < 'A' || c > 'Z') {
                throw new ValidationException(
                        "D1: last two characters of driverID must be "
                                + "uppercase letters A-Z (found '" + c + "')");
            }
        }
    }

    /**
     * Check that driverID does not already exist in
     * existingIDs. Part of D1.
     */
    public static void validateDriverIdUnique(String driverID,
                                              Set<String> existingIDs) {
        if (existingIDs.contains(driverID)) {
            throw new ValidationException(
                    "D1: driverID '" + driverID + "' already exists "
                            + "(duplicates not allowed)");
        }
    }

    // =================================================================
    // D2 - address format
    // =================================================================

    /**
     * Validate that the address has exactly the five pipe-separated
     * components mandated by D2.
     */
    public static void validateAddressFormat(String address) {
        if (address == null) {
            throw new ValidationException("D2: address must not be null");
        }
        // -1 limit retains trailing empty strings, so we can catch
        // "1|2|3|4|" (missing country) as well.
        String[] parts = address.split("\\|", -1);
        if (parts.length != 5) {
            throw new ValidationException(
                    "D2: address must have format "
                            + "'Street Number|Street Name|City|State|Country' "
                            + "(found " + parts.length + " parts)");
        }
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].trim().isEmpty()) {
                throw new ValidationException(
                        "D2: address part #" + (i + 1) + " must not be empty");
            }
        }
    }

    // =================================================================
    // D3 - birthdate format
    // =================================================================

    /**
     * Validate that birthdate parses as DD-MM-YYYY and refers
     * to a real (not future) date.
     */
    public static void validateBirthdateFormat(String birthdate) {
        if (birthdate == null) {
            throw new ValidationException("D3: birthdate must not be null");
        }
        LocalDate dob;
        try {
            dob = LocalDate.parse(birthdate, DATE_FMT);
        } catch (DateTimeParseException ex) {
            throw new ValidationException(
                    "D3: birthdate must be in DD-MM-YYYY format "
                            + "(was '" + birthdate + "')", ex);
        }
        if (dob.isAfter(LocalDate.now())) {
            throw new ValidationException(
                    "D3: birthdate cannot be in the future");
        }
    }

    // =================================================================
    // License type sanity check (referenced by add/update)
    // =================================================================

    /** Helper - ensure license type is one of the allowed values. */
    public static void validateLicenseType(String licenseType) {
        if (!ALLOWED_LICENSE_TYPES.contains(licenseType)) {
            throw new ValidationException(
                    "License type must be one of " + ALLOWED_LICENSE_TYPES
                            + " (was '" + licenseType + "')");
        }
    }

    // =================================================================
    // Add-time validation entry point
    // =================================================================

    /**
     * Validate a brand-new Driver before it is added to the
     * repository: all of D1, D2, D3 plus a license-type sanity check.
     *
     * @param d           the candidate driver
     * @param existingIDs IDs already in the repository (for D1 uniqueness)
     */
    public static void validateForAdd(Driver d, Set<String> existingIDs) {
        Objects.requireNonNull(d, "driver must not be null");
        validateDriverIdFormat(d.getDriverID());
        validateDriverIdUnique(d.getDriverID(), existingIDs);
        validateLicenseType(d.getLicenseType());
        validateAddressFormat(d.getAddress());
        validateBirthdateFormat(d.getBirthdate());
        if (d.getExperienceYears() < 0) {
            throw new ValidationException(
                    "experienceYears must be non-negative");
        }
        if (d.getName() == null || d.getName().trim().isEmpty()) {
            throw new ValidationException("name must not be empty");
        }
    }

    // =================================================================
    // D4 + D5 - update validation
    // =================================================================

    /**
     * Validate an update against the previously-stored driver. Enforces
     * D4 (license lock for >10y experience) and D5 (driverID/name
     * immutability), plus re-checks D2/D3 for any changed fields.
     *
     * @param existing   the driver currently stored
     * @param updated    the updated driver values
     */
    public static void validateForUpdate(Driver existing, Driver updated) {
        Objects.requireNonNull(existing, "existing must not be null");
        Objects.requireNonNull(updated,  "updated must not be null");

        // D5: driverID and name cannot change
        if (!Objects.equals(existing.getDriverID(), updated.getDriverID())) {
            throw new ValidationException(
                    "D5: driverID cannot be modified during update");
        }
        if (!Objects.equals(existing.getName(), updated.getName())) {
            throw new ValidationException(
                    "D5: name cannot be modified during update");
        }

        // D4: license cannot change for highly experienced drivers
        if (existing.getExperienceYears() > 10
                && !Objects.equals(existing.getLicenseType(),
                                   updated.getLicenseType())) {
            throw new ValidationException(
                    "D4: licenseType cannot be changed for drivers "
                            + "with more than 10 years of experience");
        }

        // Re-validate format of mutable fields
        validateLicenseType(updated.getLicenseType());
        validateAddressFormat(updated.getAddress());
        validateBirthdateFormat(updated.getBirthdate());
        if (updated.getExperienceYears() < 0) {
            throw new ValidationException(
                    "experienceYears must be non-negative");
        }
    }
}
