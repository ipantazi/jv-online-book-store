package onlinebookstore.service.book;

import java.util.List;
import onlinebookstore.dto.book.BookDto;
import onlinebookstore.dto.book.BookDtoWithoutCategoryIds;
import onlinebookstore.dto.book.BookSearchParametersDto;
import onlinebookstore.dto.book.CreateBookRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
    BookDto save(CreateBookRequestDto bookRequestDto);

    Page<BookDto> findAll(Pageable pageable);

    List<BookDto> search(BookSearchParametersDto params);

    BookDto findById(Long id);

    BookDto update(Long id, CreateBookRequestDto bookRequestDto);

    void deleteById(Long id);

    Page<BookDtoWithoutCategoryIds> getByCategoryId(Long categoryId, Pageable pageable);
}
