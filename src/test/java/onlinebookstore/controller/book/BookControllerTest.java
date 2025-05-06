package onlinebookstore.controller.book;

import static onlinebookstore.util.TestDataUtil.BOOK_DTO_IGNORING_FIELDS;
import static onlinebookstore.util.TestDataUtil.BOOK_PAGEABLE;
import static onlinebookstore.util.TestDataUtil.EXISTING_BOOK_ID;
import static onlinebookstore.util.TestDataUtil.EXPECTED_BOOKS_SIZE;
import static onlinebookstore.util.TestDataUtil.INVALID_FORMAT_ISBN;
import static onlinebookstore.util.TestDataUtil.NEW_BOOK_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_BOOK_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_ISBN;
import static onlinebookstore.util.TestDataUtil.SAFE_DELETED_BOOK_ID;
import static onlinebookstore.util.TestDataUtil.TEST_LONG_DATA;
import static onlinebookstore.util.TestDataUtil.createTestBookDto;
import static onlinebookstore.util.TestDataUtil.createTestBookDtoList;
import static onlinebookstore.util.TestDataUtil.createTestBookRequestDto;
import static onlinebookstore.util.TestDataUtil.createTestInvalidBookDto;
import static onlinebookstore.util.TestDataUtil.createTestUpdatedBookDto;
import static onlinebookstore.util.TestDataUtil.fillCategoryCache;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertCollectionsAreEqualIgnoringFields;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertObjectsAreEqualIgnoringFields;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertValidationError;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertValidationErrorList;
import static onlinebookstore.util.controller.ControllerTestDataUtil.EXPECTED_BOOK_ERRORS;
import static onlinebookstore.util.controller.ControllerTestDataUtil.EXPECTED_BOOK_NULL_ERRORS;
import static onlinebookstore.util.controller.ControllerTestDataUtil.EXPECTED_SEARCH_ERROR_MESSAGES;
import static onlinebookstore.util.controller.ControllerTestDataUtil.NOT_FOUND;
import static onlinebookstore.util.controller.ControllerTestDataUtil.NO_CONTENT;
import static onlinebookstore.util.controller.ControllerTestDataUtil.UNPROCESSABLE_ENTITY;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_BOOKS;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_BOOKS_EXISTING_BOOK_ID;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_BOOKS_NOT_EXISTING_BOOK_ID;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_BOOKS_SAFE_DELETED_BOOK_ID;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_BOOKS_SEARCH;
import static onlinebookstore.util.controller.ControllerTestUtil.createRequestWithPageable;
import static onlinebookstore.util.controller.ControllerTestUtil.parsePageContent;
import static onlinebookstore.util.controller.ControllerTestUtil.parseResponseToList;
import static onlinebookstore.util.controller.ControllerTestUtil.parseResponseToObject;
import static onlinebookstore.util.controller.DatabaseTestUtil.executeSqlScript;
import static onlinebookstore.util.controller.MockMvcUtil.buildMockMvc;
import static onlinebookstore.util.controller.MvcTestHelper.createJsonMvcResult;
import static onlinebookstore.util.controller.MvcTestHelper.createMvcResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import onlinebookstore.dto.book.BookDto;
import onlinebookstore.dto.book.BookSearchParametersDto;
import onlinebookstore.dto.book.CreateBookRequestDto;
import onlinebookstore.repository.category.CategoryRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookControllerTest {
    protected static MockMvc mockMvc;
    private static List<BookDto> expectedBookDtos;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired DataSource dataSource,
                          @Autowired WebApplicationContext applicationContext,
                          @Autowired CategoryRepository categoryRepository) {
        mockMvc = buildMockMvc(applicationContext);

        teardown(dataSource);
        executeSqlScript(dataSource,
                    "database/books/add-test-books-to-books-table.sql",
                    "database/categories/add-test-category-to-categories-table.sql",
                    "database/bookscategories/add-test-dependencies-to-books-categories-table.sql");

        fillCategoryCache(categoryRepository);
        expectedBookDtos = createTestBookDtoList(EXISTING_BOOK_ID, EXPECTED_BOOKS_SIZE);
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        executeSqlScript(dataSource,
                    "database/bookscategories/clear-all-book-category-dependencies.sql",
                    "database/books/clear-all-books.sql",
                    "database/categories/clear-all-categories.sql");
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("All books must be returned except those safe deleted.")
    void getAll_GivenBooksCatalog_ShouldReturnAllBooks() throws Exception {
        // When
        MvcResult result = createMvcResult(
                mockMvc,
                createRequestWithPageable(URL_BOOKS, BOOK_PAGEABLE),
                status().isOk());

        // Then
        List<BookDto> actualBookDtos = parsePageContent(
                result,
                objectMapper,
                new TypeReference<List<BookDto>>() {}
        );
        assertCollectionsAreEqualIgnoringFields(
                actualBookDtos,
                expectedBookDtos,
                BOOK_DTO_IGNORING_FIELDS
        );
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @Sql(scripts = {
            "classpath:database/bookscategories/clear-all-book-category-dependencies.sql",
            "classpath:database/books/clear-all-books.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/books/add-test-books-to-books-table.sql",
            "classpath:database/bookscategories/add-test-dependencies-to-books-categories-table.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Verify getAll() method returns empty page when no books exist.")
    void getAll_GivenEmptyCatalog_ShouldReturnEmptyPage() throws Exception {
        // When
        MvcResult result = createMvcResult(
                mockMvc,
                createRequestWithPageable(URL_BOOKS, BOOK_PAGEABLE),
                status().isOk());

        // Then
        List<BookDto> actualBookDtos = parsePageContent(
                result, objectMapper,
                new TypeReference<List<BookDto>>() {}
        );
        assertThat(actualBookDtos).isEmpty();
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Search for book by all parameters at once.")
    void searchBooks_FindByAllParams_Success() throws Exception {
        // Given
        BookDto expectedBookDto = createTestBookDto(EXISTING_BOOK_ID);
        String searchTitle = expectedBookDto.getTitle();
        String searchAuthor = expectedBookDto.getAuthor();
        String searchIsbn = expectedBookDto.getIsbn();
        BigDecimal fromPrice = expectedBookDto.getPrice().subtract(BigDecimal.ONE);
        BigDecimal toPrice = expectedBookDto.getPrice().add(BigDecimal.ONE);
        BookSearchParametersDto params = new BookSearchParametersDto(
                searchTitle,
                searchAuthor,
                searchIsbn,
                List.of(fromPrice, toPrice)
        );

        // When
        MvcResult result = mockMvc.perform(get(URL_BOOKS_SEARCH)
                        .param("title", params.title())
                .param("author", params.author())
                .param("isbn", params.isbn())
                .param("priceRange", params.priceRange().get(0).toString())
                .param("priceRange", params.priceRange().get(1).toString()))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<BookDto> actualBookDtos = parseResponseToList(result, objectMapper);
        assertThat(actualBookDtos).isNotNull();
        assertThat(actualBookDtos).hasSize(1).contains(expectedBookDto);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Search for books by title.")
    void searchBooks_FindByTitleParam_Success() throws Exception {
        // Given
        BookDto expectedBookDto = createTestBookDto(EXISTING_BOOK_ID);
        String searchTitle = expectedBookDto.getTitle();
        BookSearchParametersDto params = new BookSearchParametersDto(
                searchTitle,
                null,
                null,
                null
        );

        // When
        MvcResult result = mockMvc.perform(get(URL_BOOKS_SEARCH)
                        .param("title", params.title()))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<BookDto> actualBookDtos = parseResponseToList(result, objectMapper);
        assertThat(actualBookDtos).isNotNull();
        assertThat(actualBookDtos).hasSize(1).contains(expectedBookDto);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Search for books by author.")
    void searchBooks_FindByAuthorParam_Success() throws Exception {
        // Given
        BookDto expectedBookDto = createTestBookDto(EXISTING_BOOK_ID);
        String searchAuthor = expectedBookDto.getAuthor();
        BookSearchParametersDto params = new BookSearchParametersDto(
                null,
                searchAuthor,
                null,
                null
        );

        // When
        MvcResult result = mockMvc.perform(get(URL_BOOKS_SEARCH)
                        .param("author", params.author()))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<BookDto> actualBookDtos = parseResponseToList(result, objectMapper);
        assertThat(actualBookDtos).isNotNull();
        assertThat(actualBookDtos).hasSize(1).contains(expectedBookDto);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Search for books by isbn.")
    void searchBooks_FindByIsbnParam_Success() throws Exception {
        // Given
        BookDto expectedBookDto = createTestBookDto(EXISTING_BOOK_ID);
        String searchIsbn = expectedBookDto.getIsbn();
        BookSearchParametersDto params = new BookSearchParametersDto(
                null,
                null,
                searchIsbn,
                null
        );

        // When
        MvcResult result = mockMvc.perform(get(URL_BOOKS_SEARCH)
                        .param("isbn", params.isbn()))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<BookDto> actualBookDtos = parseResponseToList(result, objectMapper);
        assertThat(actualBookDtos).isNotNull();
        assertThat(actualBookDtos).hasSize(1).contains(expectedBookDto);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Search for books by lower price.")
    void searchBooks_FindByLowerPriceParam_Success() throws Exception {
        // Given
        BookDto expectedBookDtoMiddlePrice = expectedBookDtos.get(1);
        BookDto expectedBookDtoHigherPrice = expectedBookDtos.get(2);
        List<BigDecimal> priceRange = List.of(expectedBookDtoMiddlePrice.getPrice());
        BookSearchParametersDto params = new BookSearchParametersDto(
                null,
                null,
                null,
                priceRange
        );

        // When
        MvcResult result = mockMvc.perform(get(URL_BOOKS_SEARCH)
                        .param("priceRange", params.priceRange().get(0).toString()))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<BookDto> actualBookDtos = parseResponseToList(result, objectMapper);
        assertThat(actualBookDtos).isNotNull();
        assertThat(actualBookDtos).hasSize(2);
        assertThat(actualBookDtos).contains(expectedBookDtoMiddlePrice);
        assertThat(actualBookDtos).contains(expectedBookDtoHigherPrice);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Search for books by two price.")
    void searchBooks_FindByTwoPriceParam_Success() throws Exception {
        // Given
        final BookDto expectedBookDtoLowerPrice = expectedBookDtos.get(0);
        final BookDto expectedBookDtoMiddlePrice = expectedBookDtos.get(1);
        List<BigDecimal> priceRange = List.of(expectedBookDtoLowerPrice.getPrice(),
                expectedBookDtoMiddlePrice.getPrice());
        BookSearchParametersDto params = new BookSearchParametersDto(
                null,
                null,
                null,
                priceRange
        );

        // When
        MvcResult result = mockMvc.perform(get(URL_BOOKS_SEARCH)
                        .param("priceRange", params.priceRange().get(0).toString())
                        .param("priceRange", params.priceRange().get(1).toString()))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<BookDto> actualBookDtos = parseResponseToList(result, objectMapper);
        assertThat(actualBookDtos).isNotNull();
        assertThat(actualBookDtos).hasSize(2);
        assertThat(actualBookDtos).contains(expectedBookDtoMiddlePrice);
        assertThat(actualBookDtos).contains(expectedBookDtoLowerPrice);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Verify searchBooks() method returns empty List when no find books.")
    void searchBooks_NoFindBooks_ShouldReturnEmptyList() throws Exception {
        // Given
        BookSearchParametersDto params = new BookSearchParametersDto(
                null,
                null,
                NOT_EXISTING_ISBN,
                null
        );

        // When
        MvcResult result = mockMvc.perform(get(URL_BOOKS_SEARCH)
                        .param("isbn", params.isbn()))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<BookDto> actualBookDtos = parseResponseToList(result, objectMapper);
        assertThat(actualBookDtos).isNotNull();
        assertThat(actualBookDtos).isEmpty();
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Verify searchBooks() method returns all books when no params set.")
    void searchBooks_NullAllParams_ShouldReturnAllBooks() throws Exception {
        // When
        MvcResult result = mockMvc.perform(get(URL_BOOKS_SEARCH))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<BookDto> actualBookDtos = parseResponseToList(result, objectMapper);
        assertCollectionsAreEqualIgnoringFields(
                actualBookDtos,
                expectedBookDtos,
                BOOK_DTO_IGNORING_FIELDS
        );
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Verify that an exception is throw when params is not valid.")
    void searchBooks_InvalidParams_ShouldThrowException() throws Exception {
        // Given
        List<BigDecimal> priceRange = List.of(BigDecimal.ZERO, BigDecimal.ZERO);
        BookSearchParametersDto params = new BookSearchParametersDto(
                TEST_LONG_DATA,
                TEST_LONG_DATA,
                INVALID_FORMAT_ISBN,
                priceRange
        );

        // When
        MvcResult result = mockMvc.perform(get(URL_BOOKS_SEARCH)
                        .param("title", params.title())
                        .param("author", params.author())
                        .param("isbn", params.isbn())
                        .param("priceRange", params.priceRange().get(0).toString())
                        .param("priceRange", params.priceRange().get(1).toString()))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        assertValidationErrorList(result, objectMapper, EXPECTED_SEARCH_ERROR_MESSAGES);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Get book by id.")
    void getBookById_GivenBook_ShouldReturnBook() throws Exception {
        // Given
        BookDto expectedBookDto = createTestBookDto(EXISTING_BOOK_ID);

        // When
        MvcResult result = createMvcResult(
                mockMvc,
                get(URL_BOOKS_EXISTING_BOOK_ID),status().isOk()
        );

        // Then
        BookDto actualBookDto = parseResponseToObject(
                result,
                objectMapper,
                BookDto.class
        );
        assertObjectsAreEqualIgnoringFields(
                actualBookDto,
                expectedBookDto,
                BOOK_DTO_IGNORING_FIELDS
        );
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Verify that an exception is throw when book Id doesn't exist.")
    void getBookById_BookIdNotExist_ShouldReturnNotFound() throws Exception {
        // When
        MvcResult result = createMvcResult(
                mockMvc,
                get(URL_BOOKS_NOT_EXISTING_BOOK_ID),
                status().isNotFound()
        );

        // Then
        assertValidationError(
                result,
                objectMapper,
                NOT_FOUND,
                "Can`t find the book by id: " + NOT_EXISTING_BOOK_ID
        );
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Verify that an exception is throw when a book is safe deleted.")
    void getBookById_BookIsSafeDeleted_ShouldReturnNotFound() throws Exception {
        // When
        MvcResult result = createMvcResult(
                mockMvc,
                get(URL_BOOKS_SAFE_DELETED_BOOK_ID),
                status().isNotFound()
        );

        // Then
        assertValidationError(
                result,
                objectMapper,
                NOT_FOUND,
                "Can`t find the book by id: " + SAFE_DELETED_BOOK_ID
        );
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN", "USER"})
    @Test
    @Sql(scripts = "classpath:database/books/restoring-book-id101-and-its-dependencies.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Delete book by id.")
    void deleteBookById_GivenValidBook_ShouldDeleteBook() throws Exception {
        // When
        MvcResult result = createMvcResult(
                mockMvc,
                delete(URL_BOOKS_EXISTING_BOOK_ID),
                status().isNoContent()
        );

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(NO_CONTENT);
        MvcResult checkResult = createMvcResult(
                mockMvc,
                get(URL_BOOKS_EXISTING_BOOK_ID),
                status().isNotFound()
        );
        assertValidationError(
                checkResult,
                objectMapper,
                NOT_FOUND,
                "Can`t find the book by id: " + EXISTING_BOOK_ID
        );
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN", "USER"})
    @Test
    @DisplayName("Verify that an exception is throw when book id doesn't exist.")
    void deleteBookById_BookIdNotExist_ShouldReturnNotFound() throws Exception {
        // When
        MvcResult result = createMvcResult(
                mockMvc,
                delete(URL_BOOKS_NOT_EXISTING_BOOK_ID),
                status().isNotFound()
        );

        // Then
        assertValidationError(
                result,
                objectMapper,
                NOT_FOUND,
                "Can't delete a book with ID: " + NOT_EXISTING_BOOK_ID
        );
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN", "USER"})
    @Test
    @DisplayName("Verify that an exception is throw when a book is safe deleted.")
    void deleteBookById_BookIsSafeDeleted_ShouldReturnNotFound() throws Exception {
        // When
        MvcResult result = createMvcResult(
                mockMvc,
                delete(URL_BOOKS_SAFE_DELETED_BOOK_ID),
                status().isNotFound()
        );

        // Then
        assertValidationError(
                result,
                objectMapper,
                NOT_FOUND,
                "Can't delete a book with ID: " + SAFE_DELETED_BOOK_ID
        );
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @Sql(scripts = "classpath:database/books/remove-new-test-book-and-its-dependencies.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Create a new book.")
    void createBook_ValidRequestDto_Success() throws Exception {
        // Given
        BookDto expectedBookDto = createTestBookDto(NEW_BOOK_ID);
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_BOOKS),
                status().isCreated(),
                jsonRequest
        );

        // Then
        BookDto actualBookDto = parseResponseToObject(
                result,
                objectMapper,
                BookDto.class
        );
        assertObjectsAreEqualIgnoringFields(
                actualBookDto,
                expectedBookDto,
                BOOK_DTO_IGNORING_FIELDS
        );
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when a book already exists.")
    void createBook_BookAlreadyExists_ShouldReturnUnprocessableEntity() throws Exception {
        // Given
        BookDto expectedBookDto = createTestBookDto(EXISTING_BOOK_ID);
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_BOOKS),
                status().isUnprocessableEntity(),
                jsonRequest
        );

        // Then
        assertValidationError(
                result,
                objectMapper,
                UNPROCESSABLE_ENTITY,
                "Can't save a book with this ISBN: " + requestDto.isbn()
        );
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when a book already exists and safe deleted.")
    void createBook_BookExistsAndSafeDeleted_ShouldReturnUnprocessableEntity() throws Exception {
        // Given
        BookDto expectedBookDto = createTestBookDto(SAFE_DELETED_BOOK_ID);
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_BOOKS),
                status().isUnprocessableEntity(),
                jsonRequest
        );

        // Then
        assertValidationError(
                result,
                objectMapper,
                UNPROCESSABLE_ENTITY,
                "Can't save a book with this ISBN: " + requestDto.isbn()
        );
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when book fields are not in a valid format.")
    void createBook_InvalidFormatBookFields_ShouldReturnBadRequest() throws Exception {
        // Given
        BookDto expectedBookDto = createTestInvalidBookDto();
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_BOOKS),
                status().isBadRequest(),
                jsonRequest
        );

        // Then
        assertValidationErrorList(result, objectMapper, EXPECTED_BOOK_ERRORS);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when the book fields are null.")
    void createBook_BookFieldsNull_ShouldReturnBadRequest() throws Exception {
        // Given
        BookDto expectedBookDto = new BookDto();
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                post(URL_BOOKS),
                status().isBadRequest(),
                jsonRequest
        );

        // Then
        assertValidationErrorList(result, objectMapper, EXPECTED_BOOK_NULL_ERRORS);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @Sql(scripts = "classpath:database/books/restoring-book-id101-and-its-dependencies.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Update the book.")
    void updateBook_ValidRequestDto_Success() throws Exception {
        // Given
        BookDto expectedBookDto = createTestUpdatedBookDto(EXISTING_BOOK_ID);
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                put(URL_BOOKS_EXISTING_BOOK_ID),
                status().isOk(),
                jsonRequest
        );

        // Then
        BookDto actualBookDto = parseResponseToObject(
                result,
                objectMapper,
                BookDto.class
        );
        assertObjectsAreEqualIgnoringFields(
                actualBookDto,
                expectedBookDto,
                BOOK_DTO_IGNORING_FIELDS
        );
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when book id is invalid.")
    void updateBook_InvalidBookId_ShouldReturnNotFound() throws Exception {
        // Given
        BookDto expectedBookDto = createTestUpdatedBookDto(NOT_EXISTING_BOOK_ID);
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                put(URL_BOOKS_NOT_EXISTING_BOOK_ID),
                status().isNotFound(),
                jsonRequest
        );

        // Then
        assertValidationError(
                result,
                objectMapper,
                NOT_FOUND,
                "Can`t find the book by id: " + NOT_EXISTING_BOOK_ID
        );
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when updated books has a different ISBN.")
    void updateBook_NotEqualIsbn_ShouldReturnNotFound() throws Exception {
        // Given
        BookDto expectedBookDto = createTestUpdatedBookDto(EXISTING_BOOK_ID);
        expectedBookDto.setIsbn(NOT_EXISTING_ISBN);
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                put(URL_BOOKS_EXISTING_BOOK_ID),
                status().isNotFound(), jsonRequest
        );

        // Then
        assertValidationError(
                result,
                objectMapper,
                NOT_FOUND,
                "Can't update the book. Invalid book id: " + EXISTING_BOOK_ID + " or isbn: "
                        + NOT_EXISTING_ISBN
        );
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when book fields are not in a valid format.")
    void updateBook_InvalidFormatBookFields_ShouldReturnBadRequest() throws Exception {
        // Given
        BookDto expectedBookDto = createTestInvalidBookDto();
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                put(URL_BOOKS_EXISTING_BOOK_ID),
                status().isBadRequest(),
                jsonRequest
        );

        // Then
        assertValidationErrorList(result, objectMapper, EXPECTED_BOOK_ERRORS);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when the book fields are null.")
    void updateBook_BookFieldsNull_ShouldReturnBadRequest() throws Exception {
        // Given
        BookDto expectedBookDto = new BookDto();
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = createJsonMvcResult(
                mockMvc,
                put(URL_BOOKS_EXISTING_BOOK_ID),
                status().isBadRequest(),
                jsonRequest
        );

        // Then
        assertValidationErrorList(result, objectMapper, EXPECTED_BOOK_NULL_ERRORS);
    }
}
