package lk.ijse.bookstore.exception;

import lk.ijse.bookstore.dto.response.CommonResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<lk.ijse.bookstore.dto.response.CommonResponse> handleServerException(Exception ex, WebRequest webRequest) {
        ex.printStackTrace();
        return ResponseEntity.ok(new lk.ijse.bookstore.dto.response.CommonResponse(ResponseCode.INTERNAL_ERROR, ResponseMessage.UNEXPECTED_ERROR));
    }

    @ExceptionHandler(value = {CustomerException.class})
    public ResponseEntity<lk.ijse.bookstore.dto.response.CommonResponse> handleCustomException(CustomerException ex, WebRequest webRequest) {
        return ResponseEntity.ok(new lk.ijse.bookstore.dto.response.CommonResponse(ex.getStatus(), ex.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        lk.ijse.bookstore.dto.response.CommonResponse response = new CommonResponse(ResponseCode.VALIDATION_FAILED,
                message.isEmpty() ? ResponseMessage.VALIDATION_FAILED : message);
        return ResponseEntity.ok(response);
    }
}