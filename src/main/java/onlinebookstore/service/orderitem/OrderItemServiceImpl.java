package onlinebookstore.service.orderitem;

import lombok.RequiredArgsConstructor;
import onlinebookstore.dto.orderitem.OrderItemDto;
import onlinebookstore.exception.EntityNotFoundException;
import onlinebookstore.mapper.OrderItemMapper;
import onlinebookstore.model.Order;
import onlinebookstore.model.OrderItem;
import onlinebookstore.repository.order.OrderRepository;
import onlinebookstore.repository.orderitem.OrderItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemMapper orderItemMapper;

    @Override
    public Page<OrderItemDto> getOrderItems(Long userId, Long orderId, Pageable pageable) {
        if (orderRepository.existsByIdAndUserId(orderId, userId)) {
            return orderItemRepository.findAllByOrderId(orderId, pageable)
                    .map(orderItemMapper::toOrderItemDto);
        }
        throw new EntityNotFoundException("Can't find order with id: " + orderId
                + " for user with id: " + userId);
    }

    @Override
    public OrderItemDto getOrderItem(Long userId, Long orderId, Long itemId) {
        OrderItem orderItem = orderItemRepository.findById(itemId).orElseThrow(() ->
                new EntityNotFoundException("Can't find item with id: " + itemId));
        Order order = orderItem.getOrder();
        if (order.getId().equals(orderId) && order.getUser().getId().equals(userId)) {
            return orderItemMapper.toOrderItemDto(orderItem);
        }
        throw new EntityNotFoundException("Can't find item with order id: " + orderId
                + " for user with id: " + userId);
    }
}
