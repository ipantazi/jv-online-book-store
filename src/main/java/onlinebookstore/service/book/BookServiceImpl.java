package onlinebookstore.service.book;

import static onlinebookstore.service.category.CategoryServiceImpl.categoriesCash;

import java.util.List;
import lombok.RequiredArgsConstructor;
import onlinebookstore.dto.book.BookDto;
import onlinebookstore.dto.book.BookDtoWithoutCategoryIds;
import onlinebookstore.dto.book.BookSearchParametersDto;
import onlinebookstore.dto.book.CreateBookRequestDto;
import onlinebookstore.exception.DataProcessingException;
import onlinebookstore.exception.EntityNotFoundException;
import onlinebookstore.mapper.BookMapper;
import onlinebookstore.model.Book;
import onlinebookstore.repository.SpecificationBuilder;
import onlinebookstore.repository.book.BookRepository;
import onlinebookstore.repository.cartitem.CartItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final SpecificationBuilder<Book> specificationBuilder;
    private final CartItemRepository cartItemRepository;

    @Override
    public BookDto save(CreateBookRequestDto bookRequestDto) {
        if (bookRepository.existsByIsbnIncludingDeleted(bookRequestDto.isbn()) > 0) {
            throw new DataProcessingException("Can't save a book with this ISBN: "
                    + bookRequestDto.isbn());
        }

        Book book = bookMapper.toBookEntity(bookRequestDto);
        return bookMapper.toBookDto(bookRepository.save(book));
    }

    @Override
    public Page<BookDto> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable).map(bookMapper::toBookDto);
    }

    @Override
    public List<BookDto> search(BookSearchParametersDto params) {
        Specification<Book> bookSpecification = specificationBuilder.build(params);
        return bookRepository.findAll(bookSpecification).stream()
                .map(bookMapper::toBookDto)
                .toList();
    }

    @Override
    public BookDto findById(Long id) {
        return bookMapper.toBookDto(findBookByIdWithCategories(id));
    }

    @Override
    public BookDto update(Long id, CreateBookRequestDto bookRequestDto) {
        Book book = findBookByIdWithCategories(id);
        bookMapper.updateBookEntity(bookRequestDto, book);
        return bookMapper.toBookDto(bookRepository.save(book));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("Can't delete a book with ID: " + id);
        }
        cartItemRepository.deleteAllByBookId(id);
        bookRepository.deleteById(id);
    }

    @Override
    public Page<BookDtoWithoutCategoryIds> getByCategoryId(Long categoryId, Pageable pageable) {
        if (!categoriesCash.containsKey(categoryId)) {
            throw new EntityNotFoundException("Can't get books with category ID: " + categoryId);
        }
        return bookRepository.findAllByCategoryId(categoryId, pageable);
    }

    private Book findBookByIdWithCategories(Long id) {
        return bookRepository.findWithCategoriesById(id).orElseThrow(() ->
                new EntityNotFoundException("Can`t find the book by id: " + id));
    }
}
