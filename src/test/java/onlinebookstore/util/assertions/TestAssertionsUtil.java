package onlinebookstore.util.assertions;

import static onlinebookstore.util.TestDataUtil.EXPECTED_CART_ITEMS_SIZE;
import static onlinebookstore.util.controller.ControllerTestDataUtil.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import onlinebookstore.dto.cartitem.CartItemDto;
import onlinebookstore.dto.shoppingcart.ShoppingCartDto;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MvcResult;

public class TestAssertionsUtil {
    protected TestAssertionsUtil() {
    }

    public static void assertObjectsAreEqualIgnoringFields(Object actual,
                                                           Object expected,
                                                           String... fieldsToIgnore) {
        assertThat(actual).isNotNull();
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields(fieldsToIgnore)
                .isEqualTo(expected);
    }

    public static <T> void assertCollectionsAreEqualIgnoringFields(Collection<T> actual,
                                                               Collection<T> expected,
                                                               String... fieldsToIgnore) {
        assertThat(actual).isNotNull();
        assertThat(actual).isNotEmpty();
        assertThat(actual).hasSameSizeAs(expected);

        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(fieldsToIgnore)
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    public static void assertValidationError(MvcResult result,
                                             ObjectMapper objectMapper,
                                             int expectedStatus,
                                             String expectedMessage) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());

        assertThat(body.get("status").asInt()).isEqualTo(expectedStatus);
        assertThat(body.get("message").asText()).isEqualTo(expectedMessage);
        assertThat(body.get("timestamp").asText()).isNotBlank();
    }

    public static void assertValidationErrorList(MvcResult result,
                                                 ObjectMapper objectMapper,
                                                 List<String> expectedMessages) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());

        JsonNode errors = body.get("message");
        assertThat(errors).isNotNull();
        List<String> actualErrorMessages = new ArrayList<>();
        errors.forEach(error -> actualErrorMessages.add(error.asText()));

        assertThat(body.get("status").asInt()).isEqualTo(BAD_REQUEST);
        assertThat(body.get("timestamp").asText()).isNotBlank();
        assertThat(actualErrorMessages).containsExactlyInAnyOrderElementsOf(expectedMessages);
    }

    public static <T> void assertPageMetadataEquals(Page<T> actual, Page<?> expected) {
        assertThat(actual.getTotalElements()).isEqualTo(expected.getTotalElements());
        assertThat(actual.getSize()).isEqualTo(expected.getSize());
        assertThat(actual.getSort()).isEqualTo(expected.getSort());
        assertThat(actual.getNumber()).isEqualTo(expected.getNumber());
    }

    public static void assertShoppingCartContainsExpectedItem(ShoppingCartDto shoppingCartDto,
                                                              CartItemDto expected) {
        assertThat(shoppingCartDto.getCartItems()).isNotEmpty();
        assertThat(shoppingCartDto.getCartItems()).hasSize(EXPECTED_CART_ITEMS_SIZE);
        assertThat(shoppingCartDto.getCartItems()).anyMatch(actual ->
                Objects.equals(expected.getBookTitle(), actual.getBookTitle())
                        && expected.getQuantity() == actual.getQuantity()
        );
    }
}
