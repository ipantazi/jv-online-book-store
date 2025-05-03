package onlinebookstore.service.orderitem;

import static onlinebookstore.util.TestDataUtil.EXISTING_ORDER_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_ORDER_ITEM_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_USER_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_ORDER_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_ORDER_ITEM_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_USER_ID;
import static onlinebookstore.util.TestDataUtil.ORDER_ITEM_DTO_IGNORING_FIELDS;
import static onlinebookstore.util.TestDataUtil.ORDER_ITEM_PAGEABLE;
import static onlinebookstore.util.TestDataUtil.createTestOrder;
import static onlinebookstore.util.TestDataUtil.createTestOrderItem;
import static onlinebookstore.util.TestDataUtil.createTestOrderItemDto;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertObjectsAreEqualIgnoringFields;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertPageMetadataEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import onlinebookstore.dto.orderitem.OrderItemDto;
import onlinebookstore.exception.EntityNotFoundException;
import onlinebookstore.mapper.OrderItemMapper;
import onlinebookstore.model.Order;
import onlinebookstore.model.OrderItem;
import onlinebookstore.repository.order.OrderRepository;
import onlinebookstore.repository.orderitem.OrderItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
public class OrderItemServiceTest {
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemMapper orderItemMapper;
    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    @Test
    @DisplayName("Verify getOrderItems() method works.")
    public void getOrderItems_ValidUserIdAndOrderId_ReturnsAllOrderItemDtos() {
        //Given
        OrderItem orderItem = createTestOrderItem(EXISTING_ORDER_ITEM_ID, new Order());
        OrderItemDto expectedOrderItemDto = createTestOrderItemDto(EXISTING_ORDER_ITEM_ID);
        List<OrderItem> orderItems = Collections.singletonList(orderItem);
        Page<OrderItem> orderItemPage = new PageImpl<>(
                orderItems, ORDER_ITEM_PAGEABLE, orderItems.size());
        when(orderRepository.existsByIdAndUserId(EXISTING_USER_ID, EXISTING_ORDER_ID))
                .thenReturn(true);
        when(orderItemRepository.findAllByOrderId(EXISTING_ORDER_ID, ORDER_ITEM_PAGEABLE))
                .thenReturn(orderItemPage);
        when(orderItemMapper.toOrderItemDto(orderItem))
                .thenReturn(expectedOrderItemDto);

        //When
        Page<OrderItemDto> actualOrderItemPage = orderItemService.getOrderItems(
                EXISTING_USER_ID, EXISTING_ORDER_ID, ORDER_ITEM_PAGEABLE);

        //Then
        assertThat(actualOrderItemPage.getContent())
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(expectedOrderItemDto);
        assertPageMetadataEquals(actualOrderItemPage, orderItemPage);
        verify(orderRepository, times(1))
                .existsByIdAndUserId(EXISTING_USER_ID, EXISTING_ORDER_ID);
        verify(orderItemRepository, times(1))
                .findAllByOrderId(EXISTING_ORDER_ID, ORDER_ITEM_PAGEABLE);
        verify(orderItemMapper, times(1)).toOrderItemDto(orderItem);
        verifyNoMoreInteractions(orderRepository, orderItemRepository, orderItemMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a user id doesn't exists.")
    public void getOrderItems_InvalidUserId_ThrowsException() {
        //Given
        when(orderRepository.existsByIdAndUserId(EXISTING_ORDER_ID, NOT_EXISTING_USER_ID))
                .thenReturn(Boolean.FALSE);

        //When
        assertThatThrownBy(() -> orderItemService.getOrderItems(
                NOT_EXISTING_USER_ID, EXISTING_ORDER_ID, ORDER_ITEM_PAGEABLE))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find order with id: " + EXISTING_ORDER_ID
                        + " for user with id: " + NOT_EXISTING_USER_ID);

        //Then
        verify(orderRepository, times(1))
                .existsByIdAndUserId(EXISTING_ORDER_ID, NOT_EXISTING_USER_ID);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderItemRepository, orderItemMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when an order id doesn't exists.")
    public void getOrderItems_InvalidOrderId_ThrowsException() {
        //Given
        when(orderRepository.existsByIdAndUserId(NOT_EXISTING_ORDER_ID, EXISTING_USER_ID))
                .thenReturn(Boolean.FALSE);

        //When
        assertThatThrownBy(() -> orderItemService.getOrderItems(
                EXISTING_USER_ID, NOT_EXISTING_ORDER_ID, ORDER_ITEM_PAGEABLE))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find order with id: " + NOT_EXISTING_ORDER_ID
                        + " for user with id: " + EXISTING_USER_ID);

        //Then
        verify(orderRepository, times(1))
                .existsByIdAndUserId(NOT_EXISTING_ORDER_ID, EXISTING_USER_ID);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderItemRepository, orderItemMapper);
    }

