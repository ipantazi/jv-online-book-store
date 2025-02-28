package onlinebookstore.repository.user;

import java.util.List;
import java.util.Optional;
import onlinebookstore.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

@Repository
@EnableJpaRepositories(basePackageClasses = UserRepository.class)
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    @Query("""
            SELECT u FROM User u
            LEFT JOIN ShoppingCart sc ON u.id = sc.id
            WHERE u.isDeleted = false AND sc.id IS NULL
            """)
    List<User> findAllUsersWithoutShoppingCart();
}
