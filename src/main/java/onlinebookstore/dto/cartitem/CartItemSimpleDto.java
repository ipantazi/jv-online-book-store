package onlinebookstore.dto.cartitem;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItemSimpleDto {
    private Long id;
    private int quantity;
}
