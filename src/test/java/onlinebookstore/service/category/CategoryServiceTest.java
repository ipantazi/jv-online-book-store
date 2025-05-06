package onlinebookstore.service.category;

import static onlinebookstore.util.TestDataUtil.CATEGORY_IGNORING_FIELD;
import static onlinebookstore.util.TestDataUtil.CATEGORY_PAGEABLE;
import static onlinebookstore.util.TestDataUtil.EXISTING_CATEGORY_ID;
import static onlinebookstore.util.TestDataUtil.EXPECTED_CATEGORIES_SIZE;
import static onlinebookstore.util.TestDataUtil.NEW_CATEGORY_ID;
import static onlinebookstore.util.TestDataUtil.NOT_EXISTING_CATEGORY_ID;
import static onlinebookstore.util.TestDataUtil.createTestCategory;
import static onlinebookstore.util.TestDataUtil.createTestCategoryDto;
import static onlinebookstore.util.TestDataUtil.createTestCategoryRequestDto;
import static onlinebookstore.util.TestDataUtil.createTestCategorySet;
import static onlinebookstore.util.assertions.CategoryAssertionsUtil.assertAddingCategoriesCash;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertObjectsAreEqualIgnoringFields;
import static onlinebookstore.util.assertions.TestAssertionsUtil.assertPageMetadataEquals;
import static onlinebookstore.util.service.ServiceTestUtil.mockCategoriesCash;
import static onlinebookstore.util.service.category.CategoryMockUtil.mockCategoryMapperUpdateBookEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import onlinebookstore.dto.category.CategoryDto;
import onlinebookstore.dto.category.CreateCategoryRequestDto;
import onlinebookstore.exception.DataProcessingException;
import onlinebookstore.exception.EntityNotFoundException;
import onlinebookstore.mapper.CategoryMapper;
import onlinebookstore.model.Category;
import onlinebookstore.repository.category.CategoryRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    private static Set<Category> testCategories;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;
    @InjectMocks
    private CategoryServiceImpl categoryService;
    private Map<Long, Category> categoriesCash;

    @BeforeAll
    static void beforeAll() {
        testCategories = createTestCategorySet(EXISTING_CATEGORY_ID, EXPECTED_CATEGORIES_SIZE);
    }

    @BeforeEach
    void setUp() throws Exception {
        categoriesCash = mockCategoriesCash(testCategories);
    }

    @Test
    @DisplayName("Verify initializeCategoriesCash() method works.")
    public void initializeCategoriesCash_ValidCategories_AddToCategoriesCash() {
        // Given
        final int expectedCategoriesCashSize = 1;
        Category categoryForInitialize = createTestCategory(EXISTING_CATEGORY_ID);
        categoriesCash.clear();
        assertThat(categoriesCash).isEmpty();
        when(categoryRepository.findAll()).thenReturn(List.of(categoryForInitialize));

        // When
        categoryService.initializeCategoriesCash();

        // Then
        assertAddingCategoriesCash(categoryForInitialize, expectedCategoriesCashSize);
        verify(categoryRepository, times(1)).findAll();
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Verify initializeCategoriesCash() method when a categories List is empty.")
    public void initializeCategoriesCash_NoCategories_EmptyList() {
        // Given
        categoriesCash.clear();
        when(categoryRepository.findAll()).thenReturn(List.of());

        // When
        categoryService.initializeCategoriesCash();

        // Then
        assertThat(categoriesCash).isEmpty();
        verify(categoryRepository, times(1)).findAll();
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Verify findAll() method works.")
    public void findAll_ValidPageable_ReturnsCategoryList() {
        // Given
        CategoryDto expectedCategoryDto = createTestCategoryDto(EXISTING_CATEGORY_ID);
        Category category = createTestCategory(expectedCategoryDto);
        List<Category> categories = List.of(category);
        Page<Category> categoryPage = new PageImpl<>(
                categories,
                CATEGORY_PAGEABLE,
                categories.size()
        );
        when(categoryRepository.findAll(CATEGORY_PAGEABLE)).thenReturn(categoryPage);
        when(categoryMapper.toCategoryDto(category)).thenReturn(expectedCategoryDto);

        // When
        Page<CategoryDto> actualCategoryDtoPage = categoryService.findAll(CATEGORY_PAGEABLE);
        List<CategoryDto> actualCategoryDtos = actualCategoryDtoPage.getContent();

        // Then
        assertThat(actualCategoryDtos).hasSize(1);
        assertObjectsAreEqualIgnoringFields(
                actualCategoryDtos.get(0),
                expectedCategoryDto,
                CATEGORY_IGNORING_FIELD
        );
        assertPageMetadataEquals(actualCategoryDtoPage, categoryPage);
        verify(categoryRepository, times(1)).findAll(CATEGORY_PAGEABLE);
        verify(categoryMapper, times(1)).toCategoryDto(category);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Verify findAll() method returns empty page when no categories exist.")
    public void findAll_NoCategories_ReturnsEmptyPage() {
        // Given
        Page<Category> categoryPage = Page.empty(CATEGORY_PAGEABLE);
        when(categoryRepository.findAll(CATEGORY_PAGEABLE)).thenReturn(categoryPage);

        // When
        Page<CategoryDto> actualCategoryDtoPage = categoryService.findAll(CATEGORY_PAGEABLE);

        // Then
        assertThat(actualCategoryDtoPage).isEmpty();
        assertPageMetadataEquals(actualCategoryDtoPage, categoryPage);
        verify(categoryRepository, times(1)).findAll(CATEGORY_PAGEABLE);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Verify getById() method works.")
    public void getById_ValidCategoryId_ReturnsCategoryDto() {
        // Given
        CategoryDto expectedCategoryDto = createTestCategoryDto(EXISTING_CATEGORY_ID);
        Category category = createTestCategory(expectedCategoryDto);
        when(categoryRepository.findById(EXISTING_CATEGORY_ID)).thenReturn(Optional.of(category));
        when(categoryMapper.toCategoryDto(category)).thenReturn(expectedCategoryDto);

        // When
        CategoryDto actualCategoryDto = categoryService.getById(EXISTING_CATEGORY_ID);

        // Then
        assertObjectsAreEqualIgnoringFields(
                actualCategoryDto,
                expectedCategoryDto,
                CATEGORY_IGNORING_FIELD
        );
        verify(categoryRepository, times(1)).findById(EXISTING_CATEGORY_ID);
        verify(categoryMapper, times(1)).toCategoryDto(category);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when category doesn't exist.")
    public void getById_CategoryNotExist_ThrowsException() {
        // Given
        when(categoryRepository.findById(NOT_EXISTING_CATEGORY_ID)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> categoryService.getById(NOT_EXISTING_CATEGORY_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find the category by id: " + NOT_EXISTING_CATEGORY_ID);

        // Then
        verify(categoryRepository, times(1)).findById(NOT_EXISTING_CATEGORY_ID);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Verify save() method works.")
    public void save_ValidCreateCategoryRequestDto_ReturnsCategoryDto() {
        // Given
        final int expectedCategoriesCashSize = categoriesCash.size() + 1;
        CategoryDto expectedCategoryDto = createTestCategoryDto(NEW_CATEGORY_ID);
        String categoryName = expectedCategoryDto.name();
        Category category = createTestCategory(expectedCategoryDto);
        CreateCategoryRequestDto categoryRequestDto =
                createTestCategoryRequestDto(expectedCategoryDto);
        when(categoryRepository.existsByNameIgnoreCase(categoryName)).thenReturn(false);
        when(categoryMapper.toCategoryEntity(categoryRequestDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toCategoryDto(category)).thenReturn(expectedCategoryDto);

        // When
        CategoryDto actualCategoryDto = categoryService.save(categoryRequestDto);

        // Then
        assertObjectsAreEqualIgnoringFields(
                actualCategoryDto,
                expectedCategoryDto,
                CATEGORY_IGNORING_FIELD
        );
        assertAddingCategoriesCash(category, expectedCategoriesCashSize);
        verify(categoryRepository, times(1)).existsByNameIgnoreCase(categoryName);
        verify(categoryMapper, times(1)).toCategoryEntity(categoryRequestDto);
        verify(categoryRepository, times(1)).save(category);
        verify(categoryMapper, times(1)).toCategoryDto(category);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a category already exist.")
    public void save_CategoryAlreadyExist_ThrowsException() {
        // Given
        int expectedCategoriesCashSize = categoriesCash.size();
        CategoryDto expectedCategoryDto = createTestCategoryDto(EXISTING_CATEGORY_ID);
        String categoryName = expectedCategoryDto.name();
        CreateCategoryRequestDto categoryRequestDto =
                createTestCategoryRequestDto(expectedCategoryDto);
        when(categoryRepository.existsByNameIgnoreCase(categoryName)).thenReturn(true);

        // When
        assertThatThrownBy(() -> categoryService.save(categoryRequestDto))
                .isInstanceOf(DataProcessingException.class)
                .hasMessage("Can't save category with name: " + categoryName);

        // Then
        assertThat(categoriesCash).hasSize(expectedCategoriesCashSize);
        verify(categoryRepository, times(1)).existsByNameIgnoreCase(categoryName);
        verify(categoryRepository, never()).save(any(Category.class));
        verifyNoMoreInteractions(categoryRepository);
        verifyNoInteractions(categoryMapper);
    }

    @Test
    @DisplayName("Verify update() method works.")
    public void update_ValidIdAndCategoryRequestDto_ReturnsCategoryDto() {
        // Given
        final int expectedCategoriesCashSize = categoriesCash.size();
        String dataBeforeUpdate = "before update";
        CategoryDto expectedCategoryDto = createTestCategoryDto(EXISTING_CATEGORY_ID);
        Category category = createTestCategory(expectedCategoryDto);
        final CreateCategoryRequestDto categoryRequestDto =
                createTestCategoryRequestDto(expectedCategoryDto);
        category.setName(dataBeforeUpdate);
        category.setDescription(dataBeforeUpdate);
        when(categoryRepository.findById(EXISTING_CATEGORY_ID)).thenReturn(Optional.of(category));
        mockCategoryMapperUpdateBookEntity(categoryMapper, categoryRequestDto, category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toCategoryDto(category)).thenReturn(expectedCategoryDto);

        // When
        CategoryDto actualCategoryDto = categoryService.update(
                EXISTING_CATEGORY_ID,
                categoryRequestDto
        );

        // Then
        assertObjectsAreEqualIgnoringFields(
                actualCategoryDto,
                expectedCategoryDto,
                CATEGORY_IGNORING_FIELD
        );
        assertAddingCategoriesCash(category, expectedCategoriesCashSize);
        verify(categoryRepository, times(1)).findById(EXISTING_CATEGORY_ID);
        verify(categoryMapper, times(1))
                .updateCategoryEntity(category, categoryRequestDto);
        verify(categoryRepository, times(1)).save(category);
        verify(categoryMapper, times(1)).toCategoryDto(category);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a category doesn't exist.")
    public void update_CategoryNotExist_ThrowsException() {
        // Given
        int expectedCategoriesCashSize = categoriesCash.size();
        CategoryDto expectedCategoryDto = createTestCategoryDto(NOT_EXISTING_CATEGORY_ID);
        CreateCategoryRequestDto categoryRequestDto =
                createTestCategoryRequestDto(expectedCategoryDto);
        when(categoryRepository.findById(NOT_EXISTING_CATEGORY_ID)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> categoryService.update(
                NOT_EXISTING_CATEGORY_ID,
                categoryRequestDto
        ))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find the category by id: " + NOT_EXISTING_CATEGORY_ID);

        // Then
        assertThat(categoriesCash).hasSize(expectedCategoriesCashSize);
        assertThat(categoriesCash).doesNotContainKey(NOT_EXISTING_CATEGORY_ID);
        verify(categoryRepository, never()).save(any(Category.class));
        verify(categoryRepository, times(1)).findById(NOT_EXISTING_CATEGORY_ID);
        verifyNoMoreInteractions(categoryRepository);
        verifyNoInteractions(categoryMapper);
    }

    @Test
    @DisplayName("Verify deleteById() method works.")
    public void deleteById_ValidCategoryID_SafeDelete() {
        // Given
        final int expectedCategoriesCashSize = categoriesCash.size() - 1;
        when(categoryRepository.existsById(EXISTING_CATEGORY_ID)).thenReturn(true);

        // When
        categoryService.deleteById(EXISTING_CATEGORY_ID);

        // Then
        assertThat(categoriesCash).hasSize(expectedCategoriesCashSize);
        assertThat(categoriesCash).doesNotContainKey(EXISTING_CATEGORY_ID);
        verify(categoryRepository, times(1)).existsById(EXISTING_CATEGORY_ID);
        verify(categoryRepository, times(1)).deleteById(EXISTING_CATEGORY_ID);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Verify that an exception is throw when a category doesn't exist.")
    public void deleteById_CategoryNotExist_ThrowsException() {
        // Given
        int expectedCategoriesCashSize = categoriesCash.size();
        when(categoryRepository.existsById(NOT_EXISTING_CATEGORY_ID)).thenReturn(false);

        // When
        assertThatThrownBy(() -> categoryService.deleteById(NOT_EXISTING_CATEGORY_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't delete a category with id: " + NOT_EXISTING_CATEGORY_ID);

        // Then
        assertThat(categoriesCash).hasSize(expectedCategoriesCashSize);
        assertThat(categoriesCash).doesNotContainKey(NOT_EXISTING_CATEGORY_ID);
        verify(categoryRepository, times(1)).existsById(NOT_EXISTING_CATEGORY_ID);
        verify(categoryRepository, never()).deleteById(NOT_EXISTING_CATEGORY_ID);
        verifyNoMoreInteractions(categoryRepository);
    }
}
