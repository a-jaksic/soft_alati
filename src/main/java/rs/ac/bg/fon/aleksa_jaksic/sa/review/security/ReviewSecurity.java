package rs.ac.bg.fon.aleksa_jaksic.sa.review.security;

import rs.ac.bg.fon.aleksa_jaksic.sa.review.domain.Review;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.repository.ReviewRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("reviewSecurity")
public class ReviewSecurity {

    private final ReviewRepository reviewRepository;

    public ReviewSecurity(ReviewRepository reviewRepository){
        this.reviewRepository = reviewRepository;
    }

    public boolean isReviewOwner(Long id, Authentication authentication){
        Review review = reviewRepository.findById(id).orElse(null);
        if (review == null) {
            return false;
        }
        return review.getUser().getUsername().equals(authentication.getName());
    }
}
