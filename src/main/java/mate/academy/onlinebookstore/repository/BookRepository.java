package mate.academy.onlinebookstore.repository;

import mate.academy.onlinebookstore.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
