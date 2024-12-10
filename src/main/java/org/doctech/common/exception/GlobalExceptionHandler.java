package org.doctech.common.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.doctech.common.dto.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Handle validation exceptions
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }

        ApiResponse apiResponse = new ApiResponse(
                false,
                "Validation failed",
                errors
        );

        return handleExceptionInternal(
                ex,
                apiResponse,
                headers,
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    // Handle missing parameters
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String error = ex.getParameterName() + " parameter is missing";

        ApiResponse apiResponse = new ApiResponse(
                false,
                "Missing Parameter",
                error
        );

        return handleExceptionInternal(
                ex,
                apiResponse,
                headers,
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    // Handle constraint violations
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = new ArrayList<>();

        ex.getConstraintViolations().forEach(violation -> {
            errors.add(violation.getPropertyPath() + ": " + violation.getMessage());
        });

        return new ApiResponse(
                false,
                "Validation failed",
                errors
        );
    }

    // Handle type mismatch
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String error = ex.getName() + " should be of type " +
                Objects.requireNonNull(ex.getRequiredType()).getName();

        return new ApiResponse(
                false,
                "Type Mismatch",
                error
        );
    }

    // Handle not found exceptions
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse handleEntityNotFound(EntityNotFoundException ex) {
        return new ApiResponse(
                false,
                "Resource Not Found",
                ex.getMessage()
        );
    }

    // Handle authentication exceptions
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse handleAuthentication(AuthenticationException ex) {
        return new ApiResponse(
                false,
                "Authentication Failed",
                ex.getMessage()
        );
    }

    // Handle bad credentials
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse handleBadCredentials(BadCredentialsException ex) {
        return new ApiResponse(
                false,
                "Invalid Credentials",
                ex.getMessage()
        );
    }

    // Handle access denied
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse handleAccessDenied(AccessDeniedException ex) {
        return new ApiResponse(
                false,
                "Access Denied",
                ex.getMessage()
        );
    }

    // Handle data integrity violations
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return new ApiResponse(
                false,
                "Data Integrity Violation",
                "Database error occurred"
        );
    }

    // Handle custom business exceptions
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse handleUserNotFound(UserNotFoundException ex) {
        return new ApiResponse(
                false,
                "User Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return new ApiResponse(
                false,
                "User Already Exists",
                ex.getMessage()
        );
    }

    @ExceptionHandler(InsufficientPointsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse handleInsufficientPoints(InsufficientPointsException ex) {
        return new ApiResponse(
                false,
                "Insufficient Points",
                ex.getMessage()
        );
    }

    @ExceptionHandler(BadgeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse handleBadgeNotFound(BadgeNotFoundException ex) {
        return new ApiResponse(
                false,
                "Badge Not Found",
                ex.getMessage()
        );
    }

    // Handle all other exceptions
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse handleAll(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return new ApiResponse(
                false,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later."
        );
    }
}