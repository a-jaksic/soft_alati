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
import org.mockito.Mockito;
import rs.ac.bg.fon.aleksa_jaksic.sa.photo.domain.Photo;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.domain.Review;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PhotoTest {

    private Validator validator;
    private Review mockReview;
    private Restaurant mockRestaurant;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        mockReview = Mockito.mock(Review.class);
        mockRestaurant = Mockito.mock(Restaurant.class);
    }

    @ParameterizedTest
    @CsvSource({
            "/uploads/images/pic1.jpg",
            "C:/storage/photos/restaurant_front.png",
            "relative/path/to/file.png"
    })
    @DisplayName("Should pass field validation with valid paths")
    void validate_ValidData_NoViolations(String path) {
        Photo photo = Photo.builder()
                .id(1L)
                .filePath(path)
                .createdAt(LocalDateTime.now())
                .review(mockReview)
                .build();

        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("Should fail validation when file path is blank or null")
    void validate_InvalidFilePaths_HasViolations(String invalidPath) {
        Photo photo = Photo.builder()
                .filePath(invalidPath)
                .createdAt(LocalDateTime.now())
                .review(mockReview)
                .build();

        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("filePath")));
    }

    @Test
    @DisplayName("Should fail validation when creation timestamp is missing")
    void validate_NullCreatedAt_HasViolations() {
        Photo photo = Photo.builder()
                .filePath("/uploads/img.jpg")
                .createdAt(null)
                .review(mockReview)
                .build();

        Set<ConstraintViolation<Photo>> violations = validator.validate(photo);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("createdAt")));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when photo belongs to neither review nor restaurant")
    void validateConstraints_OrphanPhoto_ThrowsException() {
        Photo photo = Photo.builder()
                .filePath("/uploads/img.jpg")
                .createdAt(LocalDateTime.now())
                .review(null)
                .restaurant(null)
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, photo::validateConstraints);
        assertEquals("Photo must belong to either a Review or a Restaurant Gallery.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when photo is linked to both targets simultaneously")
    void validateConstraints_DualOwnedPhoto_ThrowsException() {
        Photo photo = Photo.builder()
                .filePath("/uploads/img.jpg")
                .createdAt(LocalDateTime.now())
                .review(mockReview)
                .restaurant(mockRestaurant)
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, photo::validateConstraints);
        assertEquals("Photo cannot be linked to both a Review and a Restaurant Gallery.", exception.getMessage());
    }

    @Test
    @DisplayName("Should run validateConstraints successfully when linked uniquely to a Restaurant")
    void validateConstraints_RestaurantOnly_Passes() {
        Photo photo = Photo.builder()
                .filePath("/uploads/img.jpg")
                .createdAt(LocalDateTime.now())
                .restaurant(mockRestaurant)
                .build();

        assertDoesNotThrow(photo::validateConstraints);
    }

    @Test
    @DisplayName("Should verify Lombok methods function perfectly")
    void testLombokMethods() {
        LocalDateTime time = LocalDateTime.now();
        Photo photo = new Photo();
        photo.setId(12L);
        photo.setFilePath("/var/www/img.png");
        photo.setCreatedAt(time);
        photo.setReview(mockReview);

        assertEquals(12L, photo.getId());
        assertEquals("/var/www/img.png", photo.getFilePath());
        assertEquals(time, photo.getCreatedAt());
        assertEquals(mockReview, photo.getReview());
    }
}
