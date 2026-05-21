package rs.ac.bg.fon.aleksa_jaksic.sa.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.domain.RestaurantType;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantTypeTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Italian", "Mexican", "Asian Fusion", "Traditional Serbian", "Vegan"})
    @DisplayName("Should pass validation with valid restaurant type names")
    void validate_ValidNames_NoViolations(String validName) {
        RestaurantType type = RestaurantType.builder()
                .id(1L)
                .name(validName)
                .build();

        Set<ConstraintViolation<RestaurantType>> violations = validator.validate(type);

        assertTrue(violations.isEmpty(), "Valid restaurant type name should not produce violations");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("Should fail validation when name is blank or null")
    void validate_BlankOrNullNames_HasViolations(String invalidName) {
        RestaurantType type = RestaurantType.builder()
                .name(invalidName)
                .build();

        Set<ConstraintViolation<RestaurantType>> violations = validator.validate(type);

        assertFalse(violations.isEmpty(), "Blank or null names should cause a validation failure");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")),
                "Violation should point to the 'name' field");
    }

    @Test
    @DisplayName("Should fail validation when name string exceeds its character limit boundary")
    void validate_NameTooLong_HasViolations() {
        // Generating a string that exceeds 50 characters
        String longName = "A".repeat(51);

        RestaurantType type = RestaurantType.builder()
                .name(longName)
                .build();

        Set<ConstraintViolation<RestaurantType>> violations = validator.validate(type);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("too long")));
    }

    @Test
    @DisplayName("Should verify Lombok getters, setters, and builder configurations work correctly")
    void testLombokMethods() {
        RestaurantType type = new RestaurantType();
        type.setId(42L);
        type.setName("Barbecue");

        assertEquals(42L, type.getId());
        assertEquals("Barbecue", type.getName());
    }

    @Test
    @DisplayName("Should verify AllArgsConstructor initializes all properties properly")
    void testAllArgsConstructor() {
        RestaurantType type = new RestaurantType(3L, "Mediterranean");

        assertAll(
                () -> assertEquals(3L, type.getId()),
                () -> assertEquals("Mediterranean", type.getName())
        );
    }
}