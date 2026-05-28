package com.busdriver.repository;

import com.busdriver.exception.ValidationException;
import com.busdriver.model.Driver;
import com.busdriver.validator.DriverValidator;

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
 * TXT-file backed repository for Driver entities.
 *
 * Records are stored one-per-line using "~" as the field
 * separator (chosen because Driver addresses contain the "|"
 * character mandated by D2). Lines beginning with "#" are
 * treated as comments to keep the file human-readable.
 *
 * Each public mutator (add/update) immediately persists the
 * entire in-memory map back to disk so that a fresh repository
 * instance constructed against the same file sees the new state.
 *
 * Operations supported (per Assignment 4 spec):
 *
 *   - .add(Driver) - validate D1/D2/D3 and persist.
 *   - .retrieve(String) - look up by driverID.
 *   - .update(Driver) - validate D4/D5 and persist.
 *   - .count() - return the number of stored drivers.
 */
public class DriverRepository {

    /** Field separator inside the TXT file (chosen to avoid clash with D2). */
    private static final String SEPARATOR = "~";

    /** Underlying TXT file path. */
    private final Path filePath;

    /**
     * In-memory cache of drivers keyed by driverID. LinkedHashMap
     * preserves insertion order so the file output is deterministic
     * (helps when grading the demo video).
     */
    private final Map<String, Driver> drivers = new LinkedHashMap<>();

    /**
     * Build a repository against the given TXT file. If the file
     * does not yet exist it will be created on the first write;
     * an empty repository is returned in the meantime.
     *
     * @param filePath path to the TXT "database" file
     */
    public DriverRepository(String filePath) {
        this(Paths.get(filePath));
    }

    /** Path-based constructor; loads any existing data. */
    public DriverRepository(Path filePath) {
        this.filePath = filePath;
        load();
    }

    // =================================================================
    // CRUD operations
    // =================================================================

    /**
     * Add a new driver. Throws ValidationException if D1-D3
     * are violated or if the driverID already exists.
     */
    public void add(Driver d) {
        DriverValidator.validateForAdd(d, drivers.keySet());
        drivers.put(d.getDriverID(), d);
        save();
    }

    /** Retrieve a driver by ID. */
    public Optional<Driver> retrieve(String driverID) {
        return Optional.ofNullable(drivers.get(driverID));
    }

    /** Return all stored drivers as an unmodifiable list snapshot. */
    public List<Driver> retrieveAll() {
        return Collections.unmodifiableList(new ArrayList<>(drivers.values()));
    }

    /**
     * Update an existing driver. The driverID is taken from
     * updated and must match an existing record (else
     * a ValidationException is thrown). D4 + D5 are enforced
     * before the in-memory record is replaced and the file rewritten.
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
        save();
    }

    /** Return the number of stored drivers. */
    public int count() {
        return drivers.size();
    }

    /**
     * Clear the in-memory cache AND the underlying file.
     * Used by integration tests to ensure clean fixtures.
     */
    public void clearAll() {
        drivers.clear();
        save();
    }

    // =================================================================
    // File I/O
    // =================================================================

    /**
     * Load the TXT file into the in-memory map. Invalid / malformed
     * lines are silently skipped to keep the repository robust when
     * the file has been hand-edited.
     */
    private void load() {
        drivers.clear();
        if (!Files.exists(filePath)) {
            return; // first run - nothing to load
        }
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                if (line == null || line.isBlank()) continue;
                if (line.startsWith("#")) continue; // comment

                // Limit -1 keeps any trailing empty fields, so parsing
                // a partially-blank record fails the validator rather
                // than silently truncating.
                String[] parts = line.split(SEPARATOR, -1);
                if (parts.length != 6) {
                    continue; // skip malformed
                }
                try {
                    int experience = Integer.parseInt(parts[2].trim());
                    Driver d = new Driver(
                            parts[0], parts[1], experience,
                            parts[3], parts[4], parts[5]);
                    drivers.put(d.getDriverID(), d);
                } catch (NumberFormatException nfe) {
                    // skip - experienceYears not parseable
                }
            }
        } catch (IOException ex) {
            throw new ValidationException(
                    "Failed to load driver file: " + filePath, ex);
        }
    }

    /**
     * Persist the in-memory map back to the TXT file. Each driver is
     * written on its own line preceded by a single comment header
     * for human readability.
     */
    private void save() {
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            List<String> lines = new ArrayList<>();
            lines.add("# Driver records "
                    + "(driverID~name~experienceYears~licenseType~address~birthdate)");
            for (Driver d : drivers.values()) {
                lines.add(d.toRecord());
            }
            Files.write(filePath, lines);
        } catch (IOException ex) {
            throw new ValidationException(
                    "Failed to save driver file: " + filePath, ex);
        }
    }

    /** Test helper: expose the underlying file path. */
    public Path getFilePath() {
        return filePath;
    }

    /** Test helper: snapshot of in-memory ID set. */
    public Map<String, Driver> snapshot() {
        return new HashMap<>(drivers);
    }
}
