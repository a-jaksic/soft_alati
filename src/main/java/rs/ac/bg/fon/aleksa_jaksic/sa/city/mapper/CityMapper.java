package rs.ac.bg.fon.aleksa_jaksic.sa.city.mapper;

import rs.ac.bg.fon.aleksa_jaksic.sa.city.domain.City;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.dtos.CityCreateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.dtos.CityDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CityMapper {

    City toEntity(CityDTO cityDTO);

    City toEntity(CityCreateDTO cityCreateDTO);

    CityDTO toDTO(City city);
}
