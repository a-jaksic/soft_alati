package rs.ac.bg.fon.aleksa_jaksic.sa.city.domain;

import jakarta.persistence.*;
import lombok.*;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String postalCode;
}
