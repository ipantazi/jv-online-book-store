package onlinebookstore.repository.book;

import static onlinebookstore.util.TestDataUtil.ALTERNATIVE_CATEGORY_ID;
import static onlinebookstore.util.TestDataUtil.BOOK_IGNORING_FIELDS;
import static onlinebookstore.util.TestDataUtil.BOOK_PAGEABLE;
import static onlinebookstore.util.TestDataUtil.EXISTING_BOOK_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_CATEGORY_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_ISBN;
import static onlinebookstore.util.TestDataUtil.EXPECTED_BOOKS_SIZE;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_ISBN;
import static onlinebookstore.util.TestDataUtil.SOFT_DELETED_BOOK_ISBN;
import static onlinebookstore.util.TestDataUtil.createTestBookList;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertCollectionsAreEqualIgnoringFields;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertPageMetadataEquals;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import onlinebookstore.model.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
        // Given
        Long expectedStatus = 1L;

        // When
        Long actualStatus = bookRepository.existsByIsbnIncludingDeleted(EXISTING_ISBN);

        // Then
        assertThat(actualStatus).isEqualTo(expectedStatus);
    }

    @Test
    @DisplayName("Check for book existence by ISBN when a book is safe deleted.")
    void existsByIsbnIncludingDeleted_IsExistBookByIsbnWhenIsDeleted_ReturnsLong() {
        // Given
        Long expectedStatus = 1L;

        // When
        Long actualStatus = bookRepository.existsByIsbnIncludingDeleted(SOFT_DELETED_BOOK_ISBN);

        // Then
        assertThat(actualStatus).isEqualTo(expectedStatus);
    }

    @Test
    @DisplayName("Check for book not existence dy ISBN, including safe deleted books.")
    void existsByIsbnIncludingDeleted_IsNotExistBookByIsbnIncludingDeleted_ReturnsLong() {
        // Given
        Long expectedStatus = 0L;

        // When
        Long actualStatus = bookRepository.existsByIsbnIncludingDeleted(NOT_EXISTING_ISBN);

        // Then
        assertThat(actualStatus).isEqualTo(expectedStatus);
    }

    @Test
    @DisplayName("Find all books by category id.")
    void findAllByCategoryId_ValidBooksByCategoryId_ReturnsAllBooks() {
        // Given
        List<Book> expectedBooks = createTestBookList(EXISTING_BOOK_ID, EXPECTED_BOOKS_SIZE);
        Page<Book> expectedBooksPage = new PageImpl<>(
                expectedBooks,
                BOOK_PAGEABLE,
                expectedBooks.size()
        );

        // When
        Page<Book> actualBooksPage = bookRepository.findAllByCategoryId(
                EXISTING_CATEGORY_ID,
                BOOK_PAGEABLE
        );

        // Then
        assertPageMetadataEquals(actualBooksPage, expectedBooksPage);
        assertCollectionsAreEqualIgnoringFields(
                actualBooksPage.getContent(),
                expectedBooksPage.getContent(),
                BOOK_IGNORING_FIELDS
        );
    }

    @Test
    @DisplayName("Find all books by category id when no books exist in that category.")
    void findAllByCategoryId_NoBooksByCategoryId_ReturnsEmptyPage() {
        // Given
        Page<Book> expectedBooksPage = Page.empty(BOOK_PAGEABLE);

        // When
        Page<Book> actualBooksPage = bookRepository.findAllByCategoryId(
                ALTERNATIVE_CATEGORY_ID,
                BOOK_PAGEABLE
        );

        // Then
        assertPageMetadataEquals(actualBooksPage, expectedBooksPage);
        assertThat(actualBooksPage.getContent()).isEmpty();
    }
}
