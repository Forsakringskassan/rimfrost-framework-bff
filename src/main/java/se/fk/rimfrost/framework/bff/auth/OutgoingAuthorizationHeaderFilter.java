package se.fk.rimfrost.framework.bff.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @Provider on a CDI bean registers it globally for every REST Client interface in the
// application (quarkus.rest-client.provider-autodiscovery, default true), so consuming BFFs
// get outgoing forwarding for free without declaring anything on their client interfaces.
@Provider
@ApplicationScoped
public class OutgoingAuthorizationHeaderFilter implements ClientRequestFilter
{
   private static final Logger LOGGER = LoggerFactory.getLogger(OutgoingAuthorizationHeaderFilter.class);

   @Inject
   AuthorizationHeaderHolder holder;

   @Override
   public void filter(ClientRequestContext requestContext)
   {
      // Never override a header the client interface already set explicitly (e.g. a
      // service-to-service call authenticating with its own client-credentials token).
      if (requestContext.getHeaders().containsKey(HttpHeaders.AUTHORIZATION))
      {
         return;
      }
      String authorization = readAuthorization();
      if (authorization != null)
      {
         requestContext.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, authorization);
      }
   }

   // A REST client call made outside an HTTP request (e.g. from a @Scheduled job) has no
   // active request scope to read from; there is then nothing to forward.
   private String readAuthorization()
   {
      try
      {
         return holder.getValue();
      }
      catch (ContextNotActiveException e)
      {
         LOGGER.debug("No active request scope; skipping Authorization header forwarding", e);
         return null;
      }
   }
}
