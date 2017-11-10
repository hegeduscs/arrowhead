package eu.arrowhead.common.exception;

public class ErrorMessage {

  private String errorMessage;
  private int errorCode;
  private String exceptionType;
  private String documentation = "https://github.com/hegeduscs/arrowhead/tree/master/documentation";

  public ErrorMessage() {
  }

  public ErrorMessage(String errorMessage, int errorCode, String exceptionType) {
    this.errorMessage = errorMessage;
    this.errorCode = errorCode;
    this.exceptionType = exceptionType;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(int errorCode) {
    this.errorCode = errorCode;
  }

  public String getExceptionType() {
    return exceptionType;
  }

  public void setExceptionType(String exceptionType) {
    this.exceptionType = exceptionType;
  }

  public String getDocumentation() {
    return documentation;
  }

  public void setDocumentation(String documentation) {
    this.documentation = documentation;
  }

}
