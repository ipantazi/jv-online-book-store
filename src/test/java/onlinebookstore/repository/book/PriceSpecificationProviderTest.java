package onlinebookstore.repository.book;

import static onlinebookstore.util.repository.RepositoryTestDataUtil.KEY_PRICE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import onlinebookstore.model.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class PriceSpecificationProviderTest {
    private static final BigDecimal FROM_PRICE = BigDecimal.ONE;
    private static final BigDecimal TO_PRICE = BigDecimal.TEN;
    private static final List<BigDecimal> PRICE_RANGE = List.of(FROM_PRICE, TO_PRICE);
    @Mock
    private Root<Book> root;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private CriteriaQuery<Book> query;
    @Mock
    private Path<BigDecimal> path;
    @InjectMocks
    private PriceSpecificationProvider provider;

    @Test
    @DisplayName("Verify getKey() method works.")
    public void getKey_ReturnsCorrectKey() {
        assertThat(provider.getKey()).isEqualTo(KEY_PRICE);
    }

    @Test
    @DisplayName("Verify two prices create BETWEEN clause.")
    public void getSpecification_ValidTwoParams_CreatesBetweenClause() {
        // Given
        Predicate expectedPredicate = mock(Predicate.class);
        when(root.<BigDecimal>get(KEY_PRICE)).thenReturn(path);
        when(criteriaBuilder.between(path, FROM_PRICE, TO_PRICE))
                .thenReturn(expectedPredicate);

        // When
        Specification<Book> actualSpecification = provider.getSpecification(PRICE_RANGE);

        // Then
        assertThat(actualSpecification).isNotNull();
        Predicate actualPredicate = actualSpecification.toPredicate(root, query, criteriaBuilder);
        assertThat(actualPredicate).isNotNull();
        assertThat(actualPredicate).isEqualTo(expectedPredicate);
        verify(root, times(1)).get(KEY_PRICE);
        verify(criteriaBuilder, times(1)).between(path, FROM_PRICE, TO_PRICE);
        verifyNoMoreInteractions(root, criteriaBuilder);
    }

    @Test
    @DisplayName("Verify first price value is treated as minimum price.")
    public void getSpecification_ValidFistPrice_CreatesGreaterThanOrEqualToMinimumPrice() {
        // Given
        Predicate expectedPredicate = mock(Predicate.class);
        when(root.<BigDecimal>get(KEY_PRICE)).thenReturn(path);
        when(criteriaBuilder.greaterThanOrEqualTo(path, FROM_PRICE))
                .thenReturn(expectedPredicate);

        // When
        Specification<Book> actualSpecification = provider.getSpecification(List.of(FROM_PRICE));

        // Then
        assertThat(actualSpecification).isNotNull();
        Predicate actualPredicate = actualSpecification.toPredicate(root, query, criteriaBuilder);
        assertThat(actualPredicate).isNotNull();
        assertThat(actualPredicate).isEqualTo(expectedPredicate);
        verify(root, times(1)).get(KEY_PRICE);
        verify(criteriaBuilder, times(1)).greaterThanOrEqualTo(path, FROM_PRICE);
        verifyNoMoreInteractions(root, criteriaBuilder);
    }

    @Test
    @DisplayName("Verify second price value is treated as maximum price.")
    public void getSpecification_ValidSecondPrice_CreatesLessThanOrEqualToMaximumPrice() {
        // Given
        Predicate expectedPredicate = mock(Predicate.class);
        when(root.<BigDecimal>get(KEY_PRICE)).thenReturn(path);
        when(criteriaBuilder.lessThanOrEqualTo(path, TO_PRICE)).thenReturn(expectedPredicate);

        // When
        Specification<Book> actualSpecification = provider
                .getSpecification(Arrays.asList(null, TO_PRICE));

        // Then
        assertThat(actualSpecification).isNotNull();
        Predicate actualPredicate = actualSpecification.toPredicate(root, query, criteriaBuilder);
        assertThat(actualPredicate).isNotNull();
        assertThat(actualPredicate).isEqualTo(expectedPredicate);
        verify(root, times(1)).get(KEY_PRICE);
        verify(criteriaBuilder, times(1)).lessThanOrEqualTo(path, TO_PRICE);
        verifyNoMoreInteractions(root, criteriaBuilder);
    }

    @Test
    @DisplayName("Verify specification is empty for empty price list.")
    public void getSpecification_EmptyList_ReturnsEmptySpecification() {
        // When
        Specification<Book> actualSpecification = provider.getSpecification(List.of());

        // Then
        assertThat(actualSpecification).isNotNull();
        assertThat(actualSpecification.toPredicate(root, query, criteriaBuilder)).isNull();
        verifyNoInteractions(criteriaBuilder);
    }

    @Test
    @DisplayName("Verify specification is empty for invalid price list.")
    public void getSpecification_InvalidPriceList_ReturnsEmptySpecification() {
        // When
        Specification<Book> actualSpecification = provider
                .getSpecification(List.of("TEST", "invalid data"));

        // Then
        assertThat(actualSpecification).isNotNull();
        assertThat(actualSpecification.toPredicate(root, query, criteriaBuilder)).isNull();
    }
}
