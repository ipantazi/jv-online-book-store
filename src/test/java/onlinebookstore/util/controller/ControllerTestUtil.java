package onlinebookstore.util.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import onlinebookstore.dto.book.BookDto;
import org.springframework.test.web.servlet.MvcResult;

public class ControllerTestUtil {
    public static <T> List<T> parsePageContent(MvcResult result, ObjectMapper objectMapper,
                                         TypeReference<List<T>> typeRef) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return objectMapper.readValue(root.get("content").toString(), typeRef);
    }

    public static List<BookDto> parseResultToList(MvcResult result,
                                                  ObjectMapper objectMapper) throws Exception {
        return objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );
    }
}
