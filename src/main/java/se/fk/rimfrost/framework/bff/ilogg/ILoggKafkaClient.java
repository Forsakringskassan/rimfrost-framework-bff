package se.fk.rimfrost.framework.bff.ilogg;

import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import se.fk.rimfrost.framework.bff.ilogg.exception.ILoggException;
import se.fk.rimfrost.informationsaccess.InformationsaccessEvent;

@ApplicationScoped
public class ILoggKafkaClient implements ILogg
{
   @Inject
   @Channel("ilogg-event")
   @OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 1024)
   MutinyEmitter<InformationsaccessEvent> iloggEmitter;

   @ConfigProperty(name = "rimfrost.ilogg.publish-timeout", defaultValue = "10s")
   Duration publishTimeout;

   @Override
   public void publish(InformationsaccessEvent event) throws ILoggException
   {
      try
      {
         iloggEmitter.send(event).await().atMost(publishTimeout);
      }
      catch (Exception e)
      {
         throw new ILoggException(e.getMessage(), e);
      }
   }
}
