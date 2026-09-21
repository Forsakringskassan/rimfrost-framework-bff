package se.fk.rimfrost.framework.bff.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OutgoingAuthorizationHeaderFilterTest
{

   @Mock
   private ClientRequestContext requestContext;

   private final AuthorizationHeaderHolder holder = new AuthorizationHeaderHolder();
   private final OutgoingAuthorizationHeaderFilter filter = new OutgoingAuthorizationHeaderFilter();
   private final MultivaluedMap<String, Object> outgoingHeaders = new MultivaluedHashMap<>();

   @Test
   @DisplayName("FBFF-FR-03.1: Sparad Authorization-header vidarebefordras till utgående anrop")
   void filter_authorizationCaptured_forwardsHeader()
   {
      filter.holder = holder;
      holder.setValue("Bearer abc123");
      when(requestContext.getHeaders()).thenReturn(outgoingHeaders);

      filter.filter(requestContext);

      assertEquals("Bearer abc123", outgoingHeaders.getFirst(HttpHeaders.AUTHORIZATION));
   }

   @Test
   @DisplayName("FBFF-FR-03.2: Ingen sparad Authorization-header skickas inte tom eller påhittad vidare")
   void filter_noAuthorizationCaptured_doesNotSetHeader()
   {
      filter.holder = holder;

      filter.filter(requestContext);

      assertNull(outgoingHeaders.getFirst(HttpHeaders.AUTHORIZATION));
      assertFalse(outgoingHeaders.containsKey(HttpHeaders.AUTHORIZATION));
   }

   @Test
   @DisplayName("Utgående anrop utanför request scope (t.ex. schemalagt jobb) kastar inte vidare")
   void filter_holderOutsideRequestScope_doesNotPropagateException()
   {
      filter.holder = new AuthorizationHeaderHolder()
      {
         @Override
         public String getValue()
         {
            throw new ContextNotActiveException();
         }
      };

      filter.filter(requestContext);

      assertNull(outgoingHeaders.getFirst(HttpHeaders.AUTHORIZATION));
   }
}
