package onlinebookstore.util.assertions;

import static onlinebookstore.service.category.CategoryServiceImpl.categoriesCash;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import onlinebookstore.dto.category.CategoryDto;
import onlinebookstore.model.Category;

public class CategoryAssertionsUtil extends TestAssertionsUtil {
    public static void assertListCategoryDtosAreEqual(List<CategoryDto> expected,
                                                List<CategoryDto> actual) {
        assertThat(actual).isNotNull();
        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    public static void assertCategoryDtosAreEqual(Object expected, Object actual) {
        assertThat(actual).isNotNull();
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);
    }

    public static void assertAddingCategoriesCash(Category actual, int expectedSize) {
        assertThat(categoriesCash).hasSize(expectedSize);
        assertThat(categoriesCash).containsKey(actual.getId());
        assertCategoryDtosAreEqual(actual, categoriesCash.get(actual.getId()));
    }
}
