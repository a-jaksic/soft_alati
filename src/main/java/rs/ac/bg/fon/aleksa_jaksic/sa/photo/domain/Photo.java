package rs.ac.bg.fon.aleksa_jaksic.sa.photo.domain;

import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.domain.Review;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * Represents a photo uploaded for a review or a restaurant gallery.
 * Enforces a unique constraint on the file path to prevent duplicate files.
 * @author Aleksa Jaksic (a-jaksic)
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "photos", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"filePath"})
})
public class Photo {

    /**
     * Unique ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The file path where the photo is stored locally or on the server.
     */
    private String filePath;

    /**
     * The timestamp indicating when the photo record was created.
     */
    private LocalDateTime createdAt;

    /**
     * The review associated with this photo, if applicable.
     */
    @ManyToOne
    @JoinColumn(name = "review_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Review review;

    /**
     * The restaurant gallery associated with this photo, if applicable.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Restaurant restaurant;

    /**
     * Validates that the photo is exclusively linked to either a review or a restaurant.
     * @throws java.lang.IllegalStateException If the photo is associated with a review and a restaurant, or
     * if the photo is not associated with neither a review nor a restaurant
     */
    @PrePersist
    @PreUpdate
    private void validateConstraints() {
        boolean hasReview = (review != null);
        boolean hasRestaurant = (restaurant != null);

        if (hasReview && hasRestaurant) {
            throw new IllegalStateException(
                    "Photo cannot be linked to both a Review and a Restaurant Gallery."
            );
        }

        if (!hasReview && !hasRestaurant) {
            throw new IllegalStateException(
                    "Photo must belong to either a Review or a Restaurant Gallery."
            );
        }
    }
}
