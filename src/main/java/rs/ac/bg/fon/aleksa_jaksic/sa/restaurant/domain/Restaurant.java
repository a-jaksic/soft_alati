package rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain;

import rs.ac.bg.fon.aleksa_jaksic.sa.city.domain.City;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.domain.RestaurantType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String address;

    private Double latitude;

    private Double longitude;

    private String phoneNum;

    private String website;

    private Integer reviewCount;

    private Double avgRating;


    @ManyToOne
    @JoinColumn(name = "restaurant_type_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private RestaurantType restaurantType;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private City city;

    public void recalculateAfterReviewCreate(int newRating){
        double totalScore = (this.avgRating * this.reviewCount) + newRating;
        this.reviewCount++;
        this.avgRating = totalScore / this.reviewCount;
    }

    public void recalculateAfterReviewUpdate(int oldRating, int newRating) {
        double totalScore = (this.avgRating * this.reviewCount) - oldRating + newRating;
        this.avgRating = totalScore / this.reviewCount;
    }

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
