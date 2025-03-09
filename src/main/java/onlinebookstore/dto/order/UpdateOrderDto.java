package onlinebookstore.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateOrderDto(
        @NotBlank(message = "Invalid status. Status can't be blank.")
        @Size(max = 10, message = "Invalid status. Status cannot be more than 10 characters.")
        String status
) {
}
