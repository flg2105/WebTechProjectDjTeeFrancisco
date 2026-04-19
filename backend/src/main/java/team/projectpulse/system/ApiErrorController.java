package team.projectpulse.system;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;

@RestController
public class ApiErrorController implements ErrorController {
  private final ErrorAttributes errorAttributes;

  public ApiErrorController(ErrorAttributes errorAttributes) {
    this.errorAttributes = errorAttributes;
  }

  @RequestMapping("/error")
  public ResponseEntity<Result<Void>> error(HttpServletRequest request) {
    ServletWebRequest webRequest = new ServletWebRequest(request);
    Map<String, Object> attrs = errorAttributes.getErrorAttributes(
        webRequest,
        ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE)
    );

    int httpStatus = parseInt(attrs.get("status"), 500);
    int code = normalizeCode(httpStatus);
    String message = defaultMessage(code);

    HttpStatus status = HttpStatus.resolve(code);
    if (status == null) {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    return ResponseEntity.status(status).body(Result.fail(code, message));
  }

  private int parseInt(Object value, int fallback) {
    if (value instanceof Integer i) {
      return i;
    }
    if (value instanceof String s) {
      try {
        return Integer.parseInt(s);
      } catch (NumberFormatException ignored) {
        return fallback;
      }
    }
    return fallback;
  }

  private int normalizeCode(int httpStatus) {
    return switch (httpStatus) {
      case 400 -> StatusCode.INVALID_ARGUMENT;
      case 401 -> StatusCode.UNAUTHORIZED;
      case 403 -> StatusCode.FORBIDDEN;
      case 404 -> StatusCode.NOT_FOUND;
      case 405 -> StatusCode.INVALID_ARGUMENT;
      case 409 -> StatusCode.CONFLICT;
      case 423 -> StatusCode.LOCKED;
      case 500 -> StatusCode.INTERNAL_SERVER_ERROR;
      default -> StatusCode.INTERNAL_SERVER_ERROR;
    };
  }

  private String defaultMessage(int code) {
    return switch (code) {
      case StatusCode.INVALID_ARGUMENT -> "Invalid request";
      case StatusCode.UNAUTHORIZED -> "Unauthorized";
      case StatusCode.FORBIDDEN -> "Forbidden";
      case StatusCode.NOT_FOUND -> "Not found";
      case StatusCode.CONFLICT -> "Conflict";
      case StatusCode.LOCKED -> "Locked";
      case StatusCode.INTERNAL_SERVER_ERROR -> "Internal server error";
      default -> "Internal server error";
    };
  }
}
