package com.busdriver.integration;

import com.busdriver.exception.ValidationException;
import com.busdriver.model.Bus;
import com.busdriver.model.Driver;
import com.busdriver.repository.BusRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Integration tests for BusRepository, using a real TXT file via @TempDir
// and the real BusValidator. Covers Task 4 of the assignment.
// 5 tests.
@DisplayName("Bus Integration Tests")
class BusIntegrationTest {

    @TempDir
    Path tempDir;

    private Path busesFile;
    private BusRepository repo;

    @BeforeEach
    void setUp() {
        busesFile = tempDir.resolve("buses.txt");
        repo = new BusRepository(busesFile);
    }

    // helper - build a driver of the given age / experience / licence
    private Driver makeDriver(int age, int years, String license) {
        LocalDate dob = LocalDate.now().minusYears(age).minusDays(30);
        return new Driver(
                "29@!aaaaAB", "Test Driver", years, license,
                "10|Smith St|Sydney|NSW|Australia",
                dob.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
    }

    // 1) valid buses stored + round-trip
    @Test
    @DisplayName("IT-B1 valid buses are persisted to the TXT file")
    void it_b1_validBusesArePersisted() throws Exception {
        repo.add(new Bus("12345678", 40, 50.0, "Diesel"));
        repo.add(new Bus("87654321", 30, 80.0, "Hybrid"));

        assertTrue(Files.exists(busesFile));
        List<String> lines = Files.readAllLines(busesFile);
        assertTrue(lines.stream().anyMatch(l -> l.contains("12345678")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("87654321")));

        // reload check
        BusRepository reloaded = new BusRepository(busesFile);
        assertEquals(2, reloaded.count());
        assertEquals(40, reloaded.retrieve("12345678").orElseThrow().getCapacity());
        assertEquals("Hybrid",
                reloaded.retrieve("87654321").orElseThrow().getFuelType());
    }

    // 2) invalid buses rejected
    @Test
    @DisplayName("IT-B2 invalid buses are rejected (not persisted)")
    void it_b2_invalidBusesRejected() throws Exception {
        repo.add(new Bus("12345678", 40, 50.0, "Diesel"));

        // B1 duplicate
        assertThrows(ValidationException.class, () ->
                repo.add(new Bus("12345678", 30, 50.0, "Diesel")));
        // B1 wrong length
        assertThrows(ValidationException.class, () ->
                repo.add(new Bus("1234567", 30, 50.0, "Diesel")));
        // B1 non-digit
        assertThrows(ValidationException.class, () ->
                repo.add(new Bus("1234A678", 30, 50.0, "Diesel")));
        // bad fuel type
        assertThrows(ValidationException.class, () ->
                repo.add(new Bus("11112222", 30, 50.0, "Petrol")));

        assertEquals(1, repo.count(), "only the original bus should be stored");
        List<String> lines = Files.readAllLines(busesFile);
        assertFalse(lines.stream().anyMatch(l -> l.contains("Petrol")));
    }

    // 3) update persisted, and a B2 increase attempt does NOT change the file
    @Test
    @DisplayName("IT-B3 capacity decrease update is persisted")
    void it_b3_updatesPersisted() {
        repo.add(new Bus("12345678", 50, 50.0, "Diesel"));

        // 50 -> 35 is fine (B2 allows decrease)
        Bus updated = new Bus("12345678", 35, 60.0, "Diesel");
        repo.update(updated);

        BusRepository reloaded = new BusRepository(busesFile);
        Bus fromDisk = reloaded.retrieve("12345678").orElseThrow();
        assertEquals(35, fromDisk.getCapacity());
        assertEquals(60.0, fromDisk.getFuelLevel());

        // 35 -> 60 should be blocked by B2 and the file should still say 35
        Bus bigger = new Bus("12345678", 60, 60.0, "Diesel");
        assertThrows(ValidationException.class, () -> repo.update(bigger));

        Bus afterFail = new BusRepository(busesFile)
                .retrieve("12345678").orElseThrow();
        assertEquals(35, afterFail.getCapacity(),
                "failed update must not change the file");
    }

    // 4) count works for adds + rejected adds
    @Test
    @DisplayName("IT-B4 count() reflects add/reject operations")
    void it_b4_countIsAccurate() {
        assertEquals(0, repo.count());
        repo.add(new Bus("12345678", 40, 50.0, "Diesel"));
        repo.add(new Bus("87654321", 30, 80.0, "Hybrid"));
        repo.add(new Bus("11112222", 20, 10.0, "Electricity"));
        assertEquals(3, repo.count());

        assertThrows(ValidationException.class, () ->
                repo.add(new Bus("11112222", 25, 10.0, "Diesel")));
        assertEquals(3, repo.count());

        assertEquals(3, new BusRepository(busesFile).count());
    }

    // 5) bonus - assignDriver actually enforces B3/B4/B5
    @Test
    @DisplayName("IT-B5 assignDriver enforces B3/B4/B5 against TXT file")
    void it_b5_assignDriverEnforcesRules() {
        repo.add(new Bus("12345678", 60, 80.0, "Electricity"));

        // 2 years experience -> B4 fails
        Driver junior = makeDriver(30, 2, "Heavy");
        assertThrows(ValidationException.class, () ->
                repo.assignDriver("12345678", junior));

        // Light licence -> B5 fails (electric needs Heavy / PublicTransport)
        Driver wrongLicense = makeDriver(30, 10, "Light");
        assertThrows(ValidationException.class, () ->
                repo.assignDriver("12345678", wrongLicense));

        // ok one - should succeed and write the assignment to disk
        Driver ok = makeDriver(30, 10, "Heavy");
        repo.assignDriver("12345678", ok);

        Bus reloaded = new BusRepository(busesFile)
                .retrieve("12345678").orElseThrow();
        assertEquals("29@!aaaaAB", reloaded.getAssignedDriverID());
    }
}
