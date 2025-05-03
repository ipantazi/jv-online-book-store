package onlinebookstore.service.user;

import static onlinebookstore.util.TestDataUtil.EXISTING_USER_ID;
import static onlinebookstore.util.TestDataUtil.NEW_USER_ID;
import static onlinebookstore.util.TestDataUtil.USER_DTO_IGNORING_FIELD;
import static onlinebookstore.util.TestDataUtil.createTestRoleAdmin;
import static onlinebookstore.util.TestDataUtil.createTestRoleUser;
import static onlinebookstore.util.TestDataUtil.createTestUser;
import static onlinebookstore.util.TestDataUtil.createTestUserRegistrationRequestDto;
import static onlinebookstore.util.TestDataUtil.createTestUserResponseDto;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertObjectsAreEqualIgnoringFields;
import static onlinebookstore.util.service.ServiceTestUtil.mockRolesCash;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;
import onlinebookstore.dto.user.UserRegistrationRequestDto;
import onlinebookstore.dto.user.UserResponseDto;
import onlinebookstore.exception.RegistrationException;
import onlinebookstore.mapper.UserMapper;
import onlinebookstore.model.Role;
import onlinebookstore.model.User;
import onlinebookstore.repository.user.UserRepository;
import onlinebookstore.service.shoppingcart.ShoppingCartService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ShoppingCartService shoppingCartService;
    @InjectMocks
    private UserServiceImpl userService;

    @BeforeAll
    static void beforeAll() throws Exception {
        Set<Role> roles = Set.of(createTestRoleUser(), createTestRoleAdmin());
        mockRolesCash(roles);
    }

    @Test
    @DisplayName("Verify register() method works.")
    public void register_ValidUserRegistrationRequestDto_ReturnsUserResponseDto() {
        //Given
        UserResponseDto expectedUserResponseDto = createTestUserResponseDto(NEW_USER_ID);
        User user = createTestUser(expectedUserResponseDto);
        UserRegistrationRequestDto userRegistrationDto = createTestUserRegistrationRequestDto(
                expectedUserResponseDto);
        when(userRepository.existsByEmail(userRegistrationDto.email())).thenReturn(false);
        when(userMapper.toUserEntity(userRegistrationDto)).thenReturn(user);
        when(passwordEncoder.encode(userRegistrationDto.password())).thenReturn(user.getPassword());
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toUserDto(any(User.class))).thenReturn(expectedUserResponseDto);

        //When
        UserResponseDto actualUserResponseDto = userService.register(userRegistrationDto);

        //Then
        assertObjectsAreEqualIgnoringFields(
                actualUserResponseDto,
                expectedUserResponseDto,
                USER_DTO_IGNORING_FIELD
        );
        verify(userRepository, times(1)).existsByEmail(userRegistrationDto.email());
        verify(userMapper, times(1)).toUserEntity(userRegistrationDto);
        verify(passwordEncoder, times(1)).encode(userRegistrationDto.password());
        verify(userRepository, times(1)).save(any(User.class));
        verify(shoppingCartService, times(1)).registerNewShoppingCart(any(User.class));
        verify(userMapper, times(1)).toUserDto(any(User.class));
        verifyNoMoreInteractions(userRepository, userMapper, passwordEncoder, shoppingCartService);
    }

    @Test
    @DisplayName("Verify that an exception is throw when an email already exists.")
    public void register_UserEmailAlreadyExists_ThrowsException() {
        //Given
        UserRegistrationRequestDto userRegistrationDto =
                createTestUserRegistrationRequestDto(EXISTING_USER_ID);
        when(userRepository.existsByEmail(userRegistrationDto.email())).thenReturn(true);

        //When
        assertThatThrownBy(() -> userService.register(userRegistrationDto))
                .isInstanceOf(RegistrationException.class)
                .hasMessageContaining("Can't register user with this email: "
                        + userRegistrationDto.email());
        //Then

        verify(userRepository, times(1)).existsByEmail(userRegistrationDto.email());
        verify(userRepository, never()).save(any(User.class));
        verify(shoppingCartService, never()).registerNewShoppingCart(any(User.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userMapper, passwordEncoder, shoppingCartService);
    }
}
