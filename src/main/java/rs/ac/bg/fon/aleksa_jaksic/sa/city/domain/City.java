package rs.ac.bg.fon.aleksa_jaksic.sa.city.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents the city that the restaurant is located in.
 * Enforces a unique constraint on the combination of name and postal code.
 * @author Aleksa Jaksic (a-jaksic)
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "cities", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "postalCode"})
})
public class City {

    /**
     * Unique ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the city.
     */
    private String name;

    /**
     * The postal/zip code associated with the city.
     */
    private String postalCode;
}
