package team.projectpulse.system;

public class Result<T> {
  private boolean flag;
  private int code;
  private String message;
  private T data;

  public Result() {}

  public Result(boolean flag, int code, String message, T data) {
    this.flag = flag;
    this.code = code;
    this.message = message;
    this.data = data;
  }

  public static <T> Result<T> ok(String message, T data) {
    return new Result<>(true, StatusCode.SUCCESS, message, data);
  }

  public static <T> Result<T> fail(int code, String message) {
    return new Result<>(false, code, message, null);
  }

  public boolean isFlag() {
    return flag;
  }

  public void setFlag(boolean flag) {
    this.flag = flag;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }
}

