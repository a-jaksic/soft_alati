package rs.ac.bg.fon.aleksa_jaksic.sa.city.controller;

import rs.ac.bg.fon.aleksa_jaksic.sa.city.dtos.CityCreateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.dtos.CityDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.city.service.CityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService){
        this.cityService = cityService;
    }

    @GetMapping("/api/cities/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        try {
            CityDTO city = cityService.get(id);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(city);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the retrieval of city was not successful!");
        }
    }

    @GetMapping("/api/cities")
    public ResponseEntity<?> list() {
        try {
            List<CityDTO> cityList = cityService.list();
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(cityList);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the retrieval of cities was not successful!");
        }
    }

    @PostMapping("/api/admin/cities/create")
    public ResponseEntity<?> create(@RequestBody CityCreateDTO cityCreateDTO){
        try {
            CityDTO city = cityService.create(cityCreateDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(city);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the city creation was not successful!");
        }
    }

    @DeleteMapping("/api/admin/cities/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        try {
            cityService.delete(id);
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the city deletion was not successful!");
        }
    }

}
