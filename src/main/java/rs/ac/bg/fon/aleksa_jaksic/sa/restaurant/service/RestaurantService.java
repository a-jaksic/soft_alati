package rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.service;

import jakarta.persistence.EntityNotFoundException;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.domain.City;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.repository.CityRepository;
import rs.ac.bg.fon.aleksa_jaksic.sa.files.FileSystemStorageService;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.dtos.RestaurantCreateUpdateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.dtos.RestaurantDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.dtos.RestaurantDetailsDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.mapper.RestaurantMapper;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.repository.RestaurantRepository;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.domain.RestaurantType;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.repository.RestaurantTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing operations for restaurants.
 * Handles matching restaurant types, cities, and cleaning up storage assets.
 * @author Aleksa Jaksic (a-jaksic)
 */
@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantTypeRepository restaurantTypeRepository;
    private final CityRepository cityRepository;
    private final RestaurantMapper restaurantMapper;
    private final FileSystemStorageService fileSystemStorageService;

    public RestaurantService(RestaurantRepository restaurantRepository,RestaurantTypeRepository restaurantTypeRepository, CityRepository cityRepository,RestaurantMapper restaurantMapper, FileSystemStorageService fileSystemStorageService){
        this.restaurantRepository = restaurantRepository;
        this.restaurantTypeRepository = restaurantTypeRepository;
        this.cityRepository = cityRepository;
        this.restaurantMapper = restaurantMapper;
        this.fileSystemStorageService = fileSystemStorageService;
    }

    /**
     * Retrieves detailed data for a single restaurant.
     * @param id unique identifier of the target restaurant.
     * @return RestaurantDetailsDTO mapping the complete domain entity structure.
     * @throws jakarta.persistence.EntityNotFoundException If no restaurant matches the provided identifier.
     */
    public RestaurantDetailsDTO getRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No restaurant found with given id!"));
        return restaurantMapper.toDetailsDTO(restaurant);
    }

    /**
     * Finds a paginated group of restaurants based on optional filter values.
     * @param name Optional text filter matched against restaurant names.
     * @param typeId Optional identifier matching a certain restaurant type.
     * @param cityId Optional identifier matching a city.
     * @param pageable Pagination details controlling limits and current page index offsets.
     * @return Page containing targeted, mapped RestaurantDTO details.
     */
    public Page<RestaurantDTO> getRestaurants(String name, Long typeId, Long cityId, Pageable pageable) {
        Page<Restaurant> restaurants = restaurantRepository.findFiltered(name, typeId, cityId, pageable);
        return restaurants.map(restaurantMapper::toDTO);

    }

    /**
     * Creates a new restaurant record in the database.
     * @param restaurantCreateUpdateDTO DTO containing necessary restaurant information.
     * @return RestaurantDetailsDTO mapping out the newly persisted dataset.
     * @throws jakarta.persistence.EntityNotFoundException If the referenced type or city do not exist.
     */
    @Transactional
    public RestaurantDetailsDTO create(RestaurantCreateUpdateDTO restaurantCreateUpdateDTO) {
        RestaurantType restaurantType = restaurantTypeRepository.findById(restaurantCreateUpdateDTO.restaurantTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Could not find restaurant type with given id!"));

        City city = cityRepository.findById(restaurantCreateUpdateDTO.cityId())
                .orElseThrow(() -> new EntityNotFoundException("Could not find city with given id!"));

        Restaurant restaurant = restaurantMapper.toEntity(restaurantCreateUpdateDTO);
        restaurant.setRestaurantType(restaurantType);
        restaurant.setCity(city);
        restaurant.setAvgRating(0.0);
        restaurant.setReviewCount(0);

        return restaurantMapper.toDetailsDTO(restaurantRepository.save(restaurant));
    }

    /**
     * Modifies an active restaurant entry.
     * @param id identifier specifying which restaurant database record to change.
     * @param restaurantCreateUpdateDTO Form updates holding new restaurant info.
     * @return RestaurantDetailsDTO with new property values.
     * @throws jakarta.persistence.EntityNotFoundException If the restaurant, restaurant type, or city references are missing.
     */
    @Transactional
    public RestaurantDetailsDTO update(Long id, RestaurantCreateUpdateDTO restaurantCreateUpdateDTO) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No restaurant found with given id!"));

        restaurantMapper.updateEntityFromUpdateDto(restaurantCreateUpdateDTO, restaurant);

        RestaurantType restaurantType = restaurantTypeRepository.findById(restaurantCreateUpdateDTO.restaurantTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Type not found"));

        City city = cityRepository.findById(restaurantCreateUpdateDTO.cityId())
                .orElseThrow(() -> new EntityNotFoundException("City not found"));

        restaurant.setRestaurantType(restaurantType);
        restaurant.setCity(city);
        return restaurantMapper.toDetailsDTO(restaurantRepository.save(restaurant));
    }

    /**
     * Deletes a restaurant from the system and sweeps all linked photo assets from storage disk.
     * @param id unique identifier of the restaurant targeted for deletion.
     * @throws jakarta.persistence.EntityNotFoundException If the specified restaurant cannot be found.
     */
    @Transactional
    public void delete(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No restaurant found with given id!"));
        try {
            fileSystemStorageService.deleteRestaurantFolder(id);
            restaurantRepository.delete(restaurant);
        } catch (Exception e) {
            throw new EntityNotFoundException("Could not delete restaurant with given id!");
        }
    }
}
