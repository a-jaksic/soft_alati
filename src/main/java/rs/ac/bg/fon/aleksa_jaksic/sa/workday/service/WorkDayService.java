package rs.ac.bg.fon.aleksa_jaksic.sa.workday.service;

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
import java.util.Optional;

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

    public List<WorkDayDTO> list(Long restaurantId) throws Exception {
        return workDayRepository.findByRestaurantId(restaurantId).stream()
                .map(workDayMapper::toDTO)
                .toList();
    }

    @Transactional
    public WorkDayDTO create(Long id,WorkDayCreateUpdateDTO workDayCreateUpdateDTO) throws Exception{
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new Exception("Restaurant not found"));

        WorkDay workDay = workDayMapper.toEntity(workDayCreateUpdateDTO);
        if (workDay.getOpenTime().isBefore(workDay.getCloseTime())){
            workDay.setRestaurant(restaurant);

            return workDayMapper.toDTO(workDayRepository.save(workDay));
        }else {
            throw new Exception("The data provided wasn't valid!");
        }
    }

    @Transactional
    public WorkDayDTO update(Long id, WorkDayCreateUpdateDTO workDayCreateUpdateDTO) throws Exception {
        WorkDay workDay = workDayRepository.findById(id).orElse(null);

        if (workDay == null){
            throw new Exception("No workday found with given id!");
        }
        workDayMapper.updateEntityFromUpdateDto(workDayCreateUpdateDTO, workDay);
        if (workDay.getOpenTime().isBefore(workDay.getCloseTime())){
            WorkDay savedWorkDay = workDayRepository.save(workDay);

            return workDayMapper.toDTO(savedWorkDay);
        }else {
            throw new Exception("The data provided wasn't valid!");
        }
    }

    @Transactional
    public void delete(Long id) throws Exception{
        Optional<WorkDay> workDay = workDayRepository.findById(id);
        if (workDay.isEmpty()){
            throw new Exception("Could not find workday with given id!");
        }
        else workDayRepository.delete(workDay.get());
    }
}
