package onlinebookstore.dto.orderitem;

public record OrderItemDto(
        Long id,
        Long bookId,
        int quantity
) {
}
