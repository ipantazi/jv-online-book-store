package onlinebookstore.controller.order;

import static onlinebookstore.util.TestDataUtil.ALTERNATIVE_USER_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_ORDER_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_ORDER_ITEM_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_USER_ID;
import static onlinebookstore.util.TestDataUtil.EXPECTED_ORDER_ITEMS_SIZE;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_ORDER_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_ORDER_ITEM_ID;
import static onlinebookstore.util.TestDataUtil.ORDER_DTO_IGNORING_FIELDS;
import static onlinebookstore.util.TestDataUtil.ORDER_ITEM_DTO_IGNORING_FIELDS;
import static onlinebookstore.util.TestDataUtil.ORDER_ITEM_PAGEABLE;
import static onlinebookstore.util.TestDataUtil.ORDER_PAGEABLE;
import static onlinebookstore.util.TestDataUtil.ORDER_TEST_DATA_MAP;
import static onlinebookstore.util.TestDataUtil.createTestOrder;
import static onlinebookstore.util.TestDataUtil.createTestOrderDto;
import static onlinebookstore.util.TestDataUtil.createTestOrderItemDto;
import static onlinebookstore.util.TestDataUtil.createTestOrderItemDtoList;
import static onlinebookstore.util.TestDataUtil.createTestOrderRequestDto;
import static onlinebookstore.util.TestDataUtil.createTestUser;
import static onlinebookstore.util.TestDataUtil.sortOrderItemsInAllOrdersByBookId;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertCollectionsAreEqualIgnoringFields;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertObjectsAreEqualIgnoringFields;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertValidationError;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertValidationErrorList;
import static onlinebookstore.util.controller.ControllerTestDataUtil.BAD_REQUEST;
import static onlinebookstore.util.controller.ControllerTestDataUtil.EXPECTED_ADD_ORDER_ERRORS;
import static onlinebookstore.util.controller.ControllerTestDataUtil.NOT_FOUND;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_ORDERS;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_ORDERS_EXISTING_ORDER_ID;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_ORDERS_NOT_EXISTING_ORDER_ID;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_ORDER_ITEMS_EXISTING_ORDER_ID;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_ORDER_ITEMS_EXISTING_ORDER_ID_AND_ITEM_ID;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_ORDER_ITEMS_EXISTING_ORDER_ID_AND_NOT_EXISTING_ITEM_ID;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_ORDER_ITEMS_NOT_EXISTING_ORDER_ID;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_ORDER_ITEMS_NOT_EXISTING_ORDER_ID_AND_EXISTING_ITEM_ID;
import static onlinebookstore.util.controller.ControllerTestUtil.createRequestWithPageable;
import static onlinebookstore.util.controller.ControllerTestUtil.parsePageContent;
import static onlinebookstore.util.controller.ControllerTestUtil.parseResponseToObject;
import static onlinebookstore.util.controller.DatabaseTestUtil.executeSqlScript;
import static onlinebookstore.util.controller.MockMvcUtil.buildMockMvc;
import static onlinebookstore.util.controller.MvcTestHelper.createJsonMvcResult;
import static onlinebookstore.util.controller.MvcTestHelper.createMvcResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import onlinebookstore.dto.order.OrderDto;
import onlinebookstore.dto.order.OrderRequestDto;
import onlinebookstore.dto.order.UpdateOrderDto;
import onlinebookstore.dto.orderitem.OrderItemDto;
import onlinebookstore.model.Order;
import onlinebookstore.model.User;
import onlinebookstore.util.TestDataUtil;
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
public class OrderControllerTest {
    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired DataSource dataSource,
                          @Autowired WebApplicationContext applicationContext) {
        mockMvc = buildMockMvc(applicationContext);

        teardown(dataSource);
        executeSqlScript(dataSource,
                "database/users/add-test-users-to-users-table.sql",
                "database/orders/add-test-order-to-orders-table.sql",
                "database/books/add-test-books-to-books-table.sql",
                "database/orderitems/add-test-order-item-to-order-items-table.sql"
        );
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        executeSqlScript(dataSource,
                "database/orderitems/clear-all-order-items.sql",
                "database/books/remove-test-books-from-books-table.sql",
                "database/orders/clear-all-orders.sql",
                "database/users/remove-test-users-from-users-table.sql"
        );
    }

    @Test
    @DisplayName("All orders must be returned by user id.")
    void getOrders_GivenOrdersCatalog_ShouldReturnAllUserOrders() throws Exception {
        // Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        OrderDto testOrderDto = createTestOrderDto(EXISTING_ORDER_ID);
        List<OrderDto> expectedOrderDtos = Collections.singletonList(testOrderDto);

        //When
        MvcResult result = createMvcResult(
                mockMvc,
                createRequestWithPageable(URL_ORDERS, ORDER_PAGEABLE),
                status().isOk()
        );

        //Then
        List<OrderDto> orderDtoList = parsePageContent(
                result,
                objectMapper,
                new TypeReference<List<OrderDto>>() {
                }
        );
        orderDtoList.forEach(TestDataUtil::sortOrderItemsInAllOrdersByBookId);
        assertCollectionsAreEqualIgnoringFields(
                orderDtoList,
                expectedOrderDtos,
                ORDER_DTO_IGNORING_FIELDS
        );
    }

    @Test
    @Sql(scripts = {
            "classpath:database/orderitems/clear-all-order-items.sql",
            "classpath:database/orders/clear-all-orders.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/orders/add-test-order-to-orders-table.sql",
            "classpath:database/orderitems/add-test-order-item-to-order-items-table.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Verify getOrders() method returns empty page when user hasn't orders.")
    void getOrders_GivenEmptyCatalog_ShouldReturnEmptyPage() throws Exception {
        // Given
        User user = createTestUser(ALTERNATIVE_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);

        //When
        MvcResult result = createMvcResult(
                mockMvc,
                createRequestWithPageable(URL_ORDERS, ORDER_PAGEABLE),
                status().isOk()
        );

        //Then
        List<OrderDto> actualOrderDtos = parsePageContent(
                result,
                objectMapper,
                new TypeReference<List<OrderDto>>() {
                }
        );
        assertThat(actualOrderDtos).isEmpty();
    }

    @Test
    @Sql(scripts = {
            "classpath:database/orderitems/clear-all-order-items.sql",
            "classpath:database/orders/clear-all-orders.sql",
            "classpath:database/carts/add-test-shoppingcart-to-shoppingcarts-table.sql",
            "classpath:database/carts/add-test-cartitems-to-cartitems-table.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/carts/remove-test-cartitems-from-cartitems-table.sql",
            "classpath:database/carts/remove-test-shoppingcart-from-shoppingcarts-table.sql",
            "classpath:database/orderitems/clear-all-order-items.sql",
            "classpath:database/orders/clear-all-orders.sql",
            "classpath:database/orders/add-test-order-to-orders-table.sql",
            "classpath:database/orderitems/add-test-order-item-to-order-items-table.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Create order.")
    void addOrder_ValidRequestDto_Success() throws Exception {
        // Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        OrderDto expectedOrderDto = createTestOrderDto(EXISTING_ORDER_ID);
        Order order = createTestOrder(expectedOrderDto);
        OrderRequestDto orderRequestDto = createTestOrderRequestDto(order);
        String jsonRequest = objectMapper.writeValueAsString(orderRequestDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_ORDERS),
                status().isCreated(),
                jsonRequest
        );

        //Then
        OrderDto actualOrderDto = parseResponseToObject(
                result,
                objectMapper,
                OrderDto.class
        );
        sortOrderItemsInAllOrdersByBookId(actualOrderDto);
        assertObjectsAreEqualIgnoringFields(
                actualOrderDto,
                expectedOrderDto,
                ORDER_DTO_IGNORING_FIELDS
        );
    }

    @Test
    @Sql(scripts = {
            "classpath:database/carts/add-test-shoppingcart-to-shoppingcarts-table.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/carts/remove-test-shoppingcart-from-shoppingcarts-table.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Verify that an exception is throw when a shopping cart is empty.")
    void addOrder_GivenEmptyShoppingCart_ShouldReturnBadRequest() throws Exception {
        // Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        OrderRequestDto orderRequestDto = new OrderRequestDto(
                ORDER_TEST_DATA_MAP.get("shippingAddress"));
        String jsonRequest = objectMapper.writeValueAsString(orderRequestDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_ORDERS),
                status().isBadRequest(),
                jsonRequest
        );

        //Then
        assertValidationError(
                result,
                objectMapper,
                BAD_REQUEST,
                "Shopping cart is empty for user: " + user.getId()
        );
    }

    @Test
    @DisplayName("""
            Verify that an exception is throw when a shipping address is blank
             and less than 10 characters.
            """)
    void addOrder_InvalidFormatOrderFields_ShouldReturnBadRequest() throws Exception {
        // Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        OrderRequestDto orderRequestDto = new OrderRequestDto("");
        String jsonRequest = objectMapper.writeValueAsString(orderRequestDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_ORDERS),
                status().isBadRequest(),
                jsonRequest
        );

        //Then
        assertValidationErrorList(
                result,
                objectMapper,
                EXPECTED_ADD_ORDER_ERRORS
        );
    }

    @Test
    @Sql(scripts = {
            "classpath:database/orders/restoring-order-id101-from-orders-table.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Update order status.")
    void updateOrder_ValidOrderIdAndUpdateOrderDto_Success() throws Exception {
        // Given
        String updatedStatus = "SHIPPED";
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        UpdateOrderDto updateOrderDto = new UpdateOrderDto(updatedStatus);
        String jsonRequest = objectMapper.writeValueAsString(updateOrderDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                patch(URL_ORDERS_EXISTING_ORDER_ID),
                status().isOk(),
                jsonRequest
        );

        //Then
        OrderDto actualOrderDto = parseResponseToObject(
                result,
                objectMapper,
                OrderDto.class
        );
        assertThat(actualOrderDto.getStatus()).isEqualTo(updatedStatus);
    }

    @Test
    @DisplayName("Verify that an exception is throw when an order id doesn't exists.")
    void updateOrder_InvalidOrderId_ShouldReturnNotFound() throws Exception {
        // Given
        String updatedStatus = "SHIPPED";
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        UpdateOrderDto updateOrderDto = new UpdateOrderDto(updatedStatus);
        String jsonRequest = objectMapper.writeValueAsString(updateOrderDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                patch(URL_ORDERS_NOT_EXISTING_ORDER_ID),
                status().isNotFound(),
                jsonRequest
        );

        //Then
        assertValidationError(
                result,
                objectMapper,
                NOT_FOUND,
                "Can't find order with id: " + NOT_EXISTING_ORDER_ID
        );
    }

    @Test
    @DisplayName("Verify that an exception is throw when a field status is not in a valid format.")
    public void updateOrder_BlankStatus_ShouldReturnBadRequest() throws Exception {
        // Given
        String updatedStatus = "";
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        UpdateOrderDto updateOrderDto = new UpdateOrderDto(updatedStatus);
        String jsonRequest = objectMapper.writeValueAsString(updateOrderDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                patch(URL_ORDERS_EXISTING_ORDER_ID),
                status().isBadRequest(),
                jsonRequest
        );

        //Then
        assertValidationErrorList(
                result,
                objectMapper,
                List.of("status Invalid status. Status can't be blank.")
        );
    }

    @Test
    @DisplayName("Verify that an exception is throw when a status is more than 10 characters.")
    public void updateOrder_InvalidFormatStatus_ShouldReturnBadRequest() throws Exception {
        // Given
        String updatedStatus = "INVALID FORMAT STATUS";
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        UpdateOrderDto updateOrderDto = new UpdateOrderDto(updatedStatus);
        String jsonRequest = objectMapper.writeValueAsString(updateOrderDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                patch(URL_ORDERS_EXISTING_ORDER_ID),
                status().isBadRequest(),
                jsonRequest
        );

        //Then
        assertValidationErrorList(
                result,
                objectMapper,
                List.of("status Invalid status. Status cannot be more than 10 characters.")
        );
    }

    @Test
    @DisplayName("Verify that an exception is throw when a field status is not valid.")
    public void updateOrder_InvalidStatus_ShouldReturnBadRequest() throws Exception {
        // Given
        String updatedStatus = "INVALID";
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        UpdateOrderDto updateOrderDto = new UpdateOrderDto(updatedStatus);
        String jsonRequest = objectMapper.writeValueAsString(updateOrderDto);

        //When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                patch(URL_ORDERS_EXISTING_ORDER_ID),
                status().isBadRequest(),
                jsonRequest
        );

        //Then
        assertValidationError(
                result,
                objectMapper,
                BAD_REQUEST,
                "Invalid order status: " + updateOrderDto.status()
        );
    }

    @Test
    @DisplayName("Get a a specific order item")
    void getOrderItem_ValidOrderIdAndItemId_Success() throws Exception {
        // Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        OrderItemDto expectedOrderItemDto = createTestOrderItemDto(EXISTING_ORDER_ITEM_ID);

        //When
        MvcResult result = createMvcResult(
                mockMvc,
                get(URL_ORDER_ITEMS_EXISTING_ORDER_ID_AND_ITEM_ID),
                status().isOk()
        );

        //Then
        OrderItemDto actualOrderItemDto = parseResponseToObject(
                result,
                objectMapper,
                OrderItemDto.class
        );
        assertObjectsAreEqualIgnoringFields(
                actualOrderItemDto,
                expectedOrderItemDto,
                ORDER_ITEM_DTO_IGNORING_FIELDS
        );
    }

    @Test
    @DisplayName("Verify that an exception is throw when an order item id doesn't exists.")
    void getOrderItem_InvalidOrderItemId_ShouldReturnNotFound() throws Exception {
        // Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);

        //When
        MvcResult result = createMvcResult(
                mockMvc,
                get(URL_ORDER_ITEMS_EXISTING_ORDER_ID_AND_NOT_EXISTING_ITEM_ID),
                status().isNotFound()
        );

        //Then
        assertValidationError(
                result,
                objectMapper,
                NOT_FOUND,
                "Can't find item with id: " + NOT_EXISTING_ORDER_ITEM_ID
        );
    }

    @Test
    @DisplayName("Verify that an exception is throw when an order id doesn't exists.")
    void getOrderItem_InvalidOrderId_ShouldReturnNotFound() throws Exception {
        // Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);

        //When
        MvcResult result = createMvcResult(
                mockMvc,
                get(URL_ORDER_ITEMS_NOT_EXISTING_ORDER_ID_AND_EXISTING_ITEM_ID),
                status().isNotFound()
        );

        //Then
        assertValidationError(
                result,
                objectMapper,
                NOT_FOUND,
                "Can't find item with order id: " + NOT_EXISTING_ORDER_ID
                        + " for user with id: " + user.getId()
        );
    }

    @Test
    @DisplayName("Verify that an exception is throw when an order belongs to another user.")
    void getOrderItem_InvalidUserId_ShouldReturnNotFound() throws Exception {
        // Given
        User user = createTestUser(ALTERNATIVE_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);

        //When
        MvcResult result = createMvcResult(
                mockMvc,
                get(URL_ORDER_ITEMS_EXISTING_ORDER_ID_AND_ITEM_ID),
                status().isNotFound()
        );

        //Then
        assertValidationError(
                result,
                objectMapper,
                NOT_FOUND,
                "Can't find item with order id: " + EXISTING_ORDER_ID
                        + " for user with id: " + user.getId()
        );
    }

    @Test
    @DisplayName("Get order items for a specific order.")
    void getOrderItems_ValidOrderId_Success() throws Exception {
        // Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);
        List<OrderItemDto> expected = createTestOrderItemDtoList(EXISTING_ORDER_ITEM_ID,
                EXPECTED_ORDER_ITEMS_SIZE);

        //When
        MvcResult result = createMvcResult(
                mockMvc,
                createRequestWithPageable(URL_ORDER_ITEMS_EXISTING_ORDER_ID, ORDER_ITEM_PAGEABLE),
                status().isOk()
        );

        //Then
        List<OrderItemDto> actual = parsePageContent(
                result,
                objectMapper,
                new TypeReference<List<OrderItemDto>>() {
                }
        );
        assertCollectionsAreEqualIgnoringFields(actual, expected, ORDER_ITEM_DTO_IGNORING_FIELDS);
    }

    @Test
    @DisplayName("Verify that an exception is throw when an order id doesn't exists.")
    void getOrderItems_InvalidOrderId_ShouldReturnNotFound() throws Exception {
        // Given
        User user = createTestUser(EXISTING_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);

        //When
        MvcResult result = createMvcResult(
                mockMvc,
                createRequestWithPageable(
                        URL_ORDER_ITEMS_NOT_EXISTING_ORDER_ID,
                        ORDER_ITEM_PAGEABLE
                ),
                status().isNotFound()
        );

        //Then
        assertValidationError(
                result,
                objectMapper,
                NOT_FOUND,
                "Can't find order with id: " + NOT_EXISTING_ORDER_ID
                        + " for user with id: " + user.getId()
        );
    }

    @Test
    @DisplayName("Verify that an exception is throw when an order belongs to another user.")
    void getOrderItems_InvalidUserId_ShouldReturnNotFound() throws Exception {
        // Given
        User user = createTestUser(ALTERNATIVE_USER_ID);
        SecurityTestUtil.setAuthenticationForUser(user);

        //When
        MvcResult result = createMvcResult(
                mockMvc,
                createRequestWithPageable(URL_ORDER_ITEMS_EXISTING_ORDER_ID, ORDER_ITEM_PAGEABLE),
                status().isNotFound()
        );

        //Then
        assertValidationError(
                result,
                objectMapper,
                NOT_FOUND,
                "Can't find order with id: " + EXISTING_ORDER_ID
                        + " for user with id: " + user.getId()
        );
    }
}
