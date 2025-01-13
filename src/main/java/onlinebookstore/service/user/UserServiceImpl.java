package onlinebookstore.service.user;

import lombok.RequiredArgsConstructor;
import onlinebookstore.dto.user.UserRegistrationRequestDto;
import onlinebookstore.dto.user.UserResponseDto;
import onlinebookstore.exception.RegistrationException;
import onlinebookstore.mapper.UserMapper;
import onlinebookstore.model.User;
import onlinebookstore.repository.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.existsByEmail(requestDto.email())) {
            throw new RegistrationException("Can't register user. Email address: "
                    + requestDto.email() + " already in use");
        }
        User user = userMapper.toModel(requestDto);
        return userMapper.toDto(userRepository.save(user));
    }
}
