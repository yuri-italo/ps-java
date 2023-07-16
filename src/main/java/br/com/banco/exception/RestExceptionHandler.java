package br.com.banco.exception;

import br.com.banco.exception.dto.ExceptionDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class RestExceptionHandler {
    private static final String METHOD_ARGUMENT_NOT_VALID_EXCEPTION_TITLE = "Validation Error";
    private static final String METHOD_ARGUMENT_TYPE_MISMATCH_EXCEPTION_TITLE = "Error converting value";
    private static final String BUSINESS_EXCEPTION_TITLE = "Business error";
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = getBindExceptionErrors(e);
        ExceptionDto exceptionDto = getBindExceptionDto(e, errors);
        return new ResponseEntity<>(exceptionDto,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionDto> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        ExceptionDto exceptionDto = getExceptionDto(e);
        return new ResponseEntity<>(exceptionDto,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ExceptionDto> handleBusinessException(BusinessException e) {
        ExceptionDto exceptionDto = getBussinessExceptionDto(e);
        return new ResponseEntity<>(exceptionDto,HttpStatus.NOT_FOUND);
    }

    private static ExceptionDto getExceptionDto(Exception e) {
        String name = e.getCause().getClass().getSimpleName();
        String message = Objects.requireNonNull(e.getMessage());
        Map<String, String> errors = Map.of(name, message);

        return new ExceptionDto(
                METHOD_ARGUMENT_TYPE_MISMATCH_EXCEPTION_TITLE,
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                e.getClass().getName(),
                errors);
    }

    private ExceptionDto getBussinessExceptionDto(BusinessException e) {
        String name = e.getCause().getClass().getSimpleName();
        String message = Objects.requireNonNull(e.getMessage());
        Map<String, String> errors = Map.of(name, message);

        return new ExceptionDto(
                BUSINESS_EXCEPTION_TITLE,
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                e.getClass().getName(),
                errors);
    }

    private Map<String, String> getBindExceptionErrors(BindException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String messageError = error.getDefaultMessage();
            errors.put(fieldName, messageError);
        });
        return errors;
    }

    private ExceptionDto getBindExceptionDto(BindException e, Map<String, String> errors) {
        return new ExceptionDto(
                METHOD_ARGUMENT_NOT_VALID_EXCEPTION_TITLE,
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                e.getClass().getName(),
                errors
        );
    }
}
