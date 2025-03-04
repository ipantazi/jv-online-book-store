package onlinebookstore.dto.cartitem;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateCartItemDto {
    @Positive(message = "Invalid quantity. Value should be positive.")
    private int quantity;
}
