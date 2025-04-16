package onlinebookstore.repository.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import onlinebookstore.dto.book.BookSearchParametersDto;
import onlinebookstore.model.Book;
import onlinebookstore.repository.SpecificationProvider;
import onlinebookstore.repository.SpecificationProviderManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class BookSpecificationBuilderTest {
    @Mock
    private SpecificationProviderManager<Book> specificationProviderManager;
    @Mock
    private Root<Book> root;
    @Mock
    private CriteriaQuery<?> query;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private Predicate expectedPredicate;
    @Mock
    private SpecificationProvider<Book> specificationProvider;
    @InjectMocks
    private BookSpecificationBuilder builder;

    @Test
    @DisplayName("Verify build() method works.")
    public void build_ValidParams_ReturnsBookSpecification() {
        Map<String, Object> testParams = Map.of(
                "title", "TEST title",
                "author", "TEST author",
                "isbn", "00000000000",
                "price", List.of(new BigDecimal("100"), new BigDecimal("200"))
        );

        for (Map.Entry<String, Object> entry : testParams.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            BookSearchParametersDto params = new BookSearchParametersDto(
                    "title".equals(key) ? (String) value : null,
                    "author".equals(key) ? (String) value : null,
                    "isbn".equals(key) ? (String) value : null,
                    "price".equals(key) && value instanceof List<?> valueList
                            ? valueList.stream().map(BigDecimal.class::cast).toList() : null
            );

            Specification<Book> expectedSpecification = (root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(key), value);

            when(specificationProviderManager.getSpecificationProvider(key))
                    .thenReturn(specificationProvider);
            when(specificationProvider.getSpecification(value))
                    .thenReturn(expectedSpecification);
            when(criteriaBuilder.equal(root.get(key), value))
                    .thenReturn(expectedPredicate);

            Specification<Book> actualSpecification = builder.build(params);
            assertThat(actualSpecification).isNotNull();
            Predicate actualPredicate = actualSpecification
                    .toPredicate(root, query, criteriaBuilder);

            assertThat(actualPredicate).isNotNull();
            assertThat(actualPredicate).isEqualTo(expectedPredicate);
            verify(specificationProviderManager, times(1)).getSpecificationProvider(key);
            verify(specificationProvider, times(1)).getSpecification(value);
        }
        verifyNoMoreInteractions(specificationProviderManager, specificationProvider);
    }

    @Test
    @DisplayName("Verify that the method returns empty specification."
            + "when a all parameters are null.")
    public void build_AllParamsNull_ReturnsEmptySpecification() {
        BookSearchParametersDto params = new BookSearchParametersDto(null, null, null, null);

        Specification<Book> actual = builder.build(params);

        assertThat(actual).isEqualTo(Specification.where(null));
    }
}
