package onlinebookstore.util;

import static onlinebookstore.model.Role.RoleName.ADMIN;
import static onlinebookstore.model.Role.RoleName.USER;
import static onlinebookstore.service.category.CategoryServiceImpl.categoriesCache;
import static onlinebookstore.service.user.UserServiceImpl.rolesCache;
import static onlinebookstore.util.repository.RepositoryTestDataUtil.DEFAULT_ORDER_STATUS;
import static onlinebookstore.util.repository.RepositoryTestDataUtil.ORDER_DATE;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import onlinebookstore.dto.book.BookDto;
import onlinebookstore.dto.book.BookDtoWithoutCategoryIds;
import onlinebookstore.dto.book.CreateBookRequestDto;
import onlinebookstore.dto.cartitem.CartItemDto;
import onlinebookstore.dto.cartitem.CartItemRequestDto;
import onlinebookstore.dto.cartitem.UpdateCartItemDto;
import onlinebookstore.dto.category.CategoryDto;
import onlinebookstore.dto.category.CreateCategoryRequestDto;
import onlinebookstore.dto.order.OrderDto;
import onlinebookstore.dto.order.OrderRequestDto;
import onlinebookstore.dto.order.UpdateOrderDto;
import onlinebookstore.dto.orderitem.OrderItemDto;
import onlinebookstore.dto.shoppingcart.ShoppingCartDto;
import onlinebookstore.dto.user.UserLoginRequestDto;
import onlinebookstore.dto.user.UserRegistrationRequestDto;
import onlinebookstore.dto.user.UserResponseDto;
import onlinebookstore.model.Book;
import onlinebookstore.model.CartItem;
import onlinebookstore.model.Category;
import onlinebookstore.model.Order;
import onlinebookstore.model.OrderItem;
import onlinebookstore.model.Role;
import onlinebookstore.model.ShoppingCart;
import onlinebookstore.model.User;
import onlinebookstore.repository.category.CategoryRepository;
import onlinebookstore.repository.role.RoleRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class TestDataUtil {

    public static final int EXPECTED_BOOKS_SIZE = 3;
    public static final int EXPECTED_CATEGORIES_SIZE = 2;
    public static final int EXPECTED_CART_ITEMS_SIZE = 2;
    public static final int EXPECTED_ORDER_ITEMS_SIZE = 2;
    public static final int UPDATED_QUANTITY = 100;
    public static final int NEGATIVE_VALUE = -1;
    public static final Long NEGATIVE_ID = -1L;
    public static final Long EXISTING_BOOK_ID = 101L;
    public static final Long ALTERNATIVE_BOOK_ID = 103L;
    public static final Long SAFE_DELETED_BOOK_ID = 104L;
    public static final Long NEW_BOOK_ID = 105L;
    public static final Long NOT_EXISTING_BOOK_ID = 999L;
    public static final Long EXISTING_CATEGORY_ID = 101L;
    public static final Long ALTERNATIVE_CATEGORY_ID = 102L;
    public static final Long SAFE_DELETED_CATEGORY_ID = 103L;
    public static final Long NEW_CATEGORY_ID = 104L;
    public static final Long NOT_EXISTING_CATEGORY_ID = 999L;
    public static final Long EXISTING_USER_ID = 101L;
    public static final Long ALTERNATIVE_USER_ID = 102L;
    public static final Long NEW_USER_ID = 103L;
    public static final Long NOT_EXISTING_USER_ID = 999L;
    public static final Long ROLE_USER_ID = 101L;
    public static final Long ROLE_ADMIN_ID = 102L;
    public static final Long EXISTING_SHOPPING_CART_ID = 101L;
    public static final Long EXISTING_CART_ITEM_ID = 101L;
    public static final Long NEW_CART_ITEM_ID = 103L;
    public static final Long NOT_EXISTING_CART_ITEM_ID = 999L;
    public static final Long EXISTING_ORDER_ID = 101L;
    public static final Long NOT_EXISTING_ORDER_ID = 999L;
    public static final Long EXISTING_ORDER_ITEM_ID = 101L;
    public static final Long NOT_EXISTING_ORDER_ITEM_ID = 999L;
    public static final String EXISTING_ISBN = "1000000000101";
    public static final String SOFT_DELETED_BOOK_ISBN = "1000000000104";
    public static final String NOT_EXISTING_ISBN = "9999999999999";
    public static final String INVALID_FORMAT_ISBN = "INVALID ISBN";
    public static final String TEST_LONG_DATA = """
            TESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTEST
            TESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTEST
            TESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTEST
            TESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTEST
            TESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTEST
            TESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTEST
            """;
    public static final String CATEGORY_IGNORING_FIELD = "id";
    public static final String USER_DTO_IGNORING_FIELD = "id";
    public static final String[] BOOK_DTO_IGNORING_FIELDS = new String[] {"id", "categoryIds"};
    public static final String[] BOOK_IGNORING_FIELDS = new String[] {"id", "categories"};
    public static final String[] SHOPPING_CART_DTO_IGNORING_FIELDS = new String[] {
            "id",
            "userId",
            "cartItems.id",
            "cartItems.bookId"
    };
    public static final String[] SHOPPING_CART_DTO_IGNORING_CART_ITEMS_FIELD = new String[] {
            "id",
            "userId",
            "cartItems"
    };
    public static final String[] SHOPPING_CART_IGNORING_FIELDS = new String[] {
            "id",
            "user",
            "isDeleted",
            "cartItems.id",
            "cartItems.shoppingCart",
            "cartItems.book.categories",
            "cartItems.book.id",
    };
    public static final String[] USER_IGNORING_FIELDS = new String[] {"id", "roles.id"};
    public static final String[] ORDER_IGNORING_FIELDS = new String[] {
            "id",
            "user",
            "orderItems.order",
            "orderItems.book.categories",
            "orderItems.book.id"
    };
    public static final String[] ORDER_DTO_IGNORING_FIELDS = new String[] {
            "id",
            "userId",
            "orderDate",
            "orderItems.id",
            "orderItems.bookId"
    };
    public static final String[] ORDER_ITEM_IGNORING_FIELDS = new String[] {
            "id",
            "book",
            "order.user",
            "order.orderItems"
    };
    public static final String[] ORDER_ITEM_DTO_IGNORING_FIELDS = new String[] {
            "id",
            "bookId"
    };
    public static final Pageable BOOK_PAGEABLE = PageRequest.of(
            0,
            10,
            Sort.by("title").ascending()
    );
    public static final Pageable CATEGORY_PAGEABLE = PageRequest.of(
            0,
            10,
            Sort.by("name").ascending()
    );
    public static final Pageable ORDER_PAGEABLE = PageRequest.of(
            0,
            10,
            Sort.by("orderDate").ascending()
    );
    public static final Pageable ORDER_ITEM_PAGEABLE = PageRequest.of(
            0,
            10,
            Sort.by("quantity").ascending()
    );
    public static final Map<String, String> BOOK_TEST_DATA_MAP = Map.of(
            "title", "Test Book ",
            "author","Test Author ",
            "isbn", "1000000000",
            "description", "Test Description",
            "coverImage", "http://example.com/test-cover.jpg"
    );
    public static final Map<String, String> CATEGORY_TEST_DATA_MAP = Map.of(
            "name", "Test category ",
            "description", "Description test"
    );
    public static final Map<String, String> USER_TEST_DATA_MAP = Map.of(
            "email", "@example.com",
            "password", "Test&password1",
            "BCryptPassword", "$2a$10$HmJz/wZv5WFjJArzq90dTOcGmYMsYtd.x61Z6qsgNoXTgtSJceOqe",
            "firstName", "FirstName",
            "lastName", "LastName",
            "shippingAddress", "Shipping address "
    );
    public static final Map<String, String> ORDER_TEST_DATA_MAP = Map.of(
            "shippingAddress", "Shipping address "
    );

    protected TestDataUtil() {
    }

    public static void fillCategoryCache(CategoryRepository categoryRepository) {
        categoriesCache.clear();
        categoryRepository.findAll().forEach(category ->
                categoriesCache.put(category.getId(), category));
    }

    public static void fillRoleCache(RoleRepository roleRepository) {
        rolesCache.clear();
        roleRepository.findAll().forEach(role ->
                rolesCache.put(role.getAuthority(), role));
    }

    public static BookDto createTestBookDto(Long id) {
        BookDto bookDto = new BookDto();
        bookDto.setId(id);
        bookDto.setTitle(BOOK_TEST_DATA_MAP.get("title") + id);
        bookDto.setAuthor(BOOK_TEST_DATA_MAP.get("author") + id);
        bookDto.setIsbn(BOOK_TEST_DATA_MAP.get("isbn") + id);
        bookDto.setPrice(new BigDecimal(id));
        bookDto.setDescription(BOOK_TEST_DATA_MAP.get("description"));
        bookDto.setCoverImage(BOOK_TEST_DATA_MAP.get("coverImage"));
        bookDto.setCategoryIds(Set.of(EXISTING_CATEGORY_ID));
        return bookDto;
    }

    public static List<BookDto> createTestBookDtoList(Long startId, int size) {
        return LongStream.range(startId, startId + size)
                .mapToObj(TestDataUtil::createTestBookDto)
                .toList();
    }

    public static CreateBookRequestDto createTestBookRequestDto(BookDto bookDto) {
        return new CreateBookRequestDto(
                bookDto.getTitle(),
                bookDto.getAuthor(),
                bookDto.getIsbn(),
                bookDto.getPrice(),
                bookDto.getDescription(),
                bookDto.getCoverImage(),
                bookDto.getCategoryIds()
        );
    }

    public static BookDto createTestInvalidBookDto() {
        Set<Long> categoryIds = new HashSet<>();

        categoryIds.add(null);
        BookDto bookDto = new BookDto();
        bookDto.setId(EXISTING_BOOK_ID);
        bookDto.setTitle("IT");
        bookDto.setAuthor("IT");
        bookDto.setIsbn("INVALID ISBN");
        bookDto.setPrice(BigDecimal.ZERO);
        bookDto.setCoverImage("Invalid cover image url");
        bookDto.setCategoryIds(categoryIds);
        return bookDto;
    }

    public static BookDto createTestUpdatedBookDto(Long id) {
        BookDto bookDto = createTestBookDto(id);
        bookDto.setTitle("Updated Title");
        bookDto.setAuthor("Updated Author");
        bookDto.setPrice(BigDecimal.TEN);
        bookDto.setDescription("Updated Description");
        bookDto.setCoverImage("http://update_example.com/test-cover.jpg");
        bookDto.setCategoryIds(Set.of(ALTERNATIVE_CATEGORY_ID));
        return bookDto;
    }

    public static Book createTestBook(BookDto bookDto) {
        Book book = new Book();
        book.setId(bookDto.getId());
        book.setTitle(bookDto.getTitle());
        book.setAuthor(bookDto.getAuthor());
        book.setIsbn(bookDto.getIsbn());
        book.setPrice(bookDto.getPrice());
        book.setDescription(bookDto.getDescription());
        book.setCoverImage(bookDto.getCoverImage());
        book.setCategories(createTestCategorySet(book.getId(), 1));
        return book;
    }

    public static Book createTestBook(Long id) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(BOOK_TEST_DATA_MAP.get("title") + id);
        book.setAuthor(BOOK_TEST_DATA_MAP.get("author") + id);
        book.setIsbn(BOOK_TEST_DATA_MAP.get("isbn") + id);
        book.setPrice(new BigDecimal(id));
        book.setDescription(BOOK_TEST_DATA_MAP.get("description"));
        book.setCoverImage(BOOK_TEST_DATA_MAP.get("coverImage"));
        book.setCategories(createTestCategorySet(id, 1));
        return book;
    }

    public static List<Book> createTestBookList(Long startId, int size) {
        return LongStream.range(startId, startId + size)
                .mapToObj(TestDataUtil::createTestBook)
                .toList();
    }

    public static BookDtoWithoutCategoryIds createTestBookDtoWithoutCategoryId(BookDto bookDto) {
        return new BookDtoWithoutCategoryIds(
                bookDto.getId(),
                bookDto.getTitle(),
                bookDto.getAuthor(),
                bookDto.getIsbn(),
                bookDto.getPrice(),
                bookDto.getDescription(),
                bookDto.getCoverImage()
        );
    }

    public static CategoryDto createTestCategoryDto(Long id) {
        return new CategoryDto(
                id,
                CATEGORY_TEST_DATA_MAP.get("name") + id,
                CATEGORY_TEST_DATA_MAP.get("description")
        );
    }

    public static Category createTestCategory(CategoryDto categoryDto) {
        Category category = new Category();
        category.setId(categoryDto.id());
        category.setName(categoryDto.name());
        return category;
    }

    public static Category createTestCategory(Long id) {
        Category category = new Category();
        category.setId(id);
        category.setName(CATEGORY_TEST_DATA_MAP.get("name") + id);
        category.setDescription(CATEGORY_TEST_DATA_MAP.get("description"));
        return category;
    }

    public static Set<Category> createTestCategorySet(Long startId, int size) {
        return LongStream.range(startId, startId + size)
                .mapToObj(TestDataUtil::createTestCategory)
                .collect(Collectors.toSet());
    }

    public static Set<Category> convertToCategorySet(Set<Long> categoryIds) {
        return categoryIds.stream()
                .map(TestDataUtil::createTestCategory)
                .collect(Collectors.toSet());
    }

    public static CreateCategoryRequestDto createTestCategoryRequestDto(CategoryDto categoryDto) {
        return new CreateCategoryRequestDto(categoryDto.name(), categoryDto.description());
    }

    public static CategoryDto createTestInvalidCategoryDto(Long id) {
        return new CategoryDto(
                id,
                "IT",
                TEST_LONG_DATA
        );
    }

    public static CategoryDto createTestUpdatedCategoryDto(Long id) {
        return new CategoryDto(id, "Updated name", "Updated description");
    }

    public static ShoppingCartDto createTestShoppingCartDto(Long userId) {
        Set<CartItemDto> cartItemDtos = createTestCartItemDtoSet(EXISTING_CART_ITEM_ID,
                EXPECTED_CART_ITEMS_SIZE);

        ShoppingCartDto shoppingCartDto = new ShoppingCartDto();
        shoppingCartDto.setId(userId);
        shoppingCartDto.setUserId(userId);
        shoppingCartDto.setCartItems(cartItemDtos);
        return shoppingCartDto;
    }

    public static ShoppingCart createTestShoppingCart(ShoppingCartDto shoppingCartDto) {
        Set<CartItemDto> cartItemDtos = shoppingCartDto.getCartItems();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(shoppingCartDto.getId());
        shoppingCart.setUser(createTestUser(shoppingCartDto.getUserId()));
        shoppingCart.setCartItems(convertToCartItemSet(cartItemDtos, shoppingCart));
        return shoppingCart;
    }

    public static ShoppingCart createTestShoppingCart(Long userId) {
        ShoppingCart shoppingCart = new ShoppingCart();
        Set<CartItem> cartItems = createTestCartItemSet(
                userId,
                EXPECTED_CART_ITEMS_SIZE,
                shoppingCart
        );

        shoppingCart.setId(userId);
        shoppingCart.setUser(createTestUser(userId));
        shoppingCart.setCartItems(cartItems);
        return shoppingCart;
    }

    public static CartItemDto createTestCartItemDto(Long id, Long bookId) {
        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setId(id);
        cartItemDto.setBookId(bookId);
        cartItemDto.setBookTitle(BOOK_TEST_DATA_MAP.get("title") + bookId);
        cartItemDto.setQuantity(Math.toIntExact(id));
        return cartItemDto;
    }

    public static Set<CartItemDto> createTestCartItemDtoSet(Long startId, int size) {
        return LongStream.range(startId, startId + size)
                .mapToObj(id -> createTestCartItemDto(id, id))
                .collect(Collectors.toSet());
    }

    public static CartItem createTestCartItem(CartItemDto cartItemDto, ShoppingCart shoppingCart) {
        CartItem cartItem = new CartItem();
        cartItem.setId(cartItemDto.getId());
        cartItem.setShoppingCart(shoppingCart);
        cartItem.setBook(createTestBook(cartItemDto.getBookId()));
        cartItem.setQuantity(cartItemDto.getQuantity());
        return cartItem;
    }

    public static CartItem getTestCartItem(Long id, ShoppingCart shoppingCart) {
        return shoppingCart.getCartItems().stream()
                .filter(cartItem -> cartItem.getId().equals(id))
                .findFirst().orElseGet(() -> {
                    CartItem cartItem = new CartItem();
                    cartItem.setId(id);
                    cartItem.setShoppingCart(shoppingCart);
                    cartItem.setBook(createTestBook(id));
                    cartItem.setQuantity(Math.toIntExact(id));
                    return cartItem;
                });
    }

    public static Set<CartItem> createTestCartItemSet(Long startId,
                                                      int size,
                                                      ShoppingCart shoppingCart) {
        return LongStream.range(startId, startId + size)
                .mapToObj(id -> getTestCartItem(id, shoppingCart))
                .collect(Collectors.toSet());
    }

    public static Set<CartItem> convertToCartItemSet(Set<CartItemDto> cartItemDtos,
                                                     ShoppingCart shoppingCart) {
        return cartItemDtos.stream()
                .map(dto -> getTestCartItem(dto.getId(), shoppingCart))
                .collect(Collectors.toSet());
    }

    public static CartItemRequestDto createTestCartItemRequestDto(CartItem cartItem) {
        CartItemRequestDto requestDto = new CartItemRequestDto();
        requestDto.setBookId(cartItem.getBook().getId());
        requestDto.setQuantity(cartItem.getQuantity());
        return requestDto;
    }

    public static CartItemRequestDto createTestCartItemRequestDto(CartItemDto cartItemDto) {
        CartItemRequestDto requestDto = new CartItemRequestDto();
        requestDto.setBookId(cartItemDto.getBookId());
        requestDto.setQuantity(cartItemDto.getQuantity());
        return requestDto;
    }

    public static CartItemRequestDto createTestCartItemRequestDto(Long id) {
        CartItemRequestDto requestDto = new CartItemRequestDto();
        requestDto.setBookId(id);
        requestDto.setQuantity(Math.toIntExact(id));
        return requestDto;
    }

    public static UpdateCartItemDto createTestUpdateCartItemDto(int updatedQuantity) {
        UpdateCartItemDto updateCartItemDto = new UpdateCartItemDto();
        updateCartItemDto.setQuantity(updatedQuantity);
        return updateCartItemDto;
    }

    public static UserResponseDto createTestUserResponseDto(Long userId) {
        return new UserResponseDto(
                userId,
                userId + USER_TEST_DATA_MAP.get("email"),
                USER_TEST_DATA_MAP.get("firstName"),
                USER_TEST_DATA_MAP.get("lastName"),
                USER_TEST_DATA_MAP.get("shippingAddress") + userId
        );
    }

    public static User createTestUser(UserResponseDto userResponseDto) {
        User user = new User();
        user.setId(userResponseDto.id());
        user.setEmail(userResponseDto.email());
        user.setFirstName(userResponseDto.firstName());
        user.setLastName(userResponseDto.lastName());
        user.setShippingAddress(userResponseDto.shippingAddress());
        user.setPassword(USER_TEST_DATA_MAP.get("password"));
        user.setRoles(new HashSet<>(Set.of(createTestRoleUser(), createTestRoleAdmin())));
        return user;
    }

    public static User createTestUser(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail(userId + USER_TEST_DATA_MAP.get("email"));
        user.setPassword(USER_TEST_DATA_MAP.get("BCryptPassword"));
        user.setFirstName(USER_TEST_DATA_MAP.get("firstName"));
        user.setLastName(USER_TEST_DATA_MAP.get("lastName"));
        user.setShippingAddress(USER_TEST_DATA_MAP.get("shippingAddress") + userId);
        user.setRoles(new HashSet<>(Set.of(createTestRoleUser(), createTestRoleAdmin())));
        return user;
    }

    public static UserRegistrationRequestDto createTestUserRegistrationRequestDto(
            UserResponseDto userResponseDto) {
        return new UserRegistrationRequestDto(
                userResponseDto.email(),
                USER_TEST_DATA_MAP.get("password"),
                USER_TEST_DATA_MAP.get("password"),
                userResponseDto.firstName(),
                userResponseDto.lastName(),
                userResponseDto.shippingAddress()
        );
    }

    public static UserRegistrationRequestDto createTestUserRegistrationRequestDto(Long userId) {
        return new UserRegistrationRequestDto(
                userId + USER_TEST_DATA_MAP.get("email"),
                USER_TEST_DATA_MAP.get("password"),
                USER_TEST_DATA_MAP.get("password"),
                USER_TEST_DATA_MAP.get("firstName"),
                USER_TEST_DATA_MAP.get("lastName"),
                USER_TEST_DATA_MAP.get("shippingAddress") + userId
        );
    }

    public static UserLoginRequestDto createTestUserLoginRequestDto(Long userId) {
        return new UserLoginRequestDto(
                userId + USER_TEST_DATA_MAP.get("email"),
                USER_TEST_DATA_MAP.get("password")
        );
    }

    public static Role createTestRoleUser() {
        Role role = new Role();
        role.setId(ROLE_USER_ID);
        role.setName(USER);
        return role;
    }

    public static Role createTestRoleAdmin() {
        Role role = new Role();
        role.setId(ROLE_ADMIN_ID);
        role.setName(ADMIN);
        return role;
    }

    public static OrderDto createTestOrderDto(Long id) {
        List<OrderItemDto> orderItemDtos = createTestOrderItemDtoList(
                id,
                EXPECTED_ORDER_ITEMS_SIZE
        );
        BigDecimal totalPrice = orderItemDtos.stream()
                .map(item -> BigDecimal.valueOf(item.quantity() * item.id()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        OrderDto orderDto = new OrderDto();
        orderDto.setId(id);
        orderDto.setOrderItems(orderItemDtos);
        orderDto.setTotal(totalPrice);
        orderDto.setUserId(id);
        orderDto.setOrderDate(ORDER_DATE);
        orderDto.setStatus(DEFAULT_ORDER_STATUS);
        return orderDto;
    }

    public static Order createTestOrder(OrderDto orderDto) {
        Order order = new Order();
        order.setId(orderDto.getId());
        order.setOrderItems(convertToOrderItemSet(orderDto.getOrderItems(), order));
        order.setTotal(orderDto.getTotal());
        order.setUser(createTestUser(orderDto.getUserId()));
        order.setOrderDate(orderDto.getOrderDate());
        order.setShippingAddress(ORDER_TEST_DATA_MAP.get("shippingAddress") + orderDto.getId());
        order.setStatus(Order.Status.PENDING);
        return order;
    }

    public static Order createTestOrder(Long id) {
        Order order = new Order();
        Set<OrderItem> orderItems = createTestOrderItemSet(id, EXPECTED_ORDER_ITEMS_SIZE, order);
        BigDecimal totalPrice = orderItems.stream()
                        .map(item -> BigDecimal.valueOf(item.getQuantity())
                                .multiply(item.getPrice()))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setId(id);
        order.setOrderItems(orderItems);
        order.setTotal(totalPrice);
        order.setUser(createTestUser(id));
        order.setOrderDate(ORDER_DATE);
        order.setShippingAddress(ORDER_TEST_DATA_MAP.get("shippingAddress") + id);
        order.setStatus(Order.Status.PENDING);
        return order;
    }

    public static OrderRequestDto createTestOrderRequestDto(Order order) {
        return new OrderRequestDto(order.getShippingAddress());
    }

    public static UpdateOrderDto createTestUpdateOrderDto(Order order) {
        return new UpdateOrderDto(order.getStatus().toString());
    }

    public static OrderItemDto createTestOrderItemDto(Long id) {
        return new OrderItemDto(id, id, (Math.toIntExact(id)));
    }

    public static List<OrderItemDto> createTestOrderItemDtoList(Long startId, int size) {
        return LongStream.range(startId, startId + size)
                .mapToObj(TestDataUtil::createTestOrderItemDto)
                .collect(Collectors.toList());
    }

    public static OrderItem createTestOrderItem(OrderItemDto orderItemDto, Order order) {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(orderItemDto.id());
        orderItem.setOrder(order);
        orderItem.setBook(createTestBook(orderItemDto.bookId()));
        orderItem.setQuantity(orderItemDto.quantity());
        orderItem.setPrice(new BigDecimal(orderItemDto.id()));
        return orderItem;
    }

    public static OrderItem createTestOrderItem(Long id, Order order) {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(id);
        orderItem.setOrder(order);
        orderItem.setBook(createTestBook(id));
        orderItem.setQuantity(Math.toIntExact(id));
        orderItem.setPrice(new BigDecimal(id));
        return orderItem;
    }

    public static Set<OrderItem> createTestOrderItemSet(Long startId, int size, Order order) {
        return LongStream.range(startId, startId + size)
                .mapToObj(id -> createTestOrderItem(id, order))
                .collect(Collectors.toSet());
    }

    public static Set<OrderItem> convertToOrderItemSet(List<OrderItemDto> orderItemDtos,
                                                       Order order) {
        return orderItemDtos.stream()
                .map(item -> createTestOrderItem(item, order))
                .collect(Collectors.toSet());
    }

    public static void sortOrderItemsInAllOrdersByBookId(OrderDto order) {
        order.getOrderItems().sort(Comparator.comparing(OrderItemDto::bookId));
    }
}
