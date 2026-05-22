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
import rs.ac.bg.fon.aleksa_jaksic.sa.city.domain.City;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.dtos.CityCreateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.dtos.CityDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.mapper.CityMapper;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.repository.CityRepository;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.service.CityService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CityMapper cityMapper;

    @InjectMocks
    private CityService cityService;

    private City sampleCity;
    private CityDTO sampleCityDTO;

    @BeforeEach
    void setUp() {
        sampleCity = new City();
        sampleCity.setId(11L);

        sampleCityDTO = new CityDTO(11L, "Beograd", "11000");
    }

    @Test
    @DisplayName("Should successfully return mapped CityDTO when city exists by ID")
    void get_CityExists_ReturnsCityDTO() {
        Long targetId = 11L;
        when(cityRepository.findById(targetId)).thenReturn(Optional.of(sampleCity));
        when(cityMapper.toDTO(sampleCity)).thenReturn(sampleCityDTO);

        CityDTO result = cityService.get(targetId);

        assertNotNull(result);
        assertEquals(targetId, result.id());
        verify(cityRepository).findById(targetId);
        verify(cityMapper).toDTO(sampleCity);
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 99L, 404L})
    @DisplayName("Should throw EntityNotFoundException with missing ID lookups")
    void get_CityDoesNotExist_ThrowsEntityNotFoundException(Long missingId) {
        when(cityRepository.findById(missingId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                cityService.get(missingId)
        );

        assertEquals("No city found with id: " + missingId, exception.getMessage());
        verify(cityRepository).findById(missingId);
        verifyNoInteractions(cityMapper);
    }

    @Test
    @DisplayName("Should return a populated list of mapped CityDTOs when records exist")
    void list_RecordsExist_ReturnsPopulatedDTOList() {
        City secondaryCity = new City();
        secondaryCity.setId(12L);

        CityDTO secondaryCityDTO = new CityDTO(12L, "Novi Sad", "21000");

        when(cityRepository.findAll()).thenReturn(List.of(sampleCity, secondaryCity));
        when(cityMapper.toDTO(sampleCity)).thenReturn(sampleCityDTO);
        when(cityMapper.toDTO(secondaryCity)).thenReturn(secondaryCityDTO);

        List<CityDTO> result = cityService.list();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(11L, result.get(0).id());
        assertEquals(12L, result.get(1).id());
        verify(cityRepository).findAll();
        verify(cityMapper, times(2)).toDTO(any(City.class));
    }

    @Test
    @DisplayName("Should return an empty list when no cities exist in database context")
    void list_NoRecords_ReturnsEmptyList() {
        when(cityRepository.findAll()).thenReturn(Collections.emptyList());

        List<CityDTO> result = cityService.list();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cityRepository).findAll();
        verifyNoInteractions(cityMapper);
    }

    @Test
    @DisplayName("Should translate incoming DTO, persist entity, and return created DTO view")
    void create_ValidInput_SavesAndReturnsDTO() {
        CityCreateDTO createDTO = new CityCreateDTO("Belgrade", "11000");

        when(cityMapper.toEntity(createDTO)).thenReturn(sampleCity);
        when(cityRepository.save(sampleCity)).thenReturn(sampleCity);
        when(cityMapper.toDTO(sampleCity)).thenReturn(sampleCityDTO);

        CityDTO result = cityService.create(createDTO);

        assertNotNull(result);
        assertEquals(11L, result.id());
        verify(cityMapper).toEntity(createDTO);
        verify(cityRepository).save(sampleCity);
        verify(cityMapper).toDTO(sampleCity);
    }

    @Test
    @DisplayName("Should safely drop the record when targeting an active city ID")
    void delete_CityExists_ExecutesRepositoryDeletion() {
        Long targetId = 11L;
        when(cityRepository.findById(targetId)).thenReturn(Optional.of(sampleCity));

        assertAll(() -> cityService.delete(targetId));

        verify(cityRepository).findById(targetId);
        verify(cityRepository).delete(sampleCity);
    }

    @Test
    @DisplayName("Should halt delete sequence and throw EntityNotFoundException if target city is missing")
    void delete_CityDoesNotExist_ThrowsEntityNotFoundException() {
        Long targetId = 11L;
        when(cityRepository.findById(targetId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                cityService.delete(targetId)
        );

        assertEquals("Could not find city with given id!", exception.getMessage());
        verify(cityRepository).findById(targetId);
        verify(cityRepository, never()).delete(any(City.class));
    }
}
