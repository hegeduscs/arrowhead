package eu.arrowhead.common.model.messages;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QoSReservationResponse {

  private boolean response;
  private QoSReservationCommand command;

  protected QoSReservationResponse() {
    super();
    // TODO Auto-generated constructor stub
  }

  public QoSReservationResponse(boolean response,
                                QoSReservationCommand command) {
    super();
    this.response = response;
    this.command = command;
  }

  public QoSReservationResponse(boolean response) {
    this.response = response;
  }

  public QoSReservationCommand getCommand() {
    return command;
  }

  public void setCommand(QoSReservationCommand command) {
    this.command = command;
  }

  public boolean isResponse() {
    return response;
  }

  public void setResponse(boolean response) {
    this.response = response;
  }

}
