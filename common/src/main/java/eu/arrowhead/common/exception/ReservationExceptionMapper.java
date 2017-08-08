/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.common.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author ID0084D
 */
@Provider
public class ReservationExceptionMapper implements ExceptionMapper<ReservationException> {

  @Override
  public Response toResponse(ReservationException ex) {
    ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), 400);
    return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).header("Content-type", "application/json").build();
  }

}
