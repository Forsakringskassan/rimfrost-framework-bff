package se.fk.rimfrost.framework.bff.ilogg;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.fk.rimfrost.framework.bff.ilogg.exception.ILoggException;
import se.fk.rimfrost.informationsaccess.InformationsaccessEvent;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ILoggKafkaClientTest
{
   @Mock
   MutinyEmitter<InformationsaccessEvent> iloggEmitter;

   ILoggKafkaClient client;

   @BeforeEach
   void setUp()
   {
      client = new ILoggKafkaClient();
      client.iloggEmitter = iloggEmitter;
      client.publishTimeout = Duration.ofMillis(200);
   }

   @Test
   @DisplayName("FBFF-FR-06.1: Event som Kafka nekar ger ILoggException")
   void publish_nackedEvent_throwsILoggException()
   {
      var event = new InformationsaccessEvent();
      when(iloggEmitter.send(event)).thenReturn(Uni.createFrom().failure(new RuntimeException("nack")));

      assertThrows(ILoggException.class, () -> client.publish(event));
   }

   @Test
   @DisplayName("FBFF-FR-06.3: Event som aldrig kvitteras ger ILoggException efter konfigurerad tid")
   void publish_unacknowledgedEvent_throwsILoggExceptionAfterTimeout()
   {
      var event = new InformationsaccessEvent();
      when(iloggEmitter.send(event)).thenReturn(Uni.createFrom().nothing());

      assertThrows(ILoggException.class, () -> client.publish(event));
   }
}
