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

        @ExceptionHandler(InvalidStatusTransitionException.class)
        public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidStatusTransitionException ex) {
                return ResponseEntity.badRequest().body(new ErrorResponse(
                                Instant.now(), 400, "Bad Request", ex.getMessage(), List.of()));
        }

        @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
        public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
                        org.springframework.web.multipart.MaxUploadSizeExceededException ex) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new ErrorResponse(
                                Instant.now(), 413, "Payload Too Large",
                                "The uploaded file exceeds the maximum allowed size of 5MB.", List.of()));
        }

        @ExceptionHandler(org.springframework.web.multipart.MultipartException.class)
        public ResponseEntity<ErrorResponse> handleMultipart(
                        org.springframework.web.multipart.MultipartException ex) {
                String message = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
                return ResponseEntity.badRequest().body(new ErrorResponse(
                                Instant.now(), 400, "Bad Request", "Multipart request parsing failed: " + message,
                                List.of()));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
                ex.printStackTrace(); // Log the stack trace on the server for debugging
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                                Instant.now(), 500, "Internal Server Error", ex.getMessage(), List.of()));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body(new ErrorResponse(
                                Instant.now(), 400, "Bad Request", ex.getMessage(), List.of()));
        }
}
