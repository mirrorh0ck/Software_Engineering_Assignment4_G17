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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Unit tests for the Bus rules B1-B5.
// 20 tests total: B1 x4, B2 x3, B3 x4, B4 x4, B5 x3 + 2 cross-rule.
@DisplayName("Bus Unit Tests (B1-B5)")
class BusUnitTest {

    // helper - standard valid diesel bus
    private static Bus validBus() {
        return new Bus("12345678", 40, 50.0, "Diesel");
    }

    // helper that builds a driver of the requested age.
    // Born = today - age - 30 days so the age stays the same any day we run.
    private static Driver driverOfAge(int age, int experienceYears, String licenseType) {
        LocalDate dob = LocalDate.now().minusYears(age).minusDays(30);
        String birthdate = dob.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        return new Driver(
                "29@!aaaaAB", "Test Driver", experienceYears,
                licenseType,
                "10|Smith St|Sydney|NSW|Australia",
                birthdate);
    }

    // ===== B1 =====
    @Nested
    @DisplayName("B1 - busID format and uniqueness")
    class B1_BusIdRules {

        @Test
        @DisplayName("B1.1 valid 8-digit busID passes (normal)")
        void b1_1_validId() {
            assertDoesNotThrow(() -> BusValidator.validateBusIdFormat("12345678"));
        }

        @Test
        @DisplayName("B1.2 7-digit or 9-digit busID is rejected (invalid)")
        void b1_2_wrongLength() {
            assertThrows(ValidationException.class, () ->
                    BusValidator.validateBusIdFormat("1234567"));
            assertThrows(ValidationException.class, () ->
                    BusValidator.validateBusIdFormat("123456789"));
        }

        @Test
        @DisplayName("B1.3 non-digit chars in busID are rejected (edge)")
        void b1_3_nonDigitChars() {
            // letter in the middle
            assertThrows(ValidationException.class, () ->
                    BusValidator.validateBusIdFormat("1234A678"));
            // hyphen
            assertThrows(ValidationException.class, () ->
                    BusValidator.validateBusIdFormat("1234-678"));
        }

