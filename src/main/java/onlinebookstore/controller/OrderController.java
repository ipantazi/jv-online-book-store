package onlinebookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import onlinebookstore.dto.order.OrderDto;
import onlinebookstore.dto.order.OrderRequestDto;
import onlinebookstore.dto.order.UpdateOrderDto;
import onlinebookstore.dto.orderitem.OrderItemDto;
import onlinebookstore.model.User;
import onlinebookstore.service.order.OrderService;
import onlinebookstore.service.orderitem.OrderItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order and order item management.",
        description = "Endpoints of management orders and order items.")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final OrderItemService orderItemService;

    @PostMapping
    @PreAuthorize(value = "hasRole('ROLE_USER')")
    @ResponseStatus(value = HttpStatus.CREATED)
    @Operation(summary = "Add order.", description = "Place an order by the user "
            + "to be able to purchase books from their shopping cart.")
    public OrderDto addOrder(Authentication authentication,
                             @RequestBody @Valid OrderRequestDto orderRequestDto) {
        User user = (User) authentication.getPrincipal();
        return orderService.createOrder(user.getId(), orderRequestDto);
    }

    @GetMapping
    @PreAuthorize(value = "hasRole('ROLE_USER')")
    @Operation(summary = "Get orders.", description = "Retrieve user's order history.")
    public Page<OrderDto> getOrders(Authentication authentication, Pageable pageable) {
        User user = (User) authentication.getPrincipal();
        return orderService.getOrders(user.getId(), pageable);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update order status.", description = "Update order status.")
    public OrderDto updateOrder(@PathVariable("id") Long orderId,
                                @RequestBody @Valid UpdateOrderDto updateOrderDto) {
        return orderService.changeStatusOrder(orderId, updateOrderDto);
    }

    @GetMapping("/{orderId}/items")
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

    @GetMapping("/{orderId}/items/{itemId}")
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
