package onlinebookstore.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import onlinebookstore.config.MapperConfig;
import onlinebookstore.dto.user.UserRegistrationRequestDto;
import onlinebookstore.dto.user.UserResponseDto;
import onlinebookstore.exception.InvalidRoleException;
import onlinebookstore.model.Role;
import onlinebookstore.model.User;
import onlinebookstore.service.user.UserServiceImpl;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toModel(UserRegistrationRequestDto userRegistrationRequestDto);

    @AfterMapping
    default void setRolesToModel(
            @MappingTarget
            User user,
            UserRegistrationRequestDto userRegistrationRequestDto) {
        Set<Role> roles = userRegistrationRequestDto.roles().stream()
                .map(element -> {
                    if (element == null || element.isBlank()) {
                        throw new InvalidRoleException("Role name cannot be null or blank");
                    }
                    Role cachedRole = UserServiceImpl.rolesCache.get(element);
                    if (cachedRole == null) {
                        throw new InvalidRoleException("Role not found: " + element);
                    }
                    return cachedRole;
                })
                .collect(Collectors.toSet());
        user.setRoles(roles);
    }

    @Mapping(target = "roles", ignore = true)
    UserResponseDto toDto(User user);

    @AfterMapping
    default void setRolesToDto(@MappingTarget UserResponseDto userResponseDto, User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getAuthority)
                .collect(Collectors.toSet());
        userResponseDto.setRoles(roles);
    }
}
