package mate.academy.onlinebookstore.dto.book;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.hibernate.validator.constraints.URL;

public record CreateBookRequestDto(
        @NotBlank(message = "Invalid title. Title should not be blank.")
        @Size(min = 3, max = 100, message = "Invalid title. Size should be between 3 to 100.")
        String title,

        @NotBlank(message = "Invalid author. Author should not be blank.")
        @Size(min = 3, max = 50, message = "Invalid author. Size should be between 3 to 50.")
        String author,

        @NotBlank(message = "Invalid ISBN. ISBN should not be blank.")
        @Size(max = 17, message = "ISBN must not exceed 17 characters (digits and dashes).")
        @Pattern(regexp = "^(?=(?:\\d){10}$|(?:\\D*\\d){13}$)[\\d-]+$",
                message = "Invalid ISBN format. "
                        + "ISBN must contain exactly 10 or 13 digits, with optional dashes.")
        String isbn,

        @NotNull(message = "Invalid price. Please enter price.")
        @Positive(message = "Invalid price. Value should be positive.")
        @Digits(integer = 10, fraction = 2, message = "Invalid price. The maximum allowed number "
                + "for a price is 10 digits and 2 digits after the decimal point.")
        BigDecimal price,

        @Size(max = 500, message = "Description must not exceed 500 characters.")
        String description,

        @URL(message = "Invalid URL. Please provide a valid UPL of cover image.")
        @Size(max = 1024, message = "URL must not exceed 1024 characters.")
        String coverImage
) {
}
