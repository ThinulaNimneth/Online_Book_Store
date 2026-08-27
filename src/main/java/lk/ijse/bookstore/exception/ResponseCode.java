package lk.ijse.bookstore.exception;

public class ResponseCode {

    public static final int  SUCCESS = 0;
    public static final int NOT_FOUND = 404;
    public static final int VALIDATION_FAILED = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int CONFLICT = 409;
    public static final int INTERNAL_ERROR = 500;


    private ResponseCode(){}
}
