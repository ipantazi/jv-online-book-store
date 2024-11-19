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
        return bookMapper.toBookDto(findBookById(id));
    }

    @Override
    public BookDto update(Long id, CreateBookRequestDto bookRequestDto) {
        Book updatedBook = bookMapper.updateBookFromDto(bookRequestDto, findBookById(id));
        bookRepository.save(updatedBook);
        return bookMapper.toBookDto(updatedBook);
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
