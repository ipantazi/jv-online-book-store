package onlinebookstore.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import onlinebookstore.config.MapperConfig;
import onlinebookstore.dto.book.BookDto;
import onlinebookstore.dto.book.BookDtoWithoutCategoryIds;
import onlinebookstore.dto.book.CreateBookRequestDto;
import onlinebookstore.model.Book;
import onlinebookstore.model.Category;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class, uses = CategoryMapper.class)
public interface BookMapper {
    @Mapping(target = "categoryIds", ignore = true)
    BookDto toBookDto(Book book);

    @AfterMapping
    default void setCategoryIds(@MappingTarget BookDto bookDto, Book book) {
        Set<Long> categoryIds = book.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
        bookDto.setCategoryIds(categoryIds);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "categories",
            source = "categoryIds",
            qualifiedByName = "mappingCategoriesIDToCategories")
    Book toBookEntity(CreateBookRequestDto bookRequestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "categories",
            source = "categoryIds",
            qualifiedByName = "mappingCategoriesIDToCategories")
    void updateBookEntity(CreateBookRequestDto bookRequestDto, @MappingTarget Book book);

    BookDtoWithoutCategoryIds toBookDtoWithoutCategoryIds(Book book);
}
