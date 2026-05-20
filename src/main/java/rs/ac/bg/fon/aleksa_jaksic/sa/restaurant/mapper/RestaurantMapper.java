package rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.mapper;

import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.dtos.RestaurantCreateUpdateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.dtos.RestaurantDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.dtos.RestaurantDetailsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RestaurantMapper {

    Restaurant toEntity(RestaurantDTO restaurantDTO);

    Restaurant toEntity(RestaurantCreateUpdateDTO restaurantCreateUpdateDTO);

    @Mapping(source = "city.name", target = "cityName")
    @Mapping(source = "restaurantType.name", target = "typeName")
    RestaurantDTO toDTO(Restaurant restaurant);

    RestaurantDetailsDTO toDetailsDTO(Restaurant restaurant);

    void updateEntityFromUpdateDto(RestaurantCreateUpdateDTO restaurantCreateUpdateDTO, @MappingTarget Restaurant restaurant);
}
