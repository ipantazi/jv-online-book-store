package onlinebookstore.security;

import lombok.RequiredArgsConstructor;
import onlinebookstore.exception.EntityNotFoundException;
import onlinebookstore.repository.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email).orElseThrow(()
                -> new EntityNotFoundException("Can't find user by email: " + email));
    }
}
