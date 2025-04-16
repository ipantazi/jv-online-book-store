package onlinebookstore.service.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import onlinebookstore.dto.book.BookDto;
import onlinebookstore.dto.book.BookDtoWithoutCategoryIds;
import onlinebookstore.dto.book.BookSearchParametersDto;
import onlinebookstore.dto.book.CreateBookRequestDto;
import onlinebookstore.exception.DataProcessingException;
import onlinebookstore.exception.EntityNotFoundException;
import onlinebookstore.mapper.BookMapper;
import onlinebookstore.model.Book;
import onlinebookstore.model.CartItem;
import onlinebookstore.model.Category;
import onlinebookstore.repository.SpecificationBuilder;
import onlinebookstore.repository.book.BookRepository;
import onlinebookstore.repository.cartitem.CartItemRepository;
import onlinebookstore.service.category.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
    private static final Long BOOK_ID = 1L;
    private static final int INDEX_FIRST = 0;
    private static final Long CATEGORY_ID = 1L;
    private static final Long INVALID_CATEGORY_ID = 999L;
    private static Set<Category> categories;
    private final Set<Long> categoryIds = Set.of(1L, 2L);
    private final List<String> ignoringFields = List.of("id", "categoriesIds");
    private final Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private SpecificationBuilder<Book> specificationBuilder;
    @Mock
    private Specification<Book> bookSpecification;
    @Mock
    private CartItemRepository cartItemRepository;
    @InjectMocks
    private BookServiceImpl bookService;
    private Book book;
    private List<Book> books;

    @BeforeAll
    static void beforeAll() throws Exception {
        categories = LongStream.range(1, 3)
                .mapToObj(id -> {
                    Category category = new Category();
                    category.setId(id);
                    category.setName("Category id: " + id);
                    return category;
                })
                .collect(Collectors.toSet());
    }

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(BOOK_ID);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("9780061124952");
        book.setPrice(BigDecimal.TEN);
        book.setDescription("Test Description");
        book.setCategories(categories);

        books = Collections.singletonList(book);
    }

    @Test
    @DisplayName("Verify save() method works")
    public void save_ValidCreateBookRequestDto_ReturnsBookDto() {
        Long existsStatus = 0L;
        CreateBookRequestDto bookRequestDto = createTestBookRequestDto();
        BookDto expectedBookDto = createTestBookDto();

        when(bookRepository.existsByIsbnIncludingDeleted(bookRequestDto.isbn()))
                .thenReturn(existsStatus);
        when(bookMapper.toBookEntity(bookRequestDto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toBookDto(book)).thenReturn(expectedBookDto);

        BookDto actualBookDto = bookService.save(bookRequestDto);

        assertNotNull(actualBookDto);
        assertBookDtosAreEqual(actualBookDto, expectedBookDto);
        verify(bookRepository, times(1)).existsByIsbnIncludingDeleted(bookRequestDto.isbn());
        verify(bookRepository, times(1)).save(book);
        verify(bookMapper, times(1)).toBookDto(book);
        verify(bookMapper, times(1)).toBookEntity(bookRequestDto);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a book already exists.")
    public void save_BookAlreadyExist_throwsException() {
        Long existsStatus = 1L;
        String existingIsbn = book.getIsbn();

        when(bookRepository.existsByIsbnIncludingDeleted(existingIsbn))
                .thenReturn(existsStatus);

        assertThatThrownBy(() -> bookService.save(createTestBookRequestDto()))
                .isInstanceOf(DataProcessingException.class)
                .hasMessage("Can't save a book with this ISBN: " + existingIsbn);

        verify(bookRepository, times(1))
                .existsByIsbnIncludingDeleted(existingIsbn);
        verify(bookRepository, never()).save(any(Book.class));
        verifyNoMoreInteractions(bookRepository);
        verifyNoInteractions(bookMapper);
    }

    @Test
    @DisplayName("Verify update() method works.")
    public void update_ValidCreateBookRequestDto_ReturnsBookDto() {
        final CreateBookRequestDto createBookRequestDto = createTestBookRequestDto();
        final BookDto expectedBookDto = createTestBookDto();
        String dataBeforeUpdate = "before update";
        BigDecimal priceBeforeUpdate = BigDecimal.ONE;

        book.setTitle(dataBeforeUpdate);
        book.setAuthor(dataBeforeUpdate);
        book.setPrice(priceBeforeUpdate);
        book.setDescription(dataBeforeUpdate);
        book.setCategories(new HashSet<>());

        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));
        mockBookMapperUpdateBookEntity(createBookRequestDto, book,
                dataBeforeUpdate, priceBeforeUpdate);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toBookDto(book)).thenReturn(expectedBookDto);

        BookDto actualBookDto = bookService.update(BOOK_ID, createBookRequestDto);

        assertNotNull(actualBookDto);
        assertBookDtosAreEqual(actualBookDto, expectedBookDto);
        verify(bookRepository, times(1)).findById(BOOK_ID);
        verify(bookMapper, times(1)).updateBookEntity(createBookRequestDto, book);
        verify(bookRepository, times(1)).save(book);
        verify(bookMapper, times(1)).toBookDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a book doesn't exists.")
    public void update_BookNotExist_throwsException() {
        CreateBookRequestDto bookRequestDto = createTestBookRequestDto();

        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.update(BOOK_ID, bookRequestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Can`t find the book by id: " + BOOK_ID);

        verify(bookRepository, times(1)).findById(BOOK_ID);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a ISBNs are not equal.")
    public void update_IsbnNotExist_throwsException() {
        CreateBookRequestDto bookRequestDto = createTestBookRequestDto();
        final String expectedIsbn = bookRequestDto.isbn();

        String actualIsbn = "1111111111111";
        book.setIsbn(actualIsbn);
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.update(BOOK_ID, bookRequestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Can't update the book. Invalid book id: " + BOOK_ID
                        + " or isbn: " + bookRequestDto.isbn());

        assertThat(actualIsbn).isNotEqualTo(expectedIsbn);
        verify(bookRepository, times(1)).findById(BOOK_ID);
        verify(bookRepository, never()).save(any(Book.class));
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Verify findAll() method works.")
    public void findAll_ValidPageable_ReturnsAllBooks() {
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
        BookDto expectedBookDto = createTestBookDto();

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(bookMapper.toBookDto(book)).thenReturn(expectedBookDto);

        Page<BookDto> actualBookDtoPage = bookService.findAll(pageable);
        List<BookDto> actualBookDtos = actualBookDtoPage.getContent();

        assertThat(actualBookDtos).hasSize(1).containsExactly(expectedBookDto);
        assertBookDtosAreEqual(actualBookDtos.get(INDEX_FIRST), expectedBookDto);
        assertPage(actualBookDtoPage, bookPage);
        verify(bookRepository, times(1)).findAll(pageable);
        verify(bookMapper, times(1)).toBookDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify findAll() returns empty page when no book exist.")
    public void findAll_NoBooks_ReturnsEmptyPage() {
        Page<Book> bookPage = Page.empty(pageable);

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);

        Page<BookDto> actualBookDtoPage = bookService.findAll(pageable);

        assertThat(actualBookDtoPage).isEmpty();
        assertPage(actualBookDtoPage, bookPage);
        verify(bookRepository, times(1)).findAll(pageable);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Verify search() method works.")
    public void search_ValidSpecification_ReturnsSpecificationBooks() {
        BookSearchParametersDto params = new BookSearchParametersDto(
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                List.of(book.getPrice().subtract(BigDecimal.TWO),
                        book.getPrice().add(BigDecimal.TWO))
        );
        BookDto expectedBookDto = createTestBookDto();

        when(specificationBuilder.build(params)).thenReturn(bookSpecification);
        when(bookRepository.findAll(bookSpecification)).thenReturn(books);
        when(bookMapper.toBookDto(book)).thenReturn(expectedBookDto);

        List<BookDto> actualBookDtos = bookService.search(params);

        assertThat(actualBookDtos).hasSize(1).containsExactly(expectedBookDto);
        assertBookDtosAreEqual(actualBookDtos.get(INDEX_FIRST), expectedBookDto);
        verify(specificationBuilder, times(1)).build(params);
        verify(bookRepository, times(1)).findAll(bookSpecification);
        verify(bookMapper, times(1)).toBookDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper, specificationBuilder);
    }

    @Test
    @DisplayName("Verify findById() method works.")
    public void findById_ValidId_ReturnsBook() {
        BookDto expectedBookDto = createTestBookDto();

        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));
        when(bookMapper.toBookDto(book)).thenReturn(expectedBookDto);

        BookDto actualBookDto = bookService.findById(BOOK_ID);

        assertThat(actualBookDto).isNotNull();
        assertThat(actualBookDto).isEqualTo(expectedBookDto);
        verify(bookRepository, times(1)).findById(BOOK_ID);
        verify(bookMapper, times(1)).toBookDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when book doesn't exists.")
    public void findById_BookIdNotExist_ThrowsException() {
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findById(BOOK_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can`t find the book by id: " + BOOK_ID);

        verify(bookRepository, times(1)).findById(BOOK_ID);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Verify deleteById() method works.")
    public void deleteById_ValidIdAndCartItem_SafeDeleteBook() {
        CartItem cartItem = new CartItem();

        when(bookRepository.existsById(BOOK_ID)).thenReturn(Boolean.TRUE);
        when(cartItemRepository.findByBookId(BOOK_ID)).thenReturn(Optional.of(cartItem));

        bookService.deleteById(BOOK_ID);

        verify(bookRepository, times(1)).existsById(BOOK_ID);
        verify(cartItemRepository, times(1)).findByBookId(BOOK_ID);
        verify(cartItemRepository, times(1)).delete(cartItem);
        verify(bookRepository, times(1)).deleteById(BOOK_ID);
        verifyNoMoreInteractions(bookRepository, cartItemRepository);
    }

    @Test
    @DisplayName("Verify deleteById() method works when doesn't exist cart item.")
    public void deleteById_CartItemNotExist_SafeDeleteBook() {
        when(bookRepository.existsById(BOOK_ID)).thenReturn(Boolean.TRUE);
        when(cartItemRepository.findByBookId(BOOK_ID)).thenReturn(Optional.empty());

        bookService.deleteById(BOOK_ID);

        verify(bookRepository, times(1)).existsById(BOOK_ID);
        verify(cartItemRepository, times(1)).findByBookId(BOOK_ID);
        verify(bookRepository, times(1)).deleteById(BOOK_ID);
        verifyNoMoreInteractions(bookRepository, cartItemRepository);
    }

    @Test
    @DisplayName("Verify that an exception is throw when book doesn't exist.")
    public void deleteById_BookIdNotExist_ThrowsException() {
        when(bookRepository.existsById(BOOK_ID)).thenReturn(Boolean.FALSE);

        assertThatThrownBy(() -> bookService.deleteById(BOOK_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't delete a book with ID: " + BOOK_ID);

        verify(bookRepository, times(1)).existsById(BOOK_ID);
        verify(bookRepository, never()).deleteById(anyLong());
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Verify getByCategoryId() method works.")
    public void getByCategoryId_ValidCategoryId_ReturnsBook() throws Exception {
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
        BookDtoWithoutCategoryIds expectedBookDto = createTestBookDtoWithoutCategoryId();

        mockCategoriesCash();
        when(bookRepository.findAllByCategoryId(CATEGORY_ID, pageable)).thenReturn(bookPage);
        when(bookMapper.toBookDtoWithoutCategoryIds(book)).thenReturn(expectedBookDto);

        Page<BookDtoWithoutCategoryIds> actualBookDtoPage = bookService
                .getByCategoryId(CATEGORY_ID, pageable);

        assertThat(actualBookDtoPage).isNotNull();
        assertThat(actualBookDtoPage).hasSize(1).containsExactly(expectedBookDto);
        assertPage(actualBookDtoPage, bookPage);
        verify(bookRepository, times(1)).findAllByCategoryId(CATEGORY_ID, pageable);
        verify(bookMapper, times(1)).toBookDtoWithoutCategoryIds(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify getByCategoryId() returns empty when no books found.")
    public void getByCategoryId_BookNotExistByCategoryId_ReturnsEmptyPage() throws Exception {
        Page<Book> bookPage = Page.empty(pageable);

        mockCategoriesCash();
        when(bookRepository.findAllByCategoryId(CATEGORY_ID, pageable)).thenReturn(bookPage);

        Page<BookDtoWithoutCategoryIds> actualBookDtoPage = bookService
                .getByCategoryId(CATEGORY_ID, pageable);

        assertThat(actualBookDtoPage).isNotNull().isEmpty();
        assertPage(actualBookDtoPage, bookPage);
        verify(bookRepository, times(1)).findAllByCategoryId(CATEGORY_ID, pageable);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Verify that an exception is throw when category id doesn't exist.")
    public void getByCategoryId_CategoryIdNotExist_ThrowsException() throws Exception {
        mockCategoriesCash();

        assertThatThrownBy(() -> bookService.getByCategoryId(INVALID_CATEGORY_ID, pageable))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't get books with category ID: " + INVALID_CATEGORY_ID);

        verifyNoInteractions(bookRepository, bookMapper);
    }

    private void mockCategoriesCash() throws Exception {
        Field field = CategoryServiceImpl.class.getDeclaredField("categoriesCash");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Long, Category> cacheInstance = (Map<Long, Category>) field.get(null);
        cacheInstance.clear();
        cacheInstance.putAll(categories.stream()
                .collect(Collectors.toMap(Category::getId, category -> category)));
    }

    private CreateBookRequestDto createTestBookRequestDto() {
        return new CreateBookRequestDto(
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPrice(),
                book.getDescription(),
                null,
                categoryIds
        );
    }

    private BookDto createTestBookDto() {
        BookDto bookDto = new BookDto();
        bookDto.setId(book.getId());
        bookDto.setTitle(book.getTitle());
        bookDto.setAuthor(book.getAuthor());
        bookDto.setIsbn(book.getIsbn());
        bookDto.setPrice(book.getPrice());
        bookDto.setDescription(book.getDescription());
        bookDto.setCategoryIds(categoryIds);
        return bookDto;
    }

    private BookDtoWithoutCategoryIds createTestBookDtoWithoutCategoryId() {
        return new BookDtoWithoutCategoryIds(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPrice(),
                book.getDescription(),
                null
        );
    }

    private void assertBookDtosAreEqual(BookDto actual, BookDto expected) {
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields(ignoringFields.get(0), ignoringFields.get(1))
                .isEqualTo(expected);
        assertThat(actual.getCategoryIds())
                .containsExactlyInAnyOrderElementsOf(expected.getCategoryIds());
    }

    private void assertPage(Page<?> actual, Page<Book> expected) {
        assertThat(actual.getTotalElements()).isEqualTo(expected.getTotalElements());
        assertThat(actual.getSize()).isEqualTo(expected.getSize());
        assertThat(actual.getSort()).isEqualTo(expected.getSort());
        assertThat(actual.getNumber()).isEqualTo(expected.getNumber());
    }

    private void mockBookMapperUpdateBookEntity(
            CreateBookRequestDto createBookRequestDto,
            Book book,
            String dataBeforeUpdate,
            BigDecimal priceBeforeUpdate
    ) {
        doAnswer(invocation -> {
            CreateBookRequestDto dto = invocation.getArgument(0);
            Book entity = invocation.getArgument(1);

            assertThat(dto.title()).isNotEqualTo(entity.getTitle());
            assertThat(dto.author()).isNotEqualTo(entity.getAuthor());
            assertThat(dto.description()).isNotEqualTo(entity.getDescription());
            assertThat(dto.price())
                    .usingComparator(BigDecimal::compareTo)
                    .isNotEqualTo(entity.getPrice());

            entity.setTitle(dto.title());
            entity.setAuthor(dto.author());
            entity.setPrice(dto.price());
            entity.setDescription(dto.description());
            entity.setCoverImage(dto.coverImage());
            entity.setCategories(categories);

            assertThat(entity)
                    .extracting(Book::getTitle, Book::getAuthor, Book::getDescription)
                    .doesNotContain(dataBeforeUpdate);
            assertThat(entity.getPrice())
                    .usingComparator(BigDecimal::compareTo)
                    .isNotEqualTo(priceBeforeUpdate);
            assertThat(entity.getCategories()).hasSize(dto.categoryIds().size());

            return null;
        }).when(bookMapper).updateBookEntity(createBookRequestDto, book);
    }
}
