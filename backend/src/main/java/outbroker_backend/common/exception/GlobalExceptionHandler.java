package outbroker_backend.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


import outbroker_backend.auth.service.OtpService;
import outbroker_backend.common.dto.ApiResponse;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404 - Resource Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(
            ResourceNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }
    @ExceptionHandler(OtpService.OtpRateLimitException.class)
public ResponseEntity<ApiResponse<Object>> handleOtpRateLimit(
        OtpService.OtpRateLimitException ex) {

    return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .body(ApiResponse.error(ex.getMessage()));
}

    // 403 - Custom Unauthorized Access
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorized(
            UnauthorizedAccessException ex) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // 403 - Spring Security @PreAuthorize Failures
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // 400 - DTO Field Validation Errors (@Valid failures)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>>
    handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName =
                            ((FieldError) error).getField();

                    String errorMessage =
                            error.getDefaultMessage();

                    errors.put(fieldName, errorMessage);
                });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed", errors));
    }

    // 400 - Parameter Type Mismatches (e.g. Invalid UUID strings)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        String message = String.format(
                "Invalid parameter '%s': value '%s' is not valid",
                ex.getName(),
                ex.getValue()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    // 400 - Bad Arguments / State Errors
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(
            IllegalArgumentException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // 500 - Generic Uncaught Server Errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex) {

        log.error("Unhandled server exception", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "An unexpected error occurred"
                ));
    }
}