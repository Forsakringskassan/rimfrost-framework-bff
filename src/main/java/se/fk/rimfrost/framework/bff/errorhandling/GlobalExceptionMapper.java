package se.fk.rimfrost.framework.bff.errorhandling;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class GlobalExceptionMapper
{
   private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionMapper.class);

   @ServerExceptionMapper
   public Response handleWebApplicationException(WebApplicationException e)
   {
      LOGGER.error("Upstream error status={}", e.getResponse().getStatus(), e);
      return Response.status(e.getResponse().getStatus())
            .entity(new ErrorResponse("Upstream error")).build();
   }

   @ServerExceptionMapper
   public Response handleException(Exception e)
   {
      LOGGER.error("Internal error", e);
      return Response.status(500).entity(new ErrorResponse("Internal server error")).build();
   }
}
