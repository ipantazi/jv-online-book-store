package onlinebookstore.util.repository;

import java.time.LocalDateTime;
import onlinebookstore.model.Order;
import onlinebookstore.util.TestDataUtil;

public class RepositoryTestDataUtil extends TestDataUtil {

    public static final String KEY_TITLE = "title";
    public static final String KEY_PRICE = "price";
    public static final String INVALID_KEY = "invalid";
    public static final String INVALID_EMAIL = "invalid";
    public static final String VALUE_FOR_SEARCH = "TEST";
    public static final String DEFAULT_ORDER_STATUS = Order.Status.PENDING.toString();
    public static final LocalDateTime ORDER_DATE = LocalDateTime.of(2025, 4, 22, 1, 25, 47);

    private RepositoryTestDataUtil() {
    }
}
