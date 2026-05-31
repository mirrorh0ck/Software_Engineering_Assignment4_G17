package com.busdriver.unit;

import com.busdriver.exception.ValidationException;
import com.busdriver.model.Driver;
import com.busdriver.validator.DriverValidator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Unit tests for the Driver rules D1-D5.
// Each rule has at least 3 tests (normal / invalid / edge) as required.
// 20 tests total: D1 x4, D2 x3, D3 x3, D4 x4, D5 x4 + 2 cross-rule.
//
// @Nested classes group tests by rule so the surefire output is easy to read.
@DisplayName("Driver Unit Tests (D1-D5)")
class DriverUnitTest {

    // helper - a valid driver we can reuse and tweak
    private static Driver validDriver() {
        return new Driver(
                "29@!aaaaAB",                             // valid per D1
                "Alice Smith",
                3,                                         // <= 10 so D4 doesn't kick in
                "Light",
                "12|King St|Melbourne|VIC|Australia",     // D2 format
                "12-05-1990");                             // D3 format
    }

    // helper for D4 - a driver with 12 years experience
    private static Driver experiencedDriver() {
        return new Driver(
                "37#$bbbbCD", "Bob Lee", 12, "Heavy",
                "10|Smith St|Sydney|NSW|Australia",
                "01-01-1975");
    }

    // ===== D1 =====
    @Nested
    @DisplayName("D1 - driverID format rules")
    class D1_DriverIdRules {

        @Test
        @DisplayName("D1.1 valid driverID passes (normal)")
        void d1_1_validId() {
            // "29@!aaaaAB" satisfies all four parts of D1
            assertDoesNotThrow(() ->
                    DriverValidator.validateDriverIdFormat("29@!aaaaAB"));
        }

