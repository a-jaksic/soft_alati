package rs.ac.bg.fon.aleksa_jaksic.sa.security.service;

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

    public UserDetails authenticate(String username, String password) {
        return (UserDetails) authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        ).getPrincipal();
    }

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

    public HttpHeaders refreshAccessToken(HttpServletRequest request) throws Exception {
        // 1. Extract the existing refresh token
        String refreshToken = jwtUtils.extractTokenFromCookie(request, "refresh_token");

        // 2. Validate it (if it's expired, this throws an exception/returns false)
        if (refreshToken == null || !jwtUtils.isTokenValid(refreshToken)) {
            throw new Exception("Refresh token expired. Please login again.");
        }

        // 3. Extract user info from the EXISTING refresh token
        String username = jwtUtils.extractUsername(refreshToken);
        List<SimpleGrantedAuthority> authorities = jwtUtils.extractAuthorities(refreshToken);

        // 4. Create ONLY a new Access Token
        // We use a dummy UserDetails or just update JwtUtils to take username/authorities
        String newAccessToken = jwtUtils.createTokenFromRefresh(username, authorities, accessExpiration);

        // 5. Create ONLY the Access Cookie
        ResponseCookie accessCookie = ResponseCookie.from("access_token", newAccessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(accessExpiration / 1000)
                .sameSite("Lax")
                .build();

        // 6. Return headers with ONLY the access cookie.
        // The browser will keep the old refresh_token until it naturally expires.
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        return headers;
    }


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

    public HttpHeaders createAuthHeadersForUpdatedUser(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // Already encrypted in DB
                .authorities("ROLE_" + user.getRole().name())
                .build();

        return createAuthHeaders(userDetails);
    }

}
