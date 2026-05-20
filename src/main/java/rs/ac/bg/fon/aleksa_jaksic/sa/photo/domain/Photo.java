package rs.ac.bg.fon.aleksa_jaksic.sa.photo.domain;

import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.domain.Review;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filePath;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "review_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Restaurant restaurant;

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
