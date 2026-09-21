package se.fk.rimfrost.framework.bff.errorhandling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GlobalExceptionMapperTest
{

   private final GlobalExceptionMapper mapper = new GlobalExceptionMapper();

   @Test
   @DisplayName("FBFF-FR-01.1: WebApplicationException mappas till samma statuskod med ErrorResponse")
   void handleWebApplicationException_returnsSameStatus()
   {
      WebApplicationException e = new WebApplicationException(Response.status(404).build());

      Response response = mapper.handleWebApplicationException(e);

      assertEquals(404, response.getStatus());
      assertEquals(new ErrorResponse("Upstream error"), response.getEntity());
   }

   @Test
   @DisplayName("FBFF-FR-01.5: Oväntade exceptions mappas till HTTP 500 med ErrorResponse")
   void handleException_returns500()
   {
      Response response = mapper.handleException(new RuntimeException("boom"));

      assertEquals(500, response.getStatus());
      assertEquals(new ErrorResponse("Internal server error"), response.getEntity());
   }
}
