package onlinebookstore.repository.shoppingcart;

import static onlinebookstore.util.TestDataUtil.ALTERNATIVE_USER_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_USER_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_USER_ID;
import static onlinebookstore.util.TestDataUtil.SHOPPING_CART_IGNORING_FIELDS;
import static onlinebookstore.util.TestDataUtil.createTestShoppingCart;
import static onlinebookstore.util.TestDataUtil.createTestShoppingCartDto;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertObjectsAreEqualIgnoringFields;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import onlinebookstore.dto.shoppingcart.ShoppingCartDto;
import onlinebookstore.model.ShoppingCart;
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
        "classpath:database/carts/add-test-shoppingcart-to-shoppingcarts-table.sql",
        "classpath:database/books/add-test-books-to-books-table.sql",
        "classpath:database/carts/add-test-cartitems-to-cartitems-table.sql"
},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {
        "classpath:database/carts/remove-test-cartitems-from-cartitems-table.sql",
        "classpath:database/books/remove-test-books-from-books-table.sql",
        "classpath:database/carts/remove-test-shoppingcart-from-shoppingcarts-table.sql",
        "classpath:database/users/remove-test-users-from-users-table.sql"
},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
public class ShoppingCartRepositoryTest {
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Test
    @DisplayName("Find shoppingCart by userId with their associated cartItem and book entities.")
    void findByUserId_GivenValidShoppingCart_ReturnsShoppingCartWithRelatedEntities() {
        // Given
        ShoppingCartDto testShoppingCartDto = createTestShoppingCartDto(EXISTING_USER_ID);
        ShoppingCart expectedShoppingCart = createTestShoppingCart(testShoppingCartDto);

        // When
        Optional<ShoppingCart> actualCartOpt = shoppingCartRepository
                .findByUserId(EXISTING_USER_ID);

        // Then
        assertThat(actualCartOpt).isNotEmpty();
        assertObjectsAreEqualIgnoringFields(
                actualCartOpt.get(),
                expectedShoppingCart,
                SHOPPING_CART_IGNORING_FIELDS
        );
    }

    @Test
    @DisplayName("No finds shoppingCart by userId.")
    void findByUserId_NoExistingShoppingCartByUserId_ReturnsEmptyOptional() {
        // When
        Optional<ShoppingCart> actualOptionalCart = shoppingCartRepository
                .findByUserId(ALTERNATIVE_USER_ID);

        // Then
        assertThat(actualOptionalCart).isEmpty();
    }

    @Test
    @DisplayName("No finds shoppingCart by invalid userId.")
    void findByUserId_InvalidUserId_ReturnsEmptyOptional() {
        // When
        Optional<ShoppingCart> actualCartOpt = shoppingCartRepository
                .findByUserId(NOT_EXISTING_USER_ID);

        // Then
        assertThat(actualCartOpt).isEmpty();
    }
}
