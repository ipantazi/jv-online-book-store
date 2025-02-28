package onlinebookstore.mapper;

import onlinebookstore.config.MapperConfig;
import onlinebookstore.dto.cartitem.CartItemDto;
import onlinebookstore.dto.cartitem.CreateCartItemDto;
import onlinebookstore.model.CartItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface CartItemMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shoppingCart", ignore = true)
    @Mapping(target = "book", ignore = true)
    CartItem toCartItemEntity(CreateCartItemDto createCartItemDto);

    @Mapping(target = "bookId", ignore = true)
    @Mapping(target = "bookTitle", ignore = true)
    CartItemDto toCartItemDto(CartItem cartItem);

    @AfterMapping
    default void setBookInfo(@MappingTarget CartItemDto cartItemDto, CartItem cartItem) {
        if (cartItem.getBook() != null) {
            cartItemDto.setBookId(cartItem.getBook().getId());
            cartItemDto.setBookTitle(cartItem.getBook().getTitle());
        }
    }
}
