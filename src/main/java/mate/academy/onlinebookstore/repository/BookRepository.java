package mate.academy.onlinebookstore.repository;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import mate.academy.onlinebookstore.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE Book SET title = :title, author = :author, isbn = :isbn, "
            + "price = :price, description = :description, coverImage = :coverImage WHERE id = :id "
            + "AND deleted = false")
    int updateBookById(@Param("id") Long id,
                        @Param("title") String title,
                        @Param("author") String author,
                        @Param("isbn") String isbn,
                        @Param("price") BigDecimal price,
                        @Param("description") String description,
                        @Param("coverImage") String coverImage);
}
