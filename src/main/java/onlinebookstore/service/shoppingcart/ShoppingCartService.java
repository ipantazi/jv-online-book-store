package onlinebookstore.service.shoppingcart;

import onlinebookstore.dto.cartitem.CartItemRequestDto;
import onlinebookstore.dto.cartitem.UpdateCartItemDto;
import onlinebookstore.dto.shoppingcart.ShoppingCartDto;
import onlinebookstore.model.ShoppingCart;
import onlinebookstore.model.User;

public interface ShoppingCartService {
    ShoppingCartDto getShoppingCart(Long userId);

    ShoppingCartDto addCartItem(Long userId, CartItemRequestDto cartItemRequestDto);

    ShoppingCartDto updateCartItem(
            Long userId,
            Long cartItemId,
            UpdateCartItemDto updateCartItem
    );

    void deleteById(Long userId, Long cartItemId);

    ShoppingCart registerNewShoppingCart(User user);

    ShoppingCart findShoppingCartByUserId(Long userId);
}
