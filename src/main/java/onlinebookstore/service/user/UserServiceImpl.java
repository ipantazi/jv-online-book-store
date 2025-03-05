package onlinebookstore.service.user;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import onlinebookstore.dto.user.UserRegistrationRequestDto;
import onlinebookstore.dto.user.UserResponseDto;
import onlinebookstore.exception.RegistrationException;
import onlinebookstore.mapper.UserMapper;
import onlinebookstore.model.Role;
import onlinebookstore.model.User;
import onlinebookstore.repository.role.RoleRepository;
import onlinebookstore.repository.user.UserRepository;
import onlinebookstore.service.shoppingcart.ShoppingCartService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    public static final Map<String, Role> rolesCache = new HashMap<>();
    private static final String DEFAULT_ROLE = "USER";
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final ShoppingCartService shoppingCartService;

    @PostConstruct
    public void initializeRolesCache() {
        roleRepository.findAll().forEach(roleFromDb ->
                rolesCache.put(roleFromDb.getAuthority(), roleFromDb));
    }

    @Override
    @Transactional
    public UserResponseDto register(UserRegistrationRequestDto userRegistrationDto)
            throws RegistrationException {
        if (userRepository.existsByEmail(userRegistrationDto.email())) {
            throw new RegistrationException("Can't register user with this email: "
                    + userRegistrationDto.email());
        }

        User user = userMapper.toUserEntity(userRegistrationDto);
        user.setPassword(passwordEncoder.encode(userRegistrationDto.password()));
        user.getRoles().add(rolesCache.get(DEFAULT_ROLE));
        userRepository.save(user);

        shoppingCartService.registerNewShoppingCart(user);

        return userMapper.toUserDto(user);
    }
}
