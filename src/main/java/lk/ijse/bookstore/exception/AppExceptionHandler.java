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
    public ResponseEntity<CommonResponse> handleServerException(Exception ex, WebRequest webRequest) {
        ex.printStackTrace();
        return ResponseEntity.status(ResponseCode.INTERNAL_ERROR)
                .body(new CommonResponse(ResponseCode.INTERNAL_ERROR, ResponseMessage.UNEXPECTED_ERROR));
    }

    @ExceptionHandler(value = {CustomerException.class})
    public ResponseEntity<CommonResponse> handleCustomException(CustomerException ex, WebRequest webRequest) {

        return ResponseEntity.status(ex.getStatus())
                .body(new CommonResponse(ex.getStatus(), ex.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        CommonResponse response = new CommonResponse(ResponseCode.VALIDATION_FAILED,
                message.isEmpty() ? ResponseMessage.VALIDATION_FAILED : message);
        return ResponseEntity.status(ResponseCode.VALIDATION_FAILED).body(response);
    }
}