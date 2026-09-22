package se.fk.rimfrost.framework.bff.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IncomingAuthorizationHeaderFilterTest
{

   @Mock
   private ContainerRequestContext requestContext;

   private final AuthorizationHeaderHolder holder = new AuthorizationHeaderHolder();
   private final IncomingAuthorizationHeaderFilter filter = new IncomingAuthorizationHeaderFilter();

   @Test
   @DisplayName("FBFF-FR-03.1: Inkommande Authorization-header sparas i holdern")
   void filter_headerPresent_capturesValue()
   {
      filter.holder = holder;
      when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer abc123");

      filter.filter(requestContext);

      assertEquals("Bearer abc123", holder.getValue());
   }

   @Test
   @DisplayName("FBFF-FR-03.2: Ingen inkommande Authorization-header lämnar holdern tom")
   void filter_headerAbsent_leavesHolderEmpty()
   {
      filter.holder = holder;
      when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn(null);

      filter.filter(requestContext);

      assertNull(holder.getValue());
   }
}
