package eu.arrowhead.common.model.messages;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FilterQoSReservationsResponse {

  public List<QoSReservationForm> reservations;

  public FilterQoSReservationsResponse() {
    super();
    // TODO Auto-generated constructor stub
  }

  public FilterQoSReservationsResponse(List<QoSReservationForm> reservations) {
    super();
    this.reservations = reservations;
  }

  public List<QoSReservationForm> getReservations() {
    return reservations;
  }

  public void setReservations(List<QoSReservationForm> reservations) {
    this.reservations = reservations;
  }

}
