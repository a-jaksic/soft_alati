package rs.ac.bg.fon.aleksa_jaksic.sa.review.service;

import jakarta.persistence.EntityNotFoundException;
import rs.ac.bg.fon.aleksa_jaksic.sa.files.FileSystemStorageService;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.repository.RestaurantRepository;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.domain.Review;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.dtos.ReviewCreateUpdateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.dtos.ReviewDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.dtos.ReviewDetailsDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.mapper.ReviewMapper;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.repository.ReviewRepository;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.domain.User;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;
    private final FileSystemStorageService fileSystemStorageService;


    public ReviewService(ReviewRepository reviewRepository, RestaurantRepository restaurantRepository, UserRepository userRepository, ReviewMapper reviewMapper, FileSystemStorageService fileSystemStorageService){
        this.reviewRepository = reviewRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.reviewMapper = reviewMapper;
        this.fileSystemStorageService = fileSystemStorageService;
    }

    public List<ReviewDTO> getCurrentUserReviews(Long id, String username) {
        List<Review>  reviews = reviewRepository.findByRestaurantIdAndUserUsername(id, username);

        return reviews.stream()
                .map(reviewMapper::toDTO)
                .toList();
    }

    public Page<ReviewDTO> getOtherReviews(Long id, String username, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByRestaurantIdAndUserUsernameNot(id, username, pageable);

        return reviews.map(reviewMapper::toDTO);
    }

    @Transactional
    public ReviewDetailsDTO create(Long id, String username,ReviewCreateUpdateDTO reviewCreateUpdateDTO) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No restaurant found for given review!"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("No user found for given review!"));

        Review review = reviewMapper.toEntity(reviewCreateUpdateDTO);
        review.setRestaurant(restaurant);
        review.setUser(user);
        review.setCreatedAt(LocalDateTime.now());

        restaurant.recalculateAfterReviewCreate(review.getRating());

        return reviewMapper.toDetailsDTO(reviewRepository.save(review));

    }

    public ReviewDetailsDTO getReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No review found with given id!"));
        return reviewMapper.toDetailsDTO(review);
    }

    @Transactional
    public ReviewDetailsDTO update(Long id, ReviewCreateUpdateDTO reviewCreateUpdateDTO) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No review found with given id!"));

        int oldRating = review.getRating();
        int newRating = reviewCreateUpdateDTO.rating();

        Restaurant restaurant = review.getRestaurant();
        restaurant.recalculateAfterReviewUpdate(oldRating,newRating);

        reviewMapper.updateEntityFromUpdateDto(reviewCreateUpdateDTO, review);
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toDetailsDTO(savedReview);
    }

    @Transactional
    public void delete(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("No review found with given id!"));
        try {
            Restaurant restaurant = review.getRestaurant();
            restaurant.recalculateAfterReviewDelete(review.getRating());

            Long restaurantId = restaurant.getId();
            fileSystemStorageService.deleteReviewFolder(restaurantId, reviewId);
            reviewRepository.delete(review);
        } catch (Exception e) {
            throw new EntityNotFoundException("Could not delete review with given id!");
        }
    }

}



