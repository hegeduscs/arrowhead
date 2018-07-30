/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.exception.misc;

import java.time.LocalDateTime;

public class ErrorMessage {

  private final LocalDateTime timestamp;
  private final int status;
  private final String message;
  private final String path;
  private final ExceptionType exceptionType;

  public ErrorMessage(LocalDateTime timestamp, int status, String message, String path, ExceptionType exceptionType) {
    this.timestamp = timestamp;
    this.status = status;
    this.message = message;
    this.path = path;
    this.exceptionType = exceptionType;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public int getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  public String getPath() {
    return path;
  }

  public ExceptionType getExceptionType() {
    return exceptionType;
  }
}
