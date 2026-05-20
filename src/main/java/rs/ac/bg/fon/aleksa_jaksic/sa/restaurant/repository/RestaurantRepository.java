package rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.repository;

import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Query("SELECT r FROM Restaurant r WHERE " +
            "(:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:typeId IS NULL OR r.restaurantType.id = :typeId) AND " +
            "(:cityId IS NULL OR r.city.id = :cityId)")
    Page<Restaurant> findFiltered(
            @Param("name") String name,
            @Param("typeId") Long typeId,
            @Param("cityId") Long cityId,
            Pageable pageable
    );
}
