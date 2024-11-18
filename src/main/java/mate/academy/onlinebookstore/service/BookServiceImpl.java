package mate.academy.onlinebookstore.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.onlinebookstore.dto.BookDto;
import mate.academy.onlinebookstore.dto.CreateBookRequestDto;
import mate.academy.onlinebookstore.exception.EntityNotFoundException;
import mate.academy.onlinebookstore.mapper.BookMapper;
import mate.academy.onlinebookstore.model.Book;
import mate.academy.onlinebookstore.repository.BookRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public BookDto save(CreateBookRequestDto bookRequestDto) {
        Book book = bookMapper.toModel(bookRequestDto);
        return bookMapper.toBookDto(bookRepository.save(book));
    }

    @Override
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream()
                .map(bookMapper::toBookDto)
                .toList();
    }

    @Override
    public BookDto findById(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Can`t find the book by id: " + id));
        return bookMapper.toBookDto(book);
    }

    @Override
    public int update(Long id, CreateBookRequestDto bookRequestDto) {
        return bookRepository.updateBookById(id,
                bookRequestDto.getTitle(),
                bookRequestDto.getAuthor(),
                bookRequestDto.getIsbn(),
                bookRequestDto.getPrice(),
                bookRequestDto.getDescription(),
                bookRequestDto.getCoverImage());
    }

    @Override
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }
}
