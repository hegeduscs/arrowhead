/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.exception;

/**
 * Thrown if a resource receives a HTTP payload which have missing mandatory fields.
 */
public class BadPayloadException extends ArrowheadException {

  public BadPayloadException(String msg, int errorCode, String exceptionType, String origin, Throwable cause) {
    super(msg, errorCode, exceptionType, origin, cause);
  }

  public BadPayloadException(String msg, int errorCode, String exceptionType, String origin) {
    super(msg, errorCode, exceptionType, origin);
  }

  public BadPayloadException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public BadPayloadException(String msg) {
    super(msg);
  }
}
