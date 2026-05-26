package com.busdriver.repository;

import com.busdriver.model.Driver;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory repository for {@link Driver} entities.
 *
 * <p>This is the very first version. It only offers the four
 * operations required by the brief (Add, Update, Retrieve, Count)
 * using a {@link LinkedHashMap}. Validation of D1-D5 and persistence
 * to a TXT file will be added in later commits.</p>
 *
 * <p><b>TODO (later commits):</b></p>
 * <ul>
 *   <li>Plug in DriverValidator for D1-D5.</li>
 *   <li>Persist to drivers.txt on every mutation; load on construction.</li>
 *   <li>Throw a dedicated ValidationException instead of generic
 *       runtime exceptions.</li>
 * </ul>
 */
public class DriverRepository {

    /** In-memory store keyed by driverID. */
    private final Map<String, Driver> drivers = new LinkedHashMap<>();

    /**
     * Add a new driver. For now this only checks for a duplicate ID.
     * Full D1-D3 validation will be enforced once DriverValidator
     * is introduced.
     */
    public void add(Driver d) {
        if (d == null) {
            throw new IllegalArgumentException("driver must not be null");
        }
        if (drivers.containsKey(d.getDriverID())) {
            // TODO: replace with ValidationException once available
            throw new IllegalStateException(
                    "driverID already exists: " + d.getDriverID());
        }
        drivers.put(d.getDriverID(), d);
    }

    /** Retrieve a driver by ID, or empty if not found. */
    public Optional<Driver> retrieve(String driverID) {
        return Optional.ofNullable(drivers.get(driverID));
    }

    /** Return all drivers as a list snapshot. */
    public List<Driver> retrieveAll() {
        return new ArrayList<>(drivers.values());
    }

    /**
     * Replace the stored driver with the supplied one.
     * D4/D5 enforcement (license lock for >10y, immutable ID/name)
     * will be added in a later commit.
     */
    public void update(Driver updated) {
        if (updated == null) {
            throw new IllegalArgumentException("driver must not be null");
        }
        if (!drivers.containsKey(updated.getDriverID())) {
            throw new IllegalStateException(
                    "Driver not found: " + updated.getDriverID());
        }
        drivers.put(updated.getDriverID(), updated);
    }

    /** Return the number of stored drivers. */
    public int count() {
        return drivers.size();
    }
}
