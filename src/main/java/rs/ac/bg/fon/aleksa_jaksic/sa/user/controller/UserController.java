package rs.ac.bg.fon.aleksa_jaksic.sa.user.controller;

import rs.ac.bg.fon.aleksa_jaksic.sa.user.dtos.UserRegisterDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.dtos.UserResponseDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.dtos.UserUpdateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){

        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody UserRegisterDTO userRegisterDTO){
        try {
            UserResponseDTO user = userService.register(userRegisterDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(user);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the user was not successfully made!");
        }

    }

    @GetMapping("/me")
    public ResponseEntity<Object> getCurrentUserDetails(Authentication authentication){
        try {
            UserResponseDTO currentUser = userService.getCurrentUserDetails(authentication.getName());
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(currentUser);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, no user found with given id!");
        }
    }

    @PatchMapping("/me/update")
    public ResponseEntity<Object> updateCurrentUser(Authentication authentication, @RequestBody UserUpdateDTO userUpdateDTO){
        try {
            Map<String,Object> map = userService.updateCurrentUser(authentication.getName(), userUpdateDTO);
            UserResponseDTO updatedUser = (UserResponseDTO) map.get("dto");
            HttpHeaders headers = (HttpHeaders) map.get("headers");
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(headers)
                    .body(updatedUser);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, no user found with given id!");
        }
    }

}
