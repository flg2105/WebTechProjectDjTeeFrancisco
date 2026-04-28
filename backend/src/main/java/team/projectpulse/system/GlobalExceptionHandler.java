package team.projectpulse.system;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Result<Void>> handleApi(ApiException ex) {
    return fail(ex.getCode(), ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException ex) {
    String msg = ex.getBindingResult().getFieldErrors().stream()
        .map(this::formatFieldError)
        .collect(Collectors.joining("; "));
    return fail(StatusCode.INVALID_ARGUMENT, msg.isBlank() ? "Validation failed" : msg);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Result<Void>> handleBadJson(HttpMessageNotReadableException ex) {
    return fail(StatusCode.INVALID_ARGUMENT, "Malformed JSON request");
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException ex) {
    String msg = ex.getConstraintViolations().stream()
        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
        .collect(Collectors.joining("; "));
    return fail(StatusCode.INVALID_ARGUMENT, msg.isBlank() ? "Validation failed" : msg);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Result<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
    return fail(StatusCode.CONFLICT, "Request conflicts with existing data");
  }

  @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
  public ResponseEntity<Result<Void>> handleNotFound(Exception ex) {
    return fail(StatusCode.NOT_FOUND, "Not found");
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<Result<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
    return fail(StatusCode.INVALID_ARGUMENT, "Method not allowed");
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<Result<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
    return fail(StatusCode.INVALID_ARGUMENT, ex.getParameterName() + " is required");
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Result<Void>> handleAccessDenied(AccessDeniedException ex) {
    return fail(StatusCode.FORBIDDEN, "Forbidden");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Result<Void>> handleUnknown(Exception ex) {
    return fail(StatusCode.INTERNAL_SERVER_ERROR, "Internal server error");
  }

  private String formatFieldError(FieldError fe) {
    String field = fe.getField();
    String msg = fe.getDefaultMessage();
    return msg == null ? field + " is invalid" : field + ": " + msg;
  }

  private ResponseEntity<Result<Void>> fail(int code, String message) {
    HttpStatus status = HttpStatus.resolve(code);
    if (status == null) {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    return ResponseEntity.status(status).body(Result.fail(code, message));
  }
}
