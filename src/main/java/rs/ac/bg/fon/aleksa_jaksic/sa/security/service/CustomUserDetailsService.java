package rs.ac.bg.fon.aleksa_jaksic.sa.security.service;

import rs.ac.bg.fon.aleksa_jaksic.sa.user.domain.User;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> foundUser = userRepository.findByUsername(username);
        if (foundUser.isEmpty()){
            throw new UsernameNotFoundException("No user found with username: " + username);
        }
        return org.springframework.security.core.userdetails.User.builder()
                .username(foundUser.get().getUsername())
                .password(foundUser.get().getPassword())
                .authorities("ROLE_" + foundUser.get().getRole().name())
                .build();
    }
}
