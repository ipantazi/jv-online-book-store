package mate.academy.onlinebookstore.repository.book;

import mate.academy.onlinebookstore.model.Book;
import mate.academy.onlinebookstore.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TitleSpecificationProvider implements SpecificationProvider<Book> {
    @Override
    public String getKey() {
        return "title";
    }

    @Override
    public Specification<Book> getSpecification(Object params) {
        if (params instanceof String strValue) {
            return ((root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(
                    root.get(this.getKey())), "%" + strValue.toLowerCase() + "%"
            ));
        }
        return Specification.where(null);
    }
}
