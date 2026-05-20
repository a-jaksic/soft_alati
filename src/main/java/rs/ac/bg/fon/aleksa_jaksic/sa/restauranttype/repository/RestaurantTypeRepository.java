package rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.repository;

import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.domain.RestaurantType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantTypeRepository extends JpaRepository<RestaurantType, Long> {

}
