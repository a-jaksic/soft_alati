package rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.service;

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

    public RestaurantDetailsDTO getRestaurant(Long id) throws Exception{
        Restaurant restaurant = restaurantRepository.findById(id).orElse(null);
        if (restaurant == null){
            throw new Exception("No restaurant found with give id!");
        }
        return restaurantMapper.toDetailsDTO(restaurant);
    }

    public Page<RestaurantDTO> getRestaurants(String name, Long typeId, Long cityId, Pageable pageable) throws Exception {
        Page<Restaurant> restaurants = restaurantRepository.findFiltered(name, typeId, cityId, pageable);
        if (restaurants.isEmpty()){
            throw new Exception("Could not find restaurant with given parameters!");
        }
        return restaurants.map(restaurantMapper::toDTO);

    }

    @Transactional
    public RestaurantDetailsDTO create(RestaurantCreateUpdateDTO restaurantCreateUpdateDTO) throws Exception{
        RestaurantType restaurantType = restaurantTypeRepository.findById(restaurantCreateUpdateDTO.restaurantTypeId()).orElse(null);
        City city = cityRepository.findById(restaurantCreateUpdateDTO.cityId()).orElse(null);
        if (restaurantType == null){
            throw new Exception("Could not find restaurant type with given id!");
        }
        if (city == null){
            throw new Exception("Could not find city with given id!");
        }
        Restaurant restaurant = restaurantMapper.toEntity(restaurantCreateUpdateDTO);
        restaurant.setRestaurantType(restaurantType);
        restaurant.setCity(city);
        restaurant.setAvgRating(0.0);
        restaurant.setReviewCount(0);

        return restaurantMapper.toDetailsDTO(restaurantRepository.save(restaurant));
    }

    @Transactional
    public RestaurantDetailsDTO update(Long id, RestaurantCreateUpdateDTO restaurantCreateUpdateDTO) throws Exception{
        Restaurant restaurant = restaurantRepository.findById(id).orElse(null);
        if (restaurant == null){
            throw new Exception("No restaurant found with given id!");
        }
        restaurantMapper.updateEntityFromUpdateDto(restaurantCreateUpdateDTO, restaurant);
        RestaurantType restaurantType = restaurantTypeRepository.findById(restaurantCreateUpdateDTO.restaurantTypeId())
                .orElseThrow(() -> new Exception("Type not found"));
        City city = cityRepository.findById(restaurantCreateUpdateDTO.cityId())
                .orElseThrow(() -> new Exception("City not found"));
        restaurant.setRestaurantType(restaurantType);
        restaurant.setCity(city);
        return restaurantMapper.toDetailsDTO(restaurantRepository.save(restaurant));
    }

    @Transactional
    public void delete(Long id) throws Exception{
        Restaurant restaurant = restaurantRepository.findById(id).orElse(null);
        if (restaurant == null) {
            throw new Exception("No review found with given id!");
        }
        try {

            fileSystemStorageService.deleteRestaurantFolder(id);
            restaurantRepository.delete(restaurant);
        } catch (Exception e) {
            throw new Exception("Could not delete review with given id!");
        }

    }
}
