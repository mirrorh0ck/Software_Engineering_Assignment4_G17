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

/**
 * Unit tests for the Driver-related business rules D1 to D5.
 *
 * Each nested class targets exactly one rule and provides
 * at least three meaningful test cases that cover normal,
 * invalid, and edge-case inputs (as required by Assignment 4,
 * Task 1).
 *
 * Total unit tests in this class: 18
 * (D1: 4, D2: 3, D3: 3, D4: 4, D5: 4).
 */
@DisplayName("Driver Unit Tests (D1-D5)")
class DriverUnitTest {

    // -----------------------------------------------------------------
    // Reusable test fixtures.  Constructed via small factory methods so
    // that each test can override only the field it wants to vary,
    // keeping individual tests focused on the rule under test.
    // -----------------------------------------------------------------

    /** Build a valid Driver matching all D1-D5 rules. */
    private static Driver validDriver() {
        return new Driver(
                "29@!aaaaAB",           // D1: 2 digits 2-9, 2+ specials, 2 uppers
                "Alice Smith",          // name
                3,                      // experience years (< 10 for D4 freedom)
                "Light",                // licenseType
                "12|King St|Melbourne|VIC|Australia", // D2 format
                "12-05-1990");          // D3 format
    }

    /** Build a valid Driver with >10 years of experience for D4 cases. */
    private static Driver experiencedDriver() {
        return new Driver(
                "37#$bbbbCD", "Bob Lee", 12, "Heavy",
                "10|Smith St|Sydney|NSW|Australia",
                "01-01-1975");
    }

    // =================================================================
    // D1 - driverID rules
    // =================================================================

    @Nested
    @DisplayName("D1 - driverID format rules")
    class D1_DriverIdRules {

        /** Normal case: a perfectly valid ID is accepted. */
        @Test
        @DisplayName("D1.1 valid driverID passes (normal case)")
        void d1_1_validId() {
            // "29@!aaaaAB" - digit-digit, two specials, two uppers
            assertDoesNotThrow(() ->
                    DriverValidator.validateDriverIdFormat("29@!aaaaAB"));
        }

