package com.busdriver.integration;

import com.busdriver.exception.ValidationException;
import com.busdriver.model.Driver;
import com.busdriver.repository.DriverRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Integration tests for DriverRepository.
// These run against a real TXT file on disk (JUnit's @TempDir gives us
// a fresh folder per test) and the real DriverValidator, so they cover
// Task 3 of the assignment.
// 5 tests: valid stored, invalid rejected, update persisted, count
// accurate, plus D4 enforced at the repo level.
@DisplayName("Driver Integration Tests")
class DriverIntegrationTest {

    @TempDir
    Path tempDir;

    private Path driversFile;
    private DriverRepository repo;

    @BeforeEach
    void setUp() {
        // a fresh file path per test - JUnit cleans it up after
        driversFile = tempDir.resolve("drivers.txt");
        repo = new DriverRepository(driversFile);
    }

    // small helper - build a valid driver with the bits the test cares about
    private Driver makeDriver(String id, String name, int years, String license) {
        return new Driver(id, name, years, license,
                "12|King St|Melbourne|VIC|Australia",
                "12-05-1990");
    }

    // 1) valid drivers should land in the TXT file and survive a reload
    @Test
    @DisplayName("IT-D1 valid drivers are persisted to the TXT file")
    void it_d1_validDriversArePersisted() throws Exception {
        Driver alice = makeDriver("29@!aaaaAB", "Alice", 3, "Light");
        Driver bob   = makeDriver("37#$bbbbCD", "Bob", 7, "Heavy");

        repo.add(alice);
        repo.add(bob);

        // file should exist and contain both records
        assertTrue(Files.exists(driversFile), "drivers.txt should exist");
        List<String> lines = Files.readAllLines(driversFile);
        assertTrue(lines.stream().anyMatch(l -> l.contains("29@!aaaaAB")),
                "file should contain Alice's record");
        assertTrue(lines.stream().anyMatch(l -> l.contains("37#$bbbbCD")),
                "file should contain Bob's record");

        // build a new repo on the same file - should see both drivers
        DriverRepository reloaded = new DriverRepository(driversFile);
        Optional<Driver> a = reloaded.retrieve("29@!aaaaAB");
        Optional<Driver> b = reloaded.retrieve("37#$bbbbCD");
        assertTrue(a.isPresent() && b.isPresent());
        assertEquals("Alice", a.get().getName());
        assertEquals(7, b.get().getExperienceYears());
    }

    // 2) invalid drivers should be rejected and never reach the file
    @Test
    @DisplayName("IT-D2 invalid drivers are rejected (not persisted)")
    void it_d2_invalidDriversRejected() throws Exception {
        Driver alice = makeDriver("29@!aaaaAB", "Alice", 3, "Light");
        repo.add(alice);

        // same ID -> D1 unique violation
        Driver duplicate = makeDriver("29@!aaaaAB", "Mallory", 1, "Light");
        assertThrows(ValidationException.class, () -> repo.add(duplicate));

        // address too short -> D2
        Driver badAddress = new Driver(
                "38@@xxxxAA", "Eve", 2, "Light",
                "12|King St|Melbourne|VIC",
                "12-05-1990");
        assertThrows(ValidationException.class, () -> repo.add(badAddress));

        // birthdate wrong format -> D3
        Driver badDob = new Driver(
                "38@@xxxxAA", "Eve", 2, "Light",
                "12|King St|Melbourne|VIC|Australia",
                "1990/05/12");
        assertThrows(ValidationException.class, () -> repo.add(badDob));

        // only the original Alice should be stored
        assertEquals(1, repo.count(), "only Alice should be stored");
        List<String> lines = Files.readAllLines(driversFile);
        assertFalse(lines.stream().anyMatch(l -> l.contains("Mallory")));
        assertFalse(lines.stream().anyMatch(l -> l.contains("38@@xxxxAA")));
    }

    // 3) updates should make it to disk
    @Test
    @DisplayName("IT-D3 update is persisted to disk and reload sees it")
    void it_d3_updatesPersisted() {
        Driver d = makeDriver("29@!aaaaAB", "Alice", 3, "Light");
        repo.add(d);

        // change licence + address (allowed because exp <= 10)
        Driver updated = new Driver(
                d.getDriverID(), d.getName(),
                d.getExperienceYears(),
                "Heavy",
                "99|Park St|Brisbane|QLD|Australia",
                d.getBirthdate());
        repo.update(updated);

        // open a fresh repo on the same file and check
        DriverRepository reloaded = new DriverRepository(driversFile);
        Driver fromDisk = reloaded.retrieve("29@!aaaaAB").orElseThrow();
        assertEquals("Heavy", fromDisk.getLicenseType());
        assertEquals("99|Park St|Brisbane|QLD|Australia", fromDisk.getAddress());
    }

    // 4) count() should reflect adds and ignore failed adds
    @Test
    @DisplayName("IT-D4 count() reflects add/reject operations")
    void it_d4_countIsAccurate() {
        assertEquals(0, repo.count(), "empty repo starts at 0");

        repo.add(makeDriver("29@!aaaaAB", "Alice", 3, "Light"));
        assertEquals(1, repo.count());

        repo.add(makeDriver("37#$bbbbCD", "Bob", 7, "Heavy"));
        assertEquals(2, repo.count());

        // duplicate add - should fail and NOT bump the count
        assertThrows(ValidationException.class, () ->
                repo.add(makeDriver("29@!aaaaAB", "X", 1, "Light")));
        assertEquals(2, repo.count(), "duplicate must not increment count");

        // reload from disk - still 2
        DriverRepository reloaded = new DriverRepository(driversFile);
        assertEquals(2, reloaded.count());
    }

    // 5) bonus - D4 lock works through the repo layer (not just validator)
    @Test
    @DisplayName("IT-D5 D4 license lock is enforced at repository level")
    void it_d5_d4EnforcedViaRepository() {
        Driver d = makeDriver("29@!aaaaAB", "Alice", 12, "Heavy");
        repo.add(d);

        // try to change licence - should fail because exp > 10
        Driver attempt = new Driver(
                d.getDriverID(), d.getName(),
                d.getExperienceYears(),
                "PublicTransport",
                d.getAddress(), d.getBirthdate());
        assertThrows(ValidationException.class, () -> repo.update(attempt));

        // file should still say Heavy
        Driver fromDisk = new DriverRepository(driversFile)
                .retrieve("29@!aaaaAB").orElseThrow();
        assertEquals("Heavy", fromDisk.getLicenseType());
    }
}
