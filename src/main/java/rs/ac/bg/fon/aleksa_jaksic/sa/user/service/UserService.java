package rs.ac.bg.fon.aleksa_jaksic.sa.user.service;

import rs.ac.bg.fon.aleksa_jaksic.sa.security.service.AuthService;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.domain.Role;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.domain.User;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.dtos.UserRegisterDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.dtos.UserResponseDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.dtos.UserUpdateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.mapper.UserMapper;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final AuthService authService;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, AuthService authService){
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    @Transactional
    public UserResponseDTO register(UserRegisterDTO userRegisterDTO) throws Exception{
        if (userRepository.existsByEmail(userRegisterDTO.email())){
            throw new Exception("This email is already taken!");
        }

        if (userRepository.existsByUsername(userRegisterDTO.username())){
            throw new Exception("This username is already taken!");
        }
        User user = userMapper.toEntity(userRegisterDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        return userMapper.toResponseDTO(userRepository.save(user));

    }

    public UserResponseDTO getCurrentUserDetails(String username) throws Exception {
        Optional<User> foundUser = userRepository.findByUsername(username);
        if (foundUser.isEmpty()){
            throw new Exception("No user found with given username");
        }
        
        return userMapper.toResponseDTO(foundUser.get());
        
    }

    public Map<String,Object> updateCurrentUser(String username, UserUpdateDTO userUpdateDTO) throws Exception {
        User foundUser = userRepository.findByUsername(username).
                orElseThrow(() -> new Exception("No user found with given username"));

        if (!foundUser.getEmail().equals(userUpdateDTO.email()) && userRepository.existsByEmail(userUpdateDTO.email())) {
            throw new Exception("This email is already taken!");
        }

        if (userUpdateDTO.newPassword() != null && !userUpdateDTO.newPassword().isBlank()) {
            if (!passwordEncoder.matches(userUpdateDTO.currentPassword(), foundUser.getPassword())) {
                throw new Exception("Current password verification failed!");
            }
            foundUser.setPassword(passwordEncoder.encode(userUpdateDTO.newPassword()));
        }

        userMapper.updateEntityFromUpdateDto(userUpdateDTO, foundUser);
        User savedUser = userRepository.save(foundUser);
        UserResponseDTO savedUserDTO = userMapper.toResponseDTO(savedUser);
        HttpHeaders headers = authService.createAuthHeadersForUpdatedUser(savedUser);

        Map<String,Object> map = new HashMap<>();
        map.put("dto",savedUserDTO);
        map.put("headers", headers);
        return map;

    }

}
