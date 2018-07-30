/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.exception;

import eu.arrowhead.common.exception.misc.ExceptionType;

public class ArrowheadException extends RuntimeException {

  private final int errorCode;
  private final ExceptionType exceptionType;

  public ArrowheadException(final String message, final int errorCode, final ExceptionType exceptionType) {
    super(message);
    this.errorCode = errorCode;
    this.exceptionType = exceptionType;
  }

  public ArrowheadException(final String message, final Throwable cause, final int errorCode, final ExceptionType exceptionType) {
    super(message, cause);
    this.errorCode = errorCode;
    this.exceptionType = exceptionType;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public ExceptionType getExceptionType() {
    return exceptionType;
  }
}
