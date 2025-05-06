package onlinebookstore.mapper;

import java.math.BigDecimal;
import onlinebookstore.config.MapperConfig;
import onlinebookstore.dto.order.OrderDto;
import onlinebookstore.model.Order;
import onlinebookstore.model.ShoppingCart;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class, uses = OrderItemMapper.class)
public interface OrderMapper {
    @Mapping(target = "userId", source = "user.id")
    OrderDto toOrderDto(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "shippingAddress", ignore = true)
    @Mapping(target = "orderItems", source = "cartItems")
    Order toOrderEntity(ShoppingCart shoppingCart);

    @AfterMapping
    default void setTotal(@MappingTarget Order order, ShoppingCart shoppingCart) {
        order.setTotal(shoppingCart.getCartItems().stream()
                .map(cartItem -> BigDecimal.valueOf(cartItem.getQuantity())
                        .multiply(cartItem.getBook().getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    @AfterMapping
    default void setCartItemOrder(@MappingTarget Order order) {
        order.getOrderItems().forEach(orderItem -> orderItem.setOrder(order));
    }
}