    @Test
    @DisplayName("Verify getOrderItem() method works.")
    public void getOrderItem_ValidUserIdAndOrderIdAndItemId_ReturnsOrderItemDto() {
        //Given
        OrderItemDto expectedOrderItemDto = createTestOrderItemDto(EXISTING_ORDER_ITEM_ID);
        Order order = createTestOrder(EXISTING_ORDER_ID);
        OrderItem orderItem = createTestOrderItem(expectedOrderItemDto, order);
        when(orderItemRepository.findById(orderItem.getId())).thenReturn(Optional.of(orderItem));
        when(orderItemMapper.toOrderItemDto(orderItem)).thenReturn(expectedOrderItemDto);

        //When
        OrderItemDto actualOrderItemDto = orderItemService.getOrderItem(
                EXISTING_USER_ID, EXISTING_ORDER_ID, EXISTING_ORDER_ITEM_ID);

        //Then
        assertObjectsAreEqualIgnoringFields(
                actualOrderItemDto,
                expectedOrderItemDto,
                ORDER_ITEM_DTO_IGNORING_FIELDS
        );
        verify(orderItemRepository, times(1)).findById(EXISTING_ORDER_ITEM_ID);
        verify(orderItemMapper, times(1)).toOrderItemDto(orderItem);
        verifyNoMoreInteractions(orderItemRepository, orderItemMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when an order item id doesn't exists.")
    public void getOrderItem_InvalidOrderItemId_ThrowsException() {
        //Given
        when(orderItemRepository.findById(NOT_EXISTING_ORDER_ITEM_ID)).thenReturn(Optional.empty());

        //When
        assertThatThrownBy(() -> orderItemService.getOrderItem(
                EXISTING_USER_ID, EXISTING_ORDER_ID, NOT_EXISTING_ORDER_ITEM_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find item with id: " + NOT_EXISTING_ORDER_ITEM_ID);

        //Then
        verify(orderItemRepository, times(1)).findById(NOT_EXISTING_ORDER_ITEM_ID);
        verifyNoMoreInteractions(orderItemRepository);
        verifyNoInteractions(orderItemMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when an order id doesn't exists.")
    public void getOrderItem_InvalidOrderId_ThrowsException() {
        //Given
        Order order = createTestOrder(EXISTING_ORDER_ID);
        OrderItem orderItem = createTestOrderItem(EXISTING_ORDER_ITEM_ID, order);
        when(orderItemRepository.findById(orderItem.getId())).thenReturn(Optional.of(orderItem));

        //When
        assertThatThrownBy(() -> orderItemService.getOrderItem(
                EXISTING_USER_ID, NOT_EXISTING_ORDER_ID, EXISTING_ORDER_ITEM_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find item with order id: " + NOT_EXISTING_ORDER_ID
                        + " for user with id: " + EXISTING_USER_ID);

        //Then
        verify(orderItemRepository, times(1)).findById(orderItem.getId());
        verifyNoMoreInteractions(orderItemRepository);
        verifyNoInteractions(orderItemMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a user id doesn't exists.")
    public void getOrderItem_InvalidUserId_ThrowsException() {
        //Given
        Order order = createTestOrder(EXISTING_ORDER_ID);
        OrderItem orderItem = createTestOrderItem(EXISTING_ORDER_ITEM_ID, order);
        when(orderItemRepository.findById(orderItem.getId())).thenReturn(Optional.of(orderItem));

        //When
        assertThatThrownBy(() -> orderItemService.getOrderItem(
                NOT_EXISTING_USER_ID, EXISTING_ORDER_ID, EXISTING_ORDER_ITEM_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find item with order id: " + EXISTING_ORDER_ID
                        + " for user with id: " + NOT_EXISTING_USER_ID);

        //Then
        verify(orderItemRepository, times(1)).findById(orderItem.getId());
        verifyNoMoreInteractions(orderItemRepository);
        verifyNoInteractions(orderItemMapper);
    }
}
