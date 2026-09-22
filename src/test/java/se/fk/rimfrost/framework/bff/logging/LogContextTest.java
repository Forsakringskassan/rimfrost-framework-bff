package se.fk.rimfrost.framework.bff.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.Map;

class LogContextTest
{

   @Test
   @DisplayName("FBFF-FR-04.1: put sätter MDC-nyckeln för anropets livslängd")
   void put_setsMdcKey()
   {
      try (LogContext ignored = LogContext.put("uppgiftId", "123"))
      {
         assertEquals("123", MDC.get("uppgiftId"));
      }
   }

   @Test
   @DisplayName("FBFF-FR-04.2: close rensar nyckeln vid normalt flöde")
   void close_normalFlow_removesKey()
   {
      try (LogContext ignored = LogContext.put("uppgiftId", "123"))
      {
      }

      assertNull(MDC.get("uppgiftId"));
   }

   @Test
   @DisplayName("FBFF-FR-04.2: nyckeln rensas garanterat även om ett exception kastas")
   void close_exceptionThrown_stillRemovesKey()
   {
      try
      {
         try (LogContext ignored = LogContext.put("uppgiftId", "123"))
         {
            throw new RuntimeException("boom");
         }
      }
      catch (RuntimeException e)
      {
         // Förväntat - vi bryr oss bara om att MDC rensades nedan.
      }

      assertNull(MDC.get("uppgiftId"));
   }

   @Test
   @DisplayName("FBFF-FR-04.1: put med flera nycklar sätter samtliga för anropets livslängd")
   void put_multipleKeys_setsAll()
   {
      try (LogContext ignored = LogContext.put(Map.of("uppgiftId", "123", "clientTypId", "ABC")))
      {
         assertEquals("123", MDC.get("uppgiftId"));
         assertEquals("ABC", MDC.get("clientTypId"));
      }

      assertNull(MDC.get("uppgiftId"));
      assertNull(MDC.get("clientTypId"));
   }

   @Test
   @DisplayName("FBFF-FR-04.2: close återställer ett tidigare värde för samma nyckel istället för att ta bort det")
   void close_nestedContextSameKey_restoresOuterValue()
   {
      try (LogContext outer = LogContext.put("uppgiftId", "outer"))
      {
         try (LogContext inner = LogContext.put("uppgiftId", "inner"))
         {
            assertEquals("inner", MDC.get("uppgiftId"));
         }

         assertEquals("outer", MDC.get("uppgiftId"));
      }

      assertNull(MDC.get("uppgiftId"));
   }
}
