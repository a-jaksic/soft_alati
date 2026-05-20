package rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.service;

import jakarta.persistence.EntityNotFoundException;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.domain.RestaurantType;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.dtos.RestaurantTypeCreateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.dtos.RestaurantTypeDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.mapper.RestaurantTypeMapper;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.repository.RestaurantTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing restaurant types with their specific cuisine types.
 * Handles tracking, listing, and maintaining restaurant types.
 * @author Aleksa Jaksic (a-jaksic)
 */
@Service
public class RestaurantTypeService {

    private final RestaurantTypeRepository restaurantTypeRepository;
    private final RestaurantTypeMapper restaurantTypeMapper;

    public RestaurantTypeService(RestaurantTypeRepository restaurantTypeRepository, RestaurantTypeMapper restaurantTypeMapper){
        this.restaurantTypeRepository = restaurantTypeRepository;
        this.restaurantTypeMapper = restaurantTypeMapper;
    }

    /**
     * Fetches a specific restaurant type by its identifier.
     * @param id unique identifier of the restaurant type.
     * @return RestaurantTypeDTO holding properties describing the matching restaurant type.
     * @throws jakarta.persistence.EntityNotFoundException If no restaurant type matches the given identifier.
     */
    public RestaurantTypeDTO get(Long id) {
        return restaurantTypeRepository.findById(id)
                .map(restaurantTypeMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("No restaurant type found with id: " + id));
    }

    /**
     * Returns a list of every type of restaurant in the system.
     * @return List of RestaurantTypeDTO items.
     */
    public List<RestaurantTypeDTO> list() {
        return restaurantTypeRepository.findAll().stream()
                .map(restaurantTypeMapper::toDTO)
                .toList();
    }

    /**
     * Creates a new restaurant type and inserts the record into the database.
     * @param restaurantTypeCreateDTO Restaurant type values containing necessary information.
     * @return RestaurantTypeDTO of the newly created restaurant type.
     */
    @Transactional
    public RestaurantTypeDTO create(RestaurantTypeCreateDTO restaurantTypeCreateDTO) {
        RestaurantType restaurantType = restaurantTypeMapper.toEntity(restaurantTypeCreateDTO);
        return restaurantTypeMapper.toDTO(restaurantTypeRepository.save(restaurantType));
    }

    /**
     * Deletes a restaurant type from the system by its identifier.
     * @param id unique identifier for the specific restaurant type targeted for removal.
     * @throws jakarta.persistence.EntityNotFoundException If the specified restaurant type doesn't exist.
     */
    @Transactional
    public void delete(Long id) {
        RestaurantType restaurantType = restaurantTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Could not find restaurant type with given id!"));

        restaurantTypeRepository.delete(restaurantType);
    }

}