        /** Invalid: too short / too long. */
        @Test
        @DisplayName("D1.2 wrong length is rejected (invalid input)")
        void d1_2_wrongLength() {
            // 9 chars - too short
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateDriverIdFormat("29@!aaaaA"));
            // 11 chars - too long
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateDriverIdFormat("29@!aaaaABC"));
        }

        /** Edge: first two chars must be digits 2-9, not 0/1. */
        @Test
        @DisplayName("D1.3 first two chars '1' or '0' are rejected (edge)")
        void d1_3_firstTwoCharsDigit2to9() {
            // '1' is below the allowed 2-9 range
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateDriverIdFormat("19@!aaaaAB"));
            // '0' is below the allowed 2-9 range
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateDriverIdFormat("09@!aaaaAB"));
        }

        /**
         * Edge: D1 mandates at least 2 special chars between positions
         * 3-8.  An ID with only 1 special must fail; an ID with exactly
         * 2 specials sits on the boundary and must pass.
         */
        @Test
        @DisplayName("D1.4 specials boundary (1 fails, exactly 2 passes)")
        void d1_4_specialsBoundary() {
            // Only one special (#) - must fail
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateDriverIdFormat("23#bbbbbAB"));
            // Exactly two specials (#$) - boundary success
            assertDoesNotThrow(() ->
                    DriverValidator.validateDriverIdFormat("23#$bbbbAB"));
        }
    }

    // =================================================================
    // D2 - address format rules
    // =================================================================

    @Nested
    @DisplayName("D2 - address format rules")
    class D2_AddressRules {

        /** Normal case: full 5-part address passes. */
        @Test
        @DisplayName("D2.1 valid 5-part address passes (normal)")
        void d2_1_validAddress() {
            assertDoesNotThrow(() ->
                    DriverValidator.validateAddressFormat(
                            "10|Smith St|Sydney|NSW|Australia"));
        }

        /** Invalid: missing components. */
        @Test
        @DisplayName("D2.2 missing component is rejected (invalid)")
        void d2_2_missingComponent() {
            // only 4 parts
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateAddressFormat(
                            "10|Smith St|Sydney|NSW"));
            // trailing empty country
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateAddressFormat(
                            "10|Smith St|Sydney|NSW|"));
        }

        /** Edge: too many parts (6+) is rejected. */
        @Test
        @DisplayName("D2.3 extra component is rejected (edge)")
        void d2_3_extraComponent() {
            // 6 parts - one too many
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateAddressFormat(
                            "10|Smith St|Sydney|NSW|Australia|Extra"));
        }
    }

    // =================================================================
    // D3 - birthdate format rules
    // =================================================================

    @Nested
    @DisplayName("D3 - birthdate format rules")
    class D3_BirthdateRules {

        /** Normal case: DD-MM-YYYY accepted. */
        @Test
        @DisplayName("D3.1 valid DD-MM-YYYY passes (normal)")
        void d3_1_validBirthdate() {
            assertDoesNotThrow(() ->
                    DriverValidator.validateBirthdateFormat("12-05-1990"));
        }

        /** Invalid: wrong separator or order. */
        @Test
        @DisplayName("D3.2 wrong format is rejected (invalid)")
        void d3_2_wrongFormat() {
            // YYYY-MM-DD instead of DD-MM-YYYY
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateBirthdateFormat("1990-05-12"));
            // slashes instead of dashes
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateBirthdateFormat("12/05/1990"));
        }

        /**
         * Edge: a future date is structurally well-formed but is
         * rejected because a driver cannot be born in the future.
         */
        @Test
        @DisplayName("D3.3 future birthdate is rejected (edge)")
        void d3_3_futureBirthdate() {
            assertThrows(ValidationException.class, () ->
                    DriverValidator.validateBirthdateFormat("12-05-2999"));
        }
    }

    // =================================================================
    // D4 - license update restriction for experienced drivers
    // =================================================================

    @Nested
    @DisplayName("D4 - license update restriction (>10y experience)")
    class D4_LicenseUpdateRules {

        /** Normal case: driver with 3y experience may change license. */
        @Test
        @DisplayName("D4.1 driver with low experience may change license")
        void d4_1_lowExperienceMayChangeLicense() {
            Driver existing = validDriver();              // 3 years
            Driver updated  = new Driver(
                    existing.getDriverID(), existing.getName(),
                    existing.getExperienceYears(),
                    "Medium",                              // changed
                    existing.getAddress(), existing.getBirthdate());
            assertDoesNotThrow(() ->
                    DriverValidator.validateForUpdate(existing, updated));
        }

        /** Invalid: 12y experience cannot change license. */
        @Test
        @DisplayName("D4.2 driver with 12y experience cannot change license")
        void d4_2_highExperienceCannotChangeLicense() {
            Driver existing = experiencedDriver();        // 12y, "Heavy"
            Driver updated  = new Driver(
                    existing.getDriverID(), existing.getName(),
                    existing.getExperienceYears(),
                    "PublicTransport",                     // attempted change
                    existing.getAddress(), existing.getBirthdate());
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> DriverValidator.validateForUpdate(existing, updated));
            assertTrue(ex.getMessage().contains("D4"));
        }

        /**
         * Edge: exactly 10 years - D4 says "more than 10", so 10
         * is still allowed to change license.
         */
        @Test
        @DisplayName("D4.3 driver with exactly 10y may change license (edge)")
        void d4_3_exactlyTenYearsBoundary() {
            Driver existing = new Driver(
                    "29@!aaaaAB", "Eve", 10, "Light",
                    "10|Smith St|Sydney|NSW|Australia", "01-01-1985");
            Driver updated  = new Driver(
                    existing.getDriverID(), existing.getName(), 10,
                    "Heavy",
                    existing.getAddress(), existing.getBirthdate());
            assertDoesNotThrow(() ->
                    DriverValidator.validateForUpdate(existing, updated));
        }

        /**
         * Edge: 11 years experience - "more than 10" - update is forbidden.
         */
        @Test
        @DisplayName("D4.4 driver with 11y experience cannot change license (edge)")
        void d4_4_elevenYearsCannotChangeLicense() {
            Driver existing = new Driver(
                    "29@!aaaaAB", "Eve", 11, "Light",
                    "10|Smith St|Sydney|NSW|Australia", "01-01-1985");
            Driver updated  = new Driver(
                    existing.getDriverID(), existing.getName(), 11,
                    "Heavy",
                    existing.getAddress(), existing.getBirthdate());
            assertThrows(ValidationException.class,
                    () -> DriverValidator.validateForUpdate(existing, updated));
        }
    }

    // =================================================================
    // D5 - immutable fields (driverID, name)
    // =================================================================

    @Nested
    @DisplayName("D5 - immutable driverID and name on update")
    class D5_ImmutableFieldRules {

        /** Normal case: updating only mutable fields succeeds. */
        @Test
        @DisplayName("D5.1 updating address/birthdate only succeeds")
        void d5_1_mutableFieldUpdateOk() {
            Driver existing = validDriver();
            Driver updated  = new Driver(
                    existing.getDriverID(), existing.getName(),
                    existing.getExperienceYears(),
                    existing.getLicenseType(),
                    "99|New St|Brisbane|QLD|Australia",   // changed
                    "01-01-1989");                          // changed
            assertDoesNotThrow(() ->
                    DriverValidator.validateForUpdate(existing, updated));
        }

        /** Invalid: changing driverID is forbidden. */
        @Test
        @DisplayName("D5.2 changing driverID throws (invalid)")
        void d5_2_cannotChangeDriverId() {
            Driver existing = validDriver();
            Driver updated  = new Driver(
                    "39@!aaaaAB",                          // different ID
                    existing.getName(),
                    existing.getExperienceYears(),
                    existing.getLicenseType(),
                    existing.getAddress(),
                    existing.getBirthdate());
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> DriverValidator.validateForUpdate(existing, updated));
            assertTrue(ex.getMessage().contains("D5"));
        }

        /** Invalid: changing name is forbidden. */
        @Test
        @DisplayName("D5.3 changing name throws (invalid)")
        void d5_3_cannotChangeName() {
            Driver existing = validDriver();
            Driver updated  = new Driver(
                    existing.getDriverID(),
                    "Different Name",                      // changed
                    existing.getExperienceYears(),
                    existing.getLicenseType(),
                    existing.getAddress(),
                    existing.getBirthdate());
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> DriverValidator.validateForUpdate(existing, updated));
            assertTrue(ex.getMessage().contains("D5"));
        }

        /**
         * Edge: address update with the same value (no-op) must still
         * succeed - validator should not flag unchanged data.
         */
        @Test
        @DisplayName("D5.4 no-op update succeeds (edge)")
        void d5_4_noOpUpdate() {
            Driver existing = validDriver();
            Driver updated  = new Driver(
                    existing.getDriverID(), existing.getName(),
                    existing.getExperienceYears(),
                    existing.getLicenseType(),
                    existing.getAddress(), existing.getBirthdate());
            assertDoesNotThrow(() ->
                    DriverValidator.validateForUpdate(existing, updated));
        }
    }

    // =================================================================
    // Cross-rule sanity: validateForAdd composes all of D1, D2, D3.
    // =================================================================

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
        ValidationException ex = assertThrows(ValidationException.class,
                () -> DriverValidator.validateForAdd(
                        d, Set.of(d.getDriverID())));
        assertTrue(ex.getMessage().contains("D1"));
        assertEquals(true,
                ex.getMessage().toLowerCase().contains("already exists"));
    }
}
