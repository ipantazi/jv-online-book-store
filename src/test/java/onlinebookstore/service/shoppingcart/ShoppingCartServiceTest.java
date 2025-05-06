package onlinebookstore.service.shoppingcart;

import static onlinebookstore.util.TestDataUtil.ALTERNATIVE_BOOK_ID;
import static onlinebookstore.util.TestDataUtil.ALTERNATIVE_USER_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_CART_ITEM_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_SHOPPING_CART_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_USER_ID;
import static onlinebookstore.util.TestDataUtil.EXPECTED_CART_ITEMS_SIZE;
import static onlinebookstore.util.TestDataUtil.NEW_CART_ITEM_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_CART_ITEM_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_USER_ID;
import static onlinebookstore.util.TestDataUtil.SHOPPING_CART_DTO_IGNORING_FIELDS;
import static onlinebookstore.util.TestDataUtil.UPDATED_QUANTITY;
import static onlinebookstore.util.TestDataUtil.createTestCartItem;
import static onlinebookstore.util.TestDataUtil.createTestCartItemDto;
import static onlinebookstore.util.TestDataUtil.createTestCartItemRequestDto;
import static onlinebookstore.util.TestDataUtil.createTestShoppingCart;
import static onlinebookstore.util.TestDataUtil.createTestShoppingCartDto;
import static onlinebookstore.util.TestDataUtil.createTestUpdateCartItemDto;
import static onlinebookstore.util.TestDataUtil.getTestCartItem;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertObjectsAreEqualIgnoringFields;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import onlinebookstore.dto.cartitem.CartItemDto;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ShoppingCartServiceTest {
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private ShoppingCartMapper shoppingCartMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartItemMapper cartItemMapper;
    @Mock
    private BookService bookService;
    @Mock
    private CartItemRepository cartItemRepository;
    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    @Test
    @DisplayName("Verify getShoppingCart() method works.")
    public void getShoppingCart_ValidUserIdAndShoppingCartExists_ReturnsShoppingCart() {
        // Given
        ShoppingCartDto expectedShoppingCartDto = createTestShoppingCartDto(EXISTING_USER_ID);
        ShoppingCart shoppingCart = createTestShoppingCart(expectedShoppingCartDto);
        when(shoppingCartRepository.findByUserId(EXISTING_USER_ID))
                .thenReturn(Optional.of(shoppingCart));
        when(shoppingCartMapper.toShoppingCartDto(shoppingCart))
                .thenReturn(expectedShoppingCartDto);

        // When
        ShoppingCartDto actualShoppingCartDto = shoppingCartService
                .getShoppingCart(EXISTING_USER_ID);

        // Then
        assertThat(actualShoppingCartDto).isNotNull();
        assertObjectsAreEqualIgnoringFields(
                actualShoppingCartDto,
                expectedShoppingCartDto,
                SHOPPING_CART_DTO_IGNORING_FIELDS
        );
        verify(shoppingCartRepository, times(1)).findByUserId(EXISTING_USER_ID);
        verify(shoppingCartMapper, times(1)).toShoppingCartDto(shoppingCart);
        verifyNoMoreInteractions(shoppingCartRepository, shoppingCartMapper);
    }

    @Test
    @DisplayName("Method should register new shopping cart if none exists and return it as DTO.")
    public void getShoppingCart_ValidUserIdAndShoppingCartNotExists_ReturnsShoppingCart() {
        // Given
        ShoppingCartDto expectedShoppingCartDto = createTestShoppingCartDto(ALTERNATIVE_USER_ID);
        ShoppingCart shoppingCart = createTestShoppingCart(expectedShoppingCartDto);
        User user = shoppingCart.getUser();
        when(shoppingCartRepository.findByUserId(ALTERNATIVE_USER_ID))
                .thenReturn(Optional.empty());
        when(userRepository.findById(ALTERNATIVE_USER_ID)).thenReturn(Optional.of(user));
        when(shoppingCartRepository.save(any(ShoppingCart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(shoppingCartMapper.toShoppingCartDto(any(ShoppingCart.class)))
                .thenReturn(expectedShoppingCartDto);

        // When
        ShoppingCartDto actualShoppingCartDto = shoppingCartService
                .getShoppingCart(ALTERNATIVE_USER_ID);

        // Then
        assertThat(actualShoppingCartDto).isNotNull();
        assertObjectsAreEqualIgnoringFields(
                actualShoppingCartDto,
                expectedShoppingCartDto,
                SHOPPING_CART_DTO_IGNORING_FIELDS
        );
        verify(shoppingCartRepository, times(1)).findByUserId(ALTERNATIVE_USER_ID);
        verify(userRepository, times(1)).findById(ALTERNATIVE_USER_ID);
        verify(shoppingCartRepository, times(1)).save(any(ShoppingCart.class));
        verify(shoppingCartMapper, times(1)).toShoppingCartDto(any(ShoppingCart.class));
        verifyNoMoreInteractions(shoppingCartRepository, userRepository, shoppingCartMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when the user id is doesn't exists.")
    public void getShoppingCart_InvalidUserId_ThrowsException() {
        // Given
        when(shoppingCartRepository.findByUserId(NOT_EXISTING_USER_ID))
                .thenReturn(Optional.empty());
        when(userRepository.findById(NOT_EXISTING_USER_ID)).thenReturn(Optional.empty());

        //When
        assertThatThrownBy(() -> shoppingCartService.getShoppingCart(NOT_EXISTING_USER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User with id " + NOT_EXISTING_USER_ID + " not found");

        //Then
        verify(shoppingCartRepository, times(1)).findByUserId(NOT_EXISTING_USER_ID);
        verify(userRepository, times(1)).findById(NOT_EXISTING_USER_ID);
        verify(shoppingCartRepository, never()).save(any(ShoppingCart.class));
        verifyNoMoreInteractions(shoppingCartRepository, userRepository);
        verifyNoInteractions(shoppingCartMapper);
    }

    @Test
    @DisplayName("Verify addCartItem() method works when the cartItem already exists in the cart.")
    public void addCartItem_ValidUserIdAndCartItemRequestDto_ReturnsShoppingCart() {
        //Given
        ShoppingCartDto expectedShoppingCartDto = createTestShoppingCartDto(EXISTING_USER_ID);
        ShoppingCart shoppingCart = createTestShoppingCart(expectedShoppingCartDto);
        CartItem existingCartItem = getTestCartItem(EXISTING_CART_ITEM_ID, shoppingCart);
        int quantityBeforeUpdate = existingCartItem.getQuantity();
        CartItemRequestDto cartItemRequestDto = createTestCartItemRequestDto(existingCartItem);
        int expectedQuantity = quantityBeforeUpdate + cartItemRequestDto.getQuantity();
        expectedShoppingCartDto.getCartItems().stream()
                .filter(cartItemDto -> cartItemDto.getId().equals(EXISTING_CART_ITEM_ID))
                .forEach(cartItemDto -> cartItemDto.setQuantity(expectedQuantity));

        when(shoppingCartRepository.findByUserId(EXISTING_USER_ID))
                .thenReturn(Optional.of(shoppingCart));
        when(shoppingCartRepository.save(any(ShoppingCart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(shoppingCartMapper.toShoppingCartDto(any(ShoppingCart.class)))
                .thenReturn(expectedShoppingCartDto);

        //When
        ShoppingCartDto actualShoppingCartDto = shoppingCartService
                .addCartItem(EXISTING_USER_ID, cartItemRequestDto);

        //Then
        Optional<CartItemDto> actualCartItemDtoOpt = actualShoppingCartDto.getCartItems()
                .stream()
                .filter(cartItemDto -> cartItemDto.getId().equals(EXISTING_CART_ITEM_ID))
                .findFirst();
        assertThat(actualCartItemDtoOpt).isPresent();
        assertThat(actualCartItemDtoOpt.get().getQuantity()).isEqualTo(expectedQuantity);
        assertObjectsAreEqualIgnoringFields(
                actualShoppingCartDto,
                expectedShoppingCartDto,
                SHOPPING_CART_DTO_IGNORING_FIELDS
        );
        verify(shoppingCartRepository, times(1)).findByUserId(EXISTING_USER_ID);
        verify(shoppingCartRepository, times(1)).save(any(ShoppingCart.class));
        verify(shoppingCartMapper, times(1)).toShoppingCartDto(any(ShoppingCart.class));
        verifyNoMoreInteractions(shoppingCartRepository, shoppingCartMapper);
        verifyNoInteractions(cartItemMapper, bookService);
    }

    @Test
    @DisplayName("Verify addCartItem() method works when the cartItem doesn't exists.")
    public void addCartItem_ValidUserIdAndCartItemNotExists_ReturnsShoppingCart() {
        //Given
        ShoppingCartDto expectedShoppingCartDto = createTestShoppingCartDto(EXISTING_USER_ID);
        int expectedSize = expectedShoppingCartDto.getCartItems().size() + 1;
        ShoppingCart shoppingCart = createTestShoppingCart(expectedShoppingCartDto);
        CartItemDto expectedCartItemDto = createTestCartItemDto(
                NEW_CART_ITEM_ID,
                ALTERNATIVE_BOOK_ID
        );
        CartItem newCartItem = createTestCartItem(expectedCartItemDto, shoppingCart);
        CartItemRequestDto cartItemRequestDto = createTestCartItemRequestDto(newCartItem);
        Book book = newCartItem.getBook();

        when(shoppingCartRepository.findByUserId(EXISTING_USER_ID))
                .thenReturn(Optional.of(shoppingCart));
        when(cartItemMapper.toCartItemEntity(cartItemRequestDto)).thenReturn(newCartItem);
        when(bookService.findBookById(ALTERNATIVE_BOOK_ID)).thenReturn(book);
        when(shoppingCartRepository.save(any(ShoppingCart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        expectedShoppingCartDto.getCartItems().add(expectedCartItemDto);
        when(shoppingCartMapper.toShoppingCartDto(any(ShoppingCart.class)))
                .thenReturn(expectedShoppingCartDto);

        //When
        ShoppingCartDto actualShoppingCartDto = shoppingCartService
                .addCartItem(EXISTING_USER_ID, cartItemRequestDto);

        //Then
        assertThat(actualShoppingCartDto.getCartItems()).hasSize(expectedSize);
        assertThat(actualShoppingCartDto.getCartItems())
                .anyMatch(item -> item.getBookId().equals(ALTERNATIVE_BOOK_ID));
        assertObjectsAreEqualIgnoringFields(
                actualShoppingCartDto,
                expectedShoppingCartDto,
                SHOPPING_CART_DTO_IGNORING_FIELDS
        );
        verify(shoppingCartRepository, times(1)).findByUserId(EXISTING_USER_ID);
        verify(cartItemMapper, times(1)).toCartItemEntity(cartItemRequestDto);
        verify(bookService, times(1)).findBookById(ALTERNATIVE_BOOK_ID);
        verify(shoppingCartRepository, times(1)).save(any(ShoppingCart.class));
        verify(shoppingCartMapper, times(1)).toShoppingCartDto(any(ShoppingCart.class));
        verifyNoMoreInteractions(shoppingCartRepository, shoppingCartMapper);
        verifyNoMoreInteractions(cartItemMapper, bookService);
    }

    @Test
    @DisplayName("Verify an exception is throw when the cart item and the book id doesn't exists.")
    public void addCartItem_CartItemAndBookIdNotExists_ThrowsException() {
        //Given
        ShoppingCartDto expectedShoppingCartDto = createTestShoppingCartDto(EXISTING_USER_ID);
        ShoppingCart shoppingCart = createTestShoppingCart(expectedShoppingCartDto);
        CartItem newCartItem = getTestCartItem(NEW_CART_ITEM_ID, shoppingCart);
        CartItemRequestDto cartItemRequestDto = createTestCartItemRequestDto(newCartItem);
        Long notExistingBookId = cartItemRequestDto.getBookId();

        when(shoppingCartRepository.findByUserId(EXISTING_USER_ID))
                .thenReturn(Optional.of(shoppingCart));
        when(cartItemMapper.toCartItemEntity(cartItemRequestDto)).thenReturn(newCartItem);
        when(bookService.findBookById(notExistingBookId)).thenThrow(new EntityNotFoundException(
                "Can`t find the book by id: " + notExistingBookId
        ));

        //When
        assertThatThrownBy(() ->
                shoppingCartService.addCartItem(EXISTING_USER_ID, cartItemRequestDto)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can`t find the book by id: " + notExistingBookId);

        //Then
        assertThat(shoppingCart.getCartItems()).hasSize(EXPECTED_CART_ITEMS_SIZE);
        verify(shoppingCartRepository, times(1)).findByUserId(EXISTING_USER_ID);
        verify(cartItemMapper, times(1)).toCartItemEntity(cartItemRequestDto);
        verify(bookService, times(1)).findBookById(ALTERNATIVE_BOOK_ID);
        verify(shoppingCartRepository, never()).save(any(ShoppingCart.class));
        verifyNoMoreInteractions(shoppingCartRepository, cartItemMapper, bookService);
        verifyNoInteractions(shoppingCartMapper);
    }

    @Test
    @DisplayName("Verify an exception is throw when the user id doesn't exists.")
    public void addCartItem_UserIdNotExists_ThrowsException() {
        //Given
        CartItemRequestDto cartItemRequestDto = new CartItemRequestDto();
        when(shoppingCartRepository.findByUserId(NOT_EXISTING_USER_ID))
                .thenReturn(Optional.empty());
        when(userRepository.findById(NOT_EXISTING_USER_ID)).thenThrow(new EntityNotFoundException(
                "User with id " + NOT_EXISTING_USER_ID + " not found"
        ));

        //When
        assertThatThrownBy(() ->
                shoppingCartService.addCartItem(NOT_EXISTING_USER_ID, cartItemRequestDto)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User with id " + NOT_EXISTING_USER_ID + " not found");

        //Then
        verify(shoppingCartRepository, times(1)).findByUserId(NOT_EXISTING_USER_ID);
        verify(userRepository, times(1)).findById(NOT_EXISTING_USER_ID);
        verify(shoppingCartRepository, never()).save(any(ShoppingCart.class));
        verifyNoMoreInteractions(shoppingCartRepository, userRepository);
        verifyNoInteractions(cartItemMapper, bookService);
    }

    @Test
    @DisplayName("Verify updateCartItem() method works.")
    public void updateCartItem_ValidUserIdAndCartItemId_ReturnsShoppingCart() {
        //Given
        ShoppingCartDto expectedShoppingCartDto = createTestShoppingCartDto(EXISTING_USER_ID);
        ShoppingCart shoppingCart = createTestShoppingCart(expectedShoppingCartDto);
        CartItem cartItem = getTestCartItem(EXISTING_CART_ITEM_ID, shoppingCart);

        UpdateCartItemDto updateCartItemDto = createTestUpdateCartItemDto(UPDATED_QUANTITY);
        expectedShoppingCartDto.getCartItems().stream()
                .filter(item -> item.getId().equals(EXISTING_CART_ITEM_ID))
                .forEach(item -> item.setQuantity(UPDATED_QUANTITY));

        when(shoppingCartRepository.findByUserId(EXISTING_USER_ID))
                .thenReturn(Optional.of(shoppingCart));
        when(cartItemRepository.findByIdAndShoppingCartId(
                EXISTING_CART_ITEM_ID, shoppingCart.getId()))
                .thenReturn(Optional.of(cartItem));
        when(shoppingCartRepository.save(any(ShoppingCart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(shoppingCartMapper.toShoppingCartDto(any(ShoppingCart.class)))
                .thenReturn(expectedShoppingCartDto);

        //When
        ShoppingCartDto actualShoppingCartDto = shoppingCartService
                .updateCartItem(EXISTING_USER_ID, EXISTING_CART_ITEM_ID, updateCartItemDto);

        //Then
        assertThat(actualShoppingCartDto.getCartItems().stream()
                .filter(item -> item.getId().equals(EXISTING_CART_ITEM_ID))
                .allMatch(item -> item.getQuantity() == updateCartItemDto.getQuantity()))
                .isTrue();
        assertObjectsAreEqualIgnoringFields(
                expectedShoppingCartDto,
                actualShoppingCartDto,
                SHOPPING_CART_DTO_IGNORING_FIELDS);
        verify(shoppingCartRepository, times(1)).findByUserId(EXISTING_USER_ID);
        verify(cartItemRepository, times(1))
                .findByIdAndShoppingCartId(EXISTING_CART_ITEM_ID, shoppingCart.getId());
        verify(shoppingCartRepository, times(1)).save(any(ShoppingCart.class));
        verify(shoppingCartMapper, times(1)).toShoppingCartDto(any(ShoppingCart.class));
        verifyNoMoreInteractions(shoppingCartRepository, cartItemRepository, shoppingCartMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a cart item id doesn't exists.")
    public void updateCartItem_ValidUserIdAndCartItemIdNoExists_ThrowsException() {
        //Given
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(EXISTING_SHOPPING_CART_ID);
        UpdateCartItemDto updateCartItemDto = new UpdateCartItemDto();

        when(shoppingCartRepository.findByUserId(EXISTING_USER_ID))
                .thenReturn(Optional.of(shoppingCart));
        when(cartItemRepository
                .findByIdAndShoppingCartId(NOT_EXISTING_CART_ITEM_ID, shoppingCart.getId()))
                .thenThrow(new EntityNotFoundException("Can't find cart with id: "
                        + NOT_EXISTING_CART_ITEM_ID));

        //When
        assertThatThrownBy(() -> shoppingCartService
                .updateCartItem(EXISTING_USER_ID, NOT_EXISTING_CART_ITEM_ID, updateCartItemDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find cart with id: " + NOT_EXISTING_CART_ITEM_ID);

        //Then
        verify(shoppingCartRepository, times(1)).findByUserId(EXISTING_USER_ID);
        verify(cartItemRepository, times(1))
                .findByIdAndShoppingCartId(NOT_EXISTING_CART_ITEM_ID, shoppingCart.getId());
        verify(shoppingCartRepository, never()).save(any(ShoppingCart.class));
        verifyNoMoreInteractions(shoppingCartRepository, cartItemRepository);
        verifyNoInteractions(shoppingCartMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a user id doesn't exists.")
    public void updateCartItem_InvalidUserId_ThrowsException() {
        //Given
        UpdateCartItemDto updateCartItemDto = new UpdateCartItemDto();
        when(shoppingCartRepository.findByUserId(NOT_EXISTING_USER_ID))
                .thenReturn(Optional.empty());
        when(userRepository.findById(NOT_EXISTING_USER_ID)).thenThrow(new EntityNotFoundException(
                "User with id " + NOT_EXISTING_USER_ID + " not found"));

        //When
        assertThatThrownBy(() -> shoppingCartService.updateCartItem(
                NOT_EXISTING_USER_ID, EXISTING_CART_ITEM_ID, updateCartItemDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User with id " + NOT_EXISTING_USER_ID + " not found");

        //Then
        verify(shoppingCartRepository, times(1)).findByUserId(NOT_EXISTING_USER_ID);
        verify(userRepository, times(1)).findById(NOT_EXISTING_USER_ID);
        verify(shoppingCartRepository, never()).save(any(ShoppingCart.class));
        verifyNoMoreInteractions(shoppingCartRepository, userRepository);
        verifyNoInteractions(shoppingCartMapper, cartItemRepository);
    }

    @Test
    @DisplayName("Verify deleteById() method works.")
    public void deleteById_ValidUserIdAndCartItemId_Success() {
        //Given
        ShoppingCart shoppingCart = createTestShoppingCart(EXISTING_USER_ID);
        final int expectedSize = shoppingCart.getCartItems().size() - 1;
        final CartItem cartItemForDelete = getTestCartItem(EXISTING_CART_ITEM_ID, shoppingCart);
        when(shoppingCartRepository.findByUserId(EXISTING_USER_ID))
                .thenReturn(Optional.of(shoppingCart));
        when(cartItemRepository
                .findByIdAndShoppingCartId(EXISTING_CART_ITEM_ID, shoppingCart.getId())
        )
                .thenReturn(Optional.of(cartItemForDelete));

        //When
        shoppingCartService.deleteById(EXISTING_USER_ID, EXISTING_CART_ITEM_ID);

        //Then
        assertThat(shoppingCart.getCartItems()).hasSize(expectedSize);
        assertThat(shoppingCart.getCartItems()).doesNotContain(cartItemForDelete);
        verify(shoppingCartRepository, times(1)).findByUserId(EXISTING_USER_ID);
        verify(cartItemRepository, times(1))
                .findByIdAndShoppingCartId(EXISTING_CART_ITEM_ID, shoppingCart.getId());
        verify(shoppingCartRepository, times(1)).save(any(ShoppingCart.class));
        verifyNoMoreInteractions(shoppingCartRepository, cartItemRepository);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a cart item id doesn't exists.")
    public void deleteById_ValidUserIdAndCartItemIdNoExists_ThrowsException() {
        //Given
        ShoppingCart shoppingCart = createTestShoppingCart(EXISTING_USER_ID);

        when(shoppingCartRepository.findByUserId(EXISTING_USER_ID))
                .thenReturn(Optional.of(shoppingCart));
        when(cartItemRepository
                .findByIdAndShoppingCartId(NOT_EXISTING_CART_ITEM_ID, shoppingCart.getId()))
                .thenThrow(new EntityNotFoundException("Can't find cart with id: "
                        + NOT_EXISTING_CART_ITEM_ID));

        //When
        assertThatThrownBy(() -> shoppingCartService
                .deleteById(EXISTING_USER_ID, NOT_EXISTING_CART_ITEM_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find cart with id: " + NOT_EXISTING_CART_ITEM_ID);

        //Then
        assertThat(shoppingCart.getCartItems()).hasSize(EXPECTED_CART_ITEMS_SIZE);
        verify(shoppingCartRepository, times(1)).findByUserId(EXISTING_USER_ID);
        verify(cartItemRepository, times(1))
                .findByIdAndShoppingCartId(NOT_EXISTING_CART_ITEM_ID, shoppingCart.getId());
        verify(shoppingCartRepository, never()).save(any(ShoppingCart.class));
        verifyNoMoreInteractions(shoppingCartRepository, cartItemRepository);
        verifyNoInteractions(shoppingCartMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a user id doesn't exists.")
    public void deleteById_InvalidUserId_ThrowsException() {
        //Given
        when(shoppingCartRepository.findByUserId(NOT_EXISTING_USER_ID))
                .thenReturn(Optional.empty());
        when(userRepository.findById(NOT_EXISTING_USER_ID)).thenThrow(new EntityNotFoundException(
                "User with id " + NOT_EXISTING_USER_ID + " not found"));

        //When
        assertThatThrownBy(() -> shoppingCartService.deleteById(
                NOT_EXISTING_USER_ID, EXISTING_CART_ITEM_ID
        ))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User with id " + NOT_EXISTING_USER_ID + " not found");

        //Then
        verify(shoppingCartRepository, times(1)).findByUserId(NOT_EXISTING_USER_ID);
        verify(userRepository, times(1)).findById(NOT_EXISTING_USER_ID);
        verify(shoppingCartRepository, never()).save(any(ShoppingCart.class));
        verifyNoMoreInteractions(shoppingCartRepository, userRepository);
        verifyNoInteractions(shoppingCartMapper, cartItemRepository);
    }
}
