package se.fk.rimfrost.framework.bff.logging;

import org.slf4j.MDC;

import java.util.LinkedHashMap;
import java.util.Map;

// try-with-resources replacement for the manual try { MDC.put(...) } finally { MDC.remove(...) }
// pattern repeated across BFF controllers. close() restores each key's previous value rather
// than removing it outright, so a nested context reusing a key doesn't clobber an outer one.
// Never pass the Authorization header's content as a value here — the keys set are logged in
// clear text.
public final class LogContext implements AutoCloseable
{
   private final Map<String, String> previousValues;

   private LogContext(Map<String, String> previousValues)
   {
      this.previousValues = previousValues;
   }

   public static LogContext put(String key, String value)
   {
      return put(Map.of(key, value));
   }

   public static LogContext put(Map<String, String> values)
   {
      Map<String, String> previousValues = new LinkedHashMap<>();
      values.forEach((key, value) ->
      {
         previousValues.put(key, MDC.get(key));
         MDC.put(key, value);
      });
      return new LogContext(previousValues);
   }

   @Override
   public void close()
   {
      previousValues.forEach((key, previousValue) ->
      {
         if (previousValue == null)
         {
            MDC.remove(key);
         }
         else
         {
            MDC.put(key, previousValue);
         }
      });
   }
}
