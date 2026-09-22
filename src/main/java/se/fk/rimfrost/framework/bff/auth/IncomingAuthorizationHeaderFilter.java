package se.fk.rimfrost.framework.bff.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;

// @Provider on a CDI bean registers it globally for every JAX-RS resource in the application,
// so consuming BFFs get this for free without declaring anything on their endpoints.
@Provider
@ApplicationScoped
public class IncomingAuthorizationHeaderFilter implements ContainerRequestFilter
{
   @Inject
   AuthorizationHeaderHolder holder;

   @Override
   public void filter(ContainerRequestContext requestContext)
   {
      String authorization = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
      if (authorization != null)
      {
         holder.setValue(authorization);
      }
   }
}
