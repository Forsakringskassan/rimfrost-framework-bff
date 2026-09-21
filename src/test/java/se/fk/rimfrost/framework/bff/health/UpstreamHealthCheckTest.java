package se.fk.rimfrost.framework.bff.health;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpstreamHealthCheckTest
{

   private WireMockServer server;

   @BeforeEach
   void startServer()
   {
      server = new WireMockServer(wireMockConfig().dynamicPort());
      server.start();
   }

   @AfterEach
   void stopServer()
   {
      server.stop();
   }

   @Test
   @DisplayName("FBFF-FR-02.2: Statuskod under 500 klassificeras som up")
   void call_statusBelow500_returnsUp()
   {
      server.stubFor(head(urlEqualTo("/")).willReturn(aResponse().withStatus(204)));
      UpstreamHealthCheck healthCheck = new UpstreamHealthCheck("oul-backend", server.baseUrl(), 2000);

      HealthCheckResponse response = healthCheck.call();

      assertEquals(HealthCheckResponse.Status.UP, response.getStatus());
      assertEquals("oul-backend", response.getName());
   }

   @Test
   @DisplayName("FBFF-FR-02.2: Statuskod 500 eller högre klassificeras som down")
   void call_statusAtOrAbove500_returnsDown()
   {
      server.stubFor(head(urlEqualTo("/")).willReturn(aResponse().withStatus(503)));
      UpstreamHealthCheck healthCheck = new UpstreamHealthCheck("oul-backend", server.baseUrl(), 2000);

      HealthCheckResponse response = healthCheck.call();

      assertEquals(HealthCheckResponse.Status.DOWN, response.getStatus());
   }

   @Test
   @DisplayName("FBFF-FR-02.2: Anrop som överskrider timeouten klassificeras som down")
   void call_timeout_returnsDown()
   {
      server.stubFor(head(urlEqualTo("/")).willReturn(aResponse().withStatus(204).withFixedDelay(2000)));
      UpstreamHealthCheck healthCheck = new UpstreamHealthCheck("oul-backend", server.baseUrl(), 200);

      HealthCheckResponse response = healthCheck.call();

      assertEquals(HealthCheckResponse.Status.DOWN, response.getStatus());
   }

   @Test
   @DisplayName("FBFF-FR-02.1: fromConfig läser URL:en från angiven config-property-nyckel")
   void fromConfig_resolvesUrlFromConfigPropertyKey()
   {
      server.stubFor(head(urlEqualTo("/")).willReturn(aResponse().withStatus(200)));
      System.setProperty("test.oul.url", server.baseUrl());
      try
      {
         UpstreamHealthCheck healthCheck = UpstreamHealthCheck.fromConfig("oul-backend", "test.oul.url", 2000);

         assertEquals(HealthCheckResponse.Status.UP, healthCheck.call().getStatus());
      }
      finally
      {
         System.clearProperty("test.oul.url");
      }
   }

   @Test
   @DisplayName("FBFF-FR-02.1: Går att registrera flera instanser med olika tjänstenamn och URL:er")
   void call_multipleInstances_areIndependentlyClassified()
   {
      server.stubFor(head(urlEqualTo("/")).willReturn(aResponse().withStatus(200)));
      UpstreamHealthCheck up = new UpstreamHealthCheck("service-a", server.baseUrl(), 2000);
      UpstreamHealthCheck down = new UpstreamHealthCheck("service-b", "http://127.0.0.1:1", 2000);

      assertEquals(HealthCheckResponse.Status.UP, up.call().getStatus());
      assertEquals("service-a", up.call().getName());
      assertEquals(HealthCheckResponse.Status.DOWN, down.call().getStatus());
      assertEquals("service-b", down.call().getName());
   }
}
