package onlinebookstore.mapper;

import onlinebookstore.config.MapperConfig;
import onlinebookstore.dto.orderitem.OrderItemDto;
import onlinebookstore.model.CartItem;
import onlinebookstore.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface OrderItemMapper {
    @Mapping(target = "bookId", source = "book.id")
    OrderItemDto toOrderItemDto(OrderItem orderItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "price", source = "book.price")
    OrderItem toOrderItemEntity(CartItem cartItem);
}
