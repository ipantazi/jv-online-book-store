package onlinebookstore.repository.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import onlinebookstore.exception.DataProcessingException;
import onlinebookstore.model.Book;
import onlinebookstore.repository.SpecificationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BookSpecificationProviderManagerTest {
    private static final String KEY = "title";
    private static final String INVALID_KEY = "invalid";
    @Mock
    private SpecificationProvider<Book> expected;
    private BookSpecificationProviderManager manager;

    @BeforeEach
    void setUp() {
        List<SpecificationProvider<Book>> providers = List.of(expected);
        manager = new BookSpecificationProviderManager(providers);

        when(expected.getKey()).thenReturn(KEY);
    }

    @Test
    @DisplayName("Verify getSpecificationProviders() method works.")
    public void getSpecificationProviders_ValidKey_ReturnsSpecificationProviders() {
        SpecificationProvider<Book> actual = manager.getSpecificationProvider(KEY);

        assertThat(actual).isNotNull();
        assertThat(actual.getKey()).isEqualTo(KEY);
        assertThat(actual).isEqualTo(expected);
        verify(expected, times(2)).getKey();
    }

    @Test
    @DisplayName("Verify that an exception is throw when sort key is invalid.")
    public void getSpecificationProviders_InvalidKey_ThrowsException() {
        assertThatThrownBy(() -> manager.getSpecificationProvider(INVALID_KEY))
                .isInstanceOf(DataProcessingException.class)
                .hasMessage("Can't find correct specification provider for key: " + INVALID_KEY);

        verify(expected, times(1)).getKey();
    }
}
