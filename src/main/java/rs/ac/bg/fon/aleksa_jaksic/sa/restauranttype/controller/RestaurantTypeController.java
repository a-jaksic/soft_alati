package rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.controller;

import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.dtos.RestaurantTypeCreateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.dtos.RestaurantTypeDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restauranttype.service.RestaurantTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RestaurantTypeController {

    private final RestaurantTypeService restaurantTypeService;

    public RestaurantTypeController(RestaurantTypeService restaurantTypeService){
        this.restaurantTypeService = restaurantTypeService;
    }

    @GetMapping("/api/restaurant_types/{id}")
    public ResponseEntity<Object> get(@PathVariable Long id) {
        try {
            RestaurantTypeDTO restaurantType = restaurantTypeService.get(id);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(restaurantType);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the retrieval of restaurant type was not successful!");
        }
    }

    @GetMapping("/api/restaurant_types")
    public ResponseEntity<Object> list() {
        try {
            List<RestaurantTypeDTO> restaurantTypeList = restaurantTypeService.list();
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(restaurantTypeList);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the retrieval of restaurant types was not successful!");
        }
    }

    @PostMapping("/api/admin/restaurant_types/create")
    public ResponseEntity<Object> create(@RequestBody RestaurantTypeCreateDTO restaurantTypeCreateDTO){
        try {
            RestaurantTypeDTO restaurantType = restaurantTypeService.create(restaurantTypeCreateDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(restaurantType);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the restaurant type creation was not successful!");
        }
    }

    @DeleteMapping("/api/admin/restaurant_types/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id){
        try {
            restaurantTypeService.delete(id);
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build();
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the restaurant type deletion was not successful!");
        }
    }
}
