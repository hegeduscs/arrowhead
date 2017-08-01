package eu.arrowhead.common.model.messages;

public class QoSReservationResponse {

  private boolean response;
  private QoSReservationCommand command;

  public QoSReservationResponse() {
  }

  public QoSReservationResponse(boolean response, QoSReservationCommand command) {
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
