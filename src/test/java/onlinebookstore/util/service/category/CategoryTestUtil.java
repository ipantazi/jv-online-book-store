package onlinebookstore.util.service.category;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import onlinebookstore.model.Category;
import onlinebookstore.service.category.CategoryServiceImpl;

public class CategoryTestUtil {
    public static Map<Long, Category> mockCategoriesCash(Set<Category> categories)
            throws Exception {
        Field field = CategoryServiceImpl.class.getDeclaredField("categoriesCash");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Long, Category> cacheInstance = (Map<Long, Category>) field.get(null);
        cacheInstance.clear();
        cacheInstance.putAll(categories.stream()
                .collect(Collectors.toMap(Category::getId, category -> category)));
        return cacheInstance;
    }
}
