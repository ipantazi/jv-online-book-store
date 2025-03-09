package onlinebookstore.service.shoppingcart;

import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import onlinebookstore.dto.cartitem.CartItemRequestDto;
import onlinebookstore.dto.cartitem.UpdateCartItemDto;
import onlinebookstore.dto.shoppingcart.ShoppingCartDto;
import onlinebookstore.exception.EntityNotFoundException;
import onlinebookstore.mapper.CartItemMapper;
import onlinebookstore.mapper.ShoppingCartMapper;
import onlinebookstore.model.CartItem;
import onlinebookstore.model.ShoppingCart;
import onlinebookstore.model.User;
import onlinebookstore.repository.cartitem.CartItemRepository;
import onlinebookstore.repository.shoppingcart.ShoppingCartRepository;
import onlinebookstore.repository.user.UserRepository;
import onlinebookstore.service.book.BookService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final UserRepository userRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;
    private final BookService bookService;

    @Override
    public ShoppingCartDto getShoppingCart(Long userId) {
        return shoppingCartMapper.toShoppingCartDto(findShoppingCartByUserId(userId));
    }

    @Override
    public ShoppingCartDto addCartItem(Long userId, CartItemRequestDto cartItemRequestDto) {
        Long bookId = cartItemRequestDto.getBookId();
        ShoppingCart shoppingCart = findShoppingCartByUserId(userId);
        Optional<CartItem> optionalCartItem = shoppingCart.getCartItems().stream()
                .filter(cartItem -> cartItem.getBook().getId().equals(bookId))
                .findFirst();

        if (optionalCartItem.isPresent()) {
            CartItem cartItem = optionalCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + cartItemRequestDto.getQuantity());
        } else {
            CartItem cartItem = cartItemMapper.toCartItemEntity(cartItemRequestDto);
            cartItem.setShoppingCart(shoppingCart);
            cartItem.setBook(bookService.findBookById(bookId));
            shoppingCart.getCartItems().add(cartItem);
        }
        return shoppingCartMapper.toShoppingCartDto(shoppingCartRepository.save(shoppingCart));
    }

    @Override
    public ShoppingCartDto updateCartItem(Long userId,
                                          Long cartItemId,
                                          UpdateCartItemDto updateCartItem) {
        ShoppingCart shoppingCart = findShoppingCartByUserId(userId);
        CartItem cartItem = findCartItemByItemIdAndCartId(cartItemId, shoppingCart.getId());
        cartItem.setQuantity(updateCartItem.getQuantity());
        shoppingCart.getCartItems().add(cartItem);
        return shoppingCartMapper.toShoppingCartDto(shoppingCartRepository.save(shoppingCart));
    }

    @Override
    public void deleteById(Long userId, Long cartItemId) {
        ShoppingCart shoppingCart = findShoppingCartByUserId(userId);
        CartItem cartItem = findCartItemByItemIdAndCartId(cartItemId, shoppingCart.getId());
        shoppingCart.getCartItems().remove(cartItem);
        shoppingCartRepository.save(shoppingCart);
    }

    @Override
    public ShoppingCart registerNewShoppingCart(User user) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        return shoppingCartRepository.save(shoppingCart);
    }

    @Override
    public ShoppingCart findShoppingCartByUserId(Long userId) {
        return shoppingCartRepository.findByUserId(userId).orElseGet(() ->
                registerNewShoppingCart(userRepository.findById(userId).orElseThrow(() ->
                        new EntityNotFoundException("User with id " + userId + " not found"))));
    }

    private CartItem findCartItemByItemIdAndCartId(Long cartItemId, Long shoppingCartId) {
        return cartItemRepository
                .findByIdAndShoppingCartId(cartItemId, shoppingCartId)
                .orElseThrow(() -> new EntityNotFoundException("Can't find cart with id: "
                        + cartItemId));
    }
}
