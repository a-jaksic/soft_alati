package rs.ac.bg.fon.aleksa_jaksic.sa.workday.service;

import jakarta.persistence.EntityNotFoundException;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.repository.RestaurantRepository;
import rs.ac.bg.fon.aleksa_jaksic.sa.workday.domain.WorkDay;
import rs.ac.bg.fon.aleksa_jaksic.sa.workday.dtos.WorkDayCreateUpdateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.workday.dtos.WorkDayDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.workday.mapper.WorkDayMapper;
import rs.ac.bg.fon.aleksa_jaksic.sa.workday.repository.WorkDayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing working days for restaurants.
 * Handles the configuration of opening and closing intervals, verification of operating
 * hour constraints, and persistence of workday information.
 * @author Aleksa Jakšić (aleksa-jaksic)
 */
@Service
public class WorkDayService {

    private final WorkDayRepository workDayRepository;
    private final RestaurantRepository restaurantRepository;
    private final WorkDayMapper workDayMapper;

    public WorkDayService(WorkDayRepository workDayRepository, RestaurantRepository restaurantRepository, WorkDayMapper workDayMapper){
        this.workDayRepository = workDayRepository;
        this.restaurantRepository = restaurantRepository;
        this.workDayMapper = workDayMapper;
    }

    /**
     * Retrieves the complete schedule of workdays for a specific restaurant.
     * @param restaurantId unique identifier of the target restaurant.
     * @return List of WorkDayDTO records representing the restaurant's workday schedule.
     */
    public List<WorkDayDTO> list(Long restaurantId) {
        return workDayRepository.findByRestaurantId(restaurantId).stream()
                .map(workDayMapper::toDTO)
                .toList();
    }

    /**
     * Creates and attaches a new workday entry to a restaurant.
     * @param id unique identifier of the restaurant.
     * @param workDayCreateUpdateDTO workday data containing the work hours and day of the week.
     * @return WorkDayDTO record containing the information of the saved workday.
     * @throws jakarta.persistence.EntityNotFoundException If the restaurant is not found.
     * @throws java.lang.IllegalArgumentException If the opening time is not before the closing time.
     */
    @Transactional
    public WorkDayDTO create(Long id,WorkDayCreateUpdateDTO workDayCreateUpdateDTO) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        WorkDay workDay = workDayMapper.toEntity(workDayCreateUpdateDTO);
        if (workDay.getOpenTime().isBefore(workDay.getCloseTime())){
            workDay.setRestaurant(restaurant);
            return workDayMapper.toDTO(workDayRepository.save(workDay));
        } else {
            throw new IllegalArgumentException("The data provided wasn't valid!");
        }
    }

    /**
     * Modifies information of an existing workday entity.
     * @param id unique identifier of the workday entry that is being modified.
     * @param workDayCreateUpdateDTO data containing updated workday information.
     * @return WorkDayDTO record containing updated workday information.
     * @throws jakarta.persistence.EntityNotFoundException If the workday entry is not found.
     * @throws java.lang.IllegalArgumentException If the updated opening time is not before the closing time.
     */
    @Transactional
    public WorkDayDTO update(Long id, WorkDayCreateUpdateDTO workDayCreateUpdateDTO) {
        WorkDay workDay = workDayRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No workday found with given id!"));

        workDayMapper.updateEntityFromUpdateDto(workDayCreateUpdateDTO, workDay);
        if (workDay.getOpenTime().isBefore(workDay.getCloseTime())){
            WorkDay savedWorkDay = workDayRepository.save(workDay);
            return workDayMapper.toDTO(savedWorkDay);
        } else {
            throw new IllegalArgumentException("The data provided wasn't valid!");
        }
    }

    /**
     * Deletes a specific workday record from the database.
     * @param id unique identifier of the workday entry that is being deleted.
     * @throws jakarta.persistence.EntityNotFoundException If no matching workday can be found.
     */
    @Transactional
    public void delete(Long id) {
        WorkDay workDay = workDayRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Could not find workday with given id!"));

        workDayRepository.delete(workDay);
    }
}
