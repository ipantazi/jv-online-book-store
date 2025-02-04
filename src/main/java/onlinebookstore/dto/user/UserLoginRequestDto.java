package onlinebookstore.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserLoginRequestDto(
        @Email(message = "Invalid format email.")
        @Size(max = 50, message = "Email address should be exceed 50 characters.")
        String email,

        @NotBlank(message = "Invalid password. Password shouldn't be blank.")
        String password
) {
}
