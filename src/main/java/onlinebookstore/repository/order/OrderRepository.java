package onlinebookstore.repository.order;

import java.util.Optional;
import onlinebookstore.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @EntityGraph(attributePaths = {"orderItems", "orderItems.book"})
    Page<Order> findByUserId(Long userId, Pageable pageable);

    Optional<Order> findById(Long id);

    boolean existsByIdAndUserId(Long id, Long userId);
}
