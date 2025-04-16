package onlinebookstore.repository.book;

import java.math.BigDecimal;
import java.util.List;
import onlinebookstore.model.Book;
import onlinebookstore.repository.SpecificationProvider;
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
        if (params instanceof List<?> paramList && !paramList.isEmpty()) {
            BigDecimal fromPrice = paramList.get(0) instanceof BigDecimal
                    ? (BigDecimal) paramList.get(0) : null;
            BigDecimal toPrice = paramList.size() > 1 && paramList.get(1) instanceof BigDecimal
                    ? (BigDecimal) paramList.get(1) : null;

            return ((root, query, criteriaBuilder) -> {
                if (fromPrice != null && toPrice != null) {
                    return criteriaBuilder.between(
                            root.get(this.getKey()), fromPrice, toPrice);
                } else if (fromPrice != null) {
                    return criteriaBuilder.greaterThanOrEqualTo(
                            root.get(this.getKey()), fromPrice);
                } else if (toPrice != null) {
                    return criteriaBuilder.lessThanOrEqualTo(
                            root.get(this.getKey()), toPrice);
                }
                return criteriaBuilder.conjunction();
            });
        }
        return Specification.where(null);
    }
}
