package eu.arrowhead.common.exception;

public class ErrorMessage {

  private String errorMessage;
  private int errorCode;
  //TODO modify this once javadocs are published, it could even go to the specific exception doc
  private String documentation = "No documentation yet.";
  // does not have a setter on purpose, only use it with RuntimeExceptions with public String constructor
  private Class exceptionType;

  public ErrorMessage() {
  }

  public ErrorMessage(String errorMessage, int errorCode, Class exceptionType) {
    this.errorMessage = errorMessage;
    this.errorCode = errorCode;
    if (RuntimeException.class == exceptionType || RuntimeException.class.isAssignableFrom(exceptionType)) {
      this.exceptionType = exceptionType;
    } else {
      this.exceptionType = RuntimeException.class;
    }
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

  public String getDocumentation() {
    return documentation;
  }

  public void setDocumentation(String documentation) {
    this.documentation = documentation;
  }

  public Class getExceptionType() {
    return exceptionType;
  }

}
