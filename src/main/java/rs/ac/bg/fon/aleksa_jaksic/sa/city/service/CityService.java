package rs.ac.bg.fon.aleksa_jaksic.sa.city.service;

import jakarta.persistence.EntityNotFoundException;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.domain.City;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.dtos.CityCreateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.dtos.CityDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.mapper.CityMapper;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.repository.CityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service managing cities within the system.
 * Handles retrieval, creation, and deletion of city entries.
 * @author Aleksa Jaksic (a-jaksic)
 */
@Service
public class CityService {

    private final CityRepository cityRepository;

    private final CityMapper cityMapper;

    public CityService(CityRepository cityRepository, CityMapper cityMapper){
        this.cityRepository = cityRepository;
        this.cityMapper = cityMapper;
    }

    /**
     * Retrieves a specific city by its unique identifier.
     * @param id unique identifier of the city.
     * @return CityDTO containing the mapped city data.
     * @throws java.lang.Exception If no city matches the provided identifier.
     */
    public CityDTO get(Long id) {
        return cityRepository.findById(id)
                .map(cityMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("No city found with id: " + id));
    }

    /**
     * Retrieves all cities registered in the system.
     * @return List of CityDTO objects.
     */
    public List<CityDTO> list() {
        return cityRepository.findAll()
                .stream()
                .map(cityMapper::toDTO)
                .toList();
    }

    /**
     * Creates and registers a new city in the database.
     * @param cityCreateDTO DTO data required to build a city entity.
     * @return CityDTO of the newly saved city.
     */
    @Transactional
    public CityDTO create(CityCreateDTO cityCreateDTO) {
        City city = cityMapper.toEntity(cityCreateDTO);
        return cityMapper.toDTO(cityRepository.save(city));
    }

    /**
     * Deletes a city from the database by its unique identifier.
     * @param id unique identifier of the city to delete.
     * @throws java.lang.Exception If the city with the given identifier does not exist.
     */
    @Transactional
    public void delete(Long id) throws Exception{
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Could not find city with given id!"));

        cityRepository.delete(city);
    }
}
