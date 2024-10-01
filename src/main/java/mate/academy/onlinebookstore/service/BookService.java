package mate.academy.onlinebookstore.service;

import java.util.List;
import mate.academy.onlinebookstore.model.Book;

public interface BookService {
    Book save(Book book);

    List<Book> findAll();
}
