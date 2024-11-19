package mate.academy.onlinebookstore.mapper;

import mate.academy.onlinebookstore.config.MapperConfig;
import mate.academy.onlinebookstore.dto.BookDto;
import mate.academy.onlinebookstore.dto.CreateBookRequestDto;
import mate.academy.onlinebookstore.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface BookMapper {
    BookDto toBookDto(Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Book toModel(CreateBookRequestDto bookRequestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Book updateBookFromDto(CreateBookRequestDto bookRequestDto, @MappingTarget Book book);
}
