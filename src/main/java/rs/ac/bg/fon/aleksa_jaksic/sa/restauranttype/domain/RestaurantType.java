package rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.domain;

import jakarta.persistence.*;
import lombok.*;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
