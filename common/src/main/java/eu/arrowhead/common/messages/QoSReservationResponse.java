package eu.arrowhead.common.messages;

public class QoSReservationResponse {

  private boolean successfulReservation;
  private QoSReservationCommand command;

  public QoSReservationResponse() {
  }

  public QoSReservationResponse(boolean successfulReservation, QoSReservationCommand command) {
    this.successfulReservation = successfulReservation;
    this.command = command;
  }

  public QoSReservationResponse(boolean successfulReservation) {
    this.successfulReservation = successfulReservation;
  }

  public QoSReservationCommand getCommand() {
    return command;
  }

  public void setCommand(QoSReservationCommand command) {
    this.command = command;
  }

  public boolean isSuccessfulReservation() {
    return successfulReservation;
  }

  public void setSuccessfulReservation(boolean successfulReservation) {
    this.successfulReservation = successfulReservation;
  }

}
