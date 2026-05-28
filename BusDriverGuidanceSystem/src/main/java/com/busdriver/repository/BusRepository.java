package com.busdriver.repository;

import com.busdriver.exception.ValidationException;
import com.busdriver.model.Bus;
import com.busdriver.model.Driver;
import com.busdriver.validator.BusValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TXT-file backed repository for Bus entities.
 *
 * Records are stored one-per-line using "|" as the field
 * separator (safe because Bus fields do not contain "|" themselves).
 * Lines beginning with "#" are treated as comments.
 *
 * Operations supported (per Assignment 4 spec):
 *
 *   - .add(Bus) - validate B1 and persist.
 *   - .retrieve(String) - look up by busID.
 *   - .update(Bus) - validate B2 and persist.
 *   - .count() - return the number of stored buses.
 *   - .assignDriver(String, Driver) - validate B3/B4/B5
 *       and link a driver to the bus.
 */
public class BusRepository {

    /** Field separator inside the TXT file. */
    private static final String SEPARATOR = "|";

    /** Regex form of the separator for split() calls. */
    private static final String SEPARATOR_REGEX = "\\|";

    /** Underlying TXT file path. */
    private final Path filePath;

    /** In-memory cache keyed by busID. */
    private final Map<String, Bus> buses = new LinkedHashMap<>();

    public BusRepository(String filePath) {
        this(Paths.get(filePath));
    }

    public BusRepository(Path filePath) {
        this.filePath = filePath;
        load();
    }

    // =================================================================
    // CRUD operations
    // =================================================================

    /** Add a new bus; validate B1 and persist. */
    public void add(Bus b) {
        BusValidator.validateForAdd(b, buses.keySet());
        buses.put(b.getBusID(), b);
        save();
    }

    /** Retrieve a bus by ID. */
    public Optional<Bus> retrieve(String busID) {
        return Optional.ofNullable(buses.get(busID));
    }

    /** Return all stored buses as an unmodifiable list snapshot. */
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
        save();
    }

    /** Return the number of stored buses. */
    public int count() {
        return buses.size();
    }

    /**
     * Assign a driver to a bus, enforcing B3, B4, B5 first.
     * The driver-bus link is then persisted to the TXT file.
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
        save();
    }

    /** Clear both in-memory map and underlying file. */
    public void clearAll() {
        buses.clear();
        save();
    }

    // =================================================================
    // File I/O
    // =================================================================

    private void load() {
        buses.clear();
        if (!Files.exists(filePath)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                if (line == null || line.isBlank()) continue;
                if (line.startsWith("#")) continue;

                String[] parts = line.split(SEPARATOR_REGEX, -1);
                if (parts.length < 4 || parts.length > 5) {
                    continue;
                }
                try {
                    int capacity = Integer.parseInt(parts[1].trim());
                    double fuelLevel = Double.parseDouble(parts[2].trim());
                    Bus b = new Bus(parts[0], capacity, fuelLevel, parts[3]);
                    if (parts.length == 5 && !"-".equals(parts[4])) {
                        b.setAssignedDriverID(parts[4]);
                    }
                    buses.put(b.getBusID(), b);
                } catch (NumberFormatException nfe) {
                    // malformed numeric field - skip
                }
            }
        } catch (IOException ex) {
            throw new ValidationException(
                    "Failed to load bus file: " + filePath, ex);
        }
    }

    private void save() {
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            List<String> lines = new ArrayList<>();
            lines.add("# Bus records "
                    + "(busID|capacity|fuelLevel|fuelType|assignedDriverID)");
            for (Bus b : buses.values()) {
                lines.add(b.toRecord());
            }
            Files.write(filePath, lines);
        } catch (IOException ex) {
            throw new ValidationException(
                    "Failed to save bus file: " + filePath, ex);
        }
    }

    /** Test helper: expose the underlying file path. */
    public Path getFilePath() {
        return filePath;
    }

    /** Test helper: snapshot of in-memory map. */
    public Map<String, Bus> snapshot() {
        return new HashMap<>(buses);
    }
}
