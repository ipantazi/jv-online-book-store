package onlinebookstore.repository.book;

import static onlinebookstore.util.repository.RepositoryTestDataUtil.INVALID_KEY;
import static onlinebookstore.util.repository.RepositoryTestDataUtil.KEY_TITLE;
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
    @Mock
    private SpecificationProvider<Book> expectedProvider;
    private BookSpecificationProviderManager manager;

    @BeforeEach
    void setUp() {
        List<SpecificationProvider<Book>> providers = List.of(expectedProvider);
        manager = new BookSpecificationProviderManager(providers);

        when(expectedProvider.getKey()).thenReturn(KEY_TITLE);
    }

    @Test
    @DisplayName("Verify getSpecificationProviders() method works.")
    public void getSpecificationProviders_ValidKey_ReturnsSpecificationProviders() {
        // When
        SpecificationProvider<Book> actual = manager.getSpecificationProvider(KEY_TITLE);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.getKey()).isEqualTo(KEY_TITLE);
        assertThat(actual).isEqualTo(expectedProvider);
        verify(expectedProvider, times(2)).getKey();
    }

    @Test
    @DisplayName("Verify that an exception is throw when sort key is invalid.")
    public void getSpecificationProviders_InvalidKey_ThrowsException() {
        // When
        assertThatThrownBy(() -> manager.getSpecificationProvider(INVALID_KEY))
                .isInstanceOf(DataProcessingException.class)
                .hasMessage("Can't find correct specification provider for key: " + INVALID_KEY);

        // Then
        verify(expectedProvider, times(1)).getKey();
    }
}
