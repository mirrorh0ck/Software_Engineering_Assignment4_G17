package com.busdriver.repository;

import com.busdriver.exception.ValidationException;
import com.busdriver.model.Bus;
import com.busdriver.model.Driver;
import com.busdriver.validator.BusValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory repository for Bus entities.
 *
 * This version delegates all B1-B5 enforcement to
 * BusValidator. Records are still kept in memory only;
 * persistence to a TXT file will be added in a later commit.
 *
 * Operations supported (per Assignment 4 spec):
 *
 *   - .add(Bus) - validate B1 then store.
 *   - .retrieve(String) - look up by busID.
 *   - .update(Bus) - validate B2 then store.
 *   - .count() - return the number of stored buses.
 *   - .assignDriver(String, Driver) - validate B3/B4/B5
 *       then link a driver to the bus.
 *
 */
public class BusRepository {

    /** In-memory store keyed by busID. */
    private final Map<String, Bus> buses = new LinkedHashMap<>();

    /** Add a new bus; validate B1 then store. */
    public void add(Bus b) {
        BusValidator.validateForAdd(b, buses.keySet());
        buses.put(b.getBusID(), b);
    }

    /** Retrieve a bus by ID. */
    public Optional<Bus> retrieve(String busID) {
        return Optional.ofNullable(buses.get(busID));
    }

    /** Return all buses as an unmodifiable list snapshot. */
    public List<Bus> retrieveAll() {
        return Collections.unmodifiableList(new ArrayList<>(buses.values()));
    }

    /**
     * Update an existing bus. busID is taken from updated
     * and must match an existing record. B2 (capacity cannot
     * increase) is enforced.
     */
    public void update(Bus updated) {
        if (updated == null) {
            throw new ValidationException("bus must not be null");
        }
        Bus existing = buses.get(updated.getBusID());
        if (existing == null) {
            throw new ValidationException(
                    "Bus with ID '" + updated.getBusID() + "' not found");
        }
        BusValidator.validateForUpdate(existing, updated);

        existing.setCapacity(updated.getCapacity());
        existing.setFuelLevel(updated.getFuelLevel());
        existing.setFuelType(updated.getFuelType());
        if (updated.getAssignedDriverID() != null) {
            existing.setAssignedDriverID(updated.getAssignedDriverID());
        }
    }

    /** Return the number of stored buses. */
    public int count() {
        return buses.size();
    }

    /**
     * Assign a driver to a bus, enforcing B3, B4, B5 first.
     *
     * @param busID  ID of the target bus (must exist)
     * @param driver the candidate driver
     */
    public void assignDriver(String busID, Driver driver) {
        Bus bus = buses.get(busID);
        if (bus == null) {
            throw new ValidationException(
                    "Bus with ID '" + busID + "' not found");
        }
        BusValidator.validateAssignment(driver, bus);
        bus.setAssignedDriverID(driver.getDriverID());
    }
}
