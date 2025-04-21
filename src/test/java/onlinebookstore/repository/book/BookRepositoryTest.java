package onlinebookstore.repository.book;

import static onlinebookstore.util.TestDataUtil.ALTERNATIVE_CATEGORY_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_CATEGORY_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_ISBN;
import static onlinebookstore.util.TestDataUtil.EXPECTED_BOOK_DTOS_SIZE;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_ISBN;
import static onlinebookstore.util.TestDataUtil.SOFT_DELETED_BOOK_ISBN;
import static onlinebookstore.util.TestDataUtil.pageableBook;
import static org.assertj.core.api.Assertions.assertThat;

import onlinebookstore.model.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {
        "classpath:database/books/add-test-books-to-books-table.sql",
        "classpath:database/categories/add-test-category-to-categories-table.sql",
        "classpath:database/bookscategories/add-test-dependencies-to-books-categories-table.sql"
},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {
        "classpath:database/bookscategories/remove-test-dependencies-from-books-categories.sql",
        "classpath:database/categories/remove-test-categories-from-categories-table.sql",
        "classpath:database/books/remove-test-books-from-books-table.sql"
},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class BookRepositoryTest {
    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("Check for book existence by ISBN, including safe deleted books.")
    void existsByIsbnIncludingDeleted_IsExistBookByIsbnIncludingDeleted_ReturnsLong() {
        Long expectedStatus = 1L;

        Long actualStatus = bookRepository.existsByIsbnIncludingDeleted(EXISTING_ISBN);

        assertThat(actualStatus).isEqualTo(expectedStatus);
    }

    @Test
    @DisplayName("Check for book existence by ISBN when a book is safe deleted.")
    void existsByIsbnIncludingDeleted_IsExistBookByIsbnWhenIsDeleted_ReturnsLong() {
        Long expectedStatus = 1L;

        Long actualStatus = bookRepository.existsByIsbnIncludingDeleted(SOFT_DELETED_BOOK_ISBN);

        assertThat(actualStatus).isEqualTo(expectedStatus);
    }

    @Test
    @DisplayName("Check for book not existence dy ISBN, including safe deleted books.")
    void existsByIsbnIncludingDeleted_IsNotExistBookByIsbnIncludingDeleted_ReturnsLong() {
        Long expectedStatus = 0L;

        Long actualStatus = bookRepository.existsByIsbnIncludingDeleted(NOT_EXISTING_ISBN);

        assertThat(actualStatus).isEqualTo(expectedStatus);
    }

    @Test
    @DisplayName("Find all books by category id.")
    void findAllByCategoryId_ValidBooksByCategoryId_ReturnsAllBooks() {
        Page<Book> actualPageBooks = bookRepository.findAllByCategoryId(EXISTING_CATEGORY_ID,
                pageableBook);

        assertThat(actualPageBooks.getSort()).isEqualTo(pageableBook.getSort());
        assertThat(actualPageBooks.getNumber()).isEqualTo(pageableBook.getPageNumber());
        assertThat(actualPageBooks.getSize()).isEqualTo(pageableBook.getPageSize());
        assertThat(actualPageBooks.getTotalElements()).isEqualTo(EXPECTED_BOOK_DTOS_SIZE);
    }

    @Test
    @DisplayName("Find all books by category id when no books exist in that category.")
    void findAllByCategoryId_NoBooksByCategoryId_ReturnsEmptyPage() {
        Page<Book> actualPageBooks = bookRepository.findAllByCategoryId(ALTERNATIVE_CATEGORY_ID,
                pageableBook);

        assertThat(actualPageBooks.getSort()).isEqualTo(pageableBook.getSort());
        assertThat(actualPageBooks.getNumber()).isEqualTo(pageableBook.getPageNumber());
        assertThat(actualPageBooks.getSize()).isEqualTo(pageableBook.getPageSize());
        assertThat(actualPageBooks.getTotalElements()).isEqualTo(0);
        assertThat(actualPageBooks.getContent()).isEmpty();
    }
}
