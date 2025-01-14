package onlinebookstore.repository.book;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import onlinebookstore.dto.book.BookSearchParametersDto;
import onlinebookstore.model.Book;
import onlinebookstore.repository.SpecificationBuilder;
import onlinebookstore.repository.SpecificationProviderManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookSpecificationBuilder implements SpecificationBuilder<Book> {
    private final SpecificationProviderManager<Book> specificationProviderManager;

    @Override
    public Specification<Book> build(BookSearchParametersDto searchParameters) {
        Specification<Book> spec = Specification.where(null);
        if (searchParameters.title() != null && !searchParameters.title().isEmpty()) {
            spec = spec.and(specificationProviderManager.getSpecificationProvider("title")
                    .getSpecification(searchParameters.title()));
        }
        if (searchParameters.author() != null && !searchParameters.author().isEmpty()) {
            System.out.println("author");
            spec = spec.and(specificationProviderManager.getSpecificationProvider("author")
                    .getSpecification(searchParameters.author()));
        }
        if (searchParameters.isbn() != null && !searchParameters.isbn().isEmpty()) {
            spec = spec.and(specificationProviderManager.getSpecificationProvider("isbn")
                    .getSpecification(searchParameters.isbn()));
        }
        if (searchParameters.price() != null
                && searchParameters.price().compareTo(BigDecimal.ZERO) > 0) {
            spec = spec.and(specificationProviderManager.getSpecificationProvider("price")
                    .getSpecification(searchParameters.price()));
        }
        return spec;
    }
}
