package onlinebookstore.dto.cartitem;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateCartItemDto {
    @NotNull(message = "Invalid book id. Value shouldn't be null")
    @Positive(message = "Invalid book id. Value should be positive.")
    private Long bookId;

    @NotNull(message = "Invalid quantity. Value shouldn't be null")
    @Positive(message = "Invalid quantity. Value should be positive.")
    private int quantity;
}
