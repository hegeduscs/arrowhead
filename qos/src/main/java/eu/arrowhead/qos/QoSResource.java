/*
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

import eu.arrowhead.common.exception.DriverNotFoundException;
import eu.arrowhead.common.exception.ReservationException;
import eu.arrowhead.common.messages.QoSReservationResponse;
import eu.arrowhead.common.messages.QoSReserve;
import eu.arrowhead.common.messages.QoSVerificationResponse;
import eu.arrowhead.common.messages.QoSVerify;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

ackage eu.arrowhead.qos;

@Path("qos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class QoSResource {

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "QoS Manager Core System";
  }

  @PUT
  @Path("verify")
  public Response qosVerification(QoSVerify qosVerify) {

    QoSVerificationResponse qvr = QoSManagerService.qosVerify(qosVerify);
    return Response.status(Status.OK).entity(qvr).build();
  }

  @PUT
  @Path("reserve")
  public Response qosReservation(QoSReserve qosReservation) throws ReservationException, DriverNotFoundException {

    QoSReservationResponse qosrr = QoSManagerService.qosReserve(qosReservation);
    return Response.status(Status.OK).entity(qosrr).build();
  }

}
