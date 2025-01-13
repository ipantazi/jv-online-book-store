package onlinebookstore.repository.book;

import onlinebookstore.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    @Query(value = "SELECT COUNT(*) > 0 FROM books WHERE isbn = :isbn", nativeQuery = true)
    Long existsByIsbnIncludingDeleted(@Param("isbn") String isbn);
}
