package onlinebookstore.util.assertions;

import static onlinebookstore.util.controller.ControllerTestDataUtil.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MvcResult;

public class TestAssertionsUtil {
    public static void assertErrorResponse(MvcResult result,
                                           ObjectMapper objectMapper,
                                           int expectedStatus,
                                           String expectedMessage) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());

        assertThat(body.get("status").asInt()).isEqualTo(expectedStatus);
        assertThat(body.get("message").asText()).isEqualTo(expectedMessage);
        assertThat(body.get("timestamp").asText()).isNotBlank();
    }

    public static void assertListErrorsResponse(MvcResult result,
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
}
