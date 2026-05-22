package rs.ac.bg.fon.aleksa_jaksic.sa.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.domain.City;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.repository.CityRepository;
import rs.ac.bg.fon.aleksa_jaksic.sa.files.FileSystemStorageService;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.dtos.RestaurantCreateUpdateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.dtos.RestaurantDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.dtos.RestaurantDetailsDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.mapper.RestaurantMapper;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.repository.RestaurantRepository;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.service.RestaurantService;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.domain.RestaurantType;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.repository.RestaurantTypeRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private RestaurantTypeRepository restaurantTypeRepository;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private RestaurantMapper restaurantMapper;
    @Mock
    private FileSystemStorageService fileSystemStorageService;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant sampleRestaurant;
    private RestaurantDetailsDTO sampleDetailsDTO;
    private RestaurantDTO sampleDTO;
    private RestaurantType sampleType;
    private City sampleCity;
    private RestaurantCreateUpdateDTO sampleFormDTO;

    @BeforeEach
    void setUp() {
        sampleRestaurant = new Restaurant();
        sampleRestaurant.setId(1L);
        sampleRestaurant.setAvgRating(4.5);
        sampleRestaurant.setReviewCount(12);

        sampleType = new RestaurantType();
        sampleType.setId(2L);

        sampleCity = new City();
        sampleCity.setId(3L);

        sampleFormDTO = new RestaurantCreateUpdateDTO(
                "Gusto", "Knez Mihailova 1", 44.8154, 20.4606, "+38111234567", "www.gusto.rs", 3L, 2L
        );

        sampleDTO = new RestaurantDTO(
                1L, "Gusto", "Beograd", "Italian", 4.5, "Knez Mihailova 1", "+38111234567", "www.gusto.rs"
        );

        sampleDetailsDTO = new RestaurantDetailsDTO(
                1L, "Gusto", "Knez Mihailova 1", 44.8154, 20.4606, "+38111234567", "www.gusto.rs", 12, 4.5, 3L, 2L
        );
    }

    @Test
    @DisplayName("Should successfully return mapped details DTO when restaurant exists by ID")
    void getRestaurant_Exists_ReturnsDetailsDTO() {
        Long id = 1L;
        when(restaurantRepository.findById(id)).thenReturn(Optional.of(sampleRestaurant));
        when(restaurantMapper.toDetailsDTO(sampleRestaurant)).thenReturn(sampleDetailsDTO);

        RestaurantDetailsDTO result = restaurantService.getRestaurant(id);

        assertNotNull(result);
        assertEquals(id, result.id());
        assertEquals("Gusto", result.name());
        verify(restaurantRepository).findById(id);
        verify(restaurantMapper).toDetailsDTO(sampleRestaurant);
    }

    @Test
    @DisplayName("Should return a mapped paginated DTO response based on query filters")
    void getRestaurants_ValidFilters_ReturnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Restaurant> restaurantPage = new PageImpl<>(List.of(sampleRestaurant));

        when(restaurantRepository.findFiltered("Gusto", 2L, 3L, pageable)).thenReturn(restaurantPage);
        when(restaurantMapper.toDTO(sampleRestaurant)).thenReturn(sampleDTO);

        Page<RestaurantDTO> result = restaurantService.getRestaurants("Gusto", 2L, 3L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Gusto", result.getContent().get(0).name());
        assertEquals("Beograd", result.getContent().get(0).cityName());
        verify(restaurantRepository).findFiltered("Gusto", 2L, 3L, pageable);
        verify(restaurantMapper).toDTO(sampleRestaurant);
    }

    @Test
    @DisplayName("Should establish counters and save new restaurant details safely")
    void create_ValidDependencies_SavesAndReturnsDetails() {
        when(restaurantTypeRepository.findById(2L)).thenReturn(Optional.of(sampleType));
        when(cityRepository.findById(3L)).thenReturn(Optional.of(sampleCity));
        when(restaurantMapper.toEntity(sampleFormDTO)).thenReturn(sampleRestaurant);
        when(restaurantRepository.save(sampleRestaurant)).thenReturn(sampleRestaurant);
        when(restaurantMapper.toDetailsDTO(sampleRestaurant)).thenReturn(sampleDetailsDTO);

        RestaurantDetailsDTO result = restaurantService.create(sampleFormDTO);

        assertNotNull(result);
        assertEquals(0.0, sampleRestaurant.getAvgRating());
        assertEquals(0, sampleRestaurant.getReviewCount());
        assertEquals(sampleType, sampleRestaurant.getRestaurantType());
        assertEquals(sampleCity, sampleRestaurant.getCity());
        verify(restaurantRepository).save(sampleRestaurant);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException during creation if target restaurant type is missing")
    void create_TypeNotFound_ThrowsEntityNotFoundException() {
        when(restaurantTypeRepository.findById(2L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                restaurantService.create(sampleFormDTO)
        );

        assertEquals("Could not find restaurant type with given id!", exception.getMessage());
        verifyNoInteractions(cityRepository, restaurantMapper, restaurantRepository);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException during creation if target city is missing")
    void create_CityNotFound_ThrowsEntityNotFoundException() {
        when(restaurantTypeRepository.findById(2L)).thenReturn(Optional.of(sampleType));
        when(cityRepository.findById(3L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                restaurantService.create(sampleFormDTO)
        );

        assertEquals("Could not find city with given id!", exception.getMessage());

        verify(restaurantMapper, never()).toEntity(any(RestaurantCreateUpdateDTO.class));
        verify(restaurantRepository, never()).save(any(Restaurant.class));
    }

    @Test
    @DisplayName("Should cleanly perform shifts and save changes when updating valid restaurant")
    void update_AllEntitiesExist_UpdatesAndSaves() {
        Long id = 1L;
        when(restaurantRepository.findById(id)).thenReturn(Optional.of(sampleRestaurant));
        when(restaurantTypeRepository.findById(2L)).thenReturn(Optional.of(sampleType));
        when(cityRepository.findById(3L)).thenReturn(Optional.of(sampleCity));
        when(restaurantRepository.save(sampleRestaurant)).thenReturn(sampleRestaurant);
        when(restaurantMapper.toDetailsDTO(sampleRestaurant)).thenReturn(sampleDetailsDTO);

        RestaurantDetailsDTO result = restaurantService.update(id, sampleFormDTO);

        assertNotNull(result);
        verify(restaurantMapper).updateEntityFromUpdateDto(sampleFormDTO, sampleRestaurant);
        assertEquals(sampleType, sampleRestaurant.getRestaurantType());
        assertEquals(sampleCity, sampleRestaurant.getCity());
        verify(restaurantRepository).save(sampleRestaurant);
    }

    @Test
    @DisplayName("Should stop updates and throw EntityNotFoundException if secondary city mapping is missing")
    void update_CityNotFound_ThrowsEntityNotFoundException() {
        Long id = 1L;
        when(restaurantRepository.findById(id)).thenReturn(Optional.of(sampleRestaurant));
        when(restaurantTypeRepository.findById(2L)).thenReturn(Optional.of(sampleType));
        when(cityRepository.findById(3L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                restaurantService.update(id, sampleFormDTO)
        );

        assertEquals("City not found", exception.getMessage());
        verify(restaurantRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should clear storage file paths and drop table record during deletion flow")
    void delete_ValidRestaurant_ClearsFolderAndRemovesRecord() {
        Long id = 1L;
        when(restaurantRepository.findById(id)).thenReturn(Optional.of(sampleRestaurant));

        assertAll(() -> restaurantService.delete(id));

        verify(fileSystemStorageService).deleteRestaurantFolder(id);
        verify(restaurantRepository).delete(sampleRestaurant);
    }

    @Test
    @DisplayName("Should wrap runtime errors into detailed EntityNotFoundException if folder deletion faults")
    void delete_StorageFails_ThrowsEntityNotFoundException() {
        Long id = 1L;
        when(restaurantRepository.findById(id)).thenReturn(Optional.of(sampleRestaurant));
        doThrow(new RuntimeException("Disk access denied")).when(fileSystemStorageService).deleteRestaurantFolder(id);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                restaurantService.delete(id)
        );

        assertEquals("Could not delete restaurant with given id!", exception.getMessage());
        verify(restaurantRepository, never()).delete(any(Restaurant.class));
    }

    @ParameterizedTest
    @ValueSource(longs = {5L, 500L})
    @DisplayName("Should assert to EntityNotFoundException across multi-point target missing cases")
    void common_RestaurantNotFound_ThrowsEntityNotFoundException(Long missingId) {
        when(restaurantRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> restaurantService.getRestaurant(missingId));
        assertThrows(EntityNotFoundException.class, () -> restaurantService.update(missingId, sampleFormDTO));
        assertThrows(EntityNotFoundException.class, () -> restaurantService.delete(missingId));

        verifyNoInteractions(fileSystemStorageService, restaurantTypeRepository, cityRepository);
    }
}
