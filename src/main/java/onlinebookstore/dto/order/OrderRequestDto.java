package onlinebookstore.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrderRequestDto(
        @NotBlank(message = "Invalid shipping address. Address can't be blank.")
        @Size(min = 10, message = "Invalid shipping address. "
                + "Address cannot be less than 10 characters.")
        String shippingAddress) {
}
