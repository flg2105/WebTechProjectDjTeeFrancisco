package team.projectpulse.system;

public class NotFoundException extends ApiException {
  public NotFoundException(String message) {
    super(StatusCode.NOT_FOUND, message);
  }
}

