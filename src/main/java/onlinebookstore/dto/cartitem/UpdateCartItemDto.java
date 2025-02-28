package onlinebookstore.dto.cartitem;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateCartItemDto {
    private Long shoppingCartId;
    private Long bookId;

    @NotNull(message = "Invalid quantity. Value shouldn't be null")
    @Positive(message = "Invalid quantity. Value should be positive.")
    private int quantity;
}
