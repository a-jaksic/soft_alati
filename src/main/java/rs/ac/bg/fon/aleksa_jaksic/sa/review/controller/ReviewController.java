package rs.ac.bg.fon.aleksa_jaksic.sa.review.controller;

import rs.ac.bg.fon.aleksa_jaksic.sa.review.dtos.ReviewCreateUpdateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.dtos.ReviewDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.dtos.ReviewDetailsDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService){
        this.reviewService = reviewService;
    }


    @GetMapping("/api/restaurants/{id}/my-reviews")
    public ResponseEntity<?> getCurrentUserReviews(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            List<ReviewDTO> userReviewList = reviewService.getCurrentUserReviews(id, authentication.getName());
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(userReviewList);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the retrieval of reviews was not successful!");
        }
    }


    @GetMapping("/api/restaurants/{id}/reviews")
    public ResponseEntity<?> getOtherReviews(
            @PathVariable Long id,
            Authentication authentication,
            Pageable pageable
    ) {
        try {
            Page<ReviewDTO> reviewList = reviewService.getOtherReviews(id, authentication.getName(), pageable);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(reviewList);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the retrieval of reviews was not successful!");
        }
    }

    @PostMapping("/api/restaurants/{id}/reviews/create")
    public ResponseEntity<?> create(
            @PathVariable Long id,
            Authentication authentication,
            @RequestBody ReviewCreateUpdateDTO reviewCreateUpdateDTO
    ) {
        try {
            ReviewDetailsDTO review = reviewService.create(id, authentication.getName(), reviewCreateUpdateDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(review);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the review creation was not successful!");
        }
    }

    @GetMapping("/api/reviews/{id}")
    public ResponseEntity<?> getReview(@PathVariable Long id) {
        try {
            ReviewDetailsDTO review = reviewService.getReview(id);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(review);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the review retrieval was not successful!");
        }
    }

    @PreAuthorize("@reviewSecurity.isReviewOwner(#id, authentication)")
    @PatchMapping("/api/reviews/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody ReviewCreateUpdateDTO reviewCreateUpdateDTO
    ) {
        try {
            ReviewDetailsDTO review = reviewService.update(id, reviewCreateUpdateDTO);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(review);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the review update was not successful!");
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN') or @reviewSecurity.isReviewOwner(#id, authentication)")
    @DeleteMapping("/api/reviews/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            reviewService.delete(id);
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build();
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the review deletion was not successful!");
        }
    }

}
