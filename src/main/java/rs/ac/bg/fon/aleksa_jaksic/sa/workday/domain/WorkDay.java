package rs.ac.bg.fon.aleksa_jaksic.sa.workday.domain;

import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Represents workday of a restaurant for a specific day of the week.
 * Tracks the opening and closing times of the workday.
 * @author Aleksa Jaksic (a-jaksic)
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "work_days")
public class WorkDay {

    /**
     * Unique ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The time of day when the restaurant opens.
     */
    private LocalTime openTime;

    /**
     * The time of day when the restaurant closes.
     */
    private LocalTime closeTime;

    /**
     * The specific day of the week the restaurant works.
     */
    @Enumerated(EnumType.STRING)
    private DayOfWeek day;

    /**
     * The restaurant that this specific workday relates to.
     */
    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Restaurant restaurant;


}
