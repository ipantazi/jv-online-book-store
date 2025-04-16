package onlinebookstore.controller.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.LongStream;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import onlinebookstore.dto.book.BookDto;
import onlinebookstore.dto.book.BookSearchParametersDto;
import onlinebookstore.dto.book.CreateBookRequestDto;
import onlinebookstore.repository.category.CategoryRepository;
import onlinebookstore.service.category.CategoryServiceImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookControllerTest {
    protected static MockMvc mockMvc;
    private static final Long VALID_BOOK_ID = 101L;
    private static final Long SAFE_DELETED_BOOK_ID = 104L;
    private static final Long ID_NEW_BOOK = 105L;
    private static final Long INVALID_BOOK_ID = 999L;
    private static final Long CATEGORY_ID = 101L;
    private static final Long UPDATE_CATEGORY_ID = 102L;
    private static final int EXPECTED_SIZE = 3;
    private static final int NO_CONTENT = HttpStatus.NO_CONTENT.value();
    private static final int BAD_REQUEST = HttpStatus.BAD_REQUEST.value();
    private static final int NOT_FOUND = HttpStatus.NOT_FOUND.value();
    private static final int UNPROCESSABLE_ENTITY = HttpStatus.UNPROCESSABLE_ENTITY.value();
    private static final String INVALID_ISBN = "9999999999999";
    private static final String DEFAULT_URL = "/books";
    private static final String URL_SEARCH = "/books/search";
    private static final String URL_VALID_BOOK_ID = "/books/" + VALID_BOOK_ID;
    private static final String URL_INVALID_BOOK_ID = "/books/" + INVALID_BOOK_ID;
    private static final String URL_SAFE_DELETED_BOOK_ID = "/books/" + SAFE_DELETED_BOOK_ID;
    private static final List<String> expectedSearchErrorMessages = List.of(
            "title Invalid title. Size should not exceed 100 characters.",
            "author Invalid author. Size should not exceed 50 characters.",
            "isbn Invalid ISBN format. Only digits and dashes. "
                    + "Size should not exceed 13 characters.",
            "priceRange[0] Invalid price. Value should be positive.",
            "priceRange[1] Invalid price. Value should be positive."
    );
    private static final List<String> expectedErrorMessages = List.of(
            "title Invalid title. Size should be between 3 to 100.",
            "author Invalid author. Size should be between 3 to 50.",
            "isbn Invalid ISBN format. ISBN must contain exactly 10 or 13 digits, "
                    + "with optional dashes.",
            "price Invalid price. Value should be positive.",
            "coverImage Invalid URL. Please provide a valid UPL of cover image.",
            "categoryIds[] Category Id shouldn't be null."
    );
    private static final List<String> expectedErrorNullMessages = List.of(
            "title Invalid title. Title should not be blank.",
            "author Invalid author. Author should not be blank.",
            "isbn Invalid ISBN. ISBN should not be blank.",
            "price Invalid price. Please enter price.",
            "categoryIds Invalid categories. Categories shouldn't be empty."
    );
    private static List<BookDto> expectedBooksDtos;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext,
            @Autowired CategoryRepository categoryRepository
    ) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();

        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            executeSqlScript(connection,
                    "database/products/add-test-books-to-books-table.sql",
                    "database/products/add-test-category-to-categories-table.sql",
                    "database/products/add-test-dependencies-to-books-categories-table.sql");
        }
        categoryRepository.findAll().forEach(category ->
                CategoryServiceImpl.categoriesCash.put(category.getId(), category));

        expectedBooksDtos = LongStream.range(VALID_BOOK_ID, VALID_BOOK_ID + EXPECTED_SIZE)
                .mapToObj(BookControllerTest::createTestBookDto)
                .toList();
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            executeSqlScript(connection,
                    "database/products/clear-all-book-category-dependencies.sql",
                    "database/products/clear-all-books.sql",
                    "database/products/clear-all-categories.sql");
        }
    }

    private static void executeSqlScript(Connection connection, String... scriptPaths) {
        for (String path : scriptPaths) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource(path));
        }
    }

    static BookDto createTestBookDto(Long id) {
        BookDto bookDto = new BookDto();
        bookDto.setId(id);
        bookDto.setTitle("Test Book " + id);
        bookDto.setAuthor("Test Author " + id);
        bookDto.setIsbn(String.valueOf(1000000000000L + id));
        bookDto.setPrice(new BigDecimal(id));
        bookDto.setDescription("Test Description");
        bookDto.setCoverImage("http://example.com/test-cover.jpg");
        bookDto.setCategoryIds(Set.of(CATEGORY_ID));
        return bookDto;
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("All books must be returned except those safe deleted.")
    void getAll_GivenBooksCatalog_ShouldReturnAllBooks() throws Exception {
        MvcResult result = createMvcResult(get(DEFAULT_URL), status().isOk());

        List<BookDto> actualBookDtos = parsePageContent(result);
        assertListBookDtosAreEqual(actualBookDtos, expectedBooksDtos);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @Sql(scripts = {
            "classpath:database/products/clear-all-book-category-dependencies.sql",
            "classpath:database/products/clear-all-books.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/products/add-test-books-to-books-table.sql",
            "classpath:database/products/add-test-dependencies-to-books-categories-table.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Verify getAll() method returns empty page when no books exist.")
    void getAll_GivenEmptyCatalog_ShouldReturnEmptyPage() throws Exception {
        MvcResult result = createMvcResult(get(DEFAULT_URL), status().isOk());

        List<BookDto> actualBookDtos = parsePageContent(result);
        assertThat(actualBookDtos).isEmpty();
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Search for book by all parameters at once.")
    void searchBooks_FindByAllParams_Success() throws Exception {
        int startIndex = 0;
        int endIndex = 4;
        BookDto expectedBookDto = createTestBookDto(VALID_BOOK_ID);
        BookSearchParametersDto params = new BookSearchParametersDto(
                expectedBookDto.getTitle().substring(startIndex, endIndex),
                expectedBookDto.getAuthor().substring(startIndex, endIndex),
                expectedBookDto.getIsbn().substring(
                expectedBookDto.getIsbn().length() - endIndex),
                List.of(expectedBookDto.getPrice().subtract(BigDecimal.ONE),
                        expectedBookDto.getPrice().add(BigDecimal.ONE))
        );

        MvcResult result = createSearchMvcResult(params, status().isOk());

        List<BookDto> actualBookDtos = parseResultToList(result);
        assertThat(actualBookDtos).isNotNull();
        assertThat(actualBookDtos).hasSize(1).contains(expectedBookDto);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Search for books by title.")
    void searchBooks_FindByTitleParam_Success() throws Exception {
        BookDto expectedBookDto = createTestBookDto(VALID_BOOK_ID);
        BookSearchParametersDto params = new BookSearchParametersDto(
                expectedBookDto.getTitle(), null, null, null);

        MvcResult result = createSearchMvcResult(params, status().isOk());

        List<BookDto> actualBookDtos = parseResultToList(result);
        assertThat(actualBookDtos).isNotNull();
        assertThat(actualBookDtos).hasSize(1).contains(expectedBookDto);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Search for books by author.")
    void searchBooks_FindByAuthorParam_Success() throws Exception {
        BookDto expectedBookDto = createTestBookDto(VALID_BOOK_ID);
        BookSearchParametersDto params = new BookSearchParametersDto(
                null, expectedBookDto.getAuthor(), null, null);

        MvcResult result = createSearchMvcResult(params, status().isOk());

        List<BookDto> actualBookDtos = parseResultToList(result);
        assertThat(actualBookDtos).isNotNull();
        assertThat(actualBookDtos).hasSize(1).contains(expectedBookDto);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Search for books by isbn.")
    void searchBooks_FindByIsbnParam_Success() throws Exception {
        BookDto expectedBookDto = createTestBookDto(VALID_BOOK_ID);
        BookSearchParametersDto params = new BookSearchParametersDto(
                null, null, expectedBookDto.getIsbn(), null);

        MvcResult result = createSearchMvcResult(params, status().isOk());

        List<BookDto> actualBookDtos = parseResultToList(result);
        assertThat(actualBookDtos).isNotNull();
        assertThat(actualBookDtos).hasSize(1).contains(expectedBookDto);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Search for books by lower price.")
    void searchBooks_FindByLowerPriceParam_Success() throws Exception {
        BookDto expectedBookDtoMiddlePrice = expectedBooksDtos.get(1);
        BookDto expectedBookDtoHigherPrice = expectedBooksDtos.get(2);
        BookSearchParametersDto params = new BookSearchParametersDto(
                null, null, null, List.of(expectedBookDtoMiddlePrice.getPrice()));

        MvcResult result = createSearchMvcResult(params, status().isOk());

        List<BookDto> actualBookDtos = parseResultToList(result);
        assertThat(actualBookDtos).isNotNull();
        assertThat(actualBookDtos).hasSize(2);
        assertThat(actualBookDtos).contains(expectedBookDtoMiddlePrice);
        assertThat(actualBookDtos).contains(expectedBookDtoHigherPrice);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Search for books by two price.")
    void searchBooks_FindByTwoPriceParam_Success() throws Exception {
        final BookDto expectedBookDtoLowerPrice = expectedBooksDtos.get(0);
        final BookDto expectedBookDtoMiddlePrice = expectedBooksDtos.get(1);
        List<BigDecimal> priceRange = List.of(expectedBookDtoLowerPrice.getPrice(),
                expectedBookDtoMiddlePrice.getPrice());
        BookSearchParametersDto params = new BookSearchParametersDto(null, null, null, priceRange);

        MvcResult result = createSearchMvcResult(params, status().isOk());

        List<BookDto> actualBookDtos = parseResultToList(result);
        assertThat(actualBookDtos).isNotNull();
        assertThat(actualBookDtos).hasSize(2);
        assertThat(actualBookDtos).contains(expectedBookDtoMiddlePrice);
        assertThat(actualBookDtos).contains(expectedBookDtoLowerPrice);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Verify searchBooks() method returns empty List when no find books.")
    void searchBooks_NoFindBooks_ShouldReturnEmptyList() throws Exception {
        BookSearchParametersDto params = new BookSearchParametersDto(
                null, null, INVALID_ISBN, null);

        MvcResult result = createSearchMvcResult(params, status().isOk());

        List<BookDto> actualBookDtos = parseResultToList(result);
        assertThat(actualBookDtos).isNotNull();
        assertThat(actualBookDtos).isEmpty();
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Verify searchBooks() method returns all books when no params set.")
    void searchBooks_NullAllParams_ShouldReturnAllBooks() throws Exception {
        BookSearchParametersDto params = new BookSearchParametersDto(
                null, null, null, null);

        MvcResult result = createSearchMvcResult(params, status().isOk());

        List<BookDto> actualBookDtos = parseResultToList(result);
        assertListBookDtosAreEqual(actualBookDtos, expectedBooksDtos);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Verify that an exception is throw when params is not valid.")
    void searchBooks_InvalidParams_ShouldThrowException() throws Exception {
        int maxBookTitleLength = 100;
        int maxBookAuthorLength = 50;
        String invalidTitle = "T".repeat(maxBookTitleLength + 1);
        String invalidAuthor = "T".repeat(maxBookAuthorLength + 1);
        String invalidIsbn = "INVALID ISBN";
        List<BigDecimal> priceRange = List.of(BigDecimal.ZERO, BigDecimal.ZERO);
        BookSearchParametersDto params = new BookSearchParametersDto(
                invalidTitle, invalidAuthor, invalidIsbn, priceRange);

        MvcResult result = createSearchMvcResult(params, status().isBadRequest());

        assertListErrorsResponse(result, expectedSearchErrorMessages);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Get book by id.")
    void getBookById_GivenBook_ShouldReturnBook() throws Exception {
        BookDto expectedBookDto = createTestBookDto(VALID_BOOK_ID);
        MvcResult result = createMvcResult(get(URL_VALID_BOOK_ID),status().isOk());

        BookDto actualBookDto = objectMapper.readValue(result.getResponse().getContentAsString(),
                BookDto.class);
        assertBookDtosAreEqual(actualBookDto, expectedBookDto);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Verify that an exception is throw when book Id doesn't exist.")
    void getBookById_BookIdNotExist_ShouldReturnNotFound() throws Exception {
        MvcResult result = createMvcResult(get(URL_INVALID_BOOK_ID), status().isNotFound());

        assertErrorResponse(result, NOT_FOUND, "Can`t find the book by id: " + INVALID_BOOK_ID);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Verify that an exception is throw when a book is safe deleted.")
    void getBookById_BookIsSafeDeleted_ShouldReturnNotFound() throws Exception {
        MvcResult result = createMvcResult(get(URL_SAFE_DELETED_BOOK_ID), status().isNotFound());

        assertErrorResponse(result, NOT_FOUND,
                "Can`t find the book by id: " + SAFE_DELETED_BOOK_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN", "USER"})
    @Test
    @Sql(scripts = "classpath:database/products/restoring-book-id101-and-its-dependencies.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Delete book by id.")
    void deleteBookById_GivenValidBook_ShouldDeleteBook() throws Exception {
        MvcResult result = createMvcResult(delete(URL_VALID_BOOK_ID), status().isNoContent());

        assertThat(result.getResponse().getStatus()).isEqualTo(NO_CONTENT);
        MvcResult checkResult = createMvcResult(get(URL_VALID_BOOK_ID), status().isNotFound());
        assertErrorResponse(checkResult, NOT_FOUND, "Can`t find the book by id: " + VALID_BOOK_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN", "USER"})
    @Test
    @DisplayName("Verify that an exception is throw when book id doesn't exist.")
    void deleteBookById_BookIdNotExist_ShouldReturnNotFound() throws Exception {
        MvcResult result = createMvcResult(delete(URL_INVALID_BOOK_ID), status().isNotFound());

        assertErrorResponse(result, NOT_FOUND, "Can't delete a book with ID: " + INVALID_BOOK_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN", "USER"})
    @Test
    @DisplayName("Verify that an exception is throw when a book is safe deleted.")
    void deleteBookById_BookIsSafeDeleted_ShouldReturnNotFound() throws Exception {
        MvcResult result = createMvcResult(delete(URL_SAFE_DELETED_BOOK_ID),
                status().isNotFound());

        assertErrorResponse(result, NOT_FOUND,
                "Can't delete a book with ID: " + SAFE_DELETED_BOOK_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @Sql(scripts = "classpath:database/products/remove-new-test-book-and-its-dependencies.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Create a new book.")
    void createBook_ValidRequestDto_Success() throws Exception {
        BookDto expectedBookDto = createTestBookDto(ID_NEW_BOOK);
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(post(DEFAULT_URL), status().isCreated(),
                jsonRequest);

        BookDto actualBookDto = objectMapper.readValue(result.getResponse().getContentAsString(),
                BookDto.class);
        assertBookDtosAreEqual(actualBookDto, expectedBookDto);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when a book already exists.")
    void createBook_BookAlreadyExists_ShouldReturnUnprocessableEntity() throws Exception {
        BookDto expectedBookDto = createTestBookDto(VALID_BOOK_ID);
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(post(DEFAULT_URL), status().isUnprocessableEntity(),
                jsonRequest);

        assertErrorResponse(result, UNPROCESSABLE_ENTITY,
                "Can't save a book with this ISBN: " + requestDto.isbn());
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when a book already exists and safe deleted.")
    void createBook_BookExistsAndSafeDeleted_ShouldReturnUnprocessableEntity() throws Exception {
        BookDto expectedBookDto = createTestBookDto(SAFE_DELETED_BOOK_ID);
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(post(DEFAULT_URL), status().isUnprocessableEntity(),
                jsonRequest);

        assertErrorResponse(result, UNPROCESSABLE_ENTITY,
                "Can't save a book with this ISBN: " + requestDto.isbn());
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when book fields are not in a valid format.")
    void createBook_InvalidFormatBookFields_ShouldReturnBadRequest() throws Exception {
        BookDto expectedBookDto = createTestInvalidBookDto();
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(post(DEFAULT_URL), status().isBadRequest(),
                jsonRequest);

        assertListErrorsResponse(result, expectedErrorMessages);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when the book fields are null.")
    void createBook_BookFieldsNull_ShouldReturnBadRequest() throws Exception {
        BookDto expectedBookDto = new BookDto();
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(post(DEFAULT_URL), status().isBadRequest(),
                jsonRequest);

        assertListErrorsResponse(result, expectedErrorNullMessages);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @Sql(scripts = "classpath:database/products/restoring-book-id101-and-its-dependencies.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Update the book.")
    void updateBook_ValidRequestDto_Success() throws Exception {
        BookDto expectedBookDto = createTestUpdatedBookDto(VALID_BOOK_ID);
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(put(URL_VALID_BOOK_ID), status().isOk(),
                jsonRequest);

        BookDto actualBookDto = objectMapper.readValue(result.getResponse().getContentAsString(),
                BookDto.class);
        assertBookDtosAreEqual(actualBookDto, expectedBookDto);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when book id is invalid.")
    void updateBook_InvalidBookId_ShouldReturnNotFound() throws Exception {
        BookDto expectedBookDto = createTestUpdatedBookDto(INVALID_BOOK_ID);
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(put(URL_INVALID_BOOK_ID), status().isNotFound(),
                jsonRequest);

        assertErrorResponse(result, NOT_FOUND, "Can`t find the book by id: " + INVALID_BOOK_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when updated books has a different ISBN.")
    void updateBook_NotEqualIsbn_ShouldReturnNotFound() throws Exception {
        BookDto expectedBookDto = createTestUpdatedBookDto(VALID_BOOK_ID);
        expectedBookDto.setIsbn(INVALID_ISBN);
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(put(URL_VALID_BOOK_ID), status().isNotFound(),
                jsonRequest);

        assertErrorResponse(result, NOT_FOUND, "Can't update the book. Invalid book id: "
                + VALID_BOOK_ID + " or isbn: " + INVALID_ISBN);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when book fields are not in a valid format.")
    void updateBook_InvalidFormatBookFields_ShouldReturnBadRequest() throws Exception {
        BookDto expectedBookDto = createTestInvalidBookDto();
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(put(URL_VALID_BOOK_ID), status().isBadRequest(),
                jsonRequest);

        assertListErrorsResponse(result, expectedErrorMessages);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when the book fields are null.")
    void updateBook_BookFieldsNull_ShouldReturnBadRequest() throws Exception {
        BookDto expectedBookDto = new BookDto();
        CreateBookRequestDto requestDto = createTestBookRequestDto(expectedBookDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(put(URL_VALID_BOOK_ID), status().isBadRequest(),
                jsonRequest);

        assertListErrorsResponse(result, expectedErrorNullMessages);
    }

    private List<BookDto> parsePageContent(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return objectMapper.readValue(
                root.get("content").toString(),
                new TypeReference<>() {
                }
        );
    }

    private List<BookDto> parseResultToList(MvcResult result) throws Exception {
        return objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );
    }

    private MvcResult createSearchMvcResult(BookSearchParametersDto params,
                                            ResultMatcher expectedStatus) throws Exception {
        MockHttpServletRequestBuilder request = get(URL_SEARCH);

        if (params.title() != null) {
            request = request.param("title", params.title());
        }
        if (params.author() != null) {
            request = request.param("author", params.author());
        }
        if (params.isbn() != null) {
            request = request.param("isbn", params.isbn());
        }
        if (params.priceRange() != null) {
            List<BigDecimal> prices = params.priceRange();
            if (!prices.isEmpty() && prices.get(0) != null) {
                request = request.param("priceRange", prices.get(0).toString());
            }
            if (prices.size() >= 2 && prices.get(1) != null) {
                request = request.param("priceRange", prices.get(1).toString());
            }
        }
        return mockMvc.perform(request)
                .andExpect(expectedStatus)
                .andReturn();
    }

    private MvcResult createJsonMvcResult(MockHttpServletRequestBuilder requestBuilder,
                                          ResultMatcher expectedStatus,
                                          String jsonRequest) throws Exception {
        return mockMvc.perform(requestBuilder
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus)
                .andReturn();
    }

    private MvcResult createMvcResult(MockHttpServletRequestBuilder requestBuilder,
                                      ResultMatcher expectedStatus) throws Exception {
        return mockMvc.perform(requestBuilder)
                .andExpect(expectedStatus)
                .andReturn();
    }

    private void assertListBookDtosAreEqual(List<BookDto> actual, List<BookDto> expected) {
        assertThat(actual).isNotNull();
        assertThat(actual).hasSize(expected.size());
        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id", "categoryIds")
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    private void assertBookDtosAreEqual(BookDto actual, BookDto expected) {
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isNotNull();
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id", "categoryIds")
                .isEqualTo(expected);
        assertThat(actual.getCategoryIds())
                .containsExactlyInAnyOrderElementsOf(expected.getCategoryIds());
    }

    private void assertErrorResponse(MvcResult result,
                                     int expectedStatus,
                                     String expectedMessage) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());

        assertThat(body.get("status").asInt()).isEqualTo(expectedStatus);
        assertThat(body.get("message").asText()).isEqualTo(expectedMessage);
        assertThat(body.get("timestamp").asText()).isNotBlank();
    }

    private void assertListErrorsResponse(MvcResult result,
                                          List<String> expectedMessages) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());

        JsonNode errors = body.get("message");
        assertThat(errors).isNotNull();
        List<String> actualErrorMessages = new ArrayList<>();
        errors.forEach(error -> actualErrorMessages.add(error.asText()));

        assertThat(body.get("status").asInt()).isEqualTo(BAD_REQUEST);
        assertThat(body.get("timestamp").asText()).isNotBlank();
        assertThat(actualErrorMessages).containsExactlyInAnyOrderElementsOf(expectedMessages);
    }

    private CreateBookRequestDto createTestBookRequestDto(BookDto bookDto) {
        return new CreateBookRequestDto(
                bookDto.getTitle(),
                bookDto.getAuthor(),
                bookDto.getIsbn(),
                bookDto.getPrice(),
                bookDto.getDescription(),
                bookDto.getCoverImage(),
                bookDto.getCategoryIds()
        );
    }

    private BookDto createTestInvalidBookDto() {
        BookDto bookDto = new BookDto();
        bookDto.setId(VALID_BOOK_ID);
        bookDto.setTitle("IT");
        bookDto.setAuthor("IT");
        bookDto.setIsbn("INVALID ISBN");
        bookDto.setPrice(BigDecimal.ZERO);
        bookDto.setCoverImage("Invalid cover image url");
        Set<Long> categoryIds = new HashSet<>();
        categoryIds.add(null);
        bookDto.setCategoryIds(categoryIds);
        return bookDto;
    }

    private BookDto createTestUpdatedBookDto(Long id) {
        BookDto bookDto = createTestBookDto(id);
        bookDto.setTitle("Updated Title");
        bookDto.setAuthor("Updated Author");
        bookDto.setPrice(BigDecimal.TEN);
        bookDto.setDescription("Updated Description");
        bookDto.setCoverImage("http://update_example.com/test-cover.jpg");
        bookDto.setCategoryIds(Set.of(UPDATE_CATEGORY_ID));
        return bookDto;
    }
}
