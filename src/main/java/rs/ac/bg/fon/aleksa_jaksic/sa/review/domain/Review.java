package rs.ac.bg.fon.aleksa_jaksic.sa.review.domain;

import jakarta.validation.constraints.*;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

/**
 * Represents a user review submitted for a specific restaurant.
 * Holds the user's score and text content
 * @author Aleksa Jaksic (a-jaksic)
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "reviews")
public class Review {

    /**
     * Unique ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The numeric score of the review.
     */
    @NotNull(message = "Rating must not be null")
    @Min(value = 1, message = "Rating score must be at least 1")
    @Max(value = 5, message = "Rating score cannot exceed 5")
    private Integer rating;

    /**
     * The title of the review.
     */
    @NotBlank(message = "Review title must not be blank")
    @Size(max = 100, message = "Review title is too long")
    private String title;

    /**
     * The textual content describing the user's dining experience.
     */
    @NotBlank(message = "Review description must not be blank")
    @Size(max = 2000, message = "Review description is too long")
    private String description;

    /**
     * The timestamp indicating when the review was created.
     */
    @NotNull(message = "Creation timestamp is required")
    private LocalDateTime createdAt;

    /**
     * The user who is the author and submitted the review.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    /**
     * The restaurant that the review is about.
     */
    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Restaurant restaurant;

}
