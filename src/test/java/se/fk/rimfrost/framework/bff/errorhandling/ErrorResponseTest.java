package se.fk.rimfrost.framework.bff.errorhandling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ErrorResponseTest
{

   private final ObjectMapper mapper = new ObjectMapper();

   @Test
   @DisplayName("FBFF-FR-01.4: ErrorResponse serialiseras till {\"error\": \"...\"}")
   void serialize_returnsErrorField() throws Exception
   {
      String json = mapper.writeValueAsString(new ErrorResponse("Internal server error"));

      assertEquals("{\"error\":\"Internal server error\"}", json);
   }
}
