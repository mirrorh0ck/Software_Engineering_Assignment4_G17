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

// File-backed repository for buses.
// Similar to DriverRepository but uses '|' as separator (Bus fields don't
// contain '|' so this is safe).
// Has an extra assignDriver(busID, driver) method - that's the only way
// to exercise rules B3 / B4 / B5.
public class BusRepository {

    private static final String SEPARATOR = "|";
    private static final String SEPARATOR_REGEX = "\\|";   // '|' is a regex metachar

    private final Path filePath;
    private final Map<String, Bus> buses = new LinkedHashMap<>();

    public BusRepository(String filePath) {
        this(Paths.get(filePath));
    }

    public BusRepository(Path filePath) {
        this.filePath = filePath;
        load();
    }

    // ===== CRUD =====

    public void add(Bus b) {
        BusValidator.validateForAdd(b, buses.keySet());  // B1
        buses.put(b.getBusID(), b);
        save();
    }

    public Optional<Bus> retrieve(String busID) {
        return Optional.ofNullable(buses.get(busID));
    }

    public List<Bus> retrieveAll() {
        return Collections.unmodifiableList(new ArrayList<>(buses.values()));
    }

    public void update(Bus updated) {
        if (updated == null) {
            throw new ValidationException("bus cannot be null");
        }
        Bus existing = buses.get(updated.getBusID());
        if (existing == null) {
            throw new ValidationException(
                    "bus with ID '" + updated.getBusID() + "' not found");
        }
        BusValidator.validateForUpdate(existing, updated);  // B2

        existing.setCapacity(updated.getCapacity());
        existing.setFuelLevel(updated.getFuelLevel());
        existing.setFuelType(updated.getFuelType());
        // only overwrite assignedDriver if a value was supplied, so an update
        // that doesn't mention the driver doesn't accidentally unassign them
        if (updated.getAssignedDriverID() != null) {
            existing.setAssignedDriverID(updated.getAssignedDriverID());
        }
        save();
    }

    public int count() {
        return buses.size();
    }

    // Assign a driver to a bus. This runs B3/B4/B5 first, then persists.
    public void assignDriver(String busID, Driver driver) {
        Bus bus = buses.get(busID);
        if (bus == null) {
            throw new ValidationException("bus with ID '" + busID + "' not found");
        }
        BusValidator.validateAssignment(driver, bus);
        bus.setAssignedDriverID(driver.getDriverID());
        save();
    }

    public void clearAll() {
        buses.clear();
        save();
    }

    // ===== file IO =====

    private void load() {
        buses.clear();
        if (!Files.exists(filePath)) return;

        try {
            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                if (line == null || line.isBlank()) continue;
                if (line.startsWith("#")) continue;

                String[] parts = line.split(SEPARATOR_REGEX, -1);
                // 4 fields when no driver assigned, 5 when assigned
                if (parts.length < 4 || parts.length > 5) continue;

                try {
                    int capacity = Integer.parseInt(parts[1].trim());
                    double fuelLevel = Double.parseDouble(parts[2].trim());
                    Bus b = new Bus(parts[0], capacity, fuelLevel, parts[3]);
                    if (parts.length == 5 && !"-".equals(parts[4])) {
                        b.setAssignedDriverID(parts[4]);
                    }
                    buses.put(b.getBusID(), b);
                } catch (NumberFormatException nfe) {
                    // numeric field parse failed, skip
                }
            }
        } catch (IOException ex) {
            throw new ValidationException("could not load bus file: " + filePath, ex);
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
            throw new ValidationException("could not save bus file: " + filePath, ex);
        }
    }

    public Path getFilePath() { return filePath; }
    public Map<String, Bus> snapshot() { return new HashMap<>(buses); }
}
