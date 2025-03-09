package onlinebookstore.repository.orderitem;

import java.util.Optional;
import onlinebookstore.model.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    Page<OrderItem> findAllByOrderId(Long orderId, Pageable pageable);

    @EntityGraph(attributePaths = "order")
    Optional<OrderItem> findById(Long id);
}
