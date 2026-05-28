package rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.controller;

import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.dtos.RestaurantCreateUpdateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.dtos.RestaurantDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.dtos.RestaurantDetailsDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.service.RestaurantService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/api/restaurants/{id}")
    public ResponseEntity<Object> get(@PathVariable Long id){
        try {
            RestaurantDetailsDTO restaurantDetailsDTO = restaurantService.getRestaurant(id);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(restaurantDetailsDTO);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the restaurant retrieval was not successful!");
        }
    }

    @GetMapping("/api/restaurants")
    public ResponseEntity<Object> getRestaurants(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Long cityId,
            @ParameterObject Pageable pageable){
        try {
            Page<RestaurantDTO> restaurantList = restaurantService.getRestaurants(name, typeId, cityId, pageable);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(restaurantList);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the restaurants retrieval was not successful!");
        }

    }

    @PostMapping("/api/admin/restaurants/create")
    public ResponseEntity<Object> create(@RequestBody RestaurantCreateUpdateDTO restaurantCreateUpdateDTO){
        try {
            RestaurantDetailsDTO restaurantDetailsDTO = restaurantService.create(restaurantCreateUpdateDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(restaurantDetailsDTO);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the restaurant creation was not successful!");
        }
    }

    @PutMapping("/api/admin/restaurants/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody RestaurantCreateUpdateDTO restaurantCreateUpdateDTO
    ) {
        try {
            RestaurantDetailsDTO restaurantDetailsDTO = restaurantService.update(id, restaurantCreateUpdateDTO);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(restaurantDetailsDTO);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the restaurant update was not successful!");
        }
    }

    @DeleteMapping("/api/admin/restaurants/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        try {
            restaurantService.delete(id);
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build();
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the restaurant deletion was not successful! " + e);
        }
    }

}
