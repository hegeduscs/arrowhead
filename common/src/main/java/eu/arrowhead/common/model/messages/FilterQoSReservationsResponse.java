package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

public class FilterQoSReservationsResponse {

  private List<QoSReservationForm> reservations = new ArrayList<>();

  public FilterQoSReservationsResponse() {
  }

  public FilterQoSReservationsResponse(List<QoSReservationForm> reservations) {
    this.reservations = reservations;
  }

  public List<QoSReservationForm> getReservations() {
    return reservations;
  }

  public void setReservations(List<QoSReservationForm> reservations) {
    this.reservations = reservations;
  }

}
