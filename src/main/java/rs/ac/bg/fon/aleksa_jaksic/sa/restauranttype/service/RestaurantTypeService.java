package rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.service;

import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.domain.RestaurantType;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.dtos.RestaurantTypeCreateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.dtos.RestaurantTypeDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.mapper.RestaurantTypeMapper;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.repository.RestaurantTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RestaurantTypeService {

    private final RestaurantTypeRepository restaurantTypeRepository;
    private final RestaurantTypeMapper restaurantTypeMapper;

    public RestaurantTypeService(RestaurantTypeRepository restaurantTypeRepository, RestaurantTypeMapper restaurantTypeMapper){
        this.restaurantTypeRepository = restaurantTypeRepository;
        this.restaurantTypeMapper = restaurantTypeMapper;
    }

    public RestaurantTypeDTO get(Long id) throws Exception{
        return restaurantTypeRepository.findById(id)
                .map(restaurantTypeMapper::toDTO)
                .orElseThrow(() -> new Exception("No restaurant type found with id: " + id));
    }

    public List<RestaurantTypeDTO> list() {
        return restaurantTypeRepository.findAll().stream()
                .map(restaurantTypeMapper::toDTO)
                .toList();
    }

    @Transactional
    public RestaurantTypeDTO create(RestaurantTypeCreateDTO restaurantTypeCreateDTO) throws Exception{
        RestaurantType restaurantType = restaurantTypeMapper.toEntity(restaurantTypeCreateDTO);
        return restaurantTypeMapper.toDTO(restaurantTypeRepository.save(restaurantType));
    }

    @Transactional
    public void delete(Long id) throws  Exception {
        Optional<RestaurantType> restaurantType = restaurantTypeRepository.findById(id);
        if (restaurantType.isEmpty()){
            throw new Exception("Could not find restaurant type with given id!");
        }
        else restaurantTypeRepository.delete(restaurantType.get());
    }

}
