package com.busdriver.unit;

import com.busdriver.exception.ValidationException;
import com.busdriver.model.Bus;
import com.busdriver.model.Driver;
import com.busdriver.validator.BusValidator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for Bus-related business rules B1 to B5.
 *
 * Initial batch: one normal case and one invalid case per rule
 * (10 tests total). Edge cases and cross-rule sanity tests will
 * be added in the next commit.
 */
@DisplayName("Bus Unit Tests (B1-B5) - initial batch")
class BusUnitTest {

    /**
     * Build a Driver of the requested age in completed years.
     * Born on (today - age years - 30 days) so the age is stable
     * across days when the suite is run.
     */
    private static Driver driverOfAge(int age, int experienceYears,
                                      String licenseType) {
        LocalDate dob = LocalDate.now()
                .minusYears(age)
                .minusDays(30);
        String birthdate = dob.format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        return new Driver(
                "29@!aaaaAB", "Test Driver", experienceYears,
                licenseType,
                "10|Smith St|Sydney|NSW|Australia",
                birthdate);
    }

    // =================================================================
    // B1 - busID rules
    // =================================================================

    @Nested
    @DisplayName("B1 - busID format")
    class B1_BusIdRules {

        @Test
        @DisplayName("B1.1 valid 8-digit busID passes (normal)")
        void b1_1_validId() {
            assertDoesNotThrow(() ->
                    BusValidator.validateBusIdFormat("12345678"));
        }

        @Test
        @DisplayName("B1.2 7-digit or 9-digit busID is rejected (invalid)")
        void b1_2_wrongLength() {
            assertThrows(ValidationException.class, () ->
                    BusValidator.validateBusIdFormat("1234567"));
            assertThrows(ValidationException.class, () ->
                    BusValidator.validateBusIdFormat("123456789"));
        }
    }

    // =================================================================
    // B2 - capacity update restriction
    // =================================================================

    @Nested
    @DisplayName("B2 - capacity cannot increase on update")
    class B2_CapacityRules {

        @Test
        @DisplayName("B2.1 decreasing capacity passes (normal)")
        void b2_1_decreaseCapacity() {
            Bus existing = new Bus("12345678", 50, 30.0, "Diesel");
            Bus updated  = new Bus("12345678", 40, 30.0, "Diesel");
            assertDoesNotThrow(() ->
                    BusValidator.validateForUpdate(existing, updated));
        }

        @Test
        @DisplayName("B2.2 increasing capacity is rejected (invalid)")
        void b2_2_increaseCapacity() {
            Bus existing = new Bus("12345678", 40, 30.0, "Diesel");
            Bus updated  = new Bus("12345678", 50, 30.0, "Diesel");
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> BusValidator.validateForUpdate(existing, updated));
            assertTrue(ex.getMessage().contains("B2"));
        }
    }

    // =================================================================
    // B3 - driver age vs capacity
    // =================================================================

    @Nested
    @DisplayName("B3 - drivers older than 50 cannot drive large buses")
    class B3_DriverAgeRules {

        @Test
        @DisplayName("B3.1 young driver may drive large bus (normal)")
        void b3_1_youngDriverLargeBus() {
            Driver d = driverOfAge(30, 8, "Heavy");
            Bus    b = new Bus("12345678", 60, 50.0, "Diesel");
            assertDoesNotThrow(() ->
                    BusValidator.validateAssignment(d, b));
        }

        @Test
        @DisplayName("B3.2 60yo driver cannot drive capacity 50 (invalid)")
        void b3_2_oldDriverLargeBus() {
            Driver d = driverOfAge(60, 20, "Heavy");
            Bus    b = new Bus("12345678", 50, 50.0, "Diesel");
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> BusValidator.validateAssignment(d, b));
            assertTrue(ex.getMessage().contains("B3"));
        }
    }

    // =================================================================
    // B4 - electric bus requires >= 5 years experience
    // =================================================================

    @Nested
    @DisplayName("B4 - electric bus requires >= 5 years experience")
    class B4_ElectricBusRules {

        @Test
        @DisplayName("B4.1 experienced driver on electric bus (normal)")
        void b4_1_experiencedDriverElectric() {
            Driver d = driverOfAge(35, 6, "Heavy");
            Bus    b = new Bus("12345678", 40, 80.0, "Electricity");
            assertDoesNotThrow(() ->
                    BusValidator.validateAssignment(d, b));
        }

        @Test
        @DisplayName("B4.2 driver with 3y experience on electric bus is rejected")
        void b4_2_insufficientExperience() {
            Driver d = driverOfAge(35, 3, "Heavy");
            Bus    b = new Bus("12345678", 40, 80.0, "Electricity");
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> BusValidator.validateAssignment(d, b));
            assertTrue(ex.getMessage().contains("B4"));
        }
    }

    // =================================================================
    // B5 - electric/hybrid buses require Heavy or PublicTransport
    // =================================================================

    @Nested
    @DisplayName("B5 - license restriction for electric/hybrid")
    class B5_LicenseRules {

        @Test
        @DisplayName("B5.1 Heavy license on hybrid is allowed (normal)")
        void b5_1_heavyOnHybrid() {
            Driver d = driverOfAge(35, 8, "Heavy");
            Bus    b = new Bus("12345678", 40, 80.0, "Hybrid");
            assertDoesNotThrow(() ->
                    BusValidator.validateAssignment(d, b));
        }

        @Test
        @DisplayName("B5.2 Light license on electric bus is rejected")
        void b5_2_lightOnElectric() {
            Driver d = driverOfAge(35, 8, "Light");
            Bus    b = new Bus("12345678", 40, 80.0, "Electricity");
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> BusValidator.validateAssignment(d, b));
            assertTrue(ex.getMessage().contains("B5"));
        }
    }
}
