package onlinebookstore.repository.book;

import java.util.List;
import java.util.Optional;
import onlinebookstore.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    @Query(value = "SELECT COUNT(*) > 0 FROM books WHERE isbn = :isbn", nativeQuery = true)
    Long existsByIsbnIncludingDeleted(@Param("isbn") String isbn);

    @Query("SELECT b FROM Book b JOIN b.categories c WHERE c.id = :categoryId")
    Page<Book> findAllByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "categories")
    Page<Book> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "categories")
    List<Book> findAll(Specification<Book> bookSpecification);

    @EntityGraph(attributePaths = "categories")
    Optional<Book> findById(Long id);
}
