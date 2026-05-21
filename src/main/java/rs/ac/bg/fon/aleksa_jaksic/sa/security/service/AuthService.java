package rs.ac.bg.fon.aleksa_jaksic.sa.security.service;

import org.springframework.security.authentication.BadCredentialsException;
import rs.ac.bg.fon.aleksa_jaksic.sa.security.jwt.JwtUtils;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for orchestrating security authentication and token management.
 * Validates credentials with standard security configurations, handles stateless session cookies,
 * processes and evaluates tokens and configures clearance cookies for sign-outs.
 * @author Aleksa Jakšić (a-jaksic)
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Value("${security.token.access-expiration}")
    private Long accessExpiration;

    @Value("${security.token.refresh-expiration}")
    private Long refreshExpiration;

    public AuthService(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Verifies the user identity with his credentials using the underlying spring security mechanism.
     * @param username the username of the user that is being authenticated.
     * @param password the password of the user that is being authenticated.
     * @return UserDetails populated with the necessary data.
     */
    public UserDetails authenticate(String username, String password) {
        return (UserDetails) authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        ).getPrincipal();
    }

    /**
     * Formats JWT security authorizations wrapped within HTTP Set-Cookie headers.
     * @param userDetails authentication containing user information.
     * @return HttpHeaders structure packed with security cookies.
     */
    public HttpHeaders createAuthHeaders(UserDetails userDetails) {
        String accessToken = jwtUtils.createToken(userDetails, accessExpiration);
        String refreshToken = jwtUtils.createToken(userDetails, refreshExpiration);

        ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                .secure(false) // Set to true in production (HTTPS)
                .path("/")
                .maxAge(accessExpiration / 1000)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/api/users/auth/refresh") // Only sent to refresh endpoint
                .maxAge(refreshExpiration / 1000)
                .sameSite("Lax")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return headers;
    }

    /**
     * Decodes and validates session to issue a new standalone access cookie.
     * @param request request processing active context details.
     * @return HttpHeaders containing the new refreshed access cookie.
     * @throws org.springframework.security.authentication.BadCredentialsException If the refresh cookie is expired or invalid.
     */
    public HttpHeaders refreshAccessToken(HttpServletRequest request) {
        String refreshToken = jwtUtils.extractTokenFromCookie(request, "refresh_token");

        if (refreshToken == null || !jwtUtils.isTokenValid(refreshToken)) {
            throw new BadCredentialsException("Refresh token expired or invalid. Please login again.");
        }

        String username = jwtUtils.extractUsername(refreshToken);
        List<SimpleGrantedAuthority> authorities = jwtUtils.extractAuthorities(refreshToken);

        String newAccessToken = jwtUtils.createTokenFromRefresh(username, authorities, accessExpiration);

        ResponseCookie accessCookie = ResponseCookie.from("access_token", newAccessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(accessExpiration / 1000)
                .sameSite("Lax")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        return headers;
    }

    /**
     * Builds a standard cookie structure with no values and instantaneous expiration time to force a logout.
     * @return HttpHeaders structure with empty cookies.
     */
    public HttpHeaders createLogoutHeaders() {
        ResponseCookie accessCookie = ResponseCookie.from("access_token", "")
                .maxAge(0).path("/").build();
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
                .maxAge(0).path("/api/users/auth/refresh").build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return headers;
    }

    /**
     * Re-creates authorization cookies after user parameters are modified to synchronize credential states.
     * @param user user information containing modified data.
     * @return HttpHeaders containing new cookies that synchronize states.
     */
    public HttpHeaders createAuthHeadersForUpdatedUser(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();

        return createAuthHeaders(userDetails);
    }

}
