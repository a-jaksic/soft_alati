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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.domain.Review;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.domain.User;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReviewTest {

    private Validator validator;
    private User mockUser;
    private Restaurant mockRestaurant;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        mockUser = Mockito.mock(User.class);
        mockRestaurant = Mockito.mock(Restaurant.class);
    }

    @ParameterizedTest
    @CsvSource({
            "5, Excellent food!, The cevapi were absolutely amazing. Service was quick.",
            "1, Awful service, Waited forty-five minutes just for appetizers. Will not come back.",
            "3, Average place, Decent ambience but the pasta carbonara lacked seasoning."
    })
    @DisplayName("Should pass validation with perfectly structured reviews")
    void validate_ValidReviews_NoViolations(int score, String title, String description) {
        Review review = Review.builder()
                .id(1L)
                .rating(score)
                .title(title)
                .description(description)
                .createdAt(LocalDateTime.now())
                .user(mockUser)
                .restaurant(mockRestaurant)
                .build();

        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("Should pass validation for all valid score tiers")
    void validate_ValidRatings_NoViolations(int validRating) {
        Review review = Review.builder()
                .rating(validRating)
                .title("Valid Title")
                .description("Valid description text about the restaurant dining experience.")
                .createdAt(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 6, 100})
    @DisplayName("Should fail validation when rating is completely out of limits")
    void validate_InvalidRatings_HasViolations(int invalidRating) {
        Review review = Review.builder()
                .rating(invalidRating)
                .title("Valid Title")
                .description("Valid description text.")
                .createdAt(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("rating")));
    }

    @ParameterizedTest
    @CsvSource({
            "'', 'Valid description text.'",
            "' ', 'Valid description text.'",
            "'Valid Title', ''",
            "'Valid Title', ' '"
    })
    @DisplayName("Should fail validation when title or description text fields are empty/blank")
    void validate_BlankTextFields_HasViolations(String title, String description) {
        Review review = Review.builder()
                .rating(4)
                .title(title)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail validation when creation timestamp is completely missing")
    void validate_NullCreatedAt_HasViolations() {
        Review review = Review.builder()
                .rating(5)
                .title("Fantastic")
                .description("Everything tasted amazing.")
                .createdAt(null)
                .build();

        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("createdAt")));
    }

    @Test
    @DisplayName("Should verify Lombok mappings handle model structural setups efficiently")
    void testLombokAndAssociations() {
        LocalDateTime timestamp = LocalDateTime.now();
        Review review = new Review();
        review.setId(88L);
        review.setRating(4);
        review.setTitle("Great view");
        review.setCreatedAt(timestamp);
        review.setUser(mockUser);
        review.setRestaurant(mockRestaurant);

        assertEquals(88L, review.getId());
        assertEquals(4, review.getRating());
        assertEquals("Great view", review.getTitle());
        assertEquals(timestamp, review.getCreatedAt());
        assertEquals(mockUser, review.getUser());
        assertEquals(mockRestaurant, review.getRestaurant());
    }
}
