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
 * Used by the legacy Service Registry, to handle DNS-SD related exceptions.
 */
public class DnsException extends RuntimeException {

  private static final long serialVersionUID = 3694632380586684627L;

  public DnsException(String message) {
    super(message);
  }
}
