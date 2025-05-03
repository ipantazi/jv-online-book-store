package onlinebookstore.service.book;

import static onlinebookstore.util.TestDataUtil.ALTERNATIVE_CATEGORY_ID;
import static onlinebookstore.util.TestDataUtil.BOOK_DTO_IGNORING_FIELDS;
import static onlinebookstore.util.TestDataUtil.BOOK_PAGEABLE;
import static onlinebookstore.util.TestDataUtil.EXISTING_BOOK_ID;
import static onlinebookstore.util.TestDataUtil.EXISTING_CATEGORY_ID;
import static onlinebookstore.util.TestDataUtil.EXPECTED_CATEGORIES_SIZE;
import static onlinebookstore.util.TestDataUtil.NEW_BOOK_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_BOOK_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_CATEGORY_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_ISBN;
import static onlinebookstore.util.TestDataUtil.createTestBook;
import static onlinebookstore.util.TestDataUtil.createTestBookDto;
import static onlinebookstore.util.TestDataUtil.createTestBookDtoWithoutCategoryId;
import static onlinebookstore.util.TestDataUtil.createTestBookRequestDto;
import static onlinebookstore.util.TestDataUtil.createTestCategorySet;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertObjectsAreEqualIgnoringFields;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertPageMetadataEquals;
import static onlinebookstore.util.service.ServiceTestUtil.mockCategoriesCash;
import static onlinebookstore.util.service.book.BookMockUtil.mockBookMapperUpdateBookEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
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

    @BeforeAll
    static void beforeAll() throws Exception {
        Set<Category> categories = createTestCategorySet(
                EXISTING_CATEGORY_ID,
                EXPECTED_CATEGORIES_SIZE
        );
        mockCategoriesCash(categories);
    }

    @Test
    @DisplayName("Verify save() method works")
    public void save_ValidCreateBookRequestDto_ReturnsBookDto() {
        // Given
        Long existsStatus = 0L;
        BookDto expectedBookDto = createTestBookDto(NEW_BOOK_ID);
        CreateBookRequestDto bookRequestDto = createTestBookRequestDto(expectedBookDto);
        Book book = createTestBook(expectedBookDto);
        when(bookRepository.existsByIsbnIncludingDeleted(bookRequestDto.isbn()))
                .thenReturn(existsStatus);
        when(bookMapper.toBookEntity(bookRequestDto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toBookDto(book)).thenReturn(expectedBookDto);

        // When
        BookDto actualBookDto = bookService.save(bookRequestDto);

        // Then
        assertObjectsAreEqualIgnoringFields(actualBookDto, expectedBookDto,
                BOOK_DTO_IGNORING_FIELDS);
        verify(bookRepository, times(1)).existsByIsbnIncludingDeleted(bookRequestDto.isbn());
        verify(bookRepository, times(1)).save(book);
        verify(bookMapper, times(1)).toBookDto(book);
        verify(bookMapper, times(1)).toBookEntity(bookRequestDto);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a book already exists.")
    public void save_BookAlreadyExist_throwsException() {
        // Given
        Long existsStatus = 1L;
        BookDto expectedBookDto = createTestBookDto(EXISTING_BOOK_ID);
        CreateBookRequestDto bookRequestDto = createTestBookRequestDto(expectedBookDto);
        String existingIsbn = bookRequestDto.isbn();
        when(bookRepository.existsByIsbnIncludingDeleted(existingIsbn))
                .thenReturn(existsStatus);

        // When
        assertThatThrownBy(() -> bookService.save(bookRequestDto))
                .isInstanceOf(DataProcessingException.class)
                .hasMessage("Can't save a book with this ISBN: " + existingIsbn);

        // Then
        verify(bookRepository, times(1))
                .existsByIsbnIncludingDeleted(existingIsbn);
        verify(bookRepository, never()).save(any(Book.class));
        verifyNoMoreInteractions(bookRepository);
        verifyNoInteractions(bookMapper);
    }

    @Test
    @DisplayName("Verify update() method works.")
    public void update_ValidCreateBookRequestDto_ReturnsBookDto() {
        // Given
        BookDto expectedBookDto = createTestBookDto(EXISTING_BOOK_ID);
        final CreateBookRequestDto bookRequestDto = createTestBookRequestDto(expectedBookDto);
        Book book = createTestBook(expectedBookDto);
        String dataBeforeUpdate = "before update";
        BigDecimal priceBeforeUpdate = BigDecimal.ONE;
        book.setTitle(dataBeforeUpdate);
        book.setAuthor(dataBeforeUpdate);
        book.setPrice(priceBeforeUpdate);
        book.setDescription(dataBeforeUpdate);
        book.setCategories(new HashSet<>());
        when(bookRepository.findById(EXISTING_BOOK_ID)).thenReturn(Optional.of(book));
        mockBookMapperUpdateBookEntity(bookMapper, bookRequestDto, book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toBookDto(book)).thenReturn(expectedBookDto);

        // When
        BookDto actualBookDto = bookService.update(EXISTING_BOOK_ID, bookRequestDto);

        // Then
        assertObjectsAreEqualIgnoringFields(
                actualBookDto,
                expectedBookDto,
                BOOK_DTO_IGNORING_FIELDS
        );
        verify(bookRepository, times(1)).findById(EXISTING_BOOK_ID);
        verify(bookMapper, times(1)).updateBookEntity(bookRequestDto, book);
        verify(bookRepository, times(1)).save(book);
        verify(bookMapper, times(1)).toBookDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a book doesn't exists.")
    public void update_BookNotExist_ThrowsException() {
        // Given
        BookDto expectedBookDto = createTestBookDto(NOT_EXISTING_BOOK_ID);
        CreateBookRequestDto bookRequestDto = createTestBookRequestDto(expectedBookDto);
        when(bookRepository.findById(NOT_EXISTING_BOOK_ID)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> bookService.update(NOT_EXISTING_BOOK_ID, bookRequestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Can`t find the book by id: " + NOT_EXISTING_BOOK_ID);

        // Then
        verify(bookRepository, times(1)).findById(NOT_EXISTING_BOOK_ID);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a ISBNs are not equal.")
    public void update_IsbnNotExist_ThrowsException() {
        // Given
        BookDto expectedBookDto = createTestBookDto(EXISTING_BOOK_ID);
        Book book = createTestBook(expectedBookDto);
        CreateBookRequestDto bookRequestDto = new CreateBookRequestDto(
                expectedBookDto.getTitle(),
                expectedBookDto.getAuthor(),
                NOT_EXISTING_ISBN,
                expectedBookDto.getPrice(),
                expectedBookDto.getDescription(),
                expectedBookDto.getCoverImage(),
                expectedBookDto.getCategoryIds()
        );
        when(bookRepository.findById(EXISTING_BOOK_ID)).thenReturn(Optional.of(book));

        // When
        assertThatThrownBy(() -> bookService.update(EXISTING_BOOK_ID, bookRequestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Can't update the book. Invalid book id: " + EXISTING_BOOK_ID
                        + " or isbn: " + bookRequestDto.isbn());

        // Then
        verify(bookRepository, times(1)).findById(EXISTING_BOOK_ID);
        verify(bookRepository, never()).save(any(Book.class));
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Verify findAll() method works.")
    public void findAll_ValidPageable_ReturnsAllBooks() {
        // Given
        BookDto expectedBookDto = createTestBookDto(EXISTING_BOOK_ID);
        Book book = createTestBook(expectedBookDto);
        List<Book> expectedBooks = Collections.singletonList(book);
        Page<Book> bookPage = new PageImpl<>(expectedBooks, BOOK_PAGEABLE, expectedBooks.size());
        when(bookRepository.findAll(BOOK_PAGEABLE)).thenReturn(bookPage);
        when(bookMapper.toBookDto(book)).thenReturn(expectedBookDto);

        // When
        Page<BookDto> actualBookDtoPage = bookService.findAll(BOOK_PAGEABLE);
        List<BookDto> actualBookDtos = actualBookDtoPage.getContent();

        // Then
        assertThat(actualBookDtos).hasSize(1).containsExactly(expectedBookDto);
        assertObjectsAreEqualIgnoringFields(
                actualBookDtos.get(0),
                expectedBookDto,
                BOOK_DTO_IGNORING_FIELDS
        );
        assertPageMetadataEquals(actualBookDtoPage, bookPage);
        verify(bookRepository, times(1)).findAll(BOOK_PAGEABLE);
        verify(bookMapper, times(1)).toBookDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify findAll() returns empty page when no book exists.")
    public void findAll_NoBooks_ReturnsEmptyPage() {
        // Given
        Page<Book> bookPage = Page.empty(BOOK_PAGEABLE);
        when(bookRepository.findAll(BOOK_PAGEABLE)).thenReturn(bookPage);

        // When
        Page<BookDto> actualBookDtoPage = bookService.findAll(BOOK_PAGEABLE);

        // Then
        assertThat(actualBookDtoPage).isEmpty();
        assertPageMetadataEquals(actualBookDtoPage, bookPage);
        verify(bookRepository, times(1)).findAll(BOOK_PAGEABLE);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Verify search() method works.")
    public void search_ValidSpecification_ReturnsSpecificationBooks() {
        // Given
        BookDto expectedBookDto = createTestBookDto(EXISTING_BOOK_ID);
        BookSearchParametersDto params = new BookSearchParametersDto(
                expectedBookDto.getTitle(),
                expectedBookDto.getAuthor(),
                expectedBookDto.getIsbn(),
                List.of(expectedBookDto.getPrice().subtract(BigDecimal.TWO),
                        expectedBookDto.getPrice().add(BigDecimal.TWO))
        );
        Book book = createTestBook(expectedBookDto);
        List<Book> books = Collections.singletonList(book);
        when(specificationBuilder.build(params)).thenReturn(bookSpecification);
        when(bookRepository.findAll(bookSpecification)).thenReturn(books);
        when(bookMapper.toBookDto(book)).thenReturn(expectedBookDto);

        // When
        List<BookDto> actualBookDtos = bookService.search(params);

        // Then
        assertThat(actualBookDtos).hasSize(1).containsExactly(expectedBookDto);
        assertObjectsAreEqualIgnoringFields(
                actualBookDtos.get(0),
                expectedBookDto,
                BOOK_DTO_IGNORING_FIELDS
        );
        verify(specificationBuilder, times(1)).build(params);
        verify(bookRepository, times(1)).findAll(bookSpecification);
        verify(bookMapper, times(1)).toBookDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper, specificationBuilder);
    }

    @Test
    @DisplayName("Verify findById() method works.")
    public void findById_ValidId_ReturnsBook() {
        // Given
        BookDto expectedBookDto = createTestBookDto(EXISTING_BOOK_ID);
        Book book = createTestBook(expectedBookDto);
        when(bookRepository.findById(EXISTING_BOOK_ID)).thenReturn(Optional.of(book));
        when(bookMapper.toBookDto(book)).thenReturn(expectedBookDto);

        // When
        BookDto actualBookDto = bookService.findById(EXISTING_BOOK_ID);

        // Then
        assertThat(actualBookDto).isNotNull();
        assertObjectsAreEqualIgnoringFields(
                actualBookDto,
                expectedBookDto,
                BOOK_DTO_IGNORING_FIELDS
        );
        verify(bookRepository, times(1)).findById(EXISTING_BOOK_ID);
        verify(bookMapper, times(1)).toBookDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when book doesn't exists.")
    public void findById_BookIdNotExist_ThrowsException() {
        // Given
        when(bookRepository.findById(NOT_EXISTING_BOOK_ID)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> bookService.findById(NOT_EXISTING_BOOK_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can`t find the book by id: " + NOT_EXISTING_BOOK_ID);

        // Then
        verify(bookRepository, times(1)).findById(NOT_EXISTING_BOOK_ID);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Verify deleteById() method works.")
    public void deleteById_ValidIdAndCartItem_SafeDeleteBook() {
        // Given
        CartItem cartItem = new CartItem();
        when(bookRepository.existsById(EXISTING_BOOK_ID)).thenReturn(Boolean.TRUE);
        when(cartItemRepository.findByBookId(EXISTING_BOOK_ID)).thenReturn(Optional.of(cartItem));

        // When
        bookService.deleteById(EXISTING_BOOK_ID);

        // Then
        verify(bookRepository, times(1)).existsById(EXISTING_BOOK_ID);
        verify(cartItemRepository, times(1)).findByBookId(EXISTING_BOOK_ID);
        verify(cartItemRepository, times(1)).delete(cartItem);
        verify(bookRepository, times(1)).deleteById(EXISTING_BOOK_ID);
        verifyNoMoreInteractions(bookRepository, cartItemRepository);
    }

    @Test
    @DisplayName("Verify deleteById() method works when doesn't exist cart item.")
    public void deleteById_CartItemNotExist_SafeDeleteBook() {
        // Given
        when(bookRepository.existsById(EXISTING_BOOK_ID)).thenReturn(Boolean.TRUE);
        when(cartItemRepository.findByBookId(EXISTING_BOOK_ID)).thenReturn(Optional.empty());

        // When
        bookService.deleteById(EXISTING_BOOK_ID);

        // Then
        verify(bookRepository, times(1)).existsById(EXISTING_BOOK_ID);
        verify(cartItemRepository, times(1)).findByBookId(EXISTING_BOOK_ID);
        verify(bookRepository, times(1)).deleteById(EXISTING_BOOK_ID);
        verifyNoMoreInteractions(bookRepository, cartItemRepository);
    }

    @Test
    @DisplayName("Verify that an exception is throw when book doesn't exist.")
    public void deleteById_BookIdNotExist_ThrowsException() {
        // Given
        when(bookRepository.existsById(NOT_EXISTING_BOOK_ID)).thenReturn(Boolean.FALSE);

        // When
        assertThatThrownBy(() -> bookService.deleteById(NOT_EXISTING_BOOK_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't delete a book with ID: " + NOT_EXISTING_BOOK_ID);

        // Then
        verify(bookRepository, times(1)).existsById(NOT_EXISTING_BOOK_ID);
        verify(bookRepository, never()).deleteById(anyLong());
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Verify getByCategoryId() method works.")
    public void getByCategoryId_ValidCategoryId_ReturnsBook() {
        // Given
        BookDto bookDto = createTestBookDto(EXISTING_BOOK_ID);
        Book book = createTestBook(bookDto);
        List<Book> books = Collections.singletonList(book);
        Page<Book> bookPage = new PageImpl<>(books, BOOK_PAGEABLE, books.size());
        BookDtoWithoutCategoryIds expectedBookDto = createTestBookDtoWithoutCategoryId(bookDto);
        when(bookRepository.findAllByCategoryId(EXISTING_CATEGORY_ID, BOOK_PAGEABLE))
                .thenReturn(bookPage);
        when(bookMapper.toBookDtoWithoutCategoryIds(book)).thenReturn(expectedBookDto);

        // When
        Page<BookDtoWithoutCategoryIds> actualBookDtoPage = bookService
                .getByCategoryId(EXISTING_CATEGORY_ID, BOOK_PAGEABLE);

        // Then
        assertThat(actualBookDtoPage).isNotNull();
        assertThat(actualBookDtoPage).hasSize(1).containsExactly(expectedBookDto);
        assertPageMetadataEquals(actualBookDtoPage, bookPage);
        verify(bookRepository, times(1)).findAllByCategoryId(EXISTING_CATEGORY_ID, BOOK_PAGEABLE);
        verify(bookMapper, times(1)).toBookDtoWithoutCategoryIds(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify getByCategoryId() returns empty when no books found.")
    public void getByCategoryId_BookNotExistByCategoryId_ReturnsEmptyPage() {
        // Given
        Page<Book> bookPage = Page.empty(BOOK_PAGEABLE);
        when(bookRepository.findAllByCategoryId(ALTERNATIVE_CATEGORY_ID, BOOK_PAGEABLE))
                .thenReturn(bookPage);

        // When
        Page<BookDtoWithoutCategoryIds> actualBookDtoPage = bookService
                .getByCategoryId(ALTERNATIVE_CATEGORY_ID, BOOK_PAGEABLE);

        // Then
        assertThat(actualBookDtoPage).isNotNull().isEmpty();
        assertPageMetadataEquals(actualBookDtoPage, bookPage);
        verify(bookRepository, times(1))
                .findAllByCategoryId(ALTERNATIVE_CATEGORY_ID, BOOK_PAGEABLE);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Verify that an exception is throw when category id doesn't exist.")
    public void getByCategoryId_CategoryIdNotExist_ThrowsException() {
        // When
        assertThatThrownBy(() -> bookService.getByCategoryId(
                NOT_EXISTING_CATEGORY_ID,
                BOOK_PAGEABLE
        ))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't get books with category ID: " + NOT_EXISTING_CATEGORY_ID);

        // Then
        verifyNoInteractions(bookRepository, bookMapper);
    }
}
