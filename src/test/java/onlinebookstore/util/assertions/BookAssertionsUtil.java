package onlinebookstore.util.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import onlinebookstore.dto.book.BookDto;

public class BookAssertionsUtil extends TestAssertionsUtil {
    public static void assertListBookDtosAreEqual(List<BookDto> actual, List<BookDto> expected) {
        assertThat(actual).isNotNull();
        assertThat(actual).hasSize(expected.size());
        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id", "categoryIds")
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    public static void assertBookDtosAreEqual(BookDto actual, BookDto expected) {
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isNotNull();
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id", "categoryIds")
                .isEqualTo(expected);
        assertThat(actual.getCategoryIds())
                .containsExactlyInAnyOrderElementsOf(expected.getCategoryIds());
    }
}
