package rs.ac.bg.fon.aleksa_jaksic.sa.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import rs.ac.bg.fon.aleksa_jaksic.sa.files.FileSystemStorageService;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.repository.RestaurantRepository;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.domain.Review;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.dtos.ReviewCreateUpdateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.dtos.ReviewDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.dtos.ReviewDetailsDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.mapper.ReviewMapper;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.repository.ReviewRepository;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.service.ReviewService;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.domain.User;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewMapper reviewMapper;
    @Mock
    private FileSystemStorageService fileSystemStorageService;

    @InjectMocks
    private ReviewService reviewService;

    private Review sampleReview;
    private Restaurant sampleRestaurant;
    private User sampleUser;

    private ReviewDTO sampleDTO;
    private ReviewDetailsDTO sampleDetailsDTO;
    private ReviewCreateUpdateDTO sampleFormDTO;
    private LocalDateTime fixedTime;

    @BeforeEach
    void setUp() {
        fixedTime = LocalDateTime.of(2026, 5, 22, 12, 0);

        sampleRestaurant = mock(Restaurant.class);

        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("a-jaksic");

        sampleReview = new Review();
        sampleReview.setId(100L);
        sampleReview.setRating(5);
        sampleReview.setCreatedAt(fixedTime);
        sampleReview.setRestaurant(sampleRestaurant);
        sampleReview.setUser(sampleUser);

        sampleFormDTO = new ReviewCreateUpdateDTO(5, "Excellent!", "Amazing food and atmosphere!");

        sampleDTO = new ReviewDTO(100L, "a-jaksic", 5, "Excellent!", fixedTime);

        sampleDetailsDTO = new ReviewDetailsDTO(100L, "a-jaksic", 5, "Excellent!", "Amazing food and atmosphere!", fixedTime);
    }

    @Test
    @DisplayName("Should return list of mapped ReviewDTOs for current user's restaurant reviews")
    void getCurrentUserReviews_ReviewsExist_ReturnsDTOList() {
        Long restaurantId = 10L;
        String username = "a-jaksic";
        when(reviewRepository.findByRestaurantIdAndUserUsername(restaurantId, username)).thenReturn(List.of(sampleReview));
        when(reviewMapper.toDTO(sampleReview)).thenReturn(sampleDTO);

        List<ReviewDTO> result = reviewService.getCurrentUserReviews(restaurantId, username);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("a-jaksic", result.get(0).username());
        assertEquals("Excellent!", result.get(0).title());
        assertNotNull(result.get(0).createdAt());
        verify(reviewRepository).findByRestaurantIdAndUserUsername(restaurantId, username);
    }

    @Test
    @DisplayName("Should return a paginated response of ReviewDTOs excluding the active user's reviews")
    void getOtherReviews_ValidCall_ReturnsMappedPage() {
        Long restaurantId = 10L;
        String username = "a-jaksic";
        Pageable pageable = PageRequest.of(0, 5);
        Page<Review> reviewPage = new PageImpl<>(List.of(sampleReview));

        when(reviewRepository.findByRestaurantIdAndUserUsernameNot(restaurantId, username, pageable)).thenReturn(reviewPage);
        when(reviewMapper.toDTO(sampleReview)).thenReturn(sampleDTO);

        Page<ReviewDTO> result = reviewService.getOtherReviews(restaurantId, username, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Excellent!", result.getContent().get(0).title());
        verify(reviewRepository).findByRestaurantIdAndUserUsernameNot(restaurantId, username, pageable);
    }

    @Test
    @DisplayName("Should trigger rating recalculation on restaurant entity and save new review record")
    void create_ValidContext_RecalculatesRatingAndSaves() {
        Long restaurantId = 10L;
        String username = "a-jaksic";

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(sampleRestaurant));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(sampleUser));
        when(reviewMapper.toEntity(sampleFormDTO)).thenReturn(sampleReview);
        when(reviewRepository.save(sampleReview)).thenReturn(sampleReview);
        when(reviewMapper.toDetailsDTO(sampleReview)).thenReturn(sampleDetailsDTO);

        ReviewDetailsDTO result = reviewService.create(restaurantId, username, sampleFormDTO);

        assertNotNull(result);
        assertEquals(sampleRestaurant, sampleReview.getRestaurant());
        assertEquals(sampleUser, sampleReview.getUser());
        assertNotNull(sampleReview.getCreatedAt());

        verify(sampleRestaurant).recalculateAfterReviewCreate(5);
        verify(reviewRepository).save(sampleReview);
    }

    @Test
    @DisplayName("Should crash creation and throw EntityNotFoundException if parent restaurant missing")
    void create_RestaurantNotFound_ThrowsEntityNotFoundException() {
        Long missingRestaurantId = 404L;
        when(restaurantRepository.findById(missingRestaurantId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                reviewService.create(missingRestaurantId, "a-jaksic", sampleFormDTO)
        );

        verifyNoInteractions(userRepository, reviewRepository);
        verify(reviewMapper, never()).toEntity(any(ReviewCreateUpdateDTO.class));
    }

    @Test
    @DisplayName("Should cleanly return mapped DetailsDTO when review matches specific ID lookup")
    void getReview_Exists_ReturnsDetailsDTO() {
        Long id = 100L;
        when(reviewRepository.findById(id)).thenReturn(Optional.of(sampleReview));
        when(reviewMapper.toDetailsDTO(sampleReview)).thenReturn(sampleDetailsDTO);

        ReviewDetailsDTO result = reviewService.getReview(id);

        assertNotNull(result);
        assertEquals(id, result.id());
        assertEquals("Excellent!", result.title());
        assertEquals("Amazing food and atmosphere!", result.description());
        verify(reviewRepository).findById(id);
    }

    @Test
    @DisplayName("Should apply changes in rating when updating existing review scores")
    void update_ReviewExists_RecalculatesAggregateAndSaves() {
        Long reviewId = 100L;
        ReviewCreateUpdateDTO updateForm = new ReviewCreateUpdateDTO(3, "Subpar experience", "Changed my mind, ok food.");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(sampleReview));
        when(reviewRepository.save(sampleReview)).thenReturn(sampleReview);
        when(reviewMapper.toDetailsDTO(sampleReview)).thenReturn(sampleDetailsDTO);

        ReviewDetailsDTO result = reviewService.update(reviewId, updateForm);

        assertNotNull(result);
        verify(sampleRestaurant).recalculateAfterReviewUpdate(5, 3);
        verify(reviewMapper).updateEntityFromUpdateDto(updateForm, sampleReview);
        verify(reviewRepository).save(sampleReview);
    }

    @Test
    @DisplayName("Should do rating reduction to restaurant and purge files from system when deleting review")
    void delete_ValidContext_UpdatesAggregateAndCleansStorage() {
        Long reviewId = 100L;
        when(sampleRestaurant.getId()).thenReturn(10L);
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(sampleReview));

        assertAll(() -> reviewService.delete(reviewId));

        verify(sampleRestaurant).recalculateAfterReviewDelete(5);
        verify(fileSystemStorageService).deleteReviewFolder(10L, 100L);
        verify(reviewRepository).delete(sampleReview);
    }

    @Test
    @DisplayName("Should wrap runtime errors into detailed EntityNotFoundException if folder deletion faults")
    void delete_StorageFails_ThrowsEntityNotFoundException() {
        Long reviewId = 100L;
        when(sampleRestaurant.getId()).thenReturn(10L);
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(sampleReview));
        doThrow(new RuntimeException("IO Lock")).when(fileSystemStorageService).deleteReviewFolder(10L, 100L);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                reviewService.delete(reviewId)
        );

        assertEquals("Could not delete review with given id!", exception.getMessage());
        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @ParameterizedTest
    @ValueSource(longs = {99L, 999L})
    @DisplayName("Should throw EntityNotFoundException on getReview when review is missing")
    void getReview_ReviewNotFound_ThrowsEntityNotFoundException(Long missingId) {
        when(reviewRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reviewService.getReview(missingId));

        verifyNoInteractions(fileSystemStorageService, reviewMapper);
    }

    @ParameterizedTest
    @ValueSource(longs = {99L, 999L})
    @DisplayName("Should throw EntityNotFoundException on update when review is missing")
    void update_ReviewNotFound_ThrowsEntityNotFoundException(Long missingId) {
        when(reviewRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reviewService.update(missingId, sampleFormDTO));

        verifyNoInteractions(fileSystemStorageService, reviewMapper);
    }

    @ParameterizedTest
    @ValueSource(longs = {99L, 999L})
    @DisplayName("Should throw EntityNotFoundException on delete when review is missing")
    void delete_ReviewNotFound_ThrowsEntityNotFoundException(Long missingId) {
        when(reviewRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reviewService.delete(missingId));

        verifyNoInteractions(fileSystemStorageService, reviewMapper);
    }
}
