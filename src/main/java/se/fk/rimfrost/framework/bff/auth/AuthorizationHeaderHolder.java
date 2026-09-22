package se.fk.rimfrost.framework.bff.auth;

import jakarta.enterprise.context.RequestScoped;

// Request-scoped so it naturally clears between requests; carries the raw header value
// unexamined, never parsed or persisted beyond the request.
@RequestScoped
public class AuthorizationHeaderHolder
{
   private String value;

   public String getValue()
   {
      return value;
   }

   public void setValue(String value)
   {
      this.value = value;
   }
}
