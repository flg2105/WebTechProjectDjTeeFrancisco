package team.projectpulse.system;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public Result<Void> handleApi(ApiException ex) {
    return Result.fail(ex.getCode(), ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Result<Void> handleValidation(MethodArgumentNotValidException ex) {
    String msg = ex.getBindingResult().getFieldErrors().stream()
        .map(this::formatFieldError)
        .collect(Collectors.joining("; "));
    return Result.fail(StatusCode.INVALID_ARGUMENT, msg.isBlank() ? "Validation failed" : msg);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public Result<Void> handleBadJson(HttpMessageNotReadableException ex) {
    return Result.fail(StatusCode.INVALID_ARGUMENT, "Malformed JSON request");
  }

  @ExceptionHandler(Exception.class)
  public Result<Void> handleUnknown(Exception ex) {
    return Result.fail(StatusCode.INTERNAL_SERVER_ERROR, "Internal server error");
  }

  private String formatFieldError(FieldError fe) {
    String field = fe.getField();
    String msg = fe.getDefaultMessage();
    return msg == null ? field + " is invalid" : field + ": " + msg;
  }
}

