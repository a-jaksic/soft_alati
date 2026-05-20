package rs.ac.bg.fon.aleksa_jaksic.sa.city.service;

import rs.ac.bg.fon.aleksa_jaksic.sa.city.domain.City;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.dtos.CityCreateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.dtos.CityDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.mapper.CityMapper;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.repository.CityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CityService {

    private final CityRepository cityRepository;

    private final CityMapper cityMapper;

    public CityService(CityRepository cityRepository, CityMapper cityMapper){
        this.cityRepository = cityRepository;
        this.cityMapper = cityMapper;
    }

    public CityDTO get(Long id) throws Exception{
        return cityRepository.findById(id)
                .map(cityMapper::toDTO)
                .orElseThrow(() -> new Exception("No city found with id: " + id));
    }

    public List<CityDTO> list() {
        return cityRepository.findAll()
                .stream()
                .map(cityMapper::toDTO)
                .toList();
    }

    @Transactional
    public CityDTO create(CityCreateDTO cityCreateDTO) {
        City city = cityMapper.toEntity(cityCreateDTO);
        return cityMapper.toDTO(cityRepository.save(city));
    }

    @Transactional
    public void delete(Long id) throws Exception{
        Optional<City> city = cityRepository.findById(id);
        if (city.isEmpty()){
            throw new Exception("Could not find city with given id!");
        }
        else cityRepository.delete(city.get());
    }
}
