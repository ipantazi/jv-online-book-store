package onlinebookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import onlinebookstore.dto.cartitem.CartItemRequestDto;
import onlinebookstore.dto.cartitem.UpdateCartItemDto;
import onlinebookstore.dto.shoppingcart.ShoppingCartDto;
import onlinebookstore.model.User;
import onlinebookstore.service.shoppingcart.ShoppingCartService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Shopping cart management.", description = "Endpoints of management shopping cart.")
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get shopping cart.",
            description = "Retrieve user's shopping cart and review its contents.")
    public ShoppingCartDto getShoppingCart(Authentication authentication) {
        User user = (User)authentication.getPrincipal();
        return shoppingCartService.getShoppingCart(user.getId());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @ResponseStatus(value = HttpStatus.CREATED)
    @Operation(summary = "Add cart item to the shopping cart.",
            description = "Add a book to the shopping cart.")
    public ShoppingCartDto addCartItem(Authentication authentication,
                                       @RequestBody @Valid CartItemRequestDto cartItemRequestDto) {
        User user = (User)authentication.getPrincipal();
        return shoppingCartService.addCartItem(user.getId(), cartItemRequestDto);
    }

    @PutMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Update cart item of a book in the shopping cart.",
            description = "Update the books quantity in the shopping cart by cart item id.")
    public ShoppingCartDto updateCartItem(Authentication authentication,
                                          @PathVariable Long cartItemId,
                                          @RequestBody @Valid UpdateCartItemDto updateCartItem) {
        User user = (User)authentication.getPrincipal();
        return shoppingCartService.updateCartItem(user.getId(), cartItemId, updateCartItem);
    }

    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a book from the shopping cart.",
            description = "Remove an item from the user's shopping cart.")
    public void delete(Authentication authentication, @PathVariable Long cartItemId) {
        User user = (User)authentication.getPrincipal();
        shoppingCartService.deleteById(user.getId(), cartItemId);
    }
}
