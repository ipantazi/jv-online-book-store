package onlinebookstore.mapper;

import static onlinebookstore.service.category.CategoryServiceImpl.categoriesCash;

import java.util.HashSet;
import java.util.Set;
import onlinebookstore.config.MapperConfig;
import onlinebookstore.dto.category.CategoryDto;
import onlinebookstore.dto.category.CreateCategoryRequestDto;
import onlinebookstore.exception.EntityNotFoundException;
import onlinebookstore.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface CategoryMapper {
    CategoryDto toCategoryDto(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Category toCategoryEntity(CreateCategoryRequestDto categoryRequestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateCategoryEntity(
            @MappingTarget Category category,
            CreateCategoryRequestDto categoryRequestDto
    );

    @Named("mappingCategoriesIDToCategories")
    default Set<Category> mappingCategoriesIdToCategories(Set<Long> categoryIDs) {
        Set<Category> categories = new HashSet<>();
        for (Long id : categoryIDs) {
            if (!categoriesCash.containsKey(id)) {
                throw new EntityNotFoundException("Category with id " + id + " not found");
            }
            categories.add(categoriesCash.get(id));
        }
        return categories;
    }
}
