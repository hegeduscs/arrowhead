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
import java.net.HttpURLConnection;

public class ValidationException extends ArrowheadException {

  public ValidationException(final String message) {
    super(message, HttpURLConnection.HTTP_BAD_REQUEST, ExceptionType.VALIDATION);
  }

  public ValidationException(final String message, final Throwable cause) {
    super(message, cause, HttpURLConnection.HTTP_BAD_REQUEST, ExceptionType.VALIDATION);
  }
}
