package onlinebookstore.service.order;

import onlinebookstore.dto.order.OrderDto;
import onlinebookstore.dto.order.OrderRequestDto;
import onlinebookstore.dto.order.UpdateOrderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDto createOrder(Long userId, OrderRequestDto orderRequestDto);

    Page<OrderDto> getOrders(Long userId, Pageable pageable);

    OrderDto changeStatusOrder(Long orderId, UpdateOrderDto updateOrderDto);
}
