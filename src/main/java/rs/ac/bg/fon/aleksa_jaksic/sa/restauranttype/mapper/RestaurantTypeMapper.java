package rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.mapper;

import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.domain.RestaurantType;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.dtos.RestaurantTypeCreateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.dtos.RestaurantTypeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RestaurantTypeMapper {

    RestaurantType toEntity(RestaurantTypeDTO restaurantTypeDTO);

    RestaurantType toEntity(RestaurantTypeCreateDTO restaurantTypeCreateDTO);

    RestaurantTypeDTO toDTO(RestaurantType restaurantType);
}
