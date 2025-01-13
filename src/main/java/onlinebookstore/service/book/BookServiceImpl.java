package onlinebookstore.service.book;

import java.util.List;
import lombok.RequiredArgsConstructor;
import onlinebookstore.dto.book.BookDto;
import onlinebookstore.dto.book.BookSearchParametersDto;
import onlinebookstore.dto.book.CreateBookRequestDto;
import onlinebookstore.exception.DataProcessingException;
import onlinebookstore.exception.EntityNotFoundException;
import onlinebookstore.mapper.BookMapper;
import onlinebookstore.model.Book;
import onlinebookstore.repository.SpecificationBuilder;
import onlinebookstore.repository.book.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final SpecificationBuilder<Book> specificationBuilder;

    @Override
    public BookDto save(CreateBookRequestDto bookRequestDto) {
        if (bookRepository.existsByIsbnIncludingDeleted(bookRequestDto.isbn()) > 0) {
            throw new DataProcessingException("Can't save a book with this ISBN: "
                    + bookRequestDto.isbn());
        }
        Book book = bookMapper.toModel(bookRequestDto);
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
        return bookMapper.toBookDto(findBookById(id));
    }

    @Override
    public BookDto update(Long id, CreateBookRequestDto bookRequestDto) {
        Book book = findBookById(id);
        bookMapper.updateBookFromDto(bookRequestDto, book);
        return bookMapper.toBookDto(bookRepository.save(book));
    }

    @Override
    public void deleteById(Long id) {
        bookRepository.deleteById(findBookById(id).getId());
    }

    private Book findBookById(Long id) {
        return bookRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Can`t find the book by id: " + id));
    }
}
