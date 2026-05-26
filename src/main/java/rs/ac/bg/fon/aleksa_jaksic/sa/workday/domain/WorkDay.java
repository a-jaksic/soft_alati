package rs.ac.bg.fon.aleksa_jaksic.sa.workday.domain;

import jakarta.validation.constraints.NotNull;
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
     * Must not be null.
     */
    @NotNull(message = "Opening time is required")
    private LocalTime openTime;

    /**
     * The time of day when the restaurant closes.
     * Must not be null.
     */
    @NotNull(message = "Closing time is required")
    private LocalTime closeTime;

    /**
     * The specific day of the week the restaurant works.
     * Must not be null.
     */
    @NotNull(message = "Day of the week is required")
    @Enumerated(EnumType.STRING)
    private DayOfWeek day;

    /**
     * The restaurant that this specific workday relates to.
     * Managed under a strict relational constraint, must not be null.
     */
    @NotNull(message = "Associated restaurant is required")
    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Restaurant restaurant;

    /**
     * A check to determine if the operating hours interval is valid.
     * @return true if the hours represent a valid chronological window,
     * false if the open time or closed time are null or the chronological order of the work hours is not correct
     */
    public boolean isValidTimeWindow() {
        if (openTime == null || closeTime == null) {
            return false;
        }
        return openTime.isBefore(closeTime);
    }


}
