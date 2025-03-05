package onlinebookstore.dto.shoppingcart;

import java.util.Set;
import lombok.Data;
import onlinebookstore.dto.cartitem.CartItemDto;

@Data
public class ShoppingCartDto {
    private Long id;
    private Long userId;
    private Set<CartItemDto> cartItems;
}
