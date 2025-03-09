package onlinebookstore.service.orderitem;

import onlinebookstore.dto.orderitem.OrderItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderItemService {
    Page<OrderItemDto> getOrderItems(Long userId, Long orderId, Pageable pageable);

    OrderItemDto getOrderItem(Long userId, Long orderId, Long itemId);
}