        @Test
        @DisplayName("B1.4 duplicate busID is rejected (edge)")
        void b1_4_duplicateId() {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> BusValidator.validateBusIdUnique("12345678",
                            Set.of("12345678")));
            assertTrue(ex.getMessage().contains("B1"));
        }
    }

    // ===== B2 =====
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

        // boundary - equal capacity is fine
        @Test
        @DisplayName("B2.3 equal capacity passes (edge)")
        void b2_3_equalCapacity() {
            Bus existing = new Bus("12345678", 40, 30.0, "Diesel");
            Bus updated  = new Bus("12345678", 40, 25.0, "Diesel");
            assertDoesNotThrow(() ->
                    BusValidator.validateForUpdate(existing, updated));
        }
    }

    // ===== B3 =====
    @Nested
    @DisplayName("B3 - drivers older than 50 cannot drive large buses")
    class B3_DriverAgeRules {

        @Test
        @DisplayName("B3.1 young driver may drive large bus (normal)")
        void b3_1_youngDriverLargeBus() {
            Driver d = driverOfAge(30, 8, "Heavy");
            Bus    b = new Bus("12345678", 60, 50.0, "Diesel");
            assertDoesNotThrow(() -> BusValidator.validateAssignment(d, b));
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

        @Test
        @DisplayName("B3.3 51yo driver, capacity 49 is allowed (edge)")
        void b3_3_oldDriverSmallBus() {
            Driver d = driverOfAge(51, 20, "Heavy");
            Bus    b = new Bus("12345678", 49, 50.0, "Diesel");
            assertDoesNotThrow(() -> BusValidator.validateAssignment(d, b));
        }

        // exact boundary - age 50 is still allowed (rule says "older than 50")
        @Test
        @DisplayName("B3.4 exactly 50yo driver, capacity 50 is allowed (edge)")
        void b3_4_exactly50yoBoundary() {
            Driver d = driverOfAge(50, 20, "Heavy");
            Bus    b = new Bus("12345678", 50, 50.0, "Diesel");
            assertDoesNotThrow(() -> BusValidator.validateAssignment(d, b));
        }
    }

    // ===== B4 =====
    @Nested
    @DisplayName("B4 - electric bus requires >= 5 years experience")
    class B4_ElectricBusRules {

        @Test
        @DisplayName("B4.1 experienced driver on electric bus (normal)")
        void b4_1_experiencedDriverElectric() {
            // 6 years + Heavy licence -> all of B3/B4/B5 happy
            Driver d = driverOfAge(35, 6, "Heavy");
            Bus    b = new Bus("12345678", 40, 80.0, "Electricity");
            assertDoesNotThrow(() -> BusValidator.validateAssignment(d, b));
        }

        @Test
        @DisplayName("B4.2 driver with 3y experience on electric bus is rejected")
        void b4_2_insufficientExperience() {
            // 3 years - breaks B4
            Driver d = driverOfAge(35, 3, "Heavy");
            Bus    b = new Bus("12345678", 40, 80.0, "Electricity");
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> BusValidator.validateAssignment(d, b));
            assertTrue(ex.getMessage().contains("B4"));
        }

        @Test
        @DisplayName("B4.3 exactly 5y experience is allowed (boundary edge)")
        void b4_3_exactly5YearsBoundary() {
            Driver d = driverOfAge(35, 5, "Heavy");
            Bus    b = new Bus("12345678", 40, 80.0, "Electricity");
            assertDoesNotThrow(() -> BusValidator.validateAssignment(d, b));
        }

        @Test
        @DisplayName("B4.4 inexperienced driver on diesel bus is still OK")
        void b4_4_inexperiencedDieselOk() {
            // B4 only applies to electric buses, diesel doesn't care about exp
            Driver d = driverOfAge(35, 1, "Light");
            Bus    b = new Bus("12345678", 30, 80.0, "Diesel");
            assertDoesNotThrow(() -> BusValidator.validateAssignment(d, b));
        }
    }

    // ===== B5 =====
    @Nested
    @DisplayName("B5 - license restriction for electric/hybrid")
    class B5_LicenseRules {

        @Test
        @DisplayName("B5.1 Heavy license on hybrid is allowed (normal)")
        void b5_1_heavyOnHybrid() {
            Driver d = driverOfAge(35, 8, "Heavy");
            Bus    b = new Bus("12345678", 40, 80.0, "Hybrid");
            assertDoesNotThrow(() -> BusValidator.validateAssignment(d, b));
        }

        @Test
        @DisplayName("B5.2 Light license on electric bus is rejected")
        void b5_2_lightOnElectric() {
            // enough experience but wrong licence
            Driver d = driverOfAge(35, 8, "Light");
            Bus    b = new Bus("12345678", 40, 80.0, "Electricity");
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> BusValidator.validateAssignment(d, b));
            assertTrue(ex.getMessage().contains("B5"));
        }

        @Test
        @DisplayName("B5.3 PublicTransport license on diesel bus is OK (edge)")
        void b5_3_publicTransportOnDieselOk() {
            // B5 only restricts electric/hybrid - diesel is fine
            Driver d = driverOfAge(35, 8, "PublicTransport");
            Bus    b = new Bus("12345678", 30, 80.0, "Diesel");
            assertDoesNotThrow(() -> BusValidator.validateAssignment(d, b));
        }
    }

    // ===== composite checks for validateForAdd =====

    @Test
    @DisplayName("validateForAdd accepts a fully-valid bus")
    void validateForAdd_acceptsValidBus() {
        assertDoesNotThrow(() ->
                BusValidator.validateForAdd(validBus(), Set.of()));
    }

    @Test
    @DisplayName("validateForAdd rejects negative capacity")
    void validateForAdd_rejectsNegativeCapacity() {
        Bus b = new Bus("12345678", -5, 50.0, "Diesel");
        assertThrows(ValidationException.class,
                () -> BusValidator.validateForAdd(b, Set.of()));
    }
}
