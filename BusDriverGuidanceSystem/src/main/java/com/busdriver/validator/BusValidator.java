package com.busdriver.validator;

import com.busdriver.exception.ValidationException;
import com.busdriver.model.Bus;
import com.busdriver.model.Driver;

import java.util.Objects;
import java.util.Set;

// All the Bus rule checks (B1-B5) as static methods.
//   B1 - busID exactly 8 digits + unique
//   B2 - capacity can't grow on update
//   B3 - older than 50 -> can't drive capacity >= 50
//   B4 - electric bus -> driver needs >= 5 years
//   B5 - electric or hybrid -> driver needs Heavy or PublicTransport licence
public final class BusValidator {

    public static final Set<String> ALLOWED_FUEL_TYPES =
            Set.of("Diesel", "Hybrid", "Electricity");

    private BusValidator() { }

    // ===== B1: busID format =====
    public static void validateBusIdFormat(String busID) {
        if (busID == null) {
            throw new ValidationException("B1: busID cannot be null");
        }
        if (busID.length() != 8) {
            throw new ValidationException(
                    "B1: busID must be 8 chars long, got " + busID.length());
        }
        for (int i = 0; i < busID.length(); i++) {
            char c = busID.charAt(i);
            if (c < '0' || c > '9') {
                throw new ValidationException(
                        "B1: busID must be digits only, found '" + c + "' at pos " + i);
            }
        }
    }

    public static void validateBusIdUnique(String busID, Set<String> existingIDs) {
        if (existingIDs.contains(busID)) {
            throw new ValidationException(
                    "B1: busID '" + busID + "' already exists");
        }
    }

    public static void validateFuelType(String fuelType) {
        if (!ALLOWED_FUEL_TYPES.contains(fuelType)) {
            throw new ValidationException(
                    "fuelType must be one of " + ALLOWED_FUEL_TYPES
                            + ", got '" + fuelType + "'");
        }
    }

    // ===== used by Repo.add() =====
    public static void validateForAdd(Bus b, Set<String> existingIDs) {
        Objects.requireNonNull(b, "bus cannot be null");
        validateBusIdFormat(b.getBusID());
        validateBusIdUnique(b.getBusID(), existingIDs);
        validateFuelType(b.getFuelType());
        if (b.getCapacity() <= 0) {
            throw new ValidationException(
                    "capacity must be positive, got " + b.getCapacity());
        }
        if (b.getFuelLevel() < 0) {
            throw new ValidationException(
                    "fuelLevel cannot be negative, got " + b.getFuelLevel());
        }
    }

    // ===== B2: update check =====
    public static void validateForUpdate(Bus existing, Bus updated) {
        Objects.requireNonNull(existing, "existing cannot be null");
        Objects.requireNonNull(updated, "updated cannot be null");

        if (!Objects.equals(existing.getBusID(), updated.getBusID())) {
            throw new ValidationException("busID cannot be changed");
        }

        // B2: capacity cannot grow. Same value or smaller is fine.
        if (updated.getCapacity() > existing.getCapacity()) {
            throw new ValidationException(
                    "B2: capacity cannot increase (was " + existing.getCapacity()
                            + ", tried " + updated.getCapacity() + ")");
        }

        validateFuelType(updated.getFuelType());
        if (updated.getCapacity() <= 0) {
            throw new ValidationException(
                    "capacity must be positive, got " + updated.getCapacity());
        }
        if (updated.getFuelLevel() < 0) {
            throw new ValidationException(
                    "fuelLevel cannot be negative, got " + updated.getFuelLevel());
        }
    }

    // ===== B3 + B4 + B5: driver-bus assignment check =====
    // Called when we want to pair a driver with a bus.
    public static void validateAssignment(Driver driver, Bus bus) {
        Objects.requireNonNull(driver, "driver cannot be null");
        Objects.requireNonNull(bus, "bus cannot be null");

        // B3 - "older than 50" -> strict >, so age 50 itself is still allowed
        if (driver.getAge() > 50 && bus.getCapacity() >= 50) {
            throw new ValidationException(
                    "B3: drivers older than 50 cannot drive capacity >= 50"
                            + " (age=" + driver.getAge()
                            + ", capacity=" + bus.getCapacity() + ")");
        }

        // B4 - electric bus needs >= 5 years experience
        if ("Electricity".equals(bus.getFuelType())
                && driver.getExperienceYears() < 5) {
            throw new ValidationException(
                    "B4: electric bus needs >= 5 years experience, got "
                            + driver.getExperienceYears());
        }

        // B5 - electric / hybrid bus needs Heavy or PublicTransport
        if (("Electricity".equals(bus.getFuelType())
                || "Hybrid".equals(bus.getFuelType()))
                && !("Heavy".equals(driver.getLicenseType())
                || "PublicTransport".equals(driver.getLicenseType()))) {
            throw new ValidationException(
                    "B5: " + bus.getFuelType() + " bus needs Heavy or PublicTransport"
                            + " licence, got " + driver.getLicenseType());
        }
    }
}
