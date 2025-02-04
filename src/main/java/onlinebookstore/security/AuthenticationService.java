package onlinebookstore.security;

import lombok.RequiredArgsConstructor;
import onlinebookstore.dto.user.UserLoginRequestDto;
import onlinebookstore.dto.user.UserLoginResponseDto;
import onlinebookstore.exception.AuthenticationException;
import onlinebookstore.model.User;
import onlinebookstore.repository.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserLoginResponseDto authenticate(UserLoginRequestDto requestDto) {
        User userFromDB = userRepository.findByEmail(requestDto.email())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password."));

        if (!passwordEncoder.matches(requestDto.password(), userFromDB.getPassword())) {
            throw new AuthenticationException("Invalid email or password.");
        }

        String token = jwtUtil.generateToken(userFromDB.getEmail());
        return new UserLoginResponseDto(token);
    }
}
