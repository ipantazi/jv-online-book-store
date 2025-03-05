package onlinebookstore.repository.cartitem;

import java.util.Optional;
import onlinebookstore.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByIdAndShoppingCartId(Long itemId, Long cartId);

    Optional<CartItem> findByBookId(Long bookId);
}
