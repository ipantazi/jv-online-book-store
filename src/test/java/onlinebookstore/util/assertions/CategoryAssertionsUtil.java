package onlinebookstore.util.assertions;

import static onlinebookstore.service.category.CategoryServiceImpl.categoriesCache;
import static onlinebookstore.util.TestDataUtil.CATEGORY_IGNORING_FIELD;
import static org.assertj.core.api.Assertions.assertThat;

import onlinebookstore.model.Category;

public class CategoryAssertionsUtil extends TestAssertionsUtil {
    private CategoryAssertionsUtil() {
    }

    public static void assertAddingCategoriesCash(Category actual, int expectedSize) {
        assertThat(categoriesCache).hasSize(expectedSize);
        assertThat(categoriesCache).containsKey(actual.getId());
        assertObjectsAreEqualIgnoringFields(
                actual,
                categoriesCache.get(actual.getId()),
                CATEGORY_IGNORING_FIELD);
    }
}
