package onlinebookstore.controller.shoppingcart;

import static onlinebookstore.util.TestDataUtil.ALTERNATIVE_BOOK_ID;
import static onlinebookstore.util.TestDataUtil.ALTERNATIVE_USER_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_BOOK_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_CART_ITEM_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_USER_ID;
import static onlinebookstore.util.TestDataUtil.EXPECTED_CART_ITEMS_SIZE;
import static onlinebookstore.util.TestDataUtil.NEGATIVE_ID;
import static onlinebookstore.util.TestDataUtil.NEGATIVE_VALUE;
import static onlinebookstore.util.TestDataUtil.NEW_CART_ITEM_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_BOOK_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_CART_ITEM_ID;
import static onlinebookstore.util.TestDataUtil.SHOPPING_CART_DTO_IGNORING_CART_ITEMS_FIELD;
import static onlinebookstore.util.TestDataUtil.SHOPPING_CART_DTO_IGNORING_FIELDS;
import static onlinebookstore.util.TestDataUtil.UPDATED_QUANTITY;
import static onlinebookstore.util.TestDataUtil.createTestCartItemDto;
import static onlinebookstore.util.TestDataUtil.createTestCartItemRequestDto;
import static onlinebookstore.util.TestDataUtil.createTestShoppingCartDto;
import static onlinebookstore.util.TestDataUtil.createTestUpdateCartItemDto;
import static onlinebookstore.util.TestDataUtil.createTestUser;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertObjectsAreEqualIgnoringFields;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertShoppingCartContainsExpectedItem;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertValidationError;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertValidationErrorList;
import static onlinebookstore.util.controller.ControllerTestDataUtil.EXPECTED_CART_ITEM_ERRORS;
import static onlinebookstore.util.controller.ControllerTestDataUtil.EXPECTED_CART_ITEM_VALUE_NEGATIVE_ERRORS;
import static onlinebookstore.util.controller.ControllerTestDataUtil.NOT_FOUND;
import static onlinebookstore.util.controller.ControllerTestDataUtil.NO_CONTENT;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_SHOPPING_CART;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_SHOPPING_CART_ITEMS_EXISTING_CART_ID;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_SHOPPING_CART_ITEMS_NOT_EXISTING_CART_ID;
import static onlinebookstore.util.controller.ControllerTestUtil.parseResponseToObject;
import static onlinebookstore.util.controller.DatabaseTestUtil.executeSqlScript;
import static onlinebookstore.util.controller.MockMvcUtil.buildMockMvc;
import static onlinebookstore.util.controller.MvcTestHelper.createJsonMvcResult;
import static onlinebookstore.util.controller.MvcTestHelper.createMvcResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import onlinebookstore.dto.cartitem.CartItemDto;
import onlinebookstore.dto.cartitem.CartItemRequestDto;
import onlinebookstore.dto.cartitem.UpdateCartItemDto;
import onlinebookstore.dto.shoppingcart.ShoppingCartDto;
import onlinebookstore.model.User;
import onlinebookstore.util.controller.SecurityTestUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShoppingCartControllerTest {
    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired DataSource dataSource,
                          @Autowired WebApplicationContext applicationContext) {
        mockMvc = buildMockMvc(applicationContext);

        teardown(dataSource);
        executeSqlScript(dataSource,
                "database/books/add-test-books-to-books-table.sql",
                "database/users/add-test-users-to-users-table.sql",
                "database/carts/add-test-shoppingcart-to-shoppingcarts-table.sql",
                "database/carts/add-test-cartitems-to-cartitems-table.sql"
        );
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        executeSqlScript(dataSource,
                "database/carts/clear-all-cartitems.sql",
                "database/carts/clear-all-shoppingcarts.sql",
                "database/users/remove-test-users-from-users-table.sql",
                "database/books/clear-all-books.sql"
        );
    }

    @Test
    @DisplayName("Get shopping cart.")
    void getShoppingCart_GivenShoppingCart_ShouldReturnShoppingCartDto() throws Exception {
        // Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        ShoppingCartDto expectedShoppingCartDto = createTestShoppingCartDto(EXISTING_USER_ID);

        // When
        MvcResult result = createMvcResult(mockMvc, get(URL_SHOPPING_CART),status().isOk());

        // Then
        ShoppingCartDto actualShoppingCartDto = parseResponseToObject(
                result,
                objectMapper,
                ShoppingCartDto.class
        );
        assertObjectsAreEqualIgnoringFields(
                actualShoppingCartDto,
                expectedShoppingCartDto,
                SHOPPING_CART_DTO_IGNORING_FIELDS
        );
    }

    @Test
    @DisplayName("Method should register new shopping cart if none exists and return it as DTO.")
    void getShoppingCart_ShoppingCartNotExists_ShouldReturnShoppingCartDto() throws Exception {
        // Given
        User user = createTestUser(ALTERNATIVE_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        ShoppingCartDto expectedShoppingCartDto = createTestShoppingCartDto(ALTERNATIVE_USER_ID);

        // When
        MvcResult result = createMvcResult(mockMvc, get(URL_SHOPPING_CART), status().isOk());

        // Then
        ShoppingCartDto actualShoppingCartDto = parseResponseToObject(
                result,
                objectMapper,
                ShoppingCartDto.class
        );
        assertObjectsAreEqualIgnoringFields(
                actualShoppingCartDto,
                expectedShoppingCartDto,
                SHOPPING_CART_DTO_IGNORING_CART_ITEMS_FIELD
        );
    }

    @Test
    @DisplayName("Add new cart item to the shopping cart when cart item doesn't exists.")
    @Sql(scripts =
            "classpath:database/carts/remove-new-test-cart-item-from-cart-items-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void addCartItem_CartItemNotExists_Success() throws Exception {
        // Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        ShoppingCartDto expectedShoppingCartDto = createTestShoppingCartDto(EXISTING_USER_ID);
        CartItemDto newCartItemDto = createTestCartItemDto(NEW_CART_ITEM_ID, ALTERNATIVE_BOOK_ID);
        expectedShoppingCartDto.getCartItems().add(newCartItemDto);
        CartItemRequestDto requestDto = createTestCartItemRequestDto(newCartItemDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_SHOPPING_CART),
                status().isCreated(),
                jsonRequest);

        //Then
        ShoppingCartDto actualShoppingCartDto = parseResponseToObject(
                result,
                objectMapper,
                ShoppingCartDto.class
        );
        assertObjectsAreEqualIgnoringFields(
                actualShoppingCartDto,
                expectedShoppingCartDto,
                SHOPPING_CART_DTO_IGNORING_FIELDS
        );
    }

    @Test
    @DisplayName("Add cart item to the shopping cart when cart item already exists.")
    @Sql(scripts =
            "classpath:database/carts/restoring-cart-item-id101-from-cart-items-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void addCartItem_CartItemAlreadyExists_Success() throws Exception {
        // Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        CartItemRequestDto requestDto = createTestCartItemRequestDto(EXISTING_CART_ITEM_ID);
        CartItemDto expected = createTestCartItemDto(EXISTING_CART_ITEM_ID, EXISTING_BOOK_ID);
        expected.setQuantity(expected.getQuantity() + requestDto.getQuantity());
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_SHOPPING_CART),
                status().isCreated(),
                jsonRequest);

        //Then
        ShoppingCartDto actualShoppingCartDto = parseResponseToObject(
                result,
                objectMapper,
                ShoppingCartDto.class
        );
        assertShoppingCartContainsExpectedItem(actualShoppingCartDto, expected);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a book id doesn't exists.")
    void addCartItem_InvalidBookId_ShouldReturnNotFound() throws Exception {
        // Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        CartItemRequestDto requestDto = createTestCartItemRequestDto(NEW_CART_ITEM_ID);
        requestDto.setBookId(NOT_EXISTING_BOOK_ID);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_SHOPPING_CART),
                status().isNotFound(),
                jsonRequest
        );

        //Then
        assertValidationError(
                result,
                objectMapper,
                NOT_FOUND,
                "Can`t find the book by id: " + requestDto.getBookId()
        );
    }

    @Test
    @DisplayName("Verify that an exception is throw when a book id is blank and quantity is null.")
    void addCartItem_BlankBookIdAndNullQuantity_ShouldReturnBadRequest() throws Exception {
        // Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        CartItemRequestDto requestDto = new CartItemRequestDto();
        requestDto.setQuantity(0);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_SHOPPING_CART),
                status().isBadRequest(),
                jsonRequest
        );

        //Then
        assertValidationErrorList(
                result,
                objectMapper,
                EXPECTED_CART_ITEM_ERRORS
        );
    }

    @Test
    @DisplayName("Verify that an exception is throw when a book id and quantity are negative.")
    void addCartItem_BookIdAndQuantityAreNegative_ShouldReturnBadRequest() throws Exception {
        // Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        CartItemRequestDto requestDto = new CartItemRequestDto();
        requestDto.setBookId(NEGATIVE_ID);
        requestDto.setQuantity(NEGATIVE_VALUE);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_SHOPPING_CART),
                status().isBadRequest(),
                jsonRequest
        );

        //Then
        assertValidationErrorList(
                result,
                objectMapper,
                EXPECTED_CART_ITEM_VALUE_NEGATIVE_ERRORS
        );
    }

    @Test
    @DisplayName("Update cart item of a book in the shopping cart.")
    @Sql(scripts =
            "classpath:database/carts/restoring-cart-item-id101-from-cart-items-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateCartItem_ValidRequest_Success() throws Exception {
        // Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        UpdateCartItemDto updateCartItem = createTestUpdateCartItemDto(UPDATED_QUANTITY);
        CartItemDto expected = createTestCartItemDto(EXISTING_CART_ITEM_ID, EXISTING_BOOK_ID);
        expected.setQuantity(updateCartItem.getQuantity());
        String jsonRequest = objectMapper.writeValueAsString(updateCartItem);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                put(URL_SHOPPING_CART_ITEMS_EXISTING_CART_ID),
                status().isOk(),
                jsonRequest
        );

        //Then
        ShoppingCartDto actualShoppingCartDto = parseResponseToObject(
                result,
                objectMapper,
                ShoppingCartDto.class
        );
        assertShoppingCartContainsExpectedItem(actualShoppingCartDto, expected);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a cart item id doesn't exists.")
    public void updateCartItem_CartItemIdNoExists_ShouldReturnNotFound() throws Exception {
        //Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        UpdateCartItemDto updateCartItem = createTestUpdateCartItemDto(UPDATED_QUANTITY);
        String jsonRequest = objectMapper.writeValueAsString(updateCartItem);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                put(URL_SHOPPING_CART_ITEMS_NOT_EXISTING_CART_ID),
                status().isNotFound(),
                jsonRequest
        );

        //Then
        assertValidationError(
                result,
                objectMapper,
                NOT_FOUND,
                "Can't find cart with id: " + NOT_EXISTING_CART_ITEM_ID
        );
    }

    @Test
    @DisplayName("Verify that an exception is throw when a quantity is negative.")
    void updateCartItem_NegativeQuantity_ShouldReturnBadRequest() throws Exception {
        // Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        UpdateCartItemDto updateCartItem = createTestUpdateCartItemDto(NEGATIVE_VALUE);
        String jsonRequest = objectMapper.writeValueAsString(updateCartItem);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                put(URL_SHOPPING_CART_ITEMS_EXISTING_CART_ID),
                status().isBadRequest(),
                jsonRequest
        );

        //Then
        assertValidationErrorList(
                result,
                objectMapper,
                List.of("quantity Invalid quantity. Value should be positive.")
        );
    }

    @Test
    @DisplayName("Remove a book from the shopping cart.")
    @Sql(scripts = {
            "classpath:database/carts/remove-test-cartitems-from-cartitems-table.sql",
            "classpath:database/carts/add-test-cartitems-to-cartitems-table.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void delete_GivenValidCartItem_ShouldDeleteCartItem() throws Exception {
        //Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);

        //When
        MvcResult result = createMvcResult(
                mockMvc,
                delete(URL_SHOPPING_CART_ITEMS_EXISTING_CART_ID),
                status().isNoContent()
        );

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(NO_CONTENT);
        MvcResult checkResult = createMvcResult(
                mockMvc,
                get(URL_SHOPPING_CART),
                status().isOk()
        );
        ShoppingCartDto shoppingCartDto = parseResponseToObject(
                checkResult,
                objectMapper,
                ShoppingCartDto.class
        );
        assertThat(shoppingCartDto.getCartItems()).hasSize(EXPECTED_CART_ITEMS_SIZE - 1);
        assertThat(shoppingCartDto.getCartItems()).noneMatch(actual ->
                Objects.equals(actual.getBookId(), EXISTING_BOOK_ID));
    }

    @Test
    @DisplayName("Verify that an exception is throw when a cart item id doesn't exists.")
    public void delete_CartItemNotExists_ShouldReturnNotFound() throws Exception {
        //Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);

        //When
        MvcResult result = createMvcResult(
                mockMvc,
                delete(URL_SHOPPING_CART_ITEMS_NOT_EXISTING_CART_ID),
                status().isNotFound()
        );

        //Then
        assertValidationError(
                result,
                objectMapper,
                NOT_FOUND,
                "Can't find cart with id: " + NOT_EXISTING_CART_ITEM_ID
        );
    }
}
