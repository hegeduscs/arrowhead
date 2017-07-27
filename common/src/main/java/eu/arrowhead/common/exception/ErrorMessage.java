package eu.arrowhead.common.exception;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ErrorMessage {

  public final String documentation = "No documentation yet.";
  private String errorMessage;
  private int errorCode;

  public ErrorMessage() {
  }

  public ErrorMessage(String errorMessage, int errorCode) {
    this.errorMessage = errorMessage;
    this.errorCode = errorCode;
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


}
