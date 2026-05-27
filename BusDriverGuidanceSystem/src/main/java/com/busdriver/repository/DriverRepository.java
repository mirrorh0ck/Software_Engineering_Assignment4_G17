package com.busdriver.repository;

import com.busdriver.exception.ValidationException;
import com.busdriver.model.Driver;
import com.busdriver.validator.DriverValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory repository for Driver entities.
 *
 * This version delegates all D1-D5 enforcement to
 * DriverValidator. Records are still kept in memory only;
 * persistence to a TXT file will be added in a later commit.
 *
 * Operations supported (per Assignment 4 spec):
 *
 *   - .add(Driver) - validate D1/D2/D3 then store.
 *   - .retrieve(String) - look up by driverID.
 *   - .update(Driver) - validate D4/D5 then store.
 *   - .count() - return the number of stored drivers.
 *
 */
public class DriverRepository {

    /** In-memory store keyed by driverID. */
    private final Map<String, Driver> drivers = new LinkedHashMap<>();

    /**
     * Add a new driver. Throws ValidationException if D1-D3
     * are violated or if the driverID already exists.
     *
     * @param d the candidate driver
     */
    public void add(Driver d) {
        DriverValidator.validateForAdd(d, drivers.keySet());
        drivers.put(d.getDriverID(), d);
    }

    /** Retrieve a driver by ID. */
    public Optional<Driver> retrieve(String driverID) {
        return Optional.ofNullable(drivers.get(driverID));
    }

    /** Return all drivers as an unmodifiable list snapshot. */
    public List<Driver> retrieveAll() {
        return Collections.unmodifiableList(new ArrayList<>(drivers.values()));
    }

    /**
     * Update an existing driver. The driverID is taken from
     * updated and must match an existing record (else
     * a ValidationException is thrown). D4 + D5 are enforced
     * before the in-memory record is mutated.
     *
     * @param updated new field values; driverID + name must equal existing
     */
    public void update(Driver updated) {
        if (updated == null) {
            throw new ValidationException("driver must not be null");
        }
        Driver existing = drivers.get(updated.getDriverID());
        if (existing == null) {
            throw new ValidationException(
                    "Driver with ID '" + updated.getDriverID()
                            + "' not found");
        }
        DriverValidator.validateForUpdate(existing, updated);

        // Mutate in-place rather than swapping the reference, so any
        // external caller holding the original Driver sees the change.
        existing.setLicenseType(updated.getLicenseType());
        existing.setExperienceYears(updated.getExperienceYears());
        existing.setAddress(updated.getAddress());
        existing.setBirthdate(updated.getBirthdate());
    }

    /** Return the number of stored drivers. */
    public int count() {
        return drivers.size();
    }
}
