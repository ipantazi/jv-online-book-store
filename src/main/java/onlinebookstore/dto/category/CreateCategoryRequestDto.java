package onlinebookstore.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequestDto(
        @NotBlank(message = "Invalid name. Name shouldn't be blank.")
        @Size(min = 3, max = 100, message = "Invalid name. Size should be between 3 or 100.")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters.")
        String description
) {
}
