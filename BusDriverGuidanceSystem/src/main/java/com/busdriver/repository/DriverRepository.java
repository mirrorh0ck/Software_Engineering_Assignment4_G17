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

// File-backed repository for drivers.
//
// Format: one driver per line, fields separated by '~'.
// We can't use '|' as the field separator because addresses already
// have '|' inside them (D2 format).
// Lines starting with '#' are comments so the file stays readable.
//
// Every add/update rewrites the whole file. A new instance loads
// from disk in its constructor.
public class DriverRepository {

    private static final String SEPARATOR = "~";

    private final Path filePath;

    // LinkedHashMap keeps insertion order so the file output is stable
    // (HashMap shuffled it around which made the demo video confusing)
    private final Map<String, Driver> drivers = new LinkedHashMap<>();

    public DriverRepository(String filePath) {
        this(Paths.get(filePath));
    }

    public DriverRepository(Path filePath) {
        this.filePath = filePath;
        load();
    }

    // ===== CRUD =====

    public void add(Driver d) {
        DriverValidator.validateForAdd(d, drivers.keySet());  // D1-D3
        drivers.put(d.getDriverID(), d);
        save();
    }

    public Optional<Driver> retrieve(String driverID) {
        return Optional.ofNullable(drivers.get(driverID));
    }

    // unmodifiable so callers can't sneak in changes
    public List<Driver> retrieveAll() {
        return Collections.unmodifiableList(new ArrayList<>(drivers.values()));
    }

    public void update(Driver updated) {
        if (updated == null) {
            throw new ValidationException("driver cannot be null");
        }
        Driver existing = drivers.get(updated.getDriverID());
        if (existing == null) {
            throw new ValidationException(
                    "driver with ID '" + updated.getDriverID() + "' not found");
        }
        DriverValidator.validateForUpdate(existing, updated);

        // Update the existing object's fields in place rather than swapping
        // references - any caller still holding the old reference sees the change.
        existing.setLicenseType(updated.getLicenseType());
        existing.setExperienceYears(updated.getExperienceYears());
        existing.setAddress(updated.getAddress());
        existing.setBirthdate(updated.getBirthdate());
        save();
    }

    public int count() {
        return drivers.size();
    }

    // used by integration tests to start from a clean slate
    public void clearAll() {
        drivers.clear();
        save();
    }

    // ===== file IO =====

    // Read the TXT file into memory. Bad lines are skipped silently so
    // hand-edits don't crash the program.
    private void load() {
        drivers.clear();
        if (!Files.exists(filePath)) {
            return;  // first run, nothing to read
        }
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                if (line == null || line.isBlank()) continue;
                if (line.startsWith("#")) continue;  // comment

                // limit -1 keeps trailing empties so partial records fail later
                String[] parts = line.split(SEPARATOR, -1);
                if (parts.length != 6) {
                    continue;  // wrong number of fields, skip
                }
                try {
                    int experience = Integer.parseInt(parts[2].trim());
                    Driver d = new Driver(
                            parts[0], parts[1], experience,
                            parts[3], parts[4], parts[5]);
                    drivers.put(d.getDriverID(), d);
                } catch (NumberFormatException nfe) {
                    // experience wasn't a number, skip the line
                }
            }
        } catch (IOException ex) {
            throw new ValidationException("could not load driver file: " + filePath, ex);
        }
    }

    // Write everything back out. First line is a header comment so anyone
    // opening the file knows what the fields are.
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
            throw new ValidationException("could not save driver file: " + filePath, ex);
        }
    }

    // little helpers used by tests
    public Path getFilePath() { return filePath; }
    public Map<String, Driver> snapshot() { return new HashMap<>(drivers); }
}
