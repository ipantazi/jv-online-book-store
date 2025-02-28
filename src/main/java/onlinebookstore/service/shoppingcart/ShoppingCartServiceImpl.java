package onlinebookstore.service.shoppingcart;

import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import onlinebookstore.dto.cartitem.CartItemDto;
import onlinebookstore.dto.cartitem.CartItemSimpleDto;
import onlinebookstore.dto.cartitem.CreateCartItemDto;
import onlinebookstore.dto.cartitem.UpdateCartItemDto;
import onlinebookstore.dto.shoppingcart.ShoppingCartDto;
import onlinebookstore.exception.EntityNotFoundException;
import onlinebookstore.mapper.CartItemMapper;
import onlinebookstore.mapper.ShoppingCartMapper;
import onlinebookstore.model.Book;
import onlinebookstore.model.CartItem;
import onlinebookstore.model.ShoppingCart;
import onlinebookstore.model.User;
import onlinebookstore.repository.book.BookRepository;
import onlinebookstore.repository.cartitem.CartItemRepository;
import onlinebookstore.repository.shoppingcart.ShoppingCartRepository;
import onlinebookstore.repository.user.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final UserRepository userRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final BookRepository bookRepository;
    private final CartItemMapper cartItemMapper;
    private final CartItemRepository cartItemRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeShoppingCart() {
        userRepository.findAllUsersWithoutShoppingCart().forEach(this::registerNewShoppingCart);
    }

    @Override
    public ShoppingCartDto getShoppingCart(String email) {
        return shoppingCartMapper.toShoppingCartDto(findShoppingCartWithoutUser(email));
    }

    @Override
    @Transactional
    public CartItemDto addCartItem(String email, CreateCartItemDto createCartItemDto) {
        Long bookId = createCartItemDto.getBookId();
        Book book = bookRepository.findById(bookId).orElseThrow(() ->
                        new EntityNotFoundException("Can't find book with id: " + bookId));
        ShoppingCart shoppingCart = findShoppingCartWithoutBook(email);

        Optional<CartItem> existingCartItem = shoppingCart.getCartItems().stream()
                .filter(cartItem -> cartItem.getBook().getId().equals(bookId))
                .findFirst();
        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + createCartItemDto.getQuantity());
            return cartItemMapper.toCartItemDto(cartItemRepository.save(cartItem));
        }

        CartItem cartItemEntity = cartItemMapper.toCartItemEntity(createCartItemDto);
        cartItemEntity.setBook(book);
        cartItemEntity.setShoppingCart(shoppingCart);
        return cartItemMapper.toCartItemDto(cartItemRepository.save(cartItemEntity));
    }

    @Override
    @Transactional
    public CartItemSimpleDto updateCartItem(Long cartItemId, UpdateCartItemDto updateCartItem) {
        int updatedRows = cartItemRepository.updateQuantity(cartItemId,
                updateCartItem.getQuantity());
        if (updatedRows == 0) {
            throw new EntityNotFoundException("Can't find cart item with id: " + cartItemId);
        }
        return new CartItemSimpleDto(cartItemId, updateCartItem.getQuantity());
    }

    @Override
    public void deleteById(Long id) {
        if (!cartItemRepository.existsById(id)) {
            throw new EntityNotFoundException("Can't find cart item with id: " + id);
        }
        cartItemRepository.deleteById(id);
    }

    @Override
    public void registerNewShoppingCart(User user) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        shoppingCartRepository.save(shoppingCart);
    }

    private ShoppingCart findShoppingCartWithoutBook(String email) {
        return shoppingCartRepository.findByUserEmail(email).orElseThrow(() ->
                new EntityNotFoundException("Can't find shopping cart by email: " + email));
    }

    private ShoppingCart findShoppingCartWithoutUser(String email) {
        return shoppingCartRepository.findByEmailAndWithoutUser(email).orElseThrow(() ->
                new EntityNotFoundException("Can't find shopping cart by email: " + email));
    }
}
