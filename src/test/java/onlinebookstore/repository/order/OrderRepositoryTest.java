package onlinebookstore.repository.order;

import static onlinebookstore.util.TestDataUtil.ALTERNATIVE_USER_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_ORDER_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_USER_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_USER_ID;
import static onlinebookstore.util.TestDataUtil.ORDER_IGNORING_FIELDS;
import static onlinebookstore.util.TestDataUtil.ORDER_PAGEABLE;
import static onlinebookstore.util.TestDataUtil.createTestOrder;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertCollectionsAreEqualIgnoringFields;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertPageMetadataEquals;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import onlinebookstore.model.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {
        "classpath:database/users/add-test-users-to-users-table.sql",
        "classpath:database/books/add-test-books-to-books-table.sql",
        "classpath:database/orders/add-test-order-to-orders-table.sql",
        "classpath:database/orderitems/add-test-order-item-to-order-items-table.sql"
},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {
        "classpath:database/orderitems/clear-all-order-items.sql",
        "classpath:database/orders/clear-all-orders.sql",
        "classpath:database/books/remove-test-books-from-books-table.sql",
        "classpath:database/users/remove-test-users-from-users-table.sql"
},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
public class OrderRepositoryTest {
    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("Find the order by user id with their associated orderItem and book entities.")
    void findByUserId_ValidOrderByUserId_ReturnsOrder() {
        // Given
        List<Order> orders = Collections.singletonList(createTestOrder(EXISTING_ORDER_ID));
        Page<Order> expectedOrdersPage = new PageImpl<>(orders, ORDER_PAGEABLE, orders.size());

        // When
        Page<Order> actualOrdersPage = orderRepository.findByUserId(
                EXISTING_USER_ID,
                ORDER_PAGEABLE
        );

        // Then
        assertThat(actualOrdersPage).isNotEmpty();
        assertPageMetadataEquals(actualOrdersPage, expectedOrdersPage);
        assertCollectionsAreEqualIgnoringFields(
                actualOrdersPage.getContent(),
                expectedOrdersPage.getContent(),
                ORDER_IGNORING_FIELDS);
    }

    @Test
    @DisplayName("No finds order by user id.")
    void findByUserId_NoExistingOrderByUserId_ReturnsEmptyOptional() {
        // Given
        Page<Order> expectedOrdersPage = Page.empty(ORDER_PAGEABLE);

        // When
        Page<Order> actualOrderPage = orderRepository.findByUserId(
                ALTERNATIVE_USER_ID,
                ORDER_PAGEABLE
        );

        // Then
        assertThat(actualOrderPage).isEmpty();
        assertPageMetadataEquals(actualOrderPage, expectedOrdersPage);
    }

    @Test
    @DisplayName("No finds order by not existing user id.")
    void findByUserId_NotExistingUserId_ReturnsEmptyOptional() {
        // Given
        Page<Order> expectedOrdersPage = Page.empty(ORDER_PAGEABLE);

        // When
        Page<Order> actualOrderPage = orderRepository.findByUserId(
                NOT_EXISTING_USER_ID,
                ORDER_PAGEABLE
        );

        // Then
        assertThat(actualOrderPage).isEmpty();
        assertPageMetadataEquals(actualOrderPage, expectedOrdersPage);
    }
}
