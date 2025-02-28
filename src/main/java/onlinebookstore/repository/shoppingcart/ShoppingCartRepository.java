package onlinebookstore.repository.shoppingcart;

import java.util.Optional;
import lombok.NonNull;
import onlinebookstore.model.ShoppingCart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    @Query("""
            SELECT DISTINCT sc FROM ShoppingCart sc
            LEFT JOIN FETCH sc.cartItems ci
            LEFT JOIN FETCH ci.book
            WHERE sc.isDeleted = false AND sc.user.email = :email
            """)
    Optional<ShoppingCart> findByEmailAndWithoutUser(@Param("email") String email);

    @EntityGraph(attributePaths = {"user", "cartItems"})
    @NonNull
    Optional<ShoppingCart> findByUserEmail(@NonNull String email);
}
