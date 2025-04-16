package onlinebookstore.repository.book;

import static org.assertj.core.api.Assertions.assertThat;

import onlinebookstore.model.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {
        "classpath:database/products/add-test-books-to-books-table.sql",
        "classpath:database/products/add-test-category-to-categories-table.sql",
        "classpath:database/products/add-test-dependencies-to-books-categories-table.sql"
},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {
        "classpath:database/products/remove-test-dependencies-from-books-categories.sql",
        "classpath:database/products/remove-test-category-from-categories-table.sql",
        "classpath:database/products/remove-test-books-from-books-table.sql"
},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class BookRepositoryTest {
    private static final int PAGE_NUMBER = 0;
    private static final int PAGE_SIZE = 10;

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("Check for book existence by ISBN, including safe deleted books.")
    void existsByIsbnIncludingDeleted_IsExistBookByIsbnIncludingDeleted_ReturnsLong() {
        Long expectedStatus = 1L;
        String existingIsbn = "1000000000101";

        Long actualStatus = bookRepository.existsByIsbnIncludingDeleted(existingIsbn);

        assertThat(actualStatus).isEqualTo(expectedStatus);
    }

    @Test
    @DisplayName("Check for book existence by ISBN when a book is safe deleted.")
    void existsByIsbnIncludingDeleted_IsExistBookByIsbnWhenIsDeleted_ReturnsLong() {
        Long expectedStatus = 1L;
        String existingIsbn = "1000000000104";

        Long actualStatus = bookRepository.existsByIsbnIncludingDeleted(existingIsbn);

        assertThat(actualStatus).isEqualTo(expectedStatus);
    }

    @Test
    @DisplayName("Check for book not existence dy ISBN, including safe deleted books.")
    void existsByIsbnIncludingDeleted_IsNotExistBookByIsbnIncludingDeleted_ReturnsLong() {
        Long expectedStatus = 0L;
        String notExistingIsbn = "9999999999999";

        Long actualStatus = bookRepository.existsByIsbnIncludingDeleted(notExistingIsbn);

        assertThat(actualStatus).isEqualTo(expectedStatus);
    }

    @Test
    @DisplayName("Find all books by category id.")
    void findAllByCategoryId_ValidBooksByCategoryId_ReturnsAllBooks() {
        Long categoryId = 101L;
        int expectedSize = 3;
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE, Sort.by("author").ascending());

        Page<Book> actualPageBooks = bookRepository.findAllByCategoryId(categoryId, pageable);

        assertThat(actualPageBooks.getSort()).isEqualTo(pageable.getSort());
        assertThat(actualPageBooks.getNumber()).isEqualTo(pageable.getPageNumber());
        assertThat(actualPageBooks.getSize()).isEqualTo(pageable.getPageSize());
        assertThat(actualPageBooks.getTotalElements()).isEqualTo(expectedSize);
    }

    @Test
    @DisplayName("Find all books by category id when no books exist in that category.")
    void findAllByCategoryId_NoBooksByCategoryId_ReturnsEmptyPage() {
        Long categoryId = 102L;
        int expectedSize = 0;
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE, Sort.by("author").ascending());

        Page<Book> actualPageBooks = bookRepository.findAllByCategoryId(categoryId, pageable);

        assertThat(actualPageBooks.getSort()).isEqualTo(pageable.getSort());
        assertThat(actualPageBooks.getNumber()).isEqualTo(pageable.getPageNumber());
        assertThat(actualPageBooks.getSize()).isEqualTo(pageable.getPageSize());
        assertThat(actualPageBooks.getTotalElements()).isEqualTo(expectedSize);
        assertThat(actualPageBooks.getContent()).isEmpty();
    }
}
