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

    public RestaurantDetailsDTO getRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No restaurant found with given id!"));
        return restaurantMapper.toDetailsDTO(restaurant);
    }

    public Page<RestaurantDTO> getRestaurants(String name, Long typeId, Long cityId, Pageable pageable) {
        Page<Restaurant> restaurants = restaurantRepository.findFiltered(name, typeId, cityId, pageable);
        return restaurants.map(restaurantMapper::toDTO);

    }

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
