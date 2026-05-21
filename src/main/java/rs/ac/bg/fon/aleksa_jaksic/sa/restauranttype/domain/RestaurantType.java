package rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
/**
 * Represents the cuisine style of a restaurant.
 * Enforces a unique constraint on the name to prevent duplicate categories.
 * @author Aleksa Jaksic (a-jaksic)
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "restaurant_types", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name"})
})
public class RestaurantType {

    /**
     * Unique ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * The name of the restaurant type category (e.g., "Italian", "Mexican").
     */
    @NotBlank(message = "Restaurant type name must not be blank")
    @Size(max = 50, message = "Restaurant type name is too long")
    private String name;
}
