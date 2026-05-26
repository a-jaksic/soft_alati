package rs.ac.bg.fon.aleksa_jaksic.sa.city.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
     * Must not be blank and is restricted to a maximum of 100 characters.
     * Part of a unique constraint, the combination of this name and the associated postal code must be unique across the system.
     */
    @NotBlank(message = "City name must not be blank")
    @Size(max = 100, message = "City name is too long")
    private String name;

    /**
     * The postal/zip code associated with the city.
     * Must not be blank and is restricted to a maximum of 20 characters.
     * Part of a unique constraint, the combination of this postal code and the associated city name must be unique across the system.
     */
    @NotBlank(message = "Postal code must not be blank")
    @Size(max = 20, message = "Postal code is too long")
    private String postalCode;
}
