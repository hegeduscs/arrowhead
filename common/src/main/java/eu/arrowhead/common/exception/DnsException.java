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
