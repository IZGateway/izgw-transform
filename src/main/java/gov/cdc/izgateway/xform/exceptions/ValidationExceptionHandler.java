package gov.cdc.izgateway.xform.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is used to tweak the standard error message provided by MethodArgumentNotValidException.
 * That is tripped when an invalid message is sent to the APIs used to configure the system.
 * The default message is a bit too generic to tell the caller _what_ was incorrect in their supplied
 * data.
 * By default, the _message_ will say something like:
 *   Validation failed for object='solution'. Error count: 1
 * This change will make that error more descriptive, something like:
 *   requestOperations: Request Operations List is required (can be empty)
 * The above example would be if you tried to add a Solution that did not have the Request Operations
 * specified.
 */
@ControllerAdvice
public class ValidationExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, Object> errors = new HashMap<>();

        errors.put("timestamp", System.currentTimeMillis());
        errors.put("status", HttpStatus.BAD_REQUEST.value());
        errors.put("error", "Bad Request");
        errors.put("path", request.getRequestURI());

        // Get all the field errors in the inbound data
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        if (!fieldErrors.isEmpty()) {
            String errorMessage = fieldErrors.stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining("; "));
            errors.put("message", errorMessage);
        }

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}

