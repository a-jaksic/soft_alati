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
import rs.ac.bg.fon.aleksa_jaksic.sa.user.domain.Role;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.domain.User;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should pass validation with perfectly valid user details")
    void validate_ValidUser_NoViolations() {
        User user = User.builder()
                .id(1L)
                .username("aleksa_j")
                .password("$2a$12$eImiTxAk4vmMbX7jw7x...HashedCryptoKey") // Mock hashed pass
                .email("aleksa.jaksic@fon.bg.ac.rs")
                .role(Role.USER)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("Should fail validation when critical text attributes are blank")
    void validate_BlankFields_HasViolations(String blankInput) {
        User invalidUser = User.builder()
                .username(blankInput)
                .password("validPassword123")
                .email("test@example.com")
                .role(Role.USER)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(invalidUser);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "a", "this_username_is_way_too_long_and_exceeds_the_fifty_character_limit_boundary"})
    @DisplayName("Should fail validation when username violates sizing rules")
    void validate_InvalidUsernameSize_HasViolations(String invalidUsername) {
        User user = User.builder()
                .username(invalidUsername)
                .password("validPassword123")
                .email("test@example.com")
                .role(Role.USER)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    @DisplayName("Should fail validation when password is shorter than minimum length")
    void validate_ShortPassword_HasViolations() {
        User user = User.builder()
                .username("validUser")
                .password("12345") // 5 characters - too short
                .email("test@example.com")
                .role(Role.USER)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"plainaddress", "#@%^%#$@#$@#.com", "@example.com", "Joe Smith <joe@example.com>", "email.example.com"})
    @DisplayName("Should fail validation when email string format is malformed")
    void validate_InvalidEmailFormat_HasViolations(String invalidEmail) {
        User user = User.builder()
                .username("validUser")
                .password("validPassword123")
                .email(invalidEmail)
                .role(Role.USER)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("Should fail validation when identity role is entirely missing")
    void validate_NullRole_HasViolations() {
        User user = User.builder()
                .username("validUser")
                .password("validPassword123")
                .email("test@example.com")
                .role(null)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }

    @Test
    @DisplayName("Should confirm Lombok implementations set and get values cleanly")
    void testLombokAndGettersSetters() {
        User user = new User();
        user.setId(505L);
        user.setUsername("example");
        user.setPassword("hashed_secret");
        user.setEmail("student@fon.bg.ac.rs");
        user.setRole(Role.ADMIN);

        assertEquals(505L, user.getId());
        assertEquals("example", user.getUsername());
        assertEquals("hashed_secret", user.getPassword());
        assertEquals("student@fon.bg.ac.rs", user.getEmail());
        assertEquals(Role.ADMIN, user.getRole());
    }
}
