package onlinebookstore.util.service.book;

import static onlinebookstore.util.TestDataUtil.convertToCategorySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;

import java.math.BigDecimal;
import java.util.Set;
import onlinebookstore.dto.book.CreateBookRequestDto;
import onlinebookstore.mapper.BookMapper;
import onlinebookstore.model.Book;
import onlinebookstore.model.Category;

public class BookMockUtil {
    private BookMockUtil() {
    }

    public static void mockBookMapperUpdateBookEntity(BookMapper bookMapper,
                                                      CreateBookRequestDto createBookRequestDto,
                                                      Book book) {
        doAnswer(invocation -> {
            CreateBookRequestDto dto = invocation.getArgument(0);
            Book entity = invocation.getArgument(1);

            assertEntitiesAreDifferent(dto, entity);
            updateEntityFromDto(dto, entity);
            verifyEntityUpdatedCorrectly(dto, entity);

            return null;
        }).when(bookMapper).updateBookEntity(createBookRequestDto, book);
    }

    private static void assertEntitiesAreDifferent(CreateBookRequestDto dto, Book entity) {
        assertThat(dto.title()).isNotEqualTo(entity.getTitle());
        assertThat(dto.author()).isNotEqualTo(entity.getAuthor());
        assertThat(dto.description()).isNotEqualTo(entity.getDescription());
        assertThat(dto.price())
                .usingComparator(BigDecimal::compareTo)
                .isNotEqualTo(entity.getPrice());
    }

    private static void updateEntityFromDto(CreateBookRequestDto dto, Book entity) {
        entity.setTitle(dto.title());
        entity.setAuthor(dto.author());
        entity.setPrice(dto.price());
        entity.setDescription(dto.description());
        entity.setCoverImage(dto.coverImage());
        Set<Long> categoryIds = dto.categoryIds();
        Set<Category> convertedCategories = convertToCategorySet(categoryIds);
        entity.setCategories(convertedCategories);
    }

    private static void verifyEntityUpdatedCorrectly(CreateBookRequestDto dto, Book entity) {
        assertThat(entity)
                .usingRecursiveComparison()
                .ignoringFields("id", "categories", "categoryIds", "isDeleted")
                .isEqualTo(dto);
        assertThat(entity.getCategories()).hasSize(dto.categoryIds().size());
    }
}
