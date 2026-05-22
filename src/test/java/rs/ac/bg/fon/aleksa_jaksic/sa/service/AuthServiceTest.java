package rs.ac.bg.fon.aleksa_jaksic.sa.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import rs.ac.bg.fon.aleksa_jaksic.sa.security.jwt.JwtUtils;
import rs.ac.bg.fon.aleksa_jaksic.sa.security.service.AuthService;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.domain.User;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.domain.Role;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private UserDetails mockUserDetails;
    private final Long accessExpiration = 3600000L; // 1 hour
    private final Long refreshExpiration = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        mockUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username("aleksa")
                .password("hashed_password")
                .authorities("ROLE_USER")
                .build();

        // Injecting @Value configurations via Reflection
        ReflectionTestUtils.setField(authService, "accessExpiration", accessExpiration);
        ReflectionTestUtils.setField(authService, "refreshExpiration", refreshExpiration);
    }

    @Test
    @DisplayName("Should successfully authenticate user and return UserDetails context")
    void authenticate_Success() {
        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getPrincipal()).thenReturn(mockUserDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);

        UserDetails result = authService.authenticate("aleksa", "password123");

        assertNotNull(result);
        assertEquals("aleksa", result.getUsername());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should build both access and refresh cookies when creating authorization headers")
    void createAuthHeaders_Success() {
        when(jwtUtils.createToken(mockUserDetails, accessExpiration)).thenReturn("mock_access_token");
        when(jwtUtils.createToken(mockUserDetails, refreshExpiration)).thenReturn("mock_refresh_token");

        HttpHeaders headers = authService.createAuthHeaders(mockUserDetails);

        List<String> cookies = headers.get(HttpHeaders.SET_COOKIE);
        assertNotNull(cookies);
        assertEquals(2, cookies.size());

        java.util.List<java.net.HttpCookie> parsedCookies = cookies.stream()
                .flatMap(cookieHeader -> java.net.HttpCookie.parse(cookieHeader).stream())
                .toList();

        java.net.HttpCookie accessCookie = parsedCookies.stream()
                .filter(c -> "access_token".equals(c.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("access_token cookie missing"));

        assertEquals("mock_access_token", accessCookie.getValue());
        assertTrue(accessCookie.isHttpOnly());
        assertEquals("/", accessCookie.getPath());
        assertEquals(accessExpiration / 1000, accessCookie.getMaxAge());

        java.net.HttpCookie refreshCookie = parsedCookies.stream()
                .filter(c -> "refresh_token".equals(c.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("refresh_token cookie missing"));

        assertEquals("mock_refresh_token", refreshCookie.getValue());
        assertTrue(refreshCookie.isHttpOnly());
        assertEquals("/api/users/auth/refresh", refreshCookie.getPath());
        assertEquals(refreshExpiration / 1000, refreshCookie.getMaxAge());
    }

    @Test
    @DisplayName("Should successfully refresh access token when a valid refresh cookie is provided")
    void refreshAccessToken_Success() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        when(jwtUtils.extractTokenFromCookie(mockRequest, "refresh_token")).thenReturn("valid_refresh_token");
        when(jwtUtils.isTokenValid("valid_refresh_token")).thenReturn(true);
        when(jwtUtils.extractUsername("valid_refresh_token")).thenReturn("aleksa");
        when(jwtUtils.extractAuthorities("valid_refresh_token")).thenReturn(authorities);
        when(jwtUtils.createTokenFromRefresh("aleksa", authorities, accessExpiration)).thenReturn("new_access_token");

        HttpHeaders headers = authService.refreshAccessToken(mockRequest);

        List<String> cookies = headers.get(HttpHeaders.SET_COOKIE);
        assertNotNull(cookies);
        assertEquals(1, cookies.size());
        assertTrue(cookies.get(0).contains("access_token=new_access_token"));
    }

    @ParameterizedTest
    @CsvSource({
            "invalid_or_expired_token, false",
            ", true" // Handles a non-existent token where extraction results in null
    })
    @DisplayName("Should throw BadCredentialsException when refresh token validation fails or is missing")
    void refreshAccessToken_InvalidOrMissingToken_ThrowsException(String token, boolean shouldBeNull) {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String extractedToken = shouldBeNull ? null : token;

        when(jwtUtils.extractTokenFromCookie(mockRequest, "refresh_token")).thenReturn(extractedToken);
        if (extractedToken != null) {
            when(jwtUtils.isTokenValid(extractedToken)).thenReturn(false);
        }

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () ->
                authService.refreshAccessToken(mockRequest)
        );

        assertEquals("Refresh token expired or invalid. Please login again.", exception.getMessage());
        verify(jwtUtils, never()).extractUsername(anyString());
    }

    @Test
    @DisplayName("Should create expired cookie headers to force browser sign-out states")
    void createLogoutHeaders_Success() {
        HttpHeaders headers = authService.createLogoutHeaders();

        List<String> cookies = headers.get(HttpHeaders.SET_COOKIE);
        assertNotNull(cookies);
        assertEquals(2, cookies.size());

        assertTrue(cookies.stream().anyMatch(c -> c.contains("access_token=") && c.contains("Max-Age=0")));
        assertTrue(cookies.stream().anyMatch(c -> c.contains("refresh_token=") && c.contains("Max-Age=0")));
    }

    @Test
    @DisplayName("Should successfully synchronize state context for an updated user profile")
    void createAuthHeadersForUpdatedUser_Success() {
        User user = new User();
        user.setUsername("aleksa_updated");
        user.setPassword("new_password");
        user.setRole(Role.USER);

        when(jwtUtils.createToken(any(UserDetails.class), eq(accessExpiration))).thenReturn("sync_access_token");
        when(jwtUtils.createToken(any(UserDetails.class), eq(refreshExpiration))).thenReturn("sync_refresh_token");

        HttpHeaders headers = authService.createAuthHeadersForUpdatedUser(user);

        List<String> cookies = headers.get(HttpHeaders.SET_COOKIE);
        assertNotNull(cookies);
        assertEquals(2, cookies.size());
        assertTrue(cookies.stream().anyMatch(c -> c.contains("access_token=sync_access_token")));
        assertTrue(cookies.stream().anyMatch(c -> c.contains("refresh_token=sync_refresh_token")));
    }
}
