package rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain;

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
     */
    private String name;

    /**
     * The physical address of the restaurant.
     */
    private String address;

    /**
     * Geographical latitude for mapping and proximity calculations.
     */
    private Double latitude;

    /**
     * Geographical longitude for mapping and proximity calculations.
     */
    private Double longitude;

    /**
     * The contact phone number of the restaurant.
     */
    private String phoneNum;

    /**
     * The official website link of the restaurant.
     */
    private String website;

    /**
     * Total number of reviews submitted for this restaurant.
     */
    private Integer reviewCount;

    /**
     * The average rating score calculated across all user reviews.
     */
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
