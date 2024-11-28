package mate.academy.onlinebookstore.repository.book;

import java.math.BigDecimal;
import mate.academy.onlinebookstore.model.Book;
import mate.academy.onlinebookstore.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class PriceSpecificationProvider implements SpecificationProvider<Book> {
    @Override
    public String getKey() {
        return "price";
    }

    @Override
    public Specification<Book> getSpecification(Object params) {
        if (params instanceof BigDecimal price) {
            return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(
                    this.getKey()), price
            ));
        }
        return Specification.where(null);
    }
}
