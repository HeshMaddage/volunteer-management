package com.volunteer.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/**
 * Catches exceptions thrown anywhere in a controller/service and converts
 * them into a consistent JSON error shape, instead of leaking a raw Java
 * stack trace to the client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        return ResponseEntity.badRequest().body(new ErrorResponse(
                Instant.now(), 400, "Validation Failed", "Request body is invalid", details));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                Instant.now(), 409, "Conflict", ex.getMessage(), List.of()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                Instant.now(), 404, "Not Found", ex.getMessage(), List.of()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(
                Instant.now(), 401, "Unauthorized", "Invalid email or password", List.of()));
    }

    @ExceptionHandler(ShiftFullException.class)
    public ResponseEntity<ErrorResponse> handleShiftFull(ShiftFullException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                Instant.now(), 409, "Conflict", ex.getMessage(), List.of()));
    }

    @ExceptionHandler(AlreadyRegisteredException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyRegistered(AlreadyRegisteredException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                Instant.now(), 409, "Conflict", ex.getMessage(), List.of()));
    }
}
