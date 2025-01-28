package onlinebookstore.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(UserEmailNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserEmailNotFoundException ex) {
        Map<String, Object> body = bodyBuilder(ex.getMessage(), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<Object> handleRegistrationException(RegistrationException ex) {
        Map<String, Object> body = bodyBuilder(ex.getMessage(), HttpStatus.CONFLICT);
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataProcessingException.class)
    public ResponseEntity<Object> handleDataProcessingException(DataProcessingException ex) {
        Map<String, Object> body = bodyBuilder(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex) {
        Map<String, Object> body = bodyBuilder(ex.getMessage(), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request
    ) {
        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(this::getErrorMessage)
                .toList();
        Map<String, Object> body = bodyBuilder(errors, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(body, headers, status);
    }

    private String getErrorMessage(ObjectError e) {
        if (e instanceof FieldError fieldError) {
            String fieldName = fieldError.getField();
            String errorMessage = e.getDefaultMessage();
            return fieldName + " " + errorMessage;
        }
        return e.getDefaultMessage();
    }

    private Map<String, Object> bodyBuilder(Object message, HttpStatus httpStatus) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", httpStatus.value());
        body.put("message", message);
        return body;
    }
}
