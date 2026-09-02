package com.zest.productapi.exception;

import com.zest.productapi.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials", request.getRequestURI(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Access denied", request.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiErrorResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), fieldErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request.getRequestURI(), null);
    }

    private ApiErrorResponse.FieldErrorDetail toFieldError(FieldError fieldError) {
        return ApiErrorResponse.FieldErrorDetail.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .build();
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message, String path,
                                                           List<ApiErrorResponse.FieldErrorDetail> fieldErrors) {
        ApiErrorResponse body = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
