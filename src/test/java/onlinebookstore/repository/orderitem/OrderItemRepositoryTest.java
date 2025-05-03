package onlinebookstore.repository.orderitem;

import static onlinebookstore.util.TestDataUtil.EXISTING_ORDER_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_ORDER_ITEM_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_ORDER_ITEM_ID;
import static onlinebookstore.util.TestDataUtil.ORDER_ITEM_IGNORING_FIELDS;
import static onlinebookstore.util.TestDataUtil.createTestOrder;
import static onlinebookstore.util.TestDataUtil.createTestOrderItem;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertObjectsAreEqualIgnoringFields;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import onlinebookstore.model.Order;
import onlinebookstore.model.OrderItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {
        "classpath:database/users/add-test-users-to-users-table.sql",
        "classpath:database/orders/add-test-order-to-orders-table.sql",
        "classpath:database/books/add-test-books-to-books-table.sql",
        "classpath:database/orderitems/add-test-order-item-to-order-items-table.sql"
},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {
        "classpath:database/orderitems/clear-all-order-items.sql",
        "classpath:database/books/remove-test-books-from-books-table.sql",
        "classpath:database/orders/clear-all-orders.sql",
        "classpath:database/users/remove-test-users-from-users-table.sql"
},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
public class OrderItemRepositoryTest {
    @Autowired
    private OrderItemRepository repository;

    @Test
    @DisplayName("Find order item by id with their associated order entity.")
    void findById_GivenValidOrderItemsCatalog_ReturnsOrderItemWithRelatedEntity() {
        // Given
        Order order = createTestOrder(EXISTING_ORDER_ID);
        OrderItem expectedOrderItem = createTestOrderItem(EXISTING_ORDER_ITEM_ID, order);

        // When
        Optional<OrderItem> actualOrderItemOptional = repository.findById(EXISTING_ORDER_ITEM_ID);

        // Then
        assertThat(actualOrderItemOptional).isNotEmpty();
        assertObjectsAreEqualIgnoringFields(
                actualOrderItemOptional.get(),
                expectedOrderItem,
                ORDER_ITEM_IGNORING_FIELDS
        );
    }

    @Test
    @DisplayName("No finds order item by id.")
    void findById_NoExistingOrderById_ReturnsEmptyOptional() {
        // When
        Optional<OrderItem> actualOrderItemOptional = repository
                .findById(NOT_EXISTING_ORDER_ITEM_ID);

        // Then
        assertThat(actualOrderItemOptional).isEmpty();
    }
}
