package onlinebookstore.util.service.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;

import onlinebookstore.dto.category.CreateCategoryRequestDto;
import onlinebookstore.mapper.CategoryMapper;
import onlinebookstore.model.Category;

public class CategoryMockUtil {
    private CategoryMockUtil() {
    }

    public static void mockCategoryMapperUpdateBookEntity(
            CategoryMapper categoryMapper,
            CreateCategoryRequestDto createCategoryRequestDto,
            Category category
    ) {
        doAnswer(invocation -> {
            Category entity = invocation.getArgument(0);
            CreateCategoryRequestDto dto = invocation.getArgument(1);

            assertEntitiesAreDifferent(dto, entity);
            updateEntityFromDto(dto, entity);
            verifyEntityUpdatedCorrectly(dto, entity);

            return null;
        }).when(categoryMapper).updateCategoryEntity(category, createCategoryRequestDto);
    }

    private static void assertEntitiesAreDifferent(CreateCategoryRequestDto dto, Category entity) {
        assertThat(dto.name()).isNotEqualTo(entity.getName());
        assertThat(dto.description()).isNotEqualTo(entity.getDescription());
    }

    private static void updateEntityFromDto(CreateCategoryRequestDto dto, Category entity) {
        entity.setName(dto.name());
        entity.setDescription(dto.description());
    }

    private static void verifyEntityUpdatedCorrectly(CreateCategoryRequestDto dto,
                                                     Category entity) {
        assertThat(entity)
                .usingRecursiveComparison()
                .ignoringFields("id", "isDeleted")
                .isEqualTo(dto);
    }
}
