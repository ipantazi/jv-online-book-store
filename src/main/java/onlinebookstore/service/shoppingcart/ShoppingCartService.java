package onlinebookstore.service.shoppingcart;

import onlinebookstore.dto.cartitem.CartItemDto;
import onlinebookstore.dto.cartitem.CartItemSimpleDto;
import onlinebookstore.dto.cartitem.CreateCartItemDto;
import onlinebookstore.dto.cartitem.UpdateCartItemDto;
import onlinebookstore.dto.shoppingcart.ShoppingCartDto;
import onlinebookstore.model.User;

public interface ShoppingCartService {
    ShoppingCartDto getShoppingCart(String email);

    CartItemDto addCartItem(String email, CreateCartItemDto createCartItemDto);

    CartItemSimpleDto updateCartItem(Long cartItemId, UpdateCartItemDto updateCartItem);

    void deleteById(Long id);

    void registerNewShoppingCart(User user);
}
