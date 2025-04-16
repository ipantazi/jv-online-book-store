package onlinebookstore.repository.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import onlinebookstore.model.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class TitleSpecificationProviderTest {
    private static final String EXPECTED_KEY = "title";
    private static final String EXPECTED_VALUE = "TEST";
    @Mock
    private Root<Book> root;
    @Mock
    private CriteriaQuery<?> query;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private Path<String> path;
    @Mock
    private Expression<String> expression;
    @InjectMocks
    private TitleSpecificationProvider provider;

    @Test
    @DisplayName("Verify getKey() method works.")
    public void getKey_ReturnsCorrectKey() {
        assertThat(provider.getKey()).isNotNull().isEqualTo(EXPECTED_KEY);
    }

    @Test
    @DisplayName("Verify getSpecification() returns correct predicate")
    public void getSpecification_ValidParams_ReturnsCorrectSpecification() {
        Predicate expectedPredicate = mock(Predicate.class);

        when(root.<String>get(EXPECTED_KEY)).thenReturn(path);
        when(criteriaBuilder.lower(path)).thenReturn(expression);
        when(criteriaBuilder.like(expression, "%" + EXPECTED_VALUE.toLowerCase() + "%"))
                .thenReturn(expectedPredicate);

        Specification<Book> actualSpecification = provider.getSpecification(EXPECTED_VALUE);

        assertThat(actualSpecification).isNotNull();
        Predicate actualPredicate = actualSpecification.toPredicate(root, query, criteriaBuilder);
        assertThat(actualPredicate).isNotNull();
        assertThat(actualPredicate).isEqualTo(expectedPredicate);
        verify(criteriaBuilder, times(1)).lower(any());
        verify(root, times(1)).get(EXPECTED_KEY);
        verify(criteriaBuilder, times(1)).like(any(), anyString());
        verifyNoMoreInteractions(criteriaBuilder, root, query);
    }

    @Test
    @DisplayName("Verify that the method returns empty specification for non-string parameters.")
    public void getSpecification_NonStringParams_ReturnsSpecificationWhereNull() {
        Specification<Book> actualSpecification = provider.getSpecification(BigDecimal.ONE);

        assertThat(actualSpecification).isNotNull();
        assertThat(actualSpecification).isEqualTo(Specification.where(null));
        verifyNoInteractions(criteriaBuilder, root);
    }

    @Test
    @DisplayName("Verify that the method returns empty specification for null parameters")
    public void getSpecification_NulParams_ReturnsSpecificationWhereNull() {
        Specification<Book> actualSpecification = provider.getSpecification(null);

        assertThat(actualSpecification).isNotNull();
        assertThat(actualSpecification).isEqualTo(Specification.where(null));
        verifyNoInteractions(criteriaBuilder, root);
    }

    @Test
    @DisplayName("Verify that the method returns empty specification for blank parameters.")
    public void getSpecification_BlankParams_ReturnsSpecificationWhereNull() {
        Specification<Book> actualSpecification = provider.getSpecification("");

        assertThat(actualSpecification).isNotNull();
        assertThat(actualSpecification.toPredicate(root, query, criteriaBuilder)).isNull();
    }
}
