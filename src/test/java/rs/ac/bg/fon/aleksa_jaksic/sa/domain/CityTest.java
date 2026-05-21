package rs.ac.bg.fon.aleksa_jaksic.sa.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.domain.City;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CityTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @ParameterizedTest
    @CsvSource({
            "Beograd, 11000",
            "Novi Sad, 21000",
            "Niš, 18000",
            "Kragujevac, 34000"
    })
    @DisplayName("Should pass validation with valid city pairs")
    void validate_ValidData_NoViolations(String name, String postalCode) {
        City city = City.builder()
                .id(1L)
                .name(name)
                .postalCode(postalCode)
                .build();

        Set<ConstraintViolation<City>> violations = validator.validate(city);

        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("Should fail validation when city name is blank or null")
    void validate_InvalidNames_HasViolations(String invalidName) {
        City city = City.builder()
                .name(invalidName)
                .postalCode("11000")
                .build();

        Set<ConstraintViolation<City>> violations = validator.validate(city);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("Should fail validation when postal code is blank or null")
    void validate_InvalidPostalCodes_HasViolations(String invalidPostalCode) {
        City city = City.builder()
                .name("Belgrade")
                .postalCode(invalidPostalCode)
                .build();

        Set<ConstraintViolation<City>> violations = validator.validate(city);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("postalCode")));
    }

    @Test
    @DisplayName("Should verify Lombok getters, setters, and builder configurations work correctly")
    void testLombokMethods() {
        City city = new City();
        city.setId(5L);
        city.setName("Subotica");
        city.setPostalCode("24000");

        assertEquals(5L, city.getId());
        assertEquals("Subotica", city.getName());
        assertEquals("24000", city.getPostalCode());
    }

    @Test
    @DisplayName("Should verify AllArgsConstructor initializes every mapped field")
    void testAllArgsConstructor() {
        City city = new City(10L, "Zrenjanin", "23000");

        assertAll(
                () -> assertEquals(10L, city.getId()),
                () -> assertEquals("Zrenjanin", city.getName()),
                () -> assertEquals("23000", city.getPostalCode())
        );
    }
}
