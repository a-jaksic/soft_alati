package rs.ac.bg.fon.aleksa_jaksic.sa.service;

import static org.junit.jupiter.api.Assertions.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import rs.ac.bg.fon.aleksa_jaksic.sa.security.service.AuthService;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.domain.Role;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.domain.User;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.dtos.UserRegisterDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.dtos.UserResponseDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.dtos.UserUpdateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.mapper.UserMapper;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.repository.UserRepository;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.service.UserService;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserService userService;

    private User user;
    private String username;

    @BeforeEach
    void setUp() {
        username = "a-jaksic";
        user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail("aleksa@example.com");
        user.setPassword("hashed_old_password");
        user.setRole(Role.USER);
    }

    @Test
    @DisplayName("Should successfully register user when username and email are unique")
    void register_Success() {
        UserRegisterDTO registerDTO = new UserRegisterDTO(username, "aleksa@example.com", "plain_password");
        UserResponseDTO responseDTO = new UserResponseDTO(1L, username, "aleksa@example.com", Role.USER);

        when(userRepository.existsByEmail(registerDTO.email())).thenReturn(false);
        when(userRepository.existsByUsername(registerDTO.username())).thenReturn(false);
        when(userMapper.toEntity(registerDTO)).thenReturn(user);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("hashed_plain_password");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(responseDTO);

        UserResponseDTO result = userService.register(registerDTO);

        assertNotNull(result);
        assertEquals(username, result.username());
        assertEquals(Role.USER, user.getRole());
        verify(userRepository).save(user);
    }

    @ParameterizedTest
    @CsvSource({
            "true,  false, This email is already taken!",
            "false, true,  This username is already taken!"
    })
    @DisplayName("Should throw IllegalArgumentException during registration when unique data constraints fail")
    void register_DuplicateData_ThrowsException(boolean emailExists, boolean usernameExists, String expectedMessage) {
        UserRegisterDTO registerDTO = new UserRegisterDTO(username, "aleksa@example.com", "plain_password");

        when(userRepository.existsByEmail(registerDTO.email())).thenReturn(emailExists);
        if (!emailExists) {
            when(userRepository.existsByUsername(registerDTO.username())).thenReturn(usernameExists);
        }

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.register(registerDTO));
        assertEquals(expectedMessage, ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return user details when user exists")
    void getCurrentUserDetails_Success() {
        UserResponseDTO responseDTO = new UserResponseDTO(1L, username, "aleksa@example.com", Role.USER);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDTO(user)).thenReturn(responseDTO);

        UserResponseDTO result = userService.getCurrentUserDetails(username);

        assertNotNull(result);
        assertEquals(username, result.username());
        verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when retrieving details for non-existent user")
    void getCurrentUserDetails_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getCurrentUserDetails(username));
    }

    @Test
    @DisplayName("Should update user profile and re-hash password when valid password update is provided")
    void updateCurrentUser_WithPasswordUpdate_Success() {
        UserUpdateDTO updateDTO = new UserUpdateDTO(username, "new_email@example.com", "plain_old_password", "new_plain_password");
        UserResponseDTO responseDTO = new UserResponseDTO(1L, username, "new_email@example.com", Role.USER);
        HttpHeaders headers = new HttpHeaders();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(updateDTO.email())).thenReturn(false);
        when(passwordEncoder.matches(updateDTO.currentPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(updateDTO.newPassword())).thenReturn("hashed_new_password");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(responseDTO);
        when(authService.createAuthHeadersForUpdatedUser(user)).thenReturn(headers);

        Map<String, Object> result = userService.updateCurrentUser(username, updateDTO);

        assertNotNull(result);
        assertEquals(responseDTO, result.get("dto"));
        assertEquals(headers, result.get("headers"));
        assertEquals("hashed_new_password", user.getPassword());
        verify(userMapper).updateEntityFromUpdateDto(updateDTO, user);
        verify(userRepository).save(user);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    @DisplayName("Should update profile details without changing password when new password field is blank or null")
    void updateCurrentUser_WithoutPasswordUpdate_Success(String blankPassword) {
        UserUpdateDTO updateDTO = new UserUpdateDTO(username, "aleksa@example.com", "plain_old_password", blankPassword);
        UserResponseDTO responseDTO = new UserResponseDTO(1L, username, "aleksa@example.com", Role.USER);
        HttpHeaders headers = new HttpHeaders();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(responseDTO);
        when(authService.createAuthHeadersForUpdatedUser(user)).thenReturn(headers);

        Map<String, Object> result = userService.updateCurrentUser(username, updateDTO);

        assertNotNull(result);
        assertEquals("hashed_old_password", user.getPassword());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException during update when new email is already taken")
    void updateCurrentUser_EmailTaken_ThrowsException() {
        UserUpdateDTO updateDTO = new UserUpdateDTO(username, "taken@example.com", null, null);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(updateDTO.email())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.updateCurrentUser(username, updateDTO));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException during update when current password verification fails")
    void updateCurrentUser_PasswordVerificationFails_ThrowsException() {
        UserUpdateDTO updateDTO = new UserUpdateDTO(username, "aleksa@example.com", "wrong_old_password", "new_password");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(updateDTO.currentPassword(), user.getPassword())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.updateCurrentUser(username, updateDTO));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException during update when user is missing")
    void updateCurrentUser_UserNotFound_ThrowsException() {
        UserUpdateDTO updateDTO = new UserUpdateDTO(username, "aleksa@example.com", null, null);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.updateCurrentUser(username, updateDTO));
    }
}