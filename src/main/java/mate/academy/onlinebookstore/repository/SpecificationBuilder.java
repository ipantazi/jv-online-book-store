package mate.academy.onlinebookstore.repository;

import mate.academy.onlinebookstore.dto.BookSearchParametersDto;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationBuilder<T> {
    public Specification<T> build(BookSearchParametersDto searchParameters);
}
