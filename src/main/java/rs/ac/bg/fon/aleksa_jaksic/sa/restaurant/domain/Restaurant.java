package rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain;

import jakarta.validation.constraints.*;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.domain.City;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.domain.RestaurantType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Represents a restaurant in the application.
 * Tracks location details, category of the restaurant and real-time user reviews.
 * @author Aleksa Jaksic (a-jaksic)
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "restaurants")
public class Restaurant {

    /**
     * Unique ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the restaurant.
     * Must not be blank and is restricted to a maximum of 100 characters.
     */
    @NotBlank(message = "Restaurant name must not be blank")
    @Size(max = 100, message = "Restaurant name is too long")
    private String name;

    /**
     * The physical address of the restaurant.
     * Must not be blank to maintain valid navigation records.
     */
    @NotBlank(message = "Address must not be blank")
    private String address;

    /**
     * Geographical latitude for mapping and proximity calculations.
     * Must not be null and must fall within valid coordinate bounds between -90 and 90 degrees.
     */
    @NotNull(message = "Latitude is required")
    @Min(value = -90, message = "Latitude must be between -90 and 90")
    @Max(value = 90, message = "Latitude must be between -90 and 90")
    private Double latitude;

    /**
     * Geographical longitude for mapping and proximity calculations.
     * Must not be null and must fall within valid coordinate bounds between -180 and 180 degrees.
     */
    @NotNull(message = "Longitude is required")
    @Min(value = -180, message = "Longitude must be between -180 and 180")
    @Max(value = 180, message = "Longitude must be between -180 and 180")
    private Double longitude;

    /**
     * The contact phone number of the restaurant.
     * Must match an international or local telephone pattern with an optional leading '+'
     * and a total length ranging between 6 and 20 digits.
     */
    @Pattern(regexp = "^\\+?[0-9\\s\\-\\/]{6,20}$", message = "Invalid phone number format")
    private String phoneNum;

    /**
     * The official website link of the restaurant.
     */
    private String website;

    /**
     * Total number of reviews submitted for this restaurant.
     * Dynamically managed running total, must not be below zero.
     */
    @Min(value = 0, message = "Review count cannot be negative")
    private Integer reviewCount;

    /**
     * The average rating score calculated across all user reviews.
     * Must sit within a strict decimal range between 0.0 and 5.0.
     */
    @Min(value = 0, message = "Average rating cannot be negative")
    @Max(value = 5, message = "Average rating cannot exceed 5")
    private Double avgRating;


    /**
     * The categorization of cuisine type for the restaurant.
     */
    @ManyToOne
    @JoinColumn(name = "restaurant_type_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private RestaurantType restaurantType;

    /**
     * The city where the restaurant is physically located.
     */
    @ManyToOne
    @JoinColumn(name = "city_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private City city;

    /**
     * Updates the average rating and total count when a new review is submitted.
     * @param newRating the rating of the new review
     */
    public void recalculateAfterReviewCreate(int newRating){
        double totalScore = (this.avgRating * this.reviewCount) + newRating;
        this.reviewCount++;
        this.avgRating = totalScore / this.reviewCount;
    }

    /**
     * Adjusts the average rating when an existing review score is modified.
     * @param oldRating the old rating of the review
     * @param newRating the new rating of the review
     */
    public void recalculateAfterReviewUpdate(int oldRating, int newRating) {
        double totalScore = (this.avgRating * this.reviewCount) - oldRating + newRating;
        this.avgRating = totalScore / this.reviewCount;
    }

    /**
     * Recalculates or resets the average rating and count when a review is deleted.
     * @param ratingToRemove the rating of the review that was deleted
     */
    public void recalculateAfterReviewDelete(int ratingToRemove) {
        if (this.reviewCount <= 1) {
            this.avgRating = 0.0;
            this.reviewCount = 0;
            return;
        }
        double totalScore = (this.avgRating * this.reviewCount) - ratingToRemove;
        this.reviewCount--;
        this.avgRating = totalScore / this.reviewCount;
    }
}
