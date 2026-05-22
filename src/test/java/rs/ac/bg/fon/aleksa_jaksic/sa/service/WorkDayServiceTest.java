package rs.ac.bg.fon.aleksa_jaksic.sa.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.repository.RestaurantRepository;
import rs.ac.bg.fon.aleksa_jaksic.sa.workday.domain.WorkDay;
import rs.ac.bg.fon.aleksa_jaksic.sa.workday.dtos.WorkDayCreateUpdateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.workday.dtos.WorkDayDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.workday.mapper.WorkDayMapper;
import rs.ac.bg.fon.aleksa_jaksic.sa.workday.repository.WorkDayRepository;
import rs.ac.bg.fon.aleksa_jaksic.sa.workday.service.WorkDayService;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkDayServiceTest {

    @Mock
    private WorkDayRepository workDayRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private WorkDayMapper workDayMapper;

    @InjectMocks
    private WorkDayService workDayService;

    private Long restaurantId;
    private Long workDayId;
    private WorkDay workDay;

    @BeforeEach
    void setUp() {
        restaurantId = 1L;
        workDayId = 100L;
        workDay = new WorkDay();
        workDay.setId(workDayId);
        workDay.setOpenTime(LocalTime.of(9, 0));
        workDay.setCloseTime(LocalTime.of(17, 0));
    }

    @Test
    @DisplayName("Should list all workdays for a restaurant")
    void list_Success() {
        WorkDayDTO expectedDto = new WorkDayDTO(workDayId, LocalTime.of(9, 0), LocalTime.of(17, 0), DayOfWeek.MONDAY);
        when(workDayRepository.findByRestaurantId(restaurantId)).thenReturn(List.of(workDay));
        when(workDayMapper.toDTO(workDay)).thenReturn(expectedDto);

        List<WorkDayDTO> result = workDayService.list(restaurantId);

        assertEquals(1, result.size());
        verify(workDayRepository).findByRestaurantId(restaurantId);
    }

    @Test
    @DisplayName("Should create workday successfully when hours are valid")
    void create_Success() {
        WorkDayCreateUpdateDTO createDto = new WorkDayCreateUpdateDTO(LocalTime.of(9, 0), LocalTime.of(17, 0), DayOfWeek.MONDAY);
        WorkDayDTO responseDto = new WorkDayDTO(workDayId, LocalTime.of(9, 0), LocalTime.of(17, 0), DayOfWeek.MONDAY);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(new Restaurant()));
        when(workDayMapper.toEntity(createDto)).thenReturn(workDay);
        when(workDayRepository.save(workDay)).thenReturn(workDay);
        when(workDayMapper.toDTO(workDay)).thenReturn(responseDto);

        WorkDayDTO result = workDayService.create(restaurantId, createDto);

        assertNotNull(result);
        verify(workDayRepository).save(workDay);
    }

    @ParameterizedTest
    @CsvSource({
            "17:00, 09:00",
            "09:00, 09:00"
    })
    @DisplayName("Should throw IllegalArgumentException during creation when time range is invalid")
    void create_InvalidTimeRange_ThrowsException(LocalTime open, LocalTime close) {
        WorkDayCreateUpdateDTO createDto = new WorkDayCreateUpdateDTO(open, close, DayOfWeek.MONDAY);
        workDay.setOpenTime(open);
        workDay.setCloseTime(close);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(new Restaurant()));
        when(workDayMapper.toEntity(createDto)).thenReturn(workDay);

        assertThrows(IllegalArgumentException.class, () -> workDayService.create(restaurantId, createDto));
        verify(workDayRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update workday successfully when hours remain valid")
    void update_Success() {
        WorkDayCreateUpdateDTO updateDto = new WorkDayCreateUpdateDTO(LocalTime.of(9, 0), LocalTime.of(17, 0), DayOfWeek.MONDAY);
        WorkDayDTO responseDto = new WorkDayDTO(workDayId, LocalTime.of(9, 0), LocalTime.of(17, 0), DayOfWeek.MONDAY);

        when(workDayRepository.findById(workDayId)).thenReturn(Optional.of(workDay));
        when(workDayRepository.save(workDay)).thenReturn(workDay);
        when(workDayMapper.toDTO(workDay)).thenReturn(responseDto);

        WorkDayDTO result = workDayService.update(workDayId, updateDto);

        assertNotNull(result);
        verify(workDayRepository).save(workDay);
    }

    @ParameterizedTest
    @CsvSource({
            "17:00, 09:00",
            "09:00, 09:00"
    })
    @DisplayName("Should throw IllegalArgumentException during update when time range is invalid")
    void update_InvalidTimeRange_ThrowsException(LocalTime open, LocalTime close) {
        WorkDayCreateUpdateDTO updateDto = new WorkDayCreateUpdateDTO(open, close, DayOfWeek.MONDAY);

        // Simulate update mapping changing entity values to invalid hours
        workDay.setOpenTime(open);
        workDay.setCloseTime(close);

        when(workDayRepository.findById(workDayId)).thenReturn(Optional.of(workDay));

        assertThrows(IllegalArgumentException.class, () -> workDayService.update(workDayId, updateDto));
        verify(workDayRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when restaurant is not found on create")
    void create_RestaurantNotFound_ThrowsException() {
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> workDayService.create(restaurantId, new WorkDayCreateUpdateDTO(LocalTime.of(9, 0), LocalTime.of(17, 0), DayOfWeek.MONDAY)));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when workday is not found on update")
    void update_WorkDayNotFound_ThrowsException() {
        when(workDayRepository.findById(workDayId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> workDayService.update(workDayId, new WorkDayCreateUpdateDTO(LocalTime.of(9, 0), LocalTime.of(17, 0), DayOfWeek.MONDAY)));
    }

    @Test
    @DisplayName("Should delete workday successfully")
    void delete_Success() {
        when(workDayRepository.findById(workDayId)).thenReturn(Optional.of(workDay));
        workDayService.delete(workDayId);
        verify(workDayRepository).delete(workDay);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when deleting non-existent workday")
    void delete_NotFound_ThrowsException() {
        when(workDayRepository.findById(workDayId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> workDayService.delete(workDayId));
    }
}