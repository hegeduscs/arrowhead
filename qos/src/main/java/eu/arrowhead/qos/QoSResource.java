/* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, you can obtain one at http://mozilla.org/MPL/2.0/. 
*
* This work was supported by National Funds through FCT (Portuguese
* Foundation for Science and Technology) and by the EU ECSEL JU
* funding, within Arrowhead project, ref. ARTEMIS/0001/2012,
* JU grant nr. 332987.
* ISEP, Polytechnic Institute of Porto.
*/
package eu.arrowhead.qos;

import eu.arrowhead.common.exception.DriverNotFoundException;
import eu.arrowhead.common.exception.ReservationException;
import eu.arrowhead.common.messages.QoSReservationResponse;
import eu.arrowhead.common.messages.QoSReserve;
import eu.arrowhead.common.messages.QoSVerificationResponse;
import eu.arrowhead.common.messages.QoSVerify;
import java.io.IOException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
  public Response qosReservation(QoSReserve qosReservation) throws ReservationException, DriverNotFoundException, IOException {

    QoSReservationResponse qosrr = QoSManagerService.qosReserve(qosReservation);
    return Response.status(Status.OK).entity(qosrr).build();
  }

}
