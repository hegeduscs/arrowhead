package eu.arrowhead.common.exception;

/**
 * Used by the QoS core system when a problem occurs during the resource reservation.
 */
public class ReservationException extends RuntimeException {

  public ReservationException() {
    super();
  }

  public ReservationException(String message) {
    super(message);
  }

}
