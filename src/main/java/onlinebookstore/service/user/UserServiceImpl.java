package onlinebookstore.service.user;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import onlinebookstore.dto.user.UserRegistrationRequestDto;
import onlinebookstore.dto.user.UserResponseDto;
import onlinebookstore.exception.InvalidRoleException;
import onlinebookstore.exception.RegistrationException;
import onlinebookstore.mapper.UserMapper;
import onlinebookstore.model.Role;
import onlinebookstore.model.User;
import onlinebookstore.repository.role.RoleRepository;
import onlinebookstore.repository.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    public static final Map<String, Role> rolesCache = new HashMap<>();
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @PostConstruct
    public void initializeRolesCache() {
        List<Role> allRolesFromDb = roleRepository.findAll();
        if (allRolesFromDb.isEmpty()) {
            throw new InvalidRoleException("No roles found in the database");
        }
        allRolesFromDb.forEach(roleFromDb ->
                rolesCache.put(roleFromDb.getAuthority(), roleFromDb));
    }

    @Override
    public UserResponseDto register(UserRegistrationRequestDto userRegistrationDto)
            throws RegistrationException {
        if (userRepository.existsByEmail(userRegistrationDto.email())) {
            throw new RegistrationException("Can't register user with this email: "
                    + userRegistrationDto.email());
        }

        User user = userMapper.toModel(userRegistrationDto);
        user.setPassword(passwordEncoder.encode(userRegistrationDto.password()));

        return userMapper.toDto(userRepository.save(user));
    }
}
