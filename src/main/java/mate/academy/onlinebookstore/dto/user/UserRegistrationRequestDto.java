package mate.academy.onlinebookstore.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import mate.academy.onlinebookstore.validation.FieldMatch;

@FieldMatch(field = "password",
        fieldMatch = "repeatPassword",
        message = "The passwords do not match."
)
public record UserRegistrationRequestDto(
        @Email(message = "Invalid format email.")
        String email,

        @NotNull(message = "Invalid password. Password shouldn't be null.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w]).{8,50}$",
                message = "Password must be 8-50 characters long, include at least one lowercase "
                        + "letter, one uppercase letter, one number, and one special character."
        )
        String password,

        String repeatPassword,

        @NotNull(message = "Invalid first name. First name shouldn't be null.")
        @Pattern(
                regexp = "^[a-zA-Z]{3,50}$", message = "First name must be between 3 "
                + "and 50 characters and contain only letters."
        )
        String firstName,

        @NotNull(message = "Invalid last name. Last name shouldn't be null.")
        @Pattern(regexp = "^[a-zA-Z]{3,50}$", message = "Last name must be between 3 "
                + "and 50 characters and contain only letters."
        )
        String lastName,

        @Size(max = 255, message = "Shipping address must not exceed 255 characters.")
        String shippingAddress
) {
}
