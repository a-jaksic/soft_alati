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
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.domain.RestaurantType;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantTest {

    private Validator validator;
    private static final double EPSILON = 0.00001;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should pass validation with perfectly valid inputs")
    void validate_ValidRestaurant_NoViolations() {
        Restaurant restaurant = Restaurant.builder()
                .name("Lorenzo & Kakalamba")
                .address("Cvijiceva 110")
                .latitude(44.8143)
                .longitude(20.4792)
                .phoneNum("+381 11 3295351")
                .website("https://lorenzokakalamba.com")
                .build();

        Set<ConstraintViolation<Restaurant>> violations = validator.validate(restaurant);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("Should fail validation when name or address is blank")
    void validate_BlankNameOrAddress_HasViolations(String blankInput) {
        Restaurant invalidName = Restaurant.builder()
                .name(blankInput)
                .address("Valid Address")
                .latitude(44.0)
                .longitude(20.0)
                .build();

        Set<ConstraintViolation<Restaurant>> violations = validator.validate(invalidName);
        assertFalse(violations.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "91.0, 20.0",   // Out of bounds Latitude
            "-90.1, 20.0",  // Out of bounds Latitude
            "44.0, 180.1",  // Out of bounds Longitude
            "44.0, -180.5"  // Out of bounds Longitude
    })
    @DisplayName("Should fail validation when coordinates cross geographic boundaries")
    void validate_InvalidCoordinates_HasViolations(Double lat, Double lon) {
        Restaurant restaurant = Restaurant.builder()
                .name("Gusto E Sapore")
                .address("Spanskih boraca 24")
                .latitude(lat)
                .longitude(lon)
                .build();

        Set<ConstraintViolation<Restaurant>> violations = validator.validate(restaurant);
        assertFalse(violations.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0.0, 5, 1, 5.0",       // First review calculation
            "1, 5.0, 3, 2, 4.0",       // Incremental review update
            "3, 4.0, 4, 4, 4.0",       // Maintaining average consistency
            "4, 4.5, 1, 5, 3.8"        // Fractional breakdown shifts
    })
    @DisplayName("Should correctly calculate state changes on review creation sequences")
    void recalculateAfterReviewCreate_ValidSequence_CalculatesExpected(
            int initialCount, double initialAvg, int newRating, int expectedCount, double expectedAvg) {

        Restaurant restaurant = Restaurant.builder()
                .reviewCount(initialCount)
                .avgRating(initialAvg)
                .build();

        restaurant.recalculateAfterReviewCreate(newRating);

        assertEquals(expectedCount, restaurant.getReviewCount());
        assertEquals(expectedAvg, restaurant.getAvgRating(), EPSILON);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 5.0, 5, 1, 1.0",       // Critical downgrade transition
            "2, 4.0, 3, 5, 5.0",       // Upgraded review shift
            "4, 4.5, 5, 1, 3.5"        // Complex score balancing
    })
    @DisplayName("Should correctly calculate metrics when a user updates an existing score")
    void recalculateAfterReviewUpdate_ValidSequence_CalculatesExpected(
            int initialCount, double initialAvg, int oldRating, int newRating, double expectedAvg) {

        Restaurant restaurant = Restaurant.builder()
                .reviewCount(initialCount)
                .avgRating(initialAvg)
                .build();

        restaurant.recalculateAfterReviewUpdate(oldRating, newRating);

        assertEquals(initialCount, restaurant.getReviewCount()); // Count should remain stationary
        assertEquals(expectedAvg, restaurant.getAvgRating(), EPSILON);
    }

    @ParameterizedTest
    @CsvSource({
            "3, 4.0, 4, 2, 4.0",       // Normal removal calculation
            "2, 4.5, 5, 1, 4.0",       // Falling down to a singular score
            "1, 4.0, 4, 0, 0.0",       // Complete reset down to zero items
            "0, 0.0, 5, 0, 0.0"        // Handling safety boundaries
    })
    @DisplayName("Should correctly drop review footprint metrics upon record deletions")
    void recalculateAfterReviewDelete_ValidSequence_CalculatesExpected(
            int initialCount, double initialAvg, int ratingToRemove, int expectedCount, double expectedAvg) {

        Restaurant restaurant = Restaurant.builder()
                .reviewCount(initialCount)
                .avgRating(initialAvg)
                .build();

        restaurant.recalculateAfterReviewDelete(ratingToRemove);

        assertEquals(expectedCount, restaurant.getReviewCount());
        assertEquals(expectedAvg, restaurant.getAvgRating(), EPSILON);
    }

    @Test
    @DisplayName("Should confirm Lombok implementations link up architecture objects properly")
    void testLombokAndRelationships() {
        City mockCity = City.builder().name("Belgrade").build();
        RestaurantType mockType = RestaurantType.builder().name("Italian").build();

        Restaurant restaurant = new Restaurant();
        restaurant.setId(7L);
        restaurant.setName("Toro");
        restaurant.setCity(mockCity);
        restaurant.setRestaurantType(mockType);

        assertEquals(7L, restaurant.getId());
        assertEquals("Toro", restaurant.getName());
        assertEquals(mockCity, restaurant.getCity());
        assertEquals(mockType, restaurant.getRestaurantType());
    }
}
