package onlinebookstore.service.category;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import onlinebookstore.dto.category.CategoryDto;
import onlinebookstore.dto.category.CreateCategoryRequestDto;
import onlinebookstore.exception.DataProcessingException;
import onlinebookstore.exception.EntityNotFoundException;
import onlinebookstore.mapper.CategoryMapper;
import onlinebookstore.model.Category;
import onlinebookstore.repository.category.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    public static final Map<Long, Category> categoriesCash = new HashMap<>();
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @PostConstruct
    public void initializeCategoriesCash() {
        categoryRepository.findAll().forEach(category -> {
            categoriesCash.put(category.getId(), category);
        });
    }

    @Override
    public Page<CategoryDto> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(categoryMapper::toCategoryDto);
    }

    @Override
    public CategoryDto getById(Long id) {
        return categoryMapper.toCategoryDto(findCategoryById(id));
    }

    @Override
    public CategoryDto save(CreateCategoryRequestDto categoryRequestDto) {
        if (categoryRepository.existsByNameIgnoreCase(categoryRequestDto.name())) {
            throw new DataProcessingException("Can't save category with name: "
                    + categoryRequestDto.name());
        }
        Category category = categoryMapper.toCategoryEntity(categoryRequestDto);
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public CategoryDto update(Long id, CreateCategoryRequestDto categoryRequestDto) {
        Category category = findCategoryById(id);
        categoryMapper.updateCategoryEntity(category, categoryRequestDto);
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public void deleteById(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Can't delete a category with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Can't find the category by id: " + id));
    }
}
