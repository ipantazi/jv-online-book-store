package onlinebookstore.service.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import onlinebookstore.dto.category.CategoryDto;
import onlinebookstore.dto.category.CreateCategoryRequestDto;
import onlinebookstore.exception.DataProcessingException;
import onlinebookstore.exception.EntityNotFoundException;
import onlinebookstore.mapper.CategoryMapper;
import onlinebookstore.model.Category;
import onlinebookstore.repository.category.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    private static final int INDEX_FIRST = 0;
    private static final Long INVALID_CATEGORY_ID = 999L;
    private static final String FIELD_ID = "id";
    private final Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;
    @InjectMocks
    private CategoryServiceImpl categoryService;
    private Category expectedCategory;
    private List<Category> categoriesList;
    private Map<Long, Category> categoriesCash;

    @BeforeEach
    void setUp() throws Exception {
        expectedCategory = createStubCategory();
        categoriesList = List.of(expectedCategory);

        Field field = CategoryServiceImpl.class.getDeclaredField("categoriesCash");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Long, Category> cashInstance = (Map<Long, Category>) field.get(null);
        cashInstance.clear();
        cashInstance.putAll(categoriesList.stream()
                .collect(Collectors.toMap(Category::getId, category -> category)));
        categoriesCash = cashInstance;
    }

    @Test
    @DisplayName("Verify initializeCategoriesCash() method works.")
    public void initializeCategoriesCash_ValidCategories_AddToCategoriesCash() {
        Category categoryForInitialize = createStubCategory();
        categoriesCash.clear();
        assertThat(categoriesCash).isEmpty();

        when(categoryRepository.findAll()).thenReturn(List.of(categoryForInitialize));

        categoryService.initializeCategoriesCash();

        assertAddingCategoriesCash(1, categoryForInitialize);
        verify(categoryRepository, times(1)).findAll();
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Verify initializeCategoriesCash() method when a categories List is empty.")
    public void initializeCategoriesCash_NoCategories_EmptyList() {
        categoriesCash.clear();
        when(categoryRepository.findAll()).thenReturn(List.of());

        categoryService.initializeCategoriesCash();

        assertThat(categoriesCash).isEmpty();
        verify(categoryRepository, times(1)).findAll();
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Verify findAll() method works.")
    public void findAll_ValidPageable_ReturnsCategoryList() {
        CategoryDto expectedCategoryDto = createTestCategoryDto(expectedCategory);
        Page<Category> categoryPage = new PageImpl<>(categoriesList, pageable,
                categoriesList.size());

        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(categoryMapper.toCategoryDto(expectedCategory)).thenReturn(expectedCategoryDto);

        Page<CategoryDto> actualCategoryDtoPage = categoryService.findAll(pageable);
        List<CategoryDto> actualCategoryDtos = actualCategoryDtoPage.getContent();

        assertThat(actualCategoryDtos).hasSize(1);
        assertUsingRecursiveComparison(actualCategoryDtos.get(INDEX_FIRST), expectedCategoryDto);
        assertPage(actualCategoryDtoPage, categoryPage);
        verify(categoryRepository, times(1)).findAll(pageable);
        verify(categoryMapper, times(1)).toCategoryDto(expectedCategory);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Verify findAll() method returns empty page when no categories exist.")
    public void findAll_NoCategories_ReturnsEmptyPage() {
        Page<Category> categoryPage = Page.empty(pageable);

        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

        Page<CategoryDto> actualCategoryDtoPage = categoryService.findAll(pageable);

        assertThat(actualCategoryDtoPage).isEmpty();
        assertPage(actualCategoryDtoPage, categoryPage);
        verify(categoryRepository, times(1)).findAll(pageable);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Verify getById() method works.")
    public void getById_ValidCategoryId_ReturnsCategoryDto() {
        Long categoryId = expectedCategory.getId();
        CategoryDto expectedCategoryDto = createTestCategoryDto(expectedCategory);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(expectedCategory));
        when(categoryMapper.toCategoryDto(expectedCategory)).thenReturn(expectedCategoryDto);

        CategoryDto actualCategoryDto = categoryService.getById(categoryId);

        assertUsingRecursiveComparison(actualCategoryDto, expectedCategoryDto);
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryMapper, times(1)).toCategoryDto(expectedCategory);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when category doesn't exist.")
    public void getById_CategoryNotExist_ThrowsException() {
        when(categoryRepository.findById(INVALID_CATEGORY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(INVALID_CATEGORY_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find the category by id: " + INVALID_CATEGORY_ID);
        verify(categoryRepository, times(1)).findById(INVALID_CATEGORY_ID);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Verify save() method works.")
    public void save_ValidCreateCategoryRequestDto_ReturnsCategoryDto() {
        final int expectedCategoriesCashSize = categoriesCash.size() + 1;
        Category newCategory = createStubCategory();
        CreateCategoryRequestDto categoryRequestDto =
                createTestCategoryRequestDto(newCategory);
        CategoryDto expectedCategoryDto = createTestCategoryDto(newCategory);

        when(categoryRepository.existsByNameIgnoreCase(newCategory.getName())).thenReturn(false);
        when(categoryMapper.toCategoryEntity(categoryRequestDto)).thenReturn(newCategory);
        when(categoryRepository.save(newCategory)).thenReturn(newCategory);
        when(categoryMapper.toCategoryDto(newCategory)).thenReturn(expectedCategoryDto);

        CategoryDto actualCategoryDto = categoryService.save(categoryRequestDto);

        assertUsingRecursiveComparison(actualCategoryDto, expectedCategoryDto);
        assertAddingCategoriesCash(expectedCategoriesCashSize, newCategory);
        verify(categoryRepository, times(1)).existsByNameIgnoreCase(newCategory.getName());
        verify(categoryMapper, times(1)).toCategoryEntity(categoryRequestDto);
        verify(categoryRepository, times(1)).save(newCategory);
        verify(categoryMapper, times(1)).toCategoryDto(newCategory);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a category already exist.")
    public void save_CategoryAlreadyExist_ThrowsException() {
        int expectedCategoriesCashSize = categoriesCash.size();
        CreateCategoryRequestDto categoryRequestDto =
                createTestCategoryRequestDto(expectedCategory);

        when(categoryRepository.existsByNameIgnoreCase(expectedCategory.getName()))
                .thenReturn(true);

        assertThatThrownBy(() -> categoryService.save(categoryRequestDto))
                .isInstanceOf(DataProcessingException.class)
                .hasMessage("Can't save category with name: " + expectedCategory.getName());

        assertThat(categoriesCash).hasSize(expectedCategoriesCashSize);
        verify(categoryRepository, times(1)).existsByNameIgnoreCase(expectedCategory.getName());
        verify(categoryRepository, never()).save(any(Category.class));
        verifyNoMoreInteractions(categoryRepository);
        verifyNoInteractions(categoryMapper);
    }

    @Test
    @DisplayName("Verify update() method works.")
    public void update_ValidIdAndCategoryRequestDto_ReturnsCategoryDto() {
        final int expectedCategoriesCashSize = categoriesCash.size();
        final CreateCategoryRequestDto categoryRequestDto =
                createTestCategoryRequestDto(expectedCategory);
        final CategoryDto expectedCategoryDto = createTestCategoryDto(expectedCategory);
        Long categoryId = expectedCategory.getId();
        String dataBeforeUpdate = "before update";

        expectedCategory.setName(dataBeforeUpdate);
        expectedCategory.setDescription(dataBeforeUpdate);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(expectedCategory));
        mockCategoryMapperUpdateCategoryEntity(categoryRequestDto, expectedCategory,
                dataBeforeUpdate);
        when(categoryRepository.save(expectedCategory)).thenReturn(expectedCategory);
        when(categoryMapper.toCategoryDto(expectedCategory)).thenReturn(expectedCategoryDto);

        CategoryDto actualCategoryDto = categoryService.update(categoryId, categoryRequestDto);

        assertUsingRecursiveComparison(actualCategoryDto, expectedCategoryDto);
        assertAddingCategoriesCash(expectedCategoriesCashSize, expectedCategory);
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryMapper, times(1))
                .updateCategoryEntity(expectedCategory, categoryRequestDto);
        verify(categoryRepository, times(1)).save(expectedCategory);
        verify(categoryMapper, times(1)).toCategoryDto(expectedCategory);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a category doesn't exist.")
    public void update_CategoryNotExist_ThrowsException() {
        int expectedCategoriesCashSize = categoriesCash.size();
        CreateCategoryRequestDto categoryRequestDto =
                createTestCategoryRequestDto(expectedCategory);

        when(categoryRepository.findById(INVALID_CATEGORY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(INVALID_CATEGORY_ID, categoryRequestDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find the category by id: " + INVALID_CATEGORY_ID);
        assertThat(categoriesCash).hasSize(expectedCategoriesCashSize);
        assertThat(categoriesCash).doesNotContainKey(INVALID_CATEGORY_ID);
        verify(categoryRepository, never()).save(any(Category.class));
        verify(categoryRepository, times(1)).findById(INVALID_CATEGORY_ID);
        verifyNoMoreInteractions(categoryRepository);
        verifyNoInteractions(categoryMapper);
    }

    @Test
    @DisplayName("Verify deleteById() method works.")
    public void deleteById_ValidCategoryID_SafeDelete() {
        final int expectedCategoriesCashSize = categoriesCash.size() - 1;
        Long categoryId = expectedCategory.getId();

        when(categoryRepository.existsById(categoryId)).thenReturn(true);

        categoryService.deleteById(categoryId);

        assertThat(categoriesCash).hasSize(expectedCategoriesCashSize);
        assertThat(categoriesCash).doesNotContainKey(categoryId);
        verify(categoryRepository, times(1)).existsById(categoryId);
        verify(categoryRepository, times(1)).deleteById(categoryId);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a category doesn't exist.")
    public void deleteById_CategoryNotExist_ThrowsException() {
        int expectedCategoriesCashSize = categoriesCash.size();

        when(categoryRepository.existsById(INVALID_CATEGORY_ID)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.deleteById(INVALID_CATEGORY_ID))
        .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't delete a category with id: " + INVALID_CATEGORY_ID);
        assertThat(categoriesCash).hasSize(expectedCategoriesCashSize);
        assertThat(categoriesCash).doesNotContainKey(INVALID_CATEGORY_ID);
        verify(categoryRepository, times(1)).existsById(INVALID_CATEGORY_ID);
        verify(categoryRepository, never()).deleteById(INVALID_CATEGORY_ID);
        verifyNoMoreInteractions(categoryRepository);
    }

    private Category createStubCategory() {
        Long currentId = categoriesCash != null ? categoriesCash.size() + 1L : 1L;
        Category category = new Category();
        category.setId(currentId);
        category.setName("Name category id: " + currentId);
        category.setDescription("Description category id: " + currentId);
        return category;
    }

    private CategoryDto createTestCategoryDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }

    private CreateCategoryRequestDto createTestCategoryRequestDto(Category category) {
        return new CreateCategoryRequestDto(category.getName(), category.getDescription());
    }

    private void assertUsingRecursiveComparison(Object actual, Object expected) {
        assertThat(actual).isNotNull();
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields(FIELD_ID)
                .isEqualTo(expected);
    }

    private void assertAddingCategoriesCash(int expectedSize, Category actual) {
        assertThat(categoriesCash).hasSize(expectedSize);
        assertThat(categoriesCash).containsKey(actual.getId());
        assertUsingRecursiveComparison(actual, categoriesCash.get(actual.getId()));
    }

    private void assertPage(Page<CategoryDto> actual, Page<Category> expected) {
        assertThat(actual.getTotalElements()).isEqualTo(expected.getTotalElements());
        assertThat(actual.getSize()).isEqualTo(expected.getSize());
        assertThat(actual.getSort()).isEqualTo(expected.getSort());
        assertThat(actual.getNumber()).isEqualTo(expected.getNumber());
    }

    private void mockCategoryMapperUpdateCategoryEntity(
            CreateCategoryRequestDto createCategoryRequestDto,
            Category category,
            String dataBeforeUpdate
    ) {
        doAnswer(invocation -> {
            Category entity = invocation.getArgument(0);
            CreateCategoryRequestDto dto = invocation.getArgument(1);

            assertThat(dto.name()).isNotEqualTo(entity.getName());
            assertThat(dto.description()).isNotEqualTo(entity.getDescription());

            entity.setName(dto.name());
            entity.setDescription(dto.description());

            assertThat(entity.getName()).isNotEqualTo(dataBeforeUpdate);
            assertThat(dto.description()).isNotEqualTo(dataBeforeUpdate);

            return null;
        }).when(categoryMapper).updateCategoryEntity(category, createCategoryRequestDto);
    }
}
