package rs.ac.bg.fon.aleksa_jaksic.sa.security.controller;

import rs.ac.bg.fon.aleksa_jaksic.sa.security.dtos.UserLoginDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.security.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/users/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO userLoginDTO) {

        try {
            UserDetails userDetails = authService.authenticate(userLoginDTO.username(), userLoginDTO.password());

            HttpHeaders cookieHeaders = authService.createAuthHeaders(userDetails);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(cookieHeaders)
                    .body("User logged in successfully");

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, login failed");
        }

    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok()
                .headers(authService.createLogoutHeaders())
                .body("Logged out");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        try {

            // 1. Validate and generate new headers
            HttpHeaders newCookies = authService.refreshAccessToken(request);

            return ResponseEntity.ok().headers(newCookies).body("Token refreshed");
        } catch (Exception e) {
            // If refresh fails (expired or invalid), return 401 so frontend redirects to login
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired, please login again");
        }
    }
}
