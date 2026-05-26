package com.busdriver.repository;

import com.busdriver.model.Bus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory repository for {@link Bus} entities.
 *
 * <p>First cut - the four CRUD operations the brief asks for,
 * backed by a {@link LinkedHashMap}.  Validation of B1-B5 and
 * persistence to a TXT file will be added in later commits.</p>
 *
 * <p><b>TODO (later commits):</b></p>
 * <ul>
 *   <li>Plug in BusValidator for B1 (format + uniqueness).</li>
 *   <li>Enforce B2 (capacity cannot increase) on update.</li>
 *   <li>Add a driver-bus assignment operation that enforces
 *       B3 (age), B4 (electric experience) and B5 (license).</li>
 *   <li>Persist to buses.txt on every mutation.</li>
 * </ul>
 */
public class BusRepository {

    /** In-memory store keyed by busID. */
    private final Map<String, Bus> buses = new LinkedHashMap<>();

    /** Add a new bus (duplicate ID rejected; full B1 validation later). */
    public void add(Bus b) {
        if (b == null) {
            throw new IllegalArgumentException("bus must not be null");
        }
        if (buses.containsKey(b.getBusID())) {
            throw new IllegalStateException(
                    "busID already exists: " + b.getBusID());
        }
        buses.put(b.getBusID(), b);
    }

    /** Retrieve a bus by ID, or empty if not found. */
    public Optional<Bus> retrieve(String busID) {
        return Optional.ofNullable(buses.get(busID));
    }

    /** Return all buses as a list snapshot. */
    public List<Bus> retrieveAll() {
        return new ArrayList<>(buses.values());
    }

    /**
     * Replace the stored bus with the supplied one.
     * B2 (capacity cannot increase) will be enforced in a later commit.
     */
    public void update(Bus updated) {
        if (updated == null) {
            throw new IllegalArgumentException("bus must not be null");
        }
        if (!buses.containsKey(updated.getBusID())) {
            throw new IllegalStateException(
                    "Bus not found: " + updated.getBusID());
        }
        buses.put(updated.getBusID(), updated);
    }

    /** Return the number of stored buses. */
    public int count() {
        return buses.size();
    }
}
