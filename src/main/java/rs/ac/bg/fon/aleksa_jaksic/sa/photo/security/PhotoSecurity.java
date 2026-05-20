package rs.ac.bg.fon.aleksa_jaksic.sa.photo.security;

import rs.ac.bg.fon.aleksa_jaksic.sa.photo.domain.Photo;
import rs.ac.bg.fon.aleksa_jaksic.sa.photo.repository.PhotoRepository;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.domain.Review;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.repository.ReviewRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("photoSecurity")
public class PhotoSecurity {

    private final PhotoRepository photoRepository;
    private final ReviewRepository reviewRepository;

    public PhotoSecurity(PhotoRepository photoRepository, ReviewRepository reviewRepository){
        this.photoRepository = photoRepository;
        this.reviewRepository = reviewRepository;
    }

    public boolean isReviewOwner(Long id, Authentication authentication){
        Review review = reviewRepository.findById(id).orElse(null);
        if (review == null) {
            return false;
        }
        return review.getUser().getUsername().equals(authentication.getName());
    }

    public boolean isPhotoOwner(Long id, Authentication authentication){
        Photo photo = photoRepository.findById(id).orElse(null);
        if (photo == null){
            return  false;
        }
        if (photo.getReview() != null){
            return  photo.getReview().getUser().getUsername().equals(authentication.getName());
        }
        return false;
    }
}
