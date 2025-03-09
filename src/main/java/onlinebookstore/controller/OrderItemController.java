package onlinebookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import onlinebookstore.dto.orderitem.OrderItemDto;
import onlinebookstore.model.User;
import onlinebookstore.service.orderitem.OrderItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order item management.", description = "Endpoints of management order items.")
@RestController
@RequestMapping("/orders/{orderId}/items")
@RequiredArgsConstructor
public class OrderItemController {
    private final OrderItemService orderItemService;

    @GetMapping()
    @PreAuthorize(value = "hasRole('ROLE_USER')")
    @Operation(
            summary = "Get order items for a specific order.",
            description = "Retrieve all items for a specific order."
    )
    public Page<OrderItemDto> getOrderItems(Authentication authentication,
                                            Pageable pageable,
                                            @PathVariable long orderId) {
        User user = (User) authentication.getPrincipal();
        return orderItemService.getOrderItems(user.getId(), orderId, pageable);
    }

    @GetMapping("/{itemId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(
            summary = "Get a a specific order item",
            description = "Retrieve a specific order item within an order."
    )
    public OrderItemDto getOrderItem(Authentication authentication,
                                     @PathVariable long orderId,
                                     @PathVariable long itemId) {
        User user = (User) authentication.getPrincipal();
        return orderItemService.getOrderItem(user.getId(), orderId, itemId);
    }
}
