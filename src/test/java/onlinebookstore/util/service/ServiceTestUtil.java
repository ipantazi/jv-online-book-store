package onlinebookstore.util.service;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import onlinebookstore.model.Category;
import onlinebookstore.model.Role;
import onlinebookstore.service.category.CategoryServiceImpl;
import onlinebookstore.service.user.UserServiceImpl;

public class ServiceTestUtil {
    private ServiceTestUtil() {
    }

    public static Map<Long, Category> mockCategoriesCash(Set<Category> categories)
            throws Exception {
        Field field = CategoryServiceImpl.class.getDeclaredField("categoriesCache");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Long, Category> cacheInstance = (Map<Long, Category>) field.get(null);
        cacheInstance.clear();
        cacheInstance.putAll(categories.stream()
                .collect(Collectors.toMap(Category::getId, category -> category)));
        return cacheInstance;
    }

    public static Map<String, Role> mockRolesCash(Set<Role> roles) throws Exception {
        Field field = UserServiceImpl.class.getDeclaredField("rolesCache");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Role> cacheInstance = (Map<String, Role>) field.get(null);
        cacheInstance.clear();
        cacheInstance.putAll(roles.stream()
                .collect(Collectors.toMap(Role::getAuthority, role -> role)));
        return cacheInstance;
    }
}
