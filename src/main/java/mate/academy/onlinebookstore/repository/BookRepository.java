package mate.academy.onlinebookstore.repository;

import java.util.List;
import mate.academy.onlinebookstore.model.Book;

public interface BookRepository {
    Book save(Book book);

    List<Book> findAll();
}
