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

public class EntityNotFoundException extends ArrowheadException {

  public EntityNotFoundException(final String message) {
    super(message, HttpURLConnection.HTTP_NOT_FOUND, ExceptionType.ENTITY_NOT_FOUND);
  }

  public EntityNotFoundException(final String message, final Throwable cause) {
    super(message, cause, HttpURLConnection.HTTP_NOT_FOUND, ExceptionType.ENTITY_NOT_FOUND);
  }
}
