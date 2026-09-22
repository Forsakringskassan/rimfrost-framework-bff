package se.fk.rimfrost.framework.bff.health;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import java.net.HttpURLConnection;
import java.net.URI;

public class UpstreamHealthCheck implements HealthCheck
{

   private final String serviceName;
   private final String url;
   private final int timeoutMillis;

   public UpstreamHealthCheck(String serviceName, String url, int timeoutMillis)
   {
      this.serviceName = serviceName;
      this.url = url;
      this.timeoutMillis = timeoutMillis;
   }

   // Resolves the URL from MP Config at construction time, so a consuming BFF can wire up an
   // instance without declaring its own @ConfigProperty field for the URL.
   public static UpstreamHealthCheck fromConfig(String serviceName, String urlConfigPropertyKey, int timeoutMillis)
   {
      String url = ConfigProvider.getConfig().getValue(urlConfigPropertyKey, String.class);
      return new UpstreamHealthCheck(serviceName, url, timeoutMillis);
   }

   @Override
   public HealthCheckResponse call()
   {
      HttpURLConnection connection = null;
      try
      {
         connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
         connection.setConnectTimeout(timeoutMillis);
         connection.setReadTimeout(timeoutMillis);
         connection.setRequestMethod("HEAD");
         int status = connection.getResponseCode();
         if (status < 500)
         {
            return HealthCheckResponse.up(serviceName);
         }
         return HealthCheckResponse.named(serviceName).down()
               .withData("status", status)
               .build();
      }
      catch (Exception e)
      {
         return HealthCheckResponse.named(serviceName).down()
               .withData("error", e.getMessage())
               .build();
      }
      finally
      {
         if (connection != null)
         {
            connection.disconnect();
         }
      }
   }
}
