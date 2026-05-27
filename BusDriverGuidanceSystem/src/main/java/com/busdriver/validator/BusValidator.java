package com.busdriver.validator;

import com.busdriver.exception.ValidationException;
import com.busdriver.model.Bus;
import com.busdriver.model.Driver;

import java.util.Objects;
import java.util.Set;

/**
 * Static validator that enforces bus business rules B1-B5 as
 * specified in the Assignment 4 brief.
 *
 * Rules:
 *
 *   - B1 - busID: exactly 8 digit characters, unique.
 *   - B2 - capacity update: capacity cannot increase, only decrease.
 *   - B3 - driver age: drivers older than 50 cannot drive buses
 *       with capacity >= 50.
 *   - B4 - electric bus: only drivers with experienceYears >= 5 may
 *       drive buses with fuelType = "Electricity".
 *   - B5 - license: only drivers with licenseType "Heavy" or
 *       "PublicTransport" may operate electric or hybrid buses.
 *
 */
public final class BusValidator {

    /** Allowed fuel type values. */
    public static final Set<String> ALLOWED_FUEL_TYPES =
            Set.of("Diesel", "Hybrid", "Electricity");

    /** Utility class - prevent instantiation. */
    private BusValidator() { }

    // =================================================================
    // B1 - busID rules
    // =================================================================

    /**
     * Validate that busID is exactly 8 digit characters.
     * Does NOT check uniqueness.
     */
    public static void validateBusIdFormat(String busID) {
        if (busID == null) {
            throw new ValidationException("B1: busID must not be null");
        }
        if (busID.length() != 8) {
            throw new ValidationException(
                    "B1: busID must be exactly 8 characters long "
                            + "(was " + busID.length() + ")");
        }
        for (int i = 0; i < busID.length(); i++) {
            char c = busID.charAt(i);
            if (c < '0' || c > '9') {
                throw new ValidationException(
                        "B1: busID must contain only digits "
                                + "(found '" + c + "' at position " + i + ")");
            }
        }
    }

    /** Uniqueness check for busID; part of B1. */
    public static void validateBusIdUnique(String busID,
                                           Set<String> existingIDs) {
        if (existingIDs.contains(busID)) {
            throw new ValidationException(
                    "B1: busID '" + busID + "' already exists "
                            + "(duplicates not allowed)");
        }
    }

    /** Ensure fuelType is one of the allowed values. */
    public static void validateFuelType(String fuelType) {
        if (!ALLOWED_FUEL_TYPES.contains(fuelType)) {
            throw new ValidationException(
                    "fuelType must be one of " + ALLOWED_FUEL_TYPES
                            + " (was '" + fuelType + "')");
        }
    }

    // =================================================================
    // Add-time validation entry point
    // =================================================================

    /**
     * Validate a brand-new Bus before it is added to the repository.
     * Enforces B1 plus value sanity checks (positive capacity,
     * non-negative fuel level, valid fuel type).
     */
    public static void validateForAdd(Bus b, Set<String> existingIDs) {
        Objects.requireNonNull(b, "bus must not be null");
        validateBusIdFormat(b.getBusID());
        validateBusIdUnique(b.getBusID(), existingIDs);
        validateFuelType(b.getFuelType());
        if (b.getCapacity() <= 0) {
            throw new ValidationException(
                    "capacity must be positive (was " + b.getCapacity() + ")");
        }
        if (b.getFuelLevel() < 0) {
            throw new ValidationException(
                    "fuelLevel must be non-negative "
                            + "(was " + b.getFuelLevel() + ")");
        }
    }

    // =================================================================
    // B2 - capacity update restriction
    // =================================================================

    /**
     * Validate an update against the existing bus.
     * Enforces B2 (capacity cannot increase).
     */
    public static void validateForUpdate(Bus existing, Bus updated) {
        Objects.requireNonNull(existing, "existing must not be null");
        Objects.requireNonNull(updated,  "updated must not be null");

        if (!Objects.equals(existing.getBusID(), updated.getBusID())) {
            throw new ValidationException(
                    "busID cannot be modified during update");
        }

        // B2: capacity cannot increase, only decrease (or stay equal)
        if (updated.getCapacity() > existing.getCapacity()) {
            throw new ValidationException(
                    "B2: capacity cannot increase during update "
                            + "(was " + existing.getCapacity()
                            + ", attempted " + updated.getCapacity() + ")");
        }

        validateFuelType(updated.getFuelType());
        if (updated.getCapacity() <= 0) {
            throw new ValidationException(
                    "capacity must be positive (was "
                            + updated.getCapacity() + ")");
        }
        if (updated.getFuelLevel() < 0) {
            throw new ValidationException(
                    "fuelLevel must be non-negative "
                            + "(was " + updated.getFuelLevel() + ")");
        }
    }

    // =================================================================
    // B3, B4, B5 - driver/bus assignment validation
    // =================================================================

    /**
     * Validate that the given driver may legally operate the given bus.
     * Enforces B3, B4, B5.
     *
     * @param driver the driver to assign
     * @param bus    the bus to be operated
     * @throws ValidationException on rule violation
     */
    public static void validateAssignment(Driver driver, Bus bus) {
        Objects.requireNonNull(driver, "driver must not be null");
        Objects.requireNonNull(bus,    "bus must not be null");

        // B3: drivers older than 50 cannot drive capacity >= 50
        if (driver.getAge() > 50 && bus.getCapacity() >= 50) {
            throw new ValidationException(
                    "B3: drivers older than 50 cannot operate buses "
                            + "with capacity 50 or more (driver age="
                            + driver.getAge() + ", capacity="
                            + bus.getCapacity() + ")");
        }

        // B4: only drivers with >= 5 years experience for electric buses
        if ("Electricity".equals(bus.getFuelType())
                && driver.getExperienceYears() < 5) {
            throw new ValidationException(
                    "B4: only drivers with at least 5 years of "
                            + "experience can operate electric buses "
                            + "(experience=" + driver.getExperienceYears() + ")");
        }

        // B5: only Heavy/PublicTransport license for electric or hybrid
        if (("Electricity".equals(bus.getFuelType())
                || "Hybrid".equals(bus.getFuelType()))
                && !("Heavy".equals(driver.getLicenseType())
                || "PublicTransport".equals(driver.getLicenseType()))) {
            throw new ValidationException(
                    "B5: only drivers with a Heavy or PublicTransport "
                            + "licence can operate electric or hybrid buses "
                            + "(licenseType=" + driver.getLicenseType()
                            + ", fuelType=" + bus.getFuelType() + ")");
        }
    }
}
