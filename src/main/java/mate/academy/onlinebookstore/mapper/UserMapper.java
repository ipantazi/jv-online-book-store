package mate.academy.onlinebookstore.mapper;

import mate.academy.onlinebookstore.config.MapperConfig;
import mate.academy.onlinebookstore.dto.user.UserRegistrationRequestDto;
import mate.academy.onlinebookstore.dto.user.UserResponseDto;
import mate.academy.onlinebookstore.model.User;
import mate.academy.onlinebookstore.util.HashUtil;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "salt", ignore = true)
    User toModel(UserRegistrationRequestDto userRegistrationRequestDto);

    @AfterMapping
    default void setHashingPassword(
            @MappingTarget
            User user,
            UserRegistrationRequestDto userRegistrationRequestDto) {
        user.setSalt(HashUtil.getSalt());
        user.setPassword(HashUtil.hashPassword(
                userRegistrationRequestDto.password(),
                user.getSalt()));
    }

    UserResponseDto toDto(User user);
}
