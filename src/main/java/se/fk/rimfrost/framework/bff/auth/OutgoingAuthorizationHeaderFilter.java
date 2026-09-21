package se.fk.rimfrost.framework.bff.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;

// @Provider on a CDI bean registers it globally for every REST Client interface in the
// application (quarkus.rest-client.provider-autodiscovery, default true), so consuming BFFs
// get outgoing forwarding for free without declaring anything on their client interfaces.
@Provider
@ApplicationScoped
public class OutgoingAuthorizationHeaderFilter implements ClientRequestFilter
{
   @Inject
   AuthorizationHeaderHolder holder;

   @Override
   public void filter(ClientRequestContext requestContext)
   {
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
         return null;
      }
   }
}
