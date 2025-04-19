package onlinebookstore.controller.category;

import static onlinebookstore.service.category.CategoryServiceImpl.categoriesCash;
import static onlinebookstore.util.TestDataUtil.EXISTING_CATEGORY_ID;
import static onlinebookstore.util.TestDataUtil.EXPECTED_BOOK_DTOS_SIZE;
import static onlinebookstore.util.TestDataUtil.EXPECTED_CATEGORIES_SIZE;
import static onlinebookstore.util.TestDataUtil.NEW_CATEGORY_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_CATEGORY_ID;
import static onlinebookstore.util.TestDataUtil.SAFE_DELETED_CATEGORY_ID;
import static onlinebookstore.util.TestDataUtil.createTestCategoryDto;
import static onlinebookstore.util.TestDataUtil.createTestCategoryRequestDto;
import static onlinebookstore.util.TestDataUtil.createTestInvalidCategoryDto;
import static onlinebookstore.util.TestDataUtil.createTestUpdatedCategoryDto;
import static onlinebookstore.util.TestDataUtil.fillCategoryCache;
import static onlinebookstore.util.assertions.CategoryAssertionsUtil.assertCategoryDtosAreEqual;
import static onlinebookstore.util.assertions.CategoryAssertionsUtil.assertErrorResponse;
import static onlinebookstore.util.assertions.CategoryAssertionsUtil.assertListCategoryDtosAreEqual;
import static onlinebookstore.util.assertions.CategoryAssertionsUtil.assertListErrorsResponse;
import static onlinebookstore.util.controller.ControllerTestDataUtil.EXPECTED_CATEGORY_ERRORS;
import static onlinebookstore.util.controller.ControllerTestDataUtil.EXPECTED_CATEGORY_NULL_ERRORS;
import static onlinebookstore.util.controller.ControllerTestDataUtil.NOT_FOUND;
import static onlinebookstore.util.controller.ControllerTestDataUtil.NO_CONTENT;
import static onlinebookstore.util.controller.ControllerTestDataUtil.UNPROCESSABLE_ENTITY;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_CATEGORIES;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_GET_BOOKS_BY_ALTERNATIVE_CATEGORY_ID;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_GET_BOOKS_BY_INVALID_CATEGORY_ID;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_GET_BOOKS_BY_VALID_CATEGORY_ID;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_INVALID_CATEGORY_ID;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_SAFE_DELETED_CATEGORY_ID;
import static onlinebookstore.util.controller.ControllerTestDataUtil.URL_VALID_CATEGORY_ID;
import static onlinebookstore.util.controller.ControllerTestUtil.parsePageContent;
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
import java.util.List;
import java.util.stream.LongStream;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import onlinebookstore.dto.book.BookDtoWithoutCategoryIds;
import onlinebookstore.dto.category.CategoryDto;
import onlinebookstore.dto.category.CreateCategoryRequestDto;
import onlinebookstore.repository.category.CategoryRepository;
import onlinebookstore.util.TestDataUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
public class CategoryControllerTest {
    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired DataSource dataSource,
                          @Autowired WebApplicationContext applicationContext) {
        mockMvc = buildMockMvc(applicationContext);

        teardown(dataSource);
        executeSqlScript(dataSource,
                    "database/categories/add-test-category-to-categories-table.sql",
                    "database/books/add-test-books-to-books-table.sql",
                    "database/bookscategories/add-test-dependencies-to-books-categories-table.sql");
    }

    @AfterAll
    public static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @BeforeEach
    void setUp(@Autowired CategoryRepository categoryRepository) {
        fillCategoryCache(categoryRepository);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        executeSqlScript(dataSource,
                    "database/bookscategories/clear-all-book-category-dependencies.sql",
                    "database/categories/clear-all-categories.sql",
                    "database/books/clear-all-books.sql");
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Get all categories.")
    void getAll_ValidCategoriesCatalog_ShouldReturnAllCategories() throws Exception {
        List<CategoryDto> expectedCategoryDtos = LongStream.range(EXISTING_CATEGORY_ID,
                        EXISTING_CATEGORY_ID + EXPECTED_CATEGORIES_SIZE)
                .mapToObj(TestDataUtil::createTestCategoryDto)
                .toList();

        MvcResult result = createMvcResult(mockMvc, get(URL_CATEGORIES), status().isOk());

        List<CategoryDto> actualCategoryDtos = parsePageContent(result, objectMapper,
                new TypeReference<List<CategoryDto>>() {});

        assertListCategoryDtosAreEqual(expectedCategoryDtos, actualCategoryDtos);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @Sql(scripts = {
            "classpath:database/bookscategories/clear-all-book-category-dependencies.sql",
            "classpath:database/categories/clear-all-categories.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/categories/add-test-category-to-categories-table.sql",
            "classpath:database/bookscategories/add-test-dependencies-to-books-categories-table.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Verify that getAll() method returns empty page when no categories exist.")
    void getAll_GivenEmptyCategoriesCatalog_ShouldReturnEmptyPage() throws Exception {
        MvcResult result = createMvcResult(mockMvc, get(URL_CATEGORIES), status().isOk());

        List<CategoryDto> actualCategoryDtos = parsePageContent(result, objectMapper,
                new TypeReference<List<CategoryDto>>() {});

        assertThat(actualCategoryDtos).isEmpty();
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Get a category by id.")
    void getCategoryById_GivenValidCategory_ShouldReturnCategory() throws Exception {
        CategoryDto expectedCategoryDto = createTestCategoryDto(EXISTING_CATEGORY_ID);

        MvcResult result = createMvcResult(mockMvc, get(URL_VALID_CATEGORY_ID), status().isOk());

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
        MvcResult result = createMvcResult(mockMvc, get(URL_INVALID_CATEGORY_ID),
                status().isNotFound());

        assertErrorResponse(result, objectMapper, NOT_FOUND,
                "Can't find the category by id: " + NOT_EXISTING_CATEGORY_ID);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Verify that an exception is throw when a category is safe deleted.")
    void getByCategoryId_CategoryIsSafeDeleted_ShouldReturnNotFound() throws Exception {
        MvcResult result = createMvcResult(mockMvc, get(URL_SAFE_DELETED_CATEGORY_ID),
                status().isNotFound());

        assertErrorResponse(result, objectMapper, NOT_FOUND,
                "Can't find the category by id: " + SAFE_DELETED_CATEGORY_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN", "USER"})
    @Test
    @Sql(scripts = {
            "classpath:database/categories/restoring-category-id101-after-test.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Deleted a specific category.")
    void delete_GivenValidCategory_ShouldDeleteCategory() throws Exception {
        MvcResult result = createMvcResult(mockMvc, delete(URL_VALID_CATEGORY_ID),
                status().isNoContent());

        assertThat(result.getResponse().getStatus()).isEqualTo(NO_CONTENT);
        MvcResult checkResult = createMvcResult(mockMvc, get(URL_VALID_CATEGORY_ID),
                status().isNotFound());
        assertErrorResponse(checkResult, objectMapper, NOT_FOUND,
                "Can't find the category by id: " + EXISTING_CATEGORY_ID);
        assertThat(categoriesCash).hasSize(EXPECTED_CATEGORIES_SIZE - 1);
        assertThat(categoriesCash).doesNotContainKey(EXISTING_CATEGORY_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when category id doesn't exist.")
    void delete_GivenInvalidCategoryId_ShouldReturnNotFound() throws Exception {
        MvcResult result = createMvcResult(mockMvc, delete(URL_INVALID_CATEGORY_ID),
                status().isNotFound());

        assertErrorResponse(result, objectMapper, NOT_FOUND,
                "Can't delete a category with id: " + NOT_EXISTING_CATEGORY_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when a category is safe deleted.")
    void delete_CategoryIsSafeDeleted_ShouldReturnNotFound() throws Exception {
        MvcResult result = createMvcResult(mockMvc, delete(URL_SAFE_DELETED_CATEGORY_ID),
                status().isNotFound());

        assertErrorResponse(result, objectMapper, NOT_FOUND,
                "Can't delete a category with id: " + SAFE_DELETED_CATEGORY_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @Sql(scripts = "classpath:database/categories/remove-new-test-category-from-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Create a new category.")
    void createCategory_ValidRequestDto_ShouldCreateCategory() throws Exception {
        CategoryDto expectedCategoryDto = createTestCategoryDto(NEW_CATEGORY_ID);
        CreateCategoryRequestDto requestDto = createTestCategoryRequestDto(expectedCategoryDto);
        String jsonResponse = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(mockMvc, post(URL_CATEGORIES), status().isCreated(),
                jsonResponse);

        CategoryDto actualCategoryDto = objectMapper.readValue(
                result.getResponse().getContentAsString(), CategoryDto.class);
        assertCategoryDtosAreEqual(expectedCategoryDto, actualCategoryDto);
        assertThat(categoriesCash).hasSize(EXPECTED_CATEGORIES_SIZE + 1);
        assertThat(categoriesCash).containsKey(NEW_CATEGORY_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when a category already exists.")
    void createCategory_CategoryAlreadyExists_ShouldReturnUnprocessableEntity() throws Exception {
        CategoryDto expectedCategoryDto = createTestCategoryDto(EXISTING_CATEGORY_ID);
        CreateCategoryRequestDto requestDto = createTestCategoryRequestDto(expectedCategoryDto);
        String jsonResponse = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(mockMvc, post(URL_CATEGORIES),
                status().isUnprocessableEntity(), jsonResponse);

        assertErrorResponse(result, objectMapper, UNPROCESSABLE_ENTITY,
                "Can't save category with name: " + expectedCategoryDto.name());
        assertThat(categoriesCash).hasSize(EXPECTED_CATEGORIES_SIZE);
        assertThat(categoriesCash).doesNotContainKey(NEW_CATEGORY_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when category fields are not in valid format.")
    void createCategory_InvalidFormatCategoryFields_ShouldReturnBadRequest() throws Exception {
        CategoryDto expectedCategoryDto = createTestInvalidCategoryDto(EXISTING_CATEGORY_ID);
        CreateCategoryRequestDto requestDto = createTestCategoryRequestDto(expectedCategoryDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(mockMvc, post(URL_CATEGORIES),
                status().isBadRequest(), jsonRequest);

        assertListErrorsResponse(result, objectMapper, EXPECTED_CATEGORY_ERRORS);
        assertThat(categoriesCash).hasSize(EXPECTED_CATEGORIES_SIZE);
        assertThat(categoriesCash).doesNotContainKey(NEW_CATEGORY_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when the category name is blank.")
    void createCategory_BlankCategoryName_ShouldReturnBadRequest() throws Exception {
        CategoryDto expectedCategoryDto = new CategoryDto(NEW_CATEGORY_ID, null, null);
        CreateCategoryRequestDto requestDto = createTestCategoryRequestDto(expectedCategoryDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(mockMvc, post(URL_CATEGORIES),
                status().isBadRequest(), jsonRequest);

        assertListErrorsResponse(result, objectMapper, EXPECTED_CATEGORY_NULL_ERRORS);
        assertThat(categoriesCash).hasSize(EXPECTED_CATEGORIES_SIZE);
        assertThat(categoriesCash).doesNotContainKey(NEW_CATEGORY_ID);
    }

    @WithMockUser(value = "bob@example.com", roles = {"ADMIN"})
    @Test
    @Sql(scripts = "classpath:database/categories/restoring-category-id101-after-test.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Update a specific category.")
    void updateCategory_ValidRequestDto_ShouldUpdateCategory() throws Exception {
        CategoryDto expectedCategoryDto = createTestUpdatedCategoryDto(EXISTING_CATEGORY_ID);
        CreateCategoryRequestDto requestDto = createTestCategoryRequestDto(expectedCategoryDto);
        String jsonResponse = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(mockMvc, put(URL_VALID_CATEGORY_ID),
                status().isOk(), jsonResponse);

        CategoryDto actualCategoryDto = objectMapper.readValue(
                result.getResponse().getContentAsString(), CategoryDto.class);
        assertCategoryDtosAreEqual(expectedCategoryDto, actualCategoryDto);
        assertThat(categoriesCash).hasSize(EXPECTED_CATEGORIES_SIZE);
        assertThat(categoriesCash).containsKey(EXISTING_CATEGORY_ID);
        assertThat(categoriesCash.get(EXISTING_CATEGORY_ID).getName())
                .isEqualTo(expectedCategoryDto.name());
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when the category id is not valid.")
    void updateCategory_InvalidCategoryId_ShouldReturnNotFound() throws Exception {
        CategoryDto expectedCategoryDto = createTestUpdatedCategoryDto(NOT_EXISTING_CATEGORY_ID);
        CreateCategoryRequestDto requestDto = createTestCategoryRequestDto(expectedCategoryDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(mockMvc, put(URL_INVALID_CATEGORY_ID),
                status().isNotFound(), jsonRequest);

        assertErrorResponse(result, objectMapper, NOT_FOUND,
                "Can't find the category by id: " + NOT_EXISTING_CATEGORY_ID);
        assertThat(categoriesCash).hasSize(EXPECTED_CATEGORIES_SIZE);
        assertThat(categoriesCash).doesNotContainKey(NOT_EXISTING_CATEGORY_ID);
    }

    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when category fields are not in valid format.")
    void updateCategory_InvalidFormatCategoryFields_ShouldReturnBadRequest() throws Exception {
        CategoryDto expectedCategoryDto = createTestInvalidCategoryDto(EXISTING_CATEGORY_ID);
        CreateCategoryRequestDto requestDto = createTestCategoryRequestDto(expectedCategoryDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(mockMvc, put(URL_VALID_CATEGORY_ID),
                status().isBadRequest(), jsonRequest);

        assertListErrorsResponse(result, objectMapper, EXPECTED_CATEGORY_ERRORS);
        assertThat(categoriesCash).hasSize(EXPECTED_CATEGORIES_SIZE);
        assertThat(categoriesCash.get(EXISTING_CATEGORY_ID).getName())
                .isNotEqualTo(expectedCategoryDto.name());
    }

    @WithMockUser(username = "bob@example.com", roles = {"ASDMIN"})
    @Test
    @DisplayName("Verify that an exception is throw when the category fields are null.")
    void updateCategory_NullCategoryFields_ShouldReturnBadRequest() throws Exception {
        CategoryDto expectedCategoryDto = new CategoryDto(EXISTING_CATEGORY_ID, null, null);
        CreateCategoryRequestDto requestDto = createTestCategoryRequestDto(expectedCategoryDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = createJsonMvcResult(mockMvc, put(URL_VALID_CATEGORY_ID),
                status().isBadRequest(), jsonRequest);

        assertListErrorsResponse(result, objectMapper, EXPECTED_CATEGORY_NULL_ERRORS);
        assertThat(categoriesCash).hasSize(EXPECTED_CATEGORIES_SIZE);
        assertThat(categoriesCash.get(EXISTING_CATEGORY_ID).getName()).isNotNull();
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Get books by a specific category.")
    void getBooksByCategoryId_ValidRequestDto_ShouldReturnBooksByCategory() throws Exception {
        MvcResult result = createMvcResult(mockMvc, get(URL_GET_BOOKS_BY_VALID_CATEGORY_ID),
                status().isOk());

        List<BookDtoWithoutCategoryIds> actualBookDtos = parsePageContent(result, objectMapper,
                new TypeReference<List<BookDtoWithoutCategoryIds>>() {});
        assertThat(actualBookDtos).hasSize(EXPECTED_BOOK_DTOS_SIZE);
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Get empty page when no books found by the category id.")
    void getBooksByCategoryId_NoBooksByCategoryId_ShouldReturnEmptyPage() throws Exception {
        MvcResult result = createMvcResult(mockMvc, get(URL_GET_BOOKS_BY_ALTERNATIVE_CATEGORY_ID),
                status().isOk());

        List<BookDtoWithoutCategoryIds> actualBookDtos = parsePageContent(result, objectMapper,
                new TypeReference<List<BookDtoWithoutCategoryIds>>() {});
        assertThat(actualBookDtos).isEmpty();
    }

    @WithMockUser(username = "alice@example.com")
    @Test
    @DisplayName("Verify that an exception is throw when category id is not valid.")
    void getBooksByCategoryId_NoValidCategoryId_ShouldReturnNotFound() throws Exception {
        MvcResult result = createMvcResult(mockMvc, get(URL_GET_BOOKS_BY_INVALID_CATEGORY_ID),
                status().isNotFound());

        assertErrorResponse(result, objectMapper, NOT_FOUND,
                "Can't get books with category ID: " + NOT_EXISTING_CATEGORY_ID);
    }
}
