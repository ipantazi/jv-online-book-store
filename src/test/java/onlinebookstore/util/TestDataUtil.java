package onlinebookstore.util;

import static onlinebookstore.service.category.CategoryServiceImpl.categoriesCash;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import onlinebookstore.dto.book.BookDto;
import onlinebookstore.dto.book.BookDtoWithoutCategoryIds;
import onlinebookstore.dto.book.CreateBookRequestDto;
import onlinebookstore.dto.category.CategoryDto;
import onlinebookstore.dto.category.CreateCategoryRequestDto;
import onlinebookstore.model.Book;
import onlinebookstore.model.Category;
import onlinebookstore.repository.category.CategoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class TestDataUtil {
    public static final Long EXISTING_BOOK_ID = 101L;
    public static final Long SAFE_DELETED_BOOK_ID = 104L;
    public static final Long NEW_BOOK_ID = 105L;
    public static final Long NOT_EXISTING_BOOK_ID = 999L;
    public static final Long EXISTING_CATEGORY_ID = 101L;
    public static final Long ALTERNATIVE_CATEGORY_ID = 102L;
    public static final Long SAFE_DELETED_CATEGORY_ID = 103L;
    public static final Long NEW_CATEGORY_ID = 104L;
    public static final Long NOT_EXISTING_CATEGORY_ID = 999L;
    public static final int EXPECTED_BOOK_DTOS_SIZE = 3;
    public static final int EXPECTED_CATEGORIES_SIZE = 2;
    public static final String EXISTING_ISBN = "1000000000101";
    public static final String SOFT_DELETED_BOOK_ISBN = "1000000000104";
    public static final String NOT_EXISTING_ISBN = "9999999999999";
    public static final String TEST_LONG_DATA = "TESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTEST"
            + "TESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTTESTT";
    public static final Pageable pageableBook = PageRequest.of(0, 10,
            Sort.by("title").ascending());
    public static final Pageable pageableCategory = PageRequest.of(0, 10,
            Sort.by("name").ascending());

    public static void fillCategoryCache(CategoryRepository categoryRepository) {
        categoriesCash.clear();
        categoryRepository.findAll().forEach(category ->
                categoriesCash.put(category.getId(), category));
    }

    public static BookDto createTestBookDto(Long id) {
        BookDto bookDto = new BookDto();
        bookDto.setId(id);
        bookDto.setTitle("Test Book " + id);
        bookDto.setAuthor("Test Author " + id);
        bookDto.setIsbn(String.valueOf(1000000000000L + id));
        bookDto.setPrice(new BigDecimal(id));
        bookDto.setDescription("Test Description");
        bookDto.setCoverImage("http://example.com/test-cover.jpg");
        bookDto.setCategoryIds(Set.of(EXISTING_CATEGORY_ID));
        return bookDto;
    }

    public static List<BookDto> createTestBookDtoList(Long startId, int size) {
        return LongStream.range(startId, startId + size)
                .mapToObj(TestDataUtil::createTestBookDto)
                .toList();
    }

    public static CreateBookRequestDto createTestBookRequestDto(BookDto bookDto) {
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

    public static BookDto createTestInvalidBookDto() {
        BookDto bookDto = new BookDto();
        bookDto.setId(EXISTING_BOOK_ID);
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

    public static BookDto createTestUpdatedBookDto(Long id) {
        BookDto bookDto = createTestBookDto(id);
        bookDto.setTitle("Updated Title");
        bookDto.setAuthor("Updated Author");
        bookDto.setPrice(BigDecimal.TEN);
        bookDto.setDescription("Updated Description");
        bookDto.setCoverImage("http://update_example.com/test-cover.jpg");
        bookDto.setCategoryIds(Set.of(ALTERNATIVE_CATEGORY_ID));
        return bookDto;
    }

    public static Book createTestBook(BookDto bookDto) {
        Book book = new Book();
        book.setId(bookDto.getId());
        book.setTitle(bookDto.getTitle());
        book.setAuthor(bookDto.getAuthor());
        book.setIsbn(bookDto.getIsbn());
        book.setPrice(bookDto.getPrice());
        book.setDescription(bookDto.getDescription());
        book.setCoverImage(bookDto.getCoverImage());
        book.setCategories(createTestCategoriesSet(book.getId(), 1));
        return book;
    }

    public static BookDtoWithoutCategoryIds createTestBookDtoWithoutCategoryId(BookDto bookDto) {
        return new BookDtoWithoutCategoryIds(
                bookDto.getId(),
                bookDto.getTitle(),
                bookDto.getAuthor(),
                bookDto.getIsbn(),
                bookDto.getPrice(),
                bookDto.getDescription(),
                bookDto.getCoverImage()
        );
    }

    public static CategoryDto createTestCategoryDto(Long id) {
        return new CategoryDto(
                id,
                "Test category " + id,
                "Description test"
        );
    }

    public static Category createTestCategory(CategoryDto categoryDto) {
        Category category = new Category();
        category.setId(categoryDto.id());
        category.setName(categoryDto.name());
        return category;
    }

    public static Category createTestCategory(Long id) {
        Category category = new Category();
        category.setId(id);
        category.setName("Test category " + id);
        category.setDescription("Description test");
        return category;
    }

    public static Set<Category> createTestCategoriesSet(Long startId, int size) {
        return LongStream.range(startId, startId + size)
                .mapToObj(TestDataUtil::createTestCategory)
                .collect(Collectors.toSet());
    }

    public static Set<Category> convertCategoryIdsToCategories(Set<Long> categoryIds) {
        return categoryIds.stream()
                .map(TestDataUtil::createTestCategory)
                .collect(Collectors.toSet());
    }

    public static CreateCategoryRequestDto createTestCategoryRequestDto(CategoryDto categoryDto) {
        return new CreateCategoryRequestDto(categoryDto.name(), categoryDto.description());
    }

    public static CategoryDto createTestInvalidCategoryDto(Long id) {
        int maxCategoryDescriptionLength = 500;
        return new CategoryDto(
                id,
                "IT",
                "T".repeat(maxCategoryDescriptionLength + 1));
    }

    public static CategoryDto createTestUpdatedCategoryDto(Long id) {
        return new CategoryDto(id, "Updated name", "Updated description");
    }
}
