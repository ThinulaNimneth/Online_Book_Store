package lk.ijse.bookstore.exception;

public class ResponseMessage {

    public static final String SUCCESS = "SUCCESS";
    public static final String UNEXPECTED_ERROR = "UNEXPECTED_ERROR";
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";

    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String EMAIL_ALREADY_REGISTERED = "EMAIL_ALREADY_REGISTERED";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";

    public static final String BOOK_NOT_FOUND = "BOOK_NOT_FOUND";
    public static final String CATEGORY_NOT_FOUND = "CATEGORY_NOT_FOUND";
    public static final String AUTHOR_NOT_FOUND = "AUTHOR_NOT_FOUND";
    public static final String PUBLISHER_NOT_FOUND = "PUBLISHER_NOT_FOUND";
    public static final String INVENTORY_NOT_FOUND = "INVENTORY_NOT_FOUND";
    public static final String OUT_OF_STOCK = "OUT_OF_STOCK";

    public static final String CART_NOT_FOUND = "CART_NOT_FOUND";
    public static final String CART_ITEM_NOT_FOUND = "CART_ITEM_NOT_FOUND";
    public static final String CART_EMPTY = "CART_EMPTY";

    public static final String WISHLIST_NOT_FOUND = "WISHLIST_NOT_FOUND";
    public static final String WISHLIST_ITEM_NOT_FOUND = "WISHLIST_ITEM_NOT_FOUND";

    public static final String ADDRESS_NOT_FOUND = "ADDRESS_NOT_FOUND";
    public static final String ORDER_NOT_FOUND = "ORDER_NOT_FOUND";
    public static final String ORDER_PLACED = "ORDER_PLACED";
    public static final String PAYMENT_NOT_FOUND = "PAYMENT_NOT_FOUND";
    public static final String REVIEW_NOT_FOUND = "REVIEW_NOT_FOUND";

    public static final String AI_NOT_CONFIGURED = "AI_NOT_CONFIGURED";
    public static final String AI_REQUEST_FAILED = "AI_REQUEST_FAILED";


    private ResponseMessage(){}
}
