package rs.ac.bg.fon.aleksa_jaksic.sa.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.domain.RestaurantType;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.dtos.RestaurantTypeCreateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.dtos.RestaurantTypeDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.mapper.RestaurantTypeMapper;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.repository.RestaurantTypeRepository;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.service.RestaurantTypeService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantTypeServiceTest {

    @Mock
    private RestaurantTypeRepository restaurantTypeRepository;

    @Mock
    private RestaurantTypeMapper restaurantTypeMapper;

    @InjectMocks
    private RestaurantTypeService restaurantTypeService;

    private RestaurantType sampleType;
    private RestaurantTypeDTO sampleTypeDTO;
    private RestaurantTypeCreateDTO sampleCreateDTO;

    @BeforeEach
    void setUp() {
        sampleType = new RestaurantType();
        sampleType.setId(1L);
        sampleType.setName("Italian");

        sampleTypeDTO = new RestaurantTypeDTO(1L, "Italian");

        sampleCreateDTO = new RestaurantTypeCreateDTO("Italian");
    }

    @Test
    @DisplayName("Should return matching RestaurantTypeDTO when fetching by valid existing ID")
    void get_ValidId_ReturnsRestaurantTypeDTO() {
        Long id = 1L;
        when(restaurantTypeRepository.findById(id)).thenReturn(Optional.of(sampleType));
        when(restaurantTypeMapper.toDTO(sampleType)).thenReturn(sampleTypeDTO);

        RestaurantTypeDTO result = restaurantTypeService.get(id);

        assertNotNull(result);
        assertEquals(id, result.id());
        assertEquals("Italian", result.name());
        verify(restaurantTypeRepository).findById(id);
        verify(restaurantTypeMapper).toDTO(sampleType);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException with accurate details when ID does not exist")
    void get_IdDoesNotExist_ThrowsEntityNotFoundException() {
        Long missingId = 404L;
        when(restaurantTypeRepository.findById(missingId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                restaurantTypeService.get(missingId)
        );

        assertEquals("No restaurant type found with id: " + missingId, exception.getMessage());
        verifyNoInteractions(restaurantTypeMapper);
    }

    @Test
    @DisplayName("Should return list of mapped DTOs matching all found entries")
    void list_EntriesExist_ReturnsPopulatedDTOList() {
        when(restaurantTypeRepository.findAll()).thenReturn(List.of(sampleType));
        when(restaurantTypeMapper.toDTO(sampleType)).thenReturn(sampleTypeDTO);

        List<RestaurantTypeDTO> result = restaurantTypeService.list();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Italian", result.get(0).name());
        verify(restaurantTypeRepository).findAll();
        verify(restaurantTypeMapper).toDTO(sampleType);
    }

    @Test
    @DisplayName("Should safely return an empty list wrapper when no types are in the system")
    void list_NoEntries_ReturnsEmptyList() {
        when(restaurantTypeRepository.findAll()).thenReturn(Collections.emptyList());

        List<RestaurantTypeDTO> result = restaurantTypeService.list();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(restaurantTypeRepository).findAll();
        verifyNoInteractions(restaurantTypeMapper);
    }

    @Test
    @DisplayName("Should map create payload to entity, trigger save pipeline, and return active DTO")
    void create_ValidInput_SavesAndReturnsDTO() {
        when(restaurantTypeMapper.toEntity(sampleCreateDTO)).thenReturn(sampleType);
        when(restaurantTypeRepository.save(sampleType)).thenReturn(sampleType);
        when(restaurantTypeMapper.toDTO(sampleType)).thenReturn(sampleTypeDTO);

        RestaurantTypeDTO result = restaurantTypeService.create(sampleCreateDTO);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Italian", result.name());
        verify(restaurantTypeMapper).toEntity(sampleCreateDTO);
        verify(restaurantTypeRepository).save(sampleType);
        verify(restaurantTypeMapper).toDTO(sampleType);
    }

    @Test
    @DisplayName("Should completely drop target type when lookup is verified")
    void delete_TypeExists_RemovesFromDatabase() {
        Long id = 1L;
        when(restaurantTypeRepository.findById(id)).thenReturn(Optional.of(sampleType));

        assertAll(() -> restaurantTypeService.delete(id));

        verify(restaurantTypeRepository).findById(id);
        verify(restaurantTypeRepository).delete(sampleType);
    }

    @Test
    @DisplayName("Should block deletion and throw EntityNotFoundException if target type lookup fails")
    void delete_TypeNotFound_ThrowsEntityNotFoundException() {
        Long missingId = 99L;
        when(restaurantTypeRepository.findById(missingId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                restaurantTypeService.delete(missingId)
        );

        assertEquals("Could not find restaurant type with given id!", exception.getMessage());
        verify(restaurantTypeRepository, never()).delete(any(RestaurantType.class));
    }
}
