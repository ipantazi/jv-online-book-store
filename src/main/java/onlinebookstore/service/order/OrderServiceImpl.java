package onlinebookstore.service.order;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
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
import onlinebookstore.service.shoppingcart.ShoppingCartService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ShoppingCartService shoppingCartService;
    private final ShoppingCartRepository shoppingCartRepository;

    @Override
    public OrderDto createOrder(Long userId, OrderRequestDto orderRequestDto) {
        String shippingAddress = orderRequestDto.shippingAddress();
        ShoppingCart shoppingCart = shoppingCartService.findShoppingCartByUserId(userId);

        if (shoppingCart.getCartItems().isEmpty()) {
            throw new OrderProcessingException("Shopping cart is empty");
        }
        Order order = orderMapper.toOrderEntity(shoppingCart);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress(shippingAddress);
        orderRepository.save(order);

        shoppingCart.getCartItems().clear();
        shoppingCartRepository.save(shoppingCart);

        return orderMapper.toOrderDto(order);
    }

    @Override
    public Page<OrderDto> getOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(orderMapper::toOrderDto);
    }

    @Override
    public OrderDto changeStatusOrder(Long orderId, UpdateOrderDto updateOrderDto) {
        Order order = orderRepository.findById(orderId).orElseThrow(() ->
                new EntityNotFoundException("Can't find order with id: " + orderId));
        order.setStatus(Order.Status.valueOfStatus(updateOrderDto.status()));
        return orderMapper.toOrderDto(orderRepository.save(order));
    }
}
