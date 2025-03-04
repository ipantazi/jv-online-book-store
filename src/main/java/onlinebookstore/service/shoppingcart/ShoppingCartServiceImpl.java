package onlinebookstore.service.shoppingcart;

import jakarta.transaction.Transactional;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import onlinebookstore.dto.cartitem.CartItemRequestDto;
import onlinebookstore.dto.cartitem.UpdateCartItemDto;
import onlinebookstore.dto.shoppingcart.ShoppingCartDto;
import onlinebookstore.exception.EntityNotFoundException;
import onlinebookstore.mapper.CartItemMapper;
import onlinebookstore.mapper.ShoppingCartMapper;
import onlinebookstore.model.Book;
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
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final UserRepository userRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;
    private final BookService bookService;

    @Override
    @Transactional
    public ShoppingCartDto getShoppingCart(Long userId) {
        return shoppingCartMapper.toShoppingCartDto(findShoppingCartByUserId(userId));
    }

    @Override
    @Transactional
    public ShoppingCartDto addCartItem(Long userId, CartItemRequestDto cartItemRequestDto) {
        Long bookId = cartItemRequestDto.getBookId();
        ShoppingCart shoppingCart = findShoppingCartByUserId(userId);
        Map<Long, CartItem> cartItemMap = shoppingCart.getCartItems().stream()
                .collect(Collectors.toMap(
                        cartItem -> cartItem.getBook().getId(),
                        cartItem -> cartItem));
        CartItem cartItem = cartItemMap.get(bookId);

        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + cartItemRequestDto.getQuantity());
        } else {
            Book book = bookService.findBookById(bookId);
            cartItem = cartItemMapper.toCartItemEntity(cartItemRequestDto);
            cartItem.setBook(book);
            cartItem.setShoppingCart(shoppingCart);
            shoppingCart.getCartItems().add(cartItem);
        }

        return shoppingCartMapper.toShoppingCartDto(shoppingCartRepository.save(shoppingCart));
    }

    @Override
    @Transactional
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
    @Transactional
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

    private ShoppingCart findShoppingCartByUserId(Long userId) {
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
