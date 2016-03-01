package eu.arrowhead.core.qos;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.QoSReservationResponse;
import eu.arrowhead.common.model.messages.QoSReserve;
import eu.arrowhead.common.model.messages.QoSVerificationResponse;
import eu.arrowhead.common.model.messages.QoSVerify;
import eu.arrowhead.core.authorization.AuthorizationResource;

/**
 * @author pardavib, mereszd
 *
 */
@Path("QoSManager")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class QoSResource {

	private Map<ArrowheadSystem, Boolean> qosMap = new HashMap<ArrowheadSystem, Boolean>();
	private Map<ArrowheadSystem, String> reject = new HashMap<ArrowheadSystem, String>();
	
	private static Logger log = Logger.getLogger(QoSResource.class.getName());

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "Hello, I am the temporary QoS Service.";
	}

	@PUT
	@Path("/QoSVerify")
	public Response qosVerification(QoSVerify qosVerify) {
		log.info("QoS: Verifying QoS paramteres.");
		//Every system is okay, the reject map is empty
		for (ArrowheadSystem system : qosVerify.getProvider()) {
			qosMap.put(system, true);
		}
		QoSVerificationResponse qvr = new QoSVerificationResponse(qosMap, reject);
		log.info("QoS: QoS paramteres verified.");
		return Response.status(Status.OK).entity(qvr).build();
	}

	@PUT
	@Path("/QoSReserve")
	public Response qosReservation(QoSReserve qosReservation) {
		log.info("QoS: Reserving resouces.");
		QoSReservationResponse qosrr = new QoSReservationResponse(true);
		return Response.status(Status.OK).entity(qosrr).build();
	}
}