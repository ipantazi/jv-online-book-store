package onlinebookstore.controller.category;

import static onlinebookstore.service.category.CategoryServiceImpl.categoriesCash;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import onlinebookstore.dto.book.BookDtoWithoutCategoryIds;
import onlinebookstore.dto.category.CategoryDto;
import onlinebookstore.dto.category.CreateCategoryRequestDto;
import onlinebookstore.repository.category.CategoryRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
public class CategoryControllerTest {
    protected static MockMvc mockMvc;
    private static final int EXPECTED_SIZE = 2;
    private static final int NOT_FOUND = HttpStatus.NOT_FOUND.value();
    private static final int NO_CONTENT = HttpStatus.NO_CONTENT.value();
    private static final int UNPROCESSABLE_ENTITY = HttpStatus.UNPROCESSABLE_ENTITY.value();
    private static final int BAD_REQUEST = HttpStatus.BAD_REQUEST.value();
    private static final Long VALID_CATEGORY_ID = 101L;
    private static final Long SAFE_DELETED_CATEGORY_ID = 103L;
    private static final Long NEW_CATEGORY_ID = 104L;
    private static final Long INVALID_CATEGORY_ID = 999L;
    private static final String DEFAULT_URL = "/categories";
    private static final String URL_VALID_CATEGORY_ID = "/categories/" + VALID_CATEGORY_ID;
    private static final String URL_INVALID_CATEGORY_ID = "/categories/" + INVALID_CATEGORY_ID;
    private static final String URL_SAFE_DELETED_CATEGORY_ID = "/categories/"
            + SAFE_DELETED_CATEGORY_ID;
    private static final String URL_GET_BOOKS_BY_CATEGORY_ID101 = "/categories/" + VALID_CATEGORY_ID
            + "/books";
    private static final String URL_GET_BOOKS_BY_CATEGORY_ID102 = "/categories/102/books";
    private static final String URL_GET_BOOKS_BY_INVALID_CATEGORY_ID = "/categories/"
            + INVALID_CATEGORY_ID + "/books";
    private static final List<String> expectedErrorMessages = List.of(
            "name Invalid name. Size should be between 3 or 100.",
            "description Description must not exceed 500 characters."
    );
    private static final List<String> expectedErrorNullMessages = List.of(
            "name Invalid name. Name shouldn't be blank."
    );

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();

        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            executeSqlScript(connection,
                    "database/products/add-test-category-to-categories-table.sql",
                    "database/products/add-test-books-to-books-table.sql",
                    "database/products/add-test-dependencies-to-books-categories-table.sql");
        }
    }

    @AfterAll
    public static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @BeforeEach
    void setUp(@Autowired CategoryRepository categoryRepository) {
        categoriesCash.clear();
        categoryRepository.findAll().forEach(category ->
                categoriesCash.put(category.getId(), category));
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            executeSqlScript(connection,
                    "database/products/clear-all-book-category-dependencies.sql",
                    "database/products/clear-all-categories.sql",
                    "database/products/clear-all-books.sql");
        }
    }

    private static void executeSqlScript(Connection connection, String... scriptPaths) {
        for (String path : scriptPaths) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource(path));
        }
    }

    private static CategoryDto createTestCategoryDto(Long id) {
        return new CategoryDto(
                id,
                "Test category " + id,
                "Description test"
        );
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Get all categories.")
    void getAll_ValidCategoriesCatalog_ShouldReturnAllCategories() throws Exception {
        List<CategoryDto> expectedCategoryDtos = LongStream.range(
                        VALID_CATEGORY_ID, VALID_CATEGORY_ID + EXPECTED_SIZE)
                .mapToObj(CategoryControllerTest::createTestCategoryDto)
                .toList();

        MvcResult result = createMvcResult(get(DEFAULT_URL), status().isOk());

        List<CategoryDto> actualCategoryDtos = parsePageContent(result,
                new TypeReference<List<CategoryDto>>() {});

        assertListCategoryDtosAreEqual(expectedCategoryDtos, actualCategoryDtos);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @Sql(scripts = {
            "classpath:database/products/clear-all-book-category-dependencies.sql",
            "classpath:database/products/clear-all-categories.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/products/add-test-category-to-categories-table.sql",
            "classpath:database/products/add-test-dependencies-to-books-categories-table.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Verify that getAll() method returns empty page when no categories exist.")
    void getAll_GivenEmptyCategoriesCatalog_ShouldReturnEmptyPage() throws Exception {
        MvcResult result = createMvcResult(get(DEFAULT_URL), status().isOk());

        List<CategoryDto> actualCategoryDtos = parsePageContent(result,
                new TypeReference<List<CategoryDto>>() {});

        assertThat(actualCategoryDtos).isEmpty();
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Get a category by id.")
    void getCategoryById_GivenValidCategory_ShouldReturnCategory() throws Exception {
        CategoryDto expectedCategoryDto = createTestCategoryDto(VALID_CATEGORY_ID);

        MvcResult result = createMvcResult(get(URL_VALID_CATEGORY_ID), status().isOk());

        CategoryDto actualCategoryDto = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CategoryDto.class
        );

        assertCategoryDtosAreEqual(expectedCategoryDto, actualCategoryDto);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Verify that an exception is throw when book id doesn't exist.")
    void getCategoryById_GivenInvalidCategoryId_ShouldReturnNotFound() throws Exception {
        MvcResult result = createMvcResult(get(URL_INVALID_CATEGORY_ID), status().isNotFound());

        assertErrorResponse(result, NOT_FOUND,
                "Can't find the category by id: " + INVALID_CATEGORY_ID);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Verify that an exception is throw when a category is safe deleted.")
    void getByCategoryId_CategoryIsSafeDeleted_ShouldReturnNotFound() throws Exception {
        MvcResult result = createMvcResult(get(URL_SAFE_DELETED_CATEGORY_ID),
                status().isNotFound());

        assertErrorResponse(result, NOT_FOUND,
                "Can't find the category by id: " + SAFE_DELETED_CATEGORY_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN", "USER"})
    @Test
    @Sql(scripts = {
            "classpath:database/products/restoring-category-id101-after-test.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Deleted a specific category.")
    void delete_GivenValidCategory_ShouldDeleteCategory() throws Exception {
        MvcResult result = createMvcResult(delete(URL_VALID_CATEGORY_ID), status().isNoContent());

        assertThat(result.getResponse().getStatus()).isEqualTo(NO_CONTENT);
        MvcResult checkResult = createMvcResult(get(URL_VALID_CATEGORY_ID), status().isNotFound());
        assertErrorResponse(checkResult, NOT_FOUND,
                "Can't find the category by id: " + VALID_CATEGORY_ID);
        assertThat(categoriesCash).hasSize(EXPECTED_SIZE - 1);
        assertThat(categoriesCash).doesNotContainKey(VALID_CATEGORY_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when category id doesn't exist.")
    void delete_GivenInvalidCategoryId_ShouldReturnNotFound() throws Exception {
        MvcResult result = createMvcResult(delete(URL_INVALID_CATEGORY_ID), status().isNotFound());

        assertErrorResponse(result, NOT_FOUND,
                "Can't delete a category with id: " + INVALID_CATEGORY_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when a category is safe deleted.")
    void delete_CategoryIsSafeDeleted_ShouldReturnNotFound() throws Exception {
        MvcResult result = createMvcResult(delete(URL_SAFE_DELETED_CATEGORY_ID),
                status().isNotFound());

        assertErrorResponse(result, NOT_FOUND,
                "Can't delete a category with id: " + SAFE_DELETED_CATEGORY_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @Sql(scripts = "classpath:database/products/remove-new-test-category-from-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Create a new category.")
    void createCategory_ValidRequestDto_ShouldCreateCategory() throws Exception {
        CategoryDto expectedCategoryDto = createTestCategoryDto(NEW_CATEGORY_ID);
        CreateCategoryRequestDto requestDto = createTestCategoryRequestDto(expectedCategoryDto);
        String jsonResponse = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(post(DEFAULT_URL), status().isCreated(),
                jsonResponse);

        CategoryDto actualCategoryDto = objectMapper.readValue(
                result.getResponse().getContentAsString(), CategoryDto.class);
        assertCategoryDtosAreEqual(expectedCategoryDto, actualCategoryDto);
        assertThat(categoriesCash).hasSize(EXPECTED_SIZE + 1);
        assertThat(categoriesCash).containsKey(NEW_CATEGORY_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when a category already exists.")
    void createCategory_CategoryAlreadyExists_ShouldReturnUnprocessableEntity() throws Exception {
        CategoryDto expectedCategoryDto = createTestCategoryDto(VALID_CATEGORY_ID);
        CreateCategoryRequestDto requestDto = createTestCategoryRequestDto(expectedCategoryDto);
        String jsonResponse = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(post(DEFAULT_URL),
                status().isUnprocessableEntity(), jsonResponse);

        assertErrorResponse(result, UNPROCESSABLE_ENTITY,
                "Can't save category with name: " + expectedCategoryDto.name());
        assertThat(categoriesCash).hasSize(EXPECTED_SIZE);
        assertThat(categoriesCash).doesNotContainKey(NEW_CATEGORY_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when category fields are not in valid format.")
    void createCategory_InvalidFormatCategoryFields_ShouldReturnBadRequest() throws Exception {
        CategoryDto expectedCategoryDto = createTestInvalidCategoryDto();
        CreateCategoryRequestDto requestDto = createTestCategoryRequestDto(expectedCategoryDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(post(DEFAULT_URL), status().isBadRequest(),
                jsonRequest);

        assertListErrorResponse(result, expectedErrorMessages);
        assertThat(categoriesCash).hasSize(EXPECTED_SIZE);
        assertThat(categoriesCash).doesNotContainKey(NEW_CATEGORY_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when the category name is blank.")
    void createCategory_BlankCategoryName_ShouldReturnBadRequest() throws Exception {
        CategoryDto expectedCategoryDto = new CategoryDto(NEW_CATEGORY_ID, null, null);
        CreateCategoryRequestDto requestDto = createTestCategoryRequestDto(expectedCategoryDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(post(DEFAULT_URL), status().isBadRequest(),
                jsonRequest);

        assertListErrorResponse(result, expectedErrorNullMessages);
        assertThat(categoriesCash).hasSize(EXPECTED_SIZE);
        assertThat(categoriesCash).doesNotContainKey(NEW_CATEGORY_ID);
    }

    @WithMockUser(value = "bob@example.com", roles = {"ADMIN"})
    @Test
    @Sql(scripts = "classpath:database/products/restoring-category-id101-after-test.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Update a specific category.")
    void updateCategory_ValidRequestDto_ShouldUpdateCategory() throws Exception {
        CategoryDto expectedCategoryDto = createTestUpdatedCategoryDto(VALID_CATEGORY_ID);
        CreateCategoryRequestDto requestDto = createTestCategoryRequestDto(expectedCategoryDto);
        String jsonResponse = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(put(URL_VALID_CATEGORY_ID), status().isOk(),
                jsonResponse);

        CategoryDto actualCategoryDto = objectMapper.readValue(
                result.getResponse().getContentAsString(), CategoryDto.class);
        assertCategoryDtosAreEqual(expectedCategoryDto, actualCategoryDto);
        assertThat(categoriesCash).hasSize(EXPECTED_SIZE);
        assertThat(categoriesCash).containsKey(VALID_CATEGORY_ID);
        assertThat(categoriesCash.get(VALID_CATEGORY_ID).getName())
                .isEqualTo(expectedCategoryDto.name());
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when the category id is not valid.")
    void updateCategory_InvalidCategoryId_ShouldReturnNotFound() throws Exception {
        CategoryDto expectedCategoryDto = createTestUpdatedCategoryDto(INVALID_CATEGORY_ID);
        CreateCategoryRequestDto requestDto = createTestCategoryRequestDto(expectedCategoryDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(put(URL_INVALID_CATEGORY_ID), status().isNotFound(),
                jsonRequest);

        assertErrorResponse(result, NOT_FOUND,
                "Can't find the category by id: " + INVALID_CATEGORY_ID);
        assertThat(categoriesCash).hasSize(EXPECTED_SIZE);
        assertThat(categoriesCash).doesNotContainKey(INVALID_CATEGORY_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when category fields are not in valid format.")
    void updateCategory_InvalidFormatCategoryFields_ShouldReturnBadRequest() throws Exception {
        CategoryDto expectedCategoryDto = createTestInvalidCategoryDto();
        CreateCategoryRequestDto requestDto = createTestCategoryRequestDto(expectedCategoryDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(put(URL_VALID_CATEGORY_ID),
                status().isBadRequest(), jsonRequest);

        assertListErrorResponse(result, expectedErrorMessages);
        assertThat(categoriesCash).hasSize(EXPECTED_SIZE);
        assertThat(categoriesCash.get(VALID_CATEGORY_ID).getName())
                .isNotEqualTo(expectedCategoryDto.name());
    }

    @WithMockUser(username = "bob@example.com", roles = {"ASDMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when the category fields are null.")
    void updateCategory_NullCategoryFields_ShouldReturnBadRequest() throws Exception {
        CategoryDto expectedCategoryDto = new CategoryDto(VALID_CATEGORY_ID, null, null);
        CreateCategoryRequestDto requestDto = createTestCategoryRequestDto(expectedCategoryDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(put(URL_VALID_CATEGORY_ID), status().isBadRequest(),
                jsonRequest);

        assertListErrorResponse(result, expectedErrorNullMessages);
        assertThat(categoriesCash).hasSize(EXPECTED_SIZE);
        assertThat(categoriesCash.get(VALID_CATEGORY_ID).getName()).isNotNull();
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Get books by a specific category.")
    void getBooksByCategoryId_ValidRequestDto_ShouldReturnBooksByCategory() throws Exception {
        int expectedBooksSize = 3;
        MvcResult result = createMvcResult(get(URL_GET_BOOKS_BY_CATEGORY_ID101),
                status().isOk());

        List<BookDtoWithoutCategoryIds> actualBookDtos = parsePageContent(result,
                new TypeReference<List<BookDtoWithoutCategoryIds>>() {});
        assertThat(actualBookDtos).hasSize(expectedBooksSize);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Get empty page when no books found by the category id.")
    void getBooksByCategoryId_NoBooksByCategoryId_ShouldReturnEmptyPage() throws Exception {
        MvcResult result = createMvcResult(get(URL_GET_BOOKS_BY_CATEGORY_ID102),
                status().isOk());

        List<BookDtoWithoutCategoryIds> actualBookDtos = parsePageContent(result,
                new TypeReference<List<BookDtoWithoutCategoryIds>>() {});
        assertThat(actualBookDtos).isEmpty();
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Verify that an exception is throw when category id is not valid.")
    void getBooksByCategoryId_NoValidCategoryId_ShouldReturnNotFound() throws Exception {
        MvcResult result = createMvcResult(get(URL_GET_BOOKS_BY_INVALID_CATEGORY_ID),
                status().isNotFound());

        assertErrorResponse(result, NOT_FOUND,
                "Can't get books with category ID: " + INVALID_CATEGORY_ID);
    }

    private MvcResult createMvcResult(MockHttpServletRequestBuilder requestBuilder,
                                      ResultMatcher expectedStatus) throws Exception {
        return mockMvc.perform(requestBuilder)
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

    private <T> List<T> parsePageContent(MvcResult result,
                                         TypeReference<List<T>> typeRef) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return objectMapper.readValue(root.get("content").toString(), typeRef);
    }

    private void assertListCategoryDtosAreEqual(List<CategoryDto> expected,
                                                List<CategoryDto> actual) {
        assertThat(actual).isNotNull();
        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    private void assertCategoryDtosAreEqual(CategoryDto expected, CategoryDto actual) {
        assertThat(actual).isNotNull();
        assertThat(actual.id()).isNotNull();
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);
    }

    private void assertErrorResponse(MvcResult result,
                                     int expectedStatus,
                                     String expectedMessage) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());

        assertThat(body.get("status").asInt()).isEqualTo(expectedStatus);
        assertThat(body.get("message").asText()).isEqualTo(expectedMessage);
        assertThat(body.get("timestamp").asText()).isNotBlank();
    }

    private void assertListErrorResponse(MvcResult result,
                                         List<String> expectedErrorMessages) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());

        JsonNode errors = body.get("message");
        assertThat(errors).isNotNull();
        List<String> actualErrorMessages = new ArrayList<>();
        errors.forEach(error -> actualErrorMessages.add(error.asText()));

        assertThat(body.get("status").asInt()).isEqualTo(BAD_REQUEST);
        assertThat(body.get("timestamp").asText()).isNotBlank();
        assertThat(actualErrorMessages).containsExactlyInAnyOrderElementsOf(expectedErrorMessages);
    }

    private CreateCategoryRequestDto createTestCategoryRequestDto(CategoryDto categoryDto) {
        return new CreateCategoryRequestDto(categoryDto.name(), categoryDto.description());
    }

    private CategoryDto createTestInvalidCategoryDto() {
        int maxCategoryDescriptionLength = 500;
        return new CategoryDto(
                VALID_CATEGORY_ID,
                "IT",
                "T".repeat(maxCategoryDescriptionLength + 1));
    }

    private CategoryDto createTestUpdatedCategoryDto(Long id) {
        return new CategoryDto(id, "Updated name", "Updated description");
    }
}
