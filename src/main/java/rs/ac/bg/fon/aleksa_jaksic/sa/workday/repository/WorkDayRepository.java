package rs.ac.bg.fon.aleksa_jaksic.sa.workday.repository;

import rs.ac.bg.fon.aleksa_jaksic.sa.workday.domain.WorkDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkDayRepository extends JpaRepository<WorkDay, Long> {

    List<WorkDay> findByRestaurantId(Long id);
}
