package onlinebookstore.dto.book;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record BookSearchParametersDto(
        @Size(max = 100, message = "Invalid title. Size should not exceed 100 characters.")
        String title,

        @Size(max = 50, message = "Invalid author. Size should not exceed 50 characters.")
        String author,

        @Pattern(regexp = "^[\\d-]{0,13}$", message = "Invalid ISBN format. "
                + "Only digits and dashes. Size should not exceed 13 characters.")
        String isbn,

        @Valid
        @Size(max = 2, message = "Price range should contain at most 2 values: from and to prices")
        List<
            @Positive(message = "Invalid price. Value should be positive.")
            @Digits(integer = 10, fraction = 2, message = "Invalid price. The maximum allowed "
                    + "number for a price is 10 digits and 2 digits after the decimal point.")
            BigDecimal
        > priceRange
) {
}
