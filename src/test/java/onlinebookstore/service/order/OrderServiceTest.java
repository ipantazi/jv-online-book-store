package onlinebookstore.service.order;

import static onlinebookstore.util.TestDataUtil.ALTERNATIVE_USER_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_ORDER_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_USER_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_ORDER_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_USER_ID;
import static onlinebookstore.util.TestDataUtil.ORDER_DTO_IGNORING_FIELDS;
import static onlinebookstore.util.TestDataUtil.ORDER_PAGEABLE;
import static onlinebookstore.util.TestDataUtil.ORDER_TEST_DATA_MAP;
import static onlinebookstore.util.TestDataUtil.createTestOrder;
import static onlinebookstore.util.TestDataUtil.createTestOrderDto;
import static onlinebookstore.util.TestDataUtil.createTestOrderRequestDto;
import static onlinebookstore.util.TestDataUtil.createTestShoppingCart;
import static onlinebookstore.util.TestDataUtil.createTestUpdateOrderDto;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertObjectsAreEqualIgnoringFields;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertPageMetadataEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import onlinebookstore.dto.order.OrderDto;
import onlinebookstore.dto.order.OrderRequestDto;
import onlinebookstore.dto.order.UpdateOrderDto;
import onlinebookstore.exception.EntityNotFoundException;
import onlinebookstore.exception.OrderProcessingException;
import onlinebookstore.mapper.OrderMapper;
import onlinebookstore.model.Order;
import onlinebookstore.model.ShoppingCart;
import onlinebookstore.repository.order.OrderRepository;
import onlinebookstore.repository.shoppingcart.ShoppingCartRepository;
import onlinebookstore.service.shoppingcart.ShoppingCartServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ShoppingCartServiceImpl shoppingCartService;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("Verify createOrder() method works.")
    public void createOrder_ValidUserIdAndOrderRequestDto_ReturnsOrderDto() {
        //Given
        ShoppingCart shoppingCart = createTestShoppingCart(EXISTING_USER_ID);
        OrderDto expectedOrderDto = createTestOrderDto(EXISTING_ORDER_ID);
        Order order = createTestOrder(expectedOrderDto);
        OrderRequestDto orderRequestDto = createTestOrderRequestDto(order);
        when(shoppingCartService.findShoppingCartByUserId(EXISTING_USER_ID))
                .thenReturn(shoppingCart);
        when(orderMapper.toOrderEntity(shoppingCart)).thenReturn(order);
        when(orderMapper.toOrderDto(any(Order.class))).thenReturn(expectedOrderDto);

        //When
        OrderDto actualOrderDto = orderService.createOrder(EXISTING_USER_ID, orderRequestDto);

        //Then
        assertObjectsAreEqualIgnoringFields(
                actualOrderDto,
                expectedOrderDto,
                ORDER_DTO_IGNORING_FIELDS
        );
        assertThat(shoppingCart.getCartItems()).isEmpty();
        verify(shoppingCartService, times(1)).findShoppingCartByUserId(EXISTING_USER_ID);
        verify(orderMapper, times(1)).toOrderEntity(shoppingCart);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(shoppingCartRepository, times(1)).save(any(ShoppingCart.class));
        verify(orderMapper, times(1)).toOrderDto(any(Order.class));
        verifyNoMoreInteractions(shoppingCartService, orderRepository);
        verifyNoMoreInteractions(orderMapper, shoppingCartRepository);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a shopping cart is empty.")
    public void createOrder_ValidUserIdAndShoppingCartEmpty_ThrowsException() {
        //Given
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setCartItems(new HashSet<>());
        OrderRequestDto orderRequestDto = new OrderRequestDto(
                ORDER_TEST_DATA_MAP.get("shippingAddress"));
        when(shoppingCartService.findShoppingCartByUserId(EXISTING_USER_ID))
                .thenReturn(shoppingCart);

        //When
        assertThatThrownBy(() -> orderService.createOrder(EXISTING_USER_ID, orderRequestDto))
                .isInstanceOf(OrderProcessingException.class)
                .hasMessage("Shopping cart is empty for user: " + EXISTING_USER_ID);

        //Then
        verify(shoppingCartService, times(1)).findShoppingCartByUserId(EXISTING_USER_ID);
        verify(orderRepository, never()).save(any(Order.class));
        verify(shoppingCartRepository, never()).save(any(ShoppingCart.class));
        verifyNoMoreInteractions(shoppingCartService);
        verifyNoInteractions(orderRepository, orderMapper, shoppingCartRepository);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a user id doesn't exists.")
    public void createOrder_InvalidUserId_ThrowsException() {
        //Given
        OrderRequestDto orderRequestDto = new OrderRequestDto(
                ORDER_TEST_DATA_MAP.get("shippingAddress"));
        when(shoppingCartService.findShoppingCartByUserId(NOT_EXISTING_USER_ID))
                .thenThrow(new EntityNotFoundException("User with id " + NOT_EXISTING_USER_ID
                        + " not found"));

        //When
        assertThatThrownBy(() -> orderService.createOrder(NOT_EXISTING_USER_ID, orderRequestDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User with id " + NOT_EXISTING_USER_ID + " not found");

        //Then
        verify(shoppingCartService, times(1)).findShoppingCartByUserId(NOT_EXISTING_USER_ID);
        verify(orderRepository, never()).save(any(Order.class));
        verify(shoppingCartRepository, never()).save(any(ShoppingCart.class));
        verifyNoMoreInteractions(shoppingCartService);
        verifyNoInteractions(orderRepository, orderMapper, shoppingCartRepository);
    }

    @Test
    @DisplayName("Verify getOrders() method works.")
    public void getOrders_ValidUserId_ReturnsAllOrders() {
        //Given
        OrderDto expectedOrderDto = createTestOrderDto(EXISTING_ORDER_ID);
        Order order = createTestOrder(expectedOrderDto);
        List<Order> orders = Collections.singletonList(order);
        Page<Order> orderPage = new PageImpl<>(
                orders,
                ORDER_PAGEABLE,
                orders.size()
        );
        when(orderRepository.findByUserId(EXISTING_USER_ID, ORDER_PAGEABLE)).thenReturn(orderPage);
        when(orderMapper.toOrderDto(order)).thenReturn(expectedOrderDto);

        //When
        Page<OrderDto> actualOrderDtoPage = orderService.getOrders(
                EXISTING_USER_ID, ORDER_PAGEABLE);

        //Then
        assertThat(actualOrderDtoPage.getContent()).isNotEmpty();
        List<OrderDto> actualOrderDtos = actualOrderDtoPage.getContent();
        assertThat(actualOrderDtos).hasSize(1).containsExactly(expectedOrderDto);
        assertObjectsAreEqualIgnoringFields(
                actualOrderDtos.get(0),
                expectedOrderDto,
                ORDER_DTO_IGNORING_FIELDS
        );
        verify(orderRepository, times(1)).findByUserId(EXISTING_USER_ID, ORDER_PAGEABLE);
        verify(orderMapper, times(1)).toOrderDto(order);
        verifyNoMoreInteractions(orderMapper, orderRepository);
    }

    @Test
    @DisplayName("Verify getOrders() returns empty page when no orders exists.")
    public void getOrders_ValidUserIdAndNoOrders_ReturnsEmptyPage() {
        // Given
        Page<Order> orderPage = Page.empty(ORDER_PAGEABLE);
        when(orderRepository.findByUserId(ALTERNATIVE_USER_ID, ORDER_PAGEABLE))
                .thenReturn(orderPage);

        //When
        Page<OrderDto> actualOrderDtoPage = orderService.getOrders(
                ALTERNATIVE_USER_ID, ORDER_PAGEABLE);

        //Then
        assertThat(actualOrderDtoPage.getContent()).isEmpty();
        assertPageMetadataEquals(actualOrderDtoPage, orderPage);
        verify(orderRepository, times(1)).findByUserId(ALTERNATIVE_USER_ID, ORDER_PAGEABLE);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderMapper);
    }

    @Test
    @DisplayName("Verify getOrders() returns empty page when a user doesn't exists.")
    public void getOrders_InvalidUserId_ReturnsEmptyPage() {
        // Given
        Page<Order> orderPage = Page.empty(ORDER_PAGEABLE);
        when(orderRepository.findByUserId(NOT_EXISTING_USER_ID, ORDER_PAGEABLE))
                .thenReturn(orderPage);

        //When
        Page<OrderDto> actualOrderDtoPage = orderService.getOrders(
                NOT_EXISTING_USER_ID, ORDER_PAGEABLE);

        //Then
        assertThat(actualOrderDtoPage.getContent()).isEmpty();
        assertPageMetadataEquals(actualOrderDtoPage, orderPage);
        verify(orderRepository, times(1)).findByUserId(NOT_EXISTING_USER_ID, ORDER_PAGEABLE);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderMapper);
    }

    @Test
    @DisplayName("Verify changeStatusOrder() method works.")
    public void changeStatusOrder_ValidOrderIdAndUpdateOrderDto_ReturnsOrderDto() {
        //Given
        OrderDto expectedOrderDto = createTestOrderDto(EXISTING_ORDER_ID);
        Order order = createTestOrder(expectedOrderDto);
        UpdateOrderDto updateOrderDto = createTestUpdateOrderDto(order);
        when(orderRepository.findById(EXISTING_ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toOrderDto(any(Order.class))).thenReturn(expectedOrderDto);

        //When
        OrderDto actualOrderDto = orderService.changeStatusOrder(
                EXISTING_ORDER_ID, updateOrderDto);

        //Then
        assertObjectsAreEqualIgnoringFields(
                actualOrderDto,
                expectedOrderDto,
                ORDER_DTO_IGNORING_FIELDS
        );
        verify(orderRepository, times(1)).findById(EXISTING_ORDER_ID);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderMapper, times(1)).toOrderDto(any(Order.class));
        verifyNoMoreInteractions(orderMapper, orderRepository);
    }

    @Test
    @DisplayName("Verify that an exception is throw when an order id doesn't exists.")
    public void changeStatusOrder_InvalidOrderId_ThrowsException() {
        //Given
        UpdateOrderDto updateOrderDto = new UpdateOrderDto(Order.Status.SHIPPED.toString());
        when(orderRepository.findById(NOT_EXISTING_ORDER_ID)).thenThrow(
                new EntityNotFoundException("Can't find order with id: " + NOT_EXISTING_ORDER_ID));

        //When
        assertThatThrownBy(() ->
                orderService.changeStatusOrder(NOT_EXISTING_ORDER_ID, updateOrderDto)
        ).isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find order with id: " + NOT_EXISTING_ORDER_ID);

        //Then
        verify(orderRepository, times(1)).findById(NOT_EXISTING_ORDER_ID);
        verify(orderRepository, never()).save(any(Order.class));
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderMapper);
    }
}
