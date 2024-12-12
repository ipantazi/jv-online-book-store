package mate.academy.onlinebookstore.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class CreateBookRequestDto {
    @NotBlank(message = "Invalid title. Title should not be blank.")
    @Size(min = 3, max = 100, message = "Invalid title. Size should be between 3 to 100.")
    private String title;

    @NotBlank(message = "Invalid author. Author should not be blank.")
    @Size(min = 3, max = 50, message = "Invalid author. Size should be between 3 to 50.")
    private String author;

    @NotBlank(message = "Invalid isbn. Isbn should not be blank.")
    @Pattern(regexp = "^(?=(?:\\D*\\d){10}$|(?:\\D*\\d){13}$)[\\d-]+$",
            message = "Invalid ISBN format. Only digits and dashes. "
                    + "It must be between 10 and 13 digits.")
    private String isbn;

    @NotNull(message = "Invalid price. Please enter price.")
    @Positive(message = "Invalid price. Value should be positive.")
    @Digits(integer = 10, fraction = 2, message = "Invalid price. The maximum allowed number "
            + "for a price is 10 digits and 2 digits after the decimal point.")
    private BigDecimal price;

    @Size(max = 500, message = "Description must not exceed 500 characters.")
    private String description;

    @URL(message = "Invalid URL. Please provide a valid UPL of cover image.")
    private String coverImage;
}
