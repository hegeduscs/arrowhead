/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

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