        @Test
        @DisplayName("D1.2 wrong length is rejected (invalid)")
        void d1_2_wrongLength() {
            // one char short
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateDriverIdFormat("29@!aaaaA"));
            // one char too long
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateDriverIdFormat("29@!aaaaABC"));
        }

        @Test
        @DisplayName("D1.3 first two chars '1' or '0' are rejected (edge)")
        void d1_3_firstTwoCharsDigit2to9() {
            // 1 is below the 2-9 range
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateDriverIdFormat("19@!aaaaAB"));
            // same for 0
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateDriverIdFormat("09@!aaaaAB"));
        }

        @Test
        @DisplayName("D1.4 specials boundary (1 fails, exactly 2 passes)")
        void d1_4_specialsBoundary() {
            // only one '#' in the middle -> fail
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateDriverIdFormat("23#bbbbbAB"));
            // exactly two specials '#$' -> boundary, should pass
            assertDoesNotThrow(() ->
                    DriverValidator.validateDriverIdFormat("23#$bbbbAB"));
        }
    }

    // ===== D2 =====
    @Nested
    @DisplayName("D2 - address format rules")
    class D2_AddressRules {

        @Test
        @DisplayName("D2.1 valid 5-part address passes (normal)")
        void d2_1_validAddress() {
            assertDoesNotThrow(() ->
                    DriverValidator.validateAddressFormat(
                            "10|Smith St|Sydney|NSW|Australia"));
        }

        @Test
        @DisplayName("D2.2 missing component is rejected (invalid)")
        void d2_2_missingComponent() {
            // only 4 parts (no country)
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateAddressFormat(
                            "10|Smith St|Sydney|NSW"));
            // trailing '|' with empty country
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateAddressFormat(
                            "10|Smith St|Sydney|NSW|"));
        }

        @Test
        @DisplayName("D2.3 extra component is rejected (edge)")
        void d2_3_extraComponent() {
            // 6 parts - one too many
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateAddressFormat(
                            "10|Smith St|Sydney|NSW|Australia|Extra"));
        }
    }

    // ===== D3 =====
    @Nested
    @DisplayName("D3 - birthdate format rules")
    class D3_BirthdateRules {

        @Test
        @DisplayName("D3.1 valid DD-MM-YYYY passes (normal)")
        void d3_1_validBirthdate() {
            assertDoesNotThrow(() ->
                    DriverValidator.validateBirthdateFormat("12-05-1990"));
        }

        @Test
        @DisplayName("D3.2 wrong format is rejected (invalid)")
        void d3_2_wrongFormat() {
            // ISO style YYYY-MM-DD is wrong
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateBirthdateFormat("1990-05-12"));
            // slashes instead of dashes
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateBirthdateFormat("12/05/1990"));
        }

        @Test
        @DisplayName("D3.3 future birthdate is rejected (edge)")
        void d3_3_futureBirthdate() {
            // year 2999 is obviously in the future
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateBirthdateFormat("12-05-2999"));
        }
    }

    // ===== D4 =====
    @Nested
    @DisplayName("D4 - license update restriction (>10y experience)")
    class D4_LicenseUpdateRules {

        @Test
        @DisplayName("D4.1 driver with low experience may change license")
        void d4_1_lowExperienceMayChangeLicense() {
            Driver existing = validDriver();                  // 3 years
            Driver updated = new Driver(
                    existing.getDriverID(), existing.getName(),
                    existing.getExperienceYears(),
                    "Medium",                                  // Light -> Medium
                    existing.getAddress(), existing.getBirthdate());
            assertDoesNotThrow(() ->
                    DriverValidator.validateForUpdate(existing, updated));
        }

        @Test
        @DisplayName("D4.2 driver with 12y experience cannot change license")
        void d4_2_highExperienceCannotChangeLicense() {
            Driver existing = experiencedDriver();            // 12y, Heavy
            Driver updated = new Driver(
                    existing.getDriverID(), existing.getName(),
                    existing.getExperienceYears(),
                    "PublicTransport",                         // try to change
                    existing.getAddress(), existing.getBirthdate());
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> DriverValidator.validateForUpdate(existing, updated));
            assertTrue(ex.getMessage().contains("D4"));
        }

        // boundary - exactly 10 years should still allow the change (strict >)
        @Test
        @DisplayName("D4.3 driver with exactly 10y may change license (edge)")
        void d4_3_exactlyTenYearsBoundary() {
            Driver existing = new Driver(
                    "29@!aaaaAB", "Eve", 10, "Light",
                    "10|Smith St|Sydney|NSW|Australia", "01-01-1985");
            Driver updated = new Driver(
                    existing.getDriverID(), existing.getName(), 10,
                    "Heavy",
                    existing.getAddress(), existing.getBirthdate());
            assertDoesNotThrow(() ->
                    DriverValidator.validateForUpdate(existing, updated));
        }

        // one over the line - 11 years is blocked
        @Test
        @DisplayName("D4.4 driver with 11y experience cannot change license (edge)")
        void d4_4_elevenYearsCannotChangeLicense() {
            Driver existing = new Driver(
                    "29@!aaaaAB", "Eve", 11, "Light",
                    "10|Smith St|Sydney|NSW|Australia", "01-01-1985");
            Driver updated = new Driver(
                    existing.getDriverID(), existing.getName(), 11,
                    "Heavy",
                    existing.getAddress(), existing.getBirthdate());
            assertThrows(ValidationException.class,
                    () -> DriverValidator.validateForUpdate(existing, updated));
        }
    }

    // ===== D5 =====
    @Nested
    @DisplayName("D5 - driverID and name are immutable on update")
    class D5_ImmutableFieldRules {

        @Test
        @DisplayName("D5.1 updating address/birthdate only succeeds")
        void d5_1_mutableFieldUpdateOk() {
            Driver existing = validDriver();
            Driver updated = new Driver(
                    existing.getDriverID(), existing.getName(),
                    existing.getExperienceYears(),
                    existing.getLicenseType(),
                    "99|New St|Brisbane|QLD|Australia",       // changed
                    "01-01-1989");                              // changed
            assertDoesNotThrow(() ->
                    DriverValidator.validateForUpdate(existing, updated));
        }

        @Test
        @DisplayName("D5.2 changing driverID throws (invalid)")
        void d5_2_cannotChangeDriverId() {
            Driver existing = validDriver();
            Driver updated = new Driver(
                    "39@!aaaaAB",                              // different ID
                    existing.getName(),
                    existing.getExperienceYears(),
                    existing.getLicenseType(),
                    existing.getAddress(),
                    existing.getBirthdate());
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> DriverValidator.validateForUpdate(existing, updated));
            assertTrue(ex.getMessage().contains("D5"));
        }

        @Test
        @DisplayName("D5.3 changing name throws (invalid)")
        void d5_3_cannotChangeName() {
            Driver existing = validDriver();
            Driver updated = new Driver(
                    existing.getDriverID(),
                    "Different Name",                          // changed
                    existing.getExperienceYears(),
                    existing.getLicenseType(),
                    existing.getAddress(),
                    existing.getBirthdate());
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> DriverValidator.validateForUpdate(existing, updated));
            assertTrue(ex.getMessage().contains("D5"));
        }

        // no-op update - nothing changed - should still be allowed
        @Test
        @DisplayName("D5.4 no-op update succeeds (edge)")
        void d5_4_noOpUpdate() {
            Driver existing = validDriver();
            Driver updated = new Driver(
                    existing.getDriverID(), existing.getName(),
                    existing.getExperienceYears(),
                    existing.getLicenseType(),
                    existing.getAddress(), existing.getBirthdate());
            assertDoesNotThrow(() ->
                    DriverValidator.validateForUpdate(existing, updated));
        }
    }

    // ===== composite checks for validateForAdd =====

    @Test
    @DisplayName("validateForAdd accepts a fully-valid driver")
    void validateForAdd_acceptsValidDriver() {
        assertDoesNotThrow(() ->
                DriverValidator.validateForAdd(validDriver(), Set.of()));
    }

    @Test
    @DisplayName("validateForAdd rejects duplicate driverID (D1 unique)")
    void validateForAdd_rejectsDuplicate() {
        Driver d = validDriver();
        // simulate the repo already containing this ID
        ValidationException ex = assertThrows(ValidationException.class,
                () -> DriverValidator.validateForAdd(d, Set.of(d.getDriverID())));
        assertTrue(ex.getMessage().contains("D1"));
        assertEquals(true,
                ex.getMessage().toLowerCase().contains("already exists"));
    }
}
