package onlinebookstore.util.controller;

import java.util.List;
import onlinebookstore.util.TestDataUtil;
import org.springframework.http.HttpStatus;

public class ControllerTestDataUtil extends TestDataUtil {

    public static final int NO_CONTENT = HttpStatus.NO_CONTENT.value();
    public static final int BAD_REQUEST = HttpStatus.BAD_REQUEST.value();
    public static final int NOT_FOUND = HttpStatus.NOT_FOUND.value();
    public static final int UNPROCESSABLE_ENTITY = HttpStatus.UNPROCESSABLE_ENTITY.value();
    public static final int UNAUTHORIZED = HttpStatus.UNAUTHORIZED.value();
    public static final int CONFLICT = HttpStatus.CONFLICT.value();
    public static final String URL_BOOKS = "/books";
    public static final String URL_BOOKS_SEARCH = "/books/search";
    public static final String URL_BOOKS_EXISTING_BOOK_ID = "/books/" + EXISTING_BOOK_ID;
    public static final String URL_BOOKS_NOT_EXISTING_BOOK_ID = "/books/" + NOT_EXISTING_BOOK_ID;
    public static final String URL_BOOKS_SAFE_DELETED_BOOK_ID = "/books/" + SAFE_DELETED_BOOK_ID;
    public static final String URL_CATEGORIES = "/categories";
    public static final String URL_CATEGORIES_EXISTING_CATEGORY_ID = "/categories/"
            + EXISTING_CATEGORY_ID;
    public static final String URL_CATEGORIES_NOT_EXISTING_CATEGORY_ID = "/categories/"
            + NOT_EXISTING_CATEGORY_ID;
    public static final String URL_CATEGORIES_SAFE_DELETED_CATEGORY_ID = "/categories/"
            + SAFE_DELETED_CATEGORY_ID;
    public static final String URL_GET_BOOKS_BY_EXISTING_CATEGORY_ID = "/categories/"
            + EXISTING_CATEGORY_ID + "/books";
    public static final String URL_GET_BOOKS_BY_ALTERNATIVE_CATEGORY_ID = "/categories/"
            + ALTERNATIVE_CATEGORY_ID + "/books";
    public static final String URL_GET_BOOKS_BY_NOT_EXISTING_CATEGORY_ID = "/categories/"
            + NOT_EXISTING_CATEGORY_ID + "/books";
    public static final String URL_SHOPPING_CART = "/cart";
    public static final String URL_SHOPPING_CART_ITEMS_EXISTING_CART_ID = "/cart/items/"
            + EXISTING_CART_ITEM_ID;
    public static final String URL_SHOPPING_CART_ITEMS_NOT_EXISTING_CART_ID = "/cart/items/"
            + NOT_EXISTING_CART_ITEM_ID;
    public static final String URL_ORDERS = "/orders";
    public static final String URL_ORDERS_EXISTING_ORDER_ID = "/orders/" + EXISTING_ORDER_ID;
    public static final String URL_ORDERS_NOT_EXISTING_ORDER_ID = "/orders/"
            + NOT_EXISTING_ORDER_ID;
    public static final String URL_ORDER_ITEMS_EXISTING_ORDER_ID_AND_ITEM_ID = "/orders/"
            + EXISTING_ORDER_ID + "/items/" + EXISTING_ORDER_ITEM_ID;
    public static final String URL_ORDER_ITEMS_EXISTING_ORDER_ID_AND_NOT_EXISTING_ITEM_ID =
            "/orders/" + EXISTING_ORDER_ID + "/items/" + NOT_EXISTING_ORDER_ITEM_ID;
    public static final String URL_ORDER_ITEMS_NOT_EXISTING_ORDER_ID_AND_EXISTING_ITEM_ID =
            "/orders/" + NOT_EXISTING_ORDER_ID + "/items/" + EXISTING_ORDER_ITEM_ID;
    public static final String URL_ORDER_ITEMS_EXISTING_ORDER_ID = "/orders/" + EXISTING_ORDER_ID
            + "/items";
    public static final String URL_ORDER_ITEMS_NOT_EXISTING_ORDER_ID = "/orders/"
            + NOT_EXISTING_ORDER_ID + "/items";
    public static final String URL_LOGIN = "/auth/login";

    public static final String URL_REGISTRATION = "/auth/registration";
    public static final List<String> EXPECTED_SEARCH_ERROR_MESSAGES = List.of(
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
    public static final List<String> EXPECTED_CART_ITEM_ERRORS = List.of(
            "bookId Invalid book id. Value shouldn't be null",
            "quantity Invalid quantity. Value should be positive."
    );
    public static final List<String> EXPECTED_CART_ITEM_VALUE_NEGATIVE_ERRORS = List.of(
            "bookId Invalid book id. Value should be positive.",
            "quantity Invalid quantity. Value should be positive."
    );
    public static final List<String> EXPECTED_ADD_ORDER_ERRORS = List.of(
            "shippingAddress Invalid shipping address. Address can't be blank.",
            "shippingAddress Invalid shipping address. Address cannot be less than 10 characters."
    );
    public static final List<String> EXPECTED_USER_LOGIN_BLANK_ERRORS = List.of(
            "email Invalid email. Email shouldn't be blank.",
            "password Invalid password. Password shouldn't be blank.",
            "password Invalid password. The password should be between 8 to 50."
    );
    public static final List<String> EXPECTED_USER_LOGIN_FORMAT_ERRORS = List.of(
            "email Email address should be exceed 50 characters.",
            "password Invalid password. The password should be between 8 to 50.",
            "email Invalid format email."
    );
    public static final List<String> EXPECTED_USER_REGISTRATION_SIZE_ERRORS = List.of(
            "email Email address must not exceed 50 characters.",
            "email Invalid format email.",
            "password Invalid password. Password shouldn't be blank.",
            "password Invalid password. The password should be between 8 to 50.",
            "password Password must include at least one lowercase letter, one uppercase letter, "
                    + "one number, and one special character.",
            "firstName Invalid first name. First name shouldn't be blank.",
            "firstName Invalid first name. First name should be between 3 to 50.",
            "lastName Invalid last name. Last name shouldn't be blank.",
            "lastName Invalid last name. Last name should be between 3 to 50.",
            "shippingAddress Shipping address must not exceed 100 characters."
    );
    public static final List<String> EXPECTED_USER_REGISTRATION_FORMAT_ERRORS = List.of(
            "email Invalid format email.",
            "password Password must include at least one lowercase letter, one uppercase letter, "
                    + "one number, and one special character.",
            "firstName First name must contain only letters.",
            "lastName Last name must be contain only letters."
    );

    private ControllerTestDataUtil() {
    }
}
