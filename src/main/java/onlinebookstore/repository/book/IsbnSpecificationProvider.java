package onlinebookstore.repository.book;

import onlinebookstore.model.Book;
import onlinebookstore.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class IsbnSpecificationProvider implements SpecificationProvider<Book> {
    @Override
    public String getKey() {
        return "isbn";
    }

    @Override
    public Specification<Book> getSpecification(Object params) {
        if (params instanceof String strValue) {
            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get(this.getKey()), "%" + strValue + "%");
        }
        return Specification.where(null);
    }
}
