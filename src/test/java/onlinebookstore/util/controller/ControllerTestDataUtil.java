package onlinebookstore.util.controller;

import java.util.List;
import onlinebookstore.util.TestDataUtil;
import org.springframework.http.HttpStatus;

public class ControllerTestDataUtil extends TestDataUtil {
    public static final int NO_CONTENT = HttpStatus.NO_CONTENT.value();
    public static final int BAD_REQUEST = HttpStatus.BAD_REQUEST.value();
    public static final int NOT_FOUND = HttpStatus.NOT_FOUND.value();
    public static final int UNPROCESSABLE_ENTITY = HttpStatus.UNPROCESSABLE_ENTITY.value();
    public static final String URL_BOOKS = "/books";
    public static final String URL_SEARCH = "/books/search";
    public static final String URL_VALID_BOOK_ID = "/books/" + EXISTING_BOOK_ID;
    public static final String URL_INVALID_BOOK_ID = "/books/" + NOT_EXISTING_BOOK_ID;
    public static final String URL_SAFE_DELETED_BOOK_ID = "/books/" + SAFE_DELETED_BOOK_ID;
    public static final String URL_CATEGORIES = "/categories";
    public static final String URL_VALID_CATEGORY_ID = "/categories/" + EXISTING_CATEGORY_ID;
    public static final String URL_INVALID_CATEGORY_ID = "/categories/" + NOT_EXISTING_CATEGORY_ID;
    public static final String URL_SAFE_DELETED_CATEGORY_ID = "/categories/"
            + SAFE_DELETED_CATEGORY_ID;
    public static final String URL_GET_BOOKS_BY_VALID_CATEGORY_ID = "/categories/"
            + EXISTING_CATEGORY_ID + "/books";
    public static final String URL_GET_BOOKS_BY_ALTERNATIVE_CATEGORY_ID = "/categories/"
            + ALTERNATIVE_CATEGORY_ID + "/books";
    public static final String URL_GET_BOOKS_BY_INVALID_CATEGORY_ID = "/categories/"
            + NOT_EXISTING_CATEGORY_ID + "/books";
    public static final List<String> expectedSearchErrorMessages = List.of(
            "title Invalid title. Size should not exceed 100 characters.",
            "author Invalid author. Size should not exceed 50 characters.",
            "isbn Invalid ISBN format. Only digits and dashes. "
                    + "Size should not exceed 13 characters.",
            "priceRange[0] Invalid price. Value should be positive.",
            "priceRange[1] Invalid price. Value should be positive."
    );
    public static final List<String> EXPECTED_BOOK_ERRORS = List.of(
            "title Invalid title. Size should be between 3 to 100.",
            "author Invalid author. Size should be between 3 to 50.",
            "isbn Invalid ISBN format. ISBN must contain exactly 10 or 13 digits, "
                    + "with optional dashes.",
            "price Invalid price. Value should be positive.",
            "coverImage Invalid URL. Please provide a valid UPL of cover image.",
            "categoryIds[] Category Id shouldn't be null."
    );
    public static final List<String> EXPECTED_BOOK_NULL_ERRORS = List.of(
            "title Invalid title. Title should not be blank.",
            "author Invalid author. Author should not be blank.",
            "isbn Invalid ISBN. ISBN should not be blank.",
            "price Invalid price. Please enter price.",
            "categoryIds Invalid categories. Categories shouldn't be empty."
    );
    public static final List<String> EXPECTED_CATEGORY_ERRORS = List.of(
            "name Invalid name. Size should be between 3 or 100.",
            "description Description must not exceed 500 characters."
    );
    public static final List<String> EXPECTED_CATEGORY_NULL_ERRORS = List.of(
            "name Invalid name. Name shouldn't be blank."
    );

}
