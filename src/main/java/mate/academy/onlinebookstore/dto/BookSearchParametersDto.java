package mate.academy.onlinebookstore.dto;

import java.math.BigDecimal;

public record BookSearchParametersDto(String title, String author, String isbn, BigDecimal price) {
}
